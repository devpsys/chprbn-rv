package ng.com.chprbn.mobile.core.persistence.converters

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Generic Room converter for `List<String>` stored as a JSON string.
 *
 * Reusable across features that need to persist string lists.
 */
class JsonStringListTypeConverter {

    private val gson = Gson()

    @TypeConverter
    fun fromJsonStringList(list: List<String>?): String {
        return gson.toJson(list ?: emptyList<String>())
    }

    @TypeConverter
    fun toJsonStringList(json: String?): List<String> {
        if (json.isNullOrBlank()) return emptyList<String>()
        return try {
            val type = object : TypeToken<List<String>>() {}.type
            gson.fromJson<List<String>>(json, type) ?: emptyList<String>()
        } catch (_: Throwable) {
            emptyList<String>()
        }
    }
}

