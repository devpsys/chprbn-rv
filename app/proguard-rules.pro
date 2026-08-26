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


# ---- kotlinx.serialization + Compose Navigation (type-safe routes) --------
# Compose Nav 2.8+ inflates the graph by calling kotlinx.serialization on
# every @Serializable route arg — including via runtime FQN class lookups
# (see `NavType.parseSerializableType` on androidx.navigation). The
# serialization plugin ships consumer rules for the `$serializer` companion
# but does NOT keep the annotated class's name — R8 was free to rename
# `ScanSource`, whose runtime FQN lookup then blew up at NavHost
# setContent with `IllegalArgumentException: Cannot find class with name
# "...ScanSource"`. See Routes.kt's block comment on the enum for the
# original failure mode.
#
# The following block:
#   1. Preserves the standard JetBrains-recommended keep set for
#      companion serializers and `INSTANCE.serializer()` accessors.
#   2. Preserves every @Serializable class's NAME (not members — the
#      plugin already handles those) so nav-arg FQN lookups can't fail.
#   3. Preserves the whole core.navigation package wholesale as
#      belt-and-braces: every class there is a route or arg type, and
#      the FQN lookup is one string-compare from breaking.

# `<class>.Companion` field on @Serializable classes.
-if @kotlinx.serialization.Serializable class **
-keepclassmembers class <1> {
    static <1>$Companion Companion;
}
# `Companion.serializer()` — the entry point kotlinx.serialization calls.
-if @kotlinx.serialization.Serializable class ** {
    static **$Companion Companion;
}
-keepclassmembers class <2>$Companion {
    kotlinx.serialization.KSerializer serializer(...);
}
# `INSTANCE.serializer()` on @Serializable objects — no such usage today
# in Routes.kt (only data classes and one enum), but cheap future-proofing
# for a future @Serializable object route.
-if @kotlinx.serialization.Serializable class ** {
    public static ** INSTANCE;
}
-keepclassmembers class <1> {
    public static <1> INSTANCE;
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep every @Serializable class's NAME across the whole app so a future
# @Serializable arg on any route can't repeat the ScanSource crash.
-keepnames @kotlinx.serialization.Serializable class **

# Nav routes — keep the whole package. Anything here is either a Route
# (@Serializable object/data class) or an arg type (like ScanSource); one
# missed keep = one crash at NavHost inflation. The wholesale rule keeps
# names, members, and inner classes intact.
-keep class ng.com.chprbn.mobile.core.navigation.** { *; }


# ---- Room AutoMigrationSpec (reflectively instantiated) -------------------
# Room's generated migration code instantiates AutoMigrationSpec subclasses
# by reflection — see `AssessmentDatabase.DropSchedulesTableSpec`. The
# wildcard `feature/**/data/local/**` already covers this via the package
# match, but this explicit rule survives a future move of any spec class.
-keep class * extends androidx.room.migration.AutoMigrationSpec { *; }


# ---- Stack traces ----------------------------------------------------------
# Keep file/line information so production crash reports stay readable. The
# obfuscation map (build/outputs/mapping/release/mapping.txt) is what's needed
# to deobfuscate; archive it with each release.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
