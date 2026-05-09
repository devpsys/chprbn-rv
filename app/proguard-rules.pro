# ============================================================================
# CHPRBN Mobile — R8 / ProGuard rules
# ============================================================================
# Most rules ship as consumer rules from the libraries themselves (Hilt, Room,
# Retrofit, OkHttp, Coil, CameraX, ML Kit, AndroidX). The app-specific rules
# below cover the gaps:
#
#   1. Gson — has no consumer rules. Kotlin data classes that go through
#      Gson reflection must be kept by name and field, plus the @SerializedName
#      attribute and generic Signature must survive shrinking.
#   2. App DTOs in feature/*/data/dto are Gson request/response payloads.
#   3. App Room entities and DAOs in feature/*/data/local are referenced by
#      Room-generated code (KSP) and are safest kept explicitly.
#   4. App domain models in feature/*/domain/model are passed through Compose
#      navigation args as Gson JSON (see AppNavHost.kt and the form ViewModels).
#      This is a known architectural smell flagged in the code-review audit;
#      until that refactor lands, these classes must survive R8 unchanged.
# ============================================================================


# ---- Attributes ------------------------------------------------------------
# Signature      — generic types (TypeToken<List<String>>)
# *Annotation*   — @SerializedName, @Retain, etc., consumed at runtime
# EnclosingMethod, InnerClasses — needed for anonymous TypeToken<...>(){}
-keepattributes Signature, *Annotation*, EnclosingMethod, InnerClasses


# ---- App: Gson-serialized DTOs --------------------------------------------
-keep class ng.com.chprbn.mobile.feature.**.data.dto.** { *; }


# ---- App: Room entities, DAOs, databases, callbacks -----------------------
# Room's KSP-generated impls reference these by name. Keeping the package
# wholesale is cheap and prevents subtle column-mapping breakage under R8.
-keep class ng.com.chprbn.mobile.feature.**.data.local.** { *; }


# ---- Gson runtime ----------------------------------------------------------
# Anonymous TypeToken<T>() {} subclasses (e.g. JsonStringListTypeConverter)
# must keep their generic Signature so Gson can read the parameter type.
-keep,allowobfuscation,allowshrinking class * extends com.google.gson.reflect.TypeToken
-keep,allowobfuscation,allowshrinking class com.google.gson.reflect.TypeToken

# Preserve fields tagged with @SerializedName on any class.
-keepclassmembers,allowobfuscation class * {
    @com.google.gson.annotations.SerializedName <fields>;
}


# ---- Retrofit suspend interfaces ------------------------------------------
# Retrofit 2.x ships consumer rules, but explicitly keeping HTTP-annotated
# methods is harmless belt-and-braces and protects against future shrinking
# changes that strip Kotlin coroutine return-type metadata.
-keepclasseswithmembers,allowobfuscation,allowshrinking interface * {
    @retrofit2.http.* <methods>;
}


# ---- Kotlin coroutines / serialization metadata ---------------------------
# Coroutines and Compose ship consumer rules; we keep ServiceLoader-loaded
# Dispatchers' factory because R8 has occasionally stripped it on Kotlin x.x
# upgrades. Cheap insurance.
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory


# ---- Stack traces ----------------------------------------------------------
# Keep file/line information so production crash reports stay readable. The
# obfuscation map (build/outputs/mapping/release/mapping.txt) is what's needed
# to deobfuscate; archive it with each release.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
