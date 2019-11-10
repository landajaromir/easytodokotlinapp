package com.kotlin.education.android.easytodo.database

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Unfortunately Room cannot save List to the database. The library does not know
 * how to save it. For this reason, we must provide TypeConverter method. To show another
 * library, saving to JSON was chosen. The list is saved as a string in JSON format and than
 * when loading from database converted back to the list.
 */
class TaskTypeConverters {

    /**
     * The ArrayList of Strings is converted to JSON String.
     */
    @TypeConverter
    fun fromImages(images: ArrayList<String>?): String? {
        if (images == null) {
            return null
        }
        val gson = Gson()
        val type = object : TypeToken<ArrayList<String>>() {}.type
        return gson.toJson(images, type)
    }

    /**
     * The JSON String is converted back to String ArrayList
     */
    @TypeConverter
    fun toImages(images: String?): ArrayList<String>? {
        if (images == null) {
            return null
        }
        val gson = Gson()
        val type = object : TypeToken<ArrayList<String>>() {}.type
        return gson.fromJson<ArrayList<String>>(images, type)
    }
}
