package com.kotlin.education.android.easytodo.shared_preferences

import android.content.SharedPreferences
import android.annotation.SuppressLint
import android.content.Context
import android.content.Context.MODE_PRIVATE


/**
 * SharedPreferences are used to store small data in a file.
 * The stored data are not deleted when the app is closed.
 * It is more simple than database and should be used for small amounts of data.
 */
class SharedPreferencesManager {

    // the name of the file it will be stored in
    private val fileName = "todosp"
    private val firstRun = "first_run"
    private val hideDone = "hide_done"

    /**
     * Returns the object to access the shared preferences.
     * @param context context
     * @return SharedPreferences object
     */
    private fun getSharedPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(fileName, Context.MODE_PRIVATE)
    }

    /**
     * Saves a boolean value to the shared preferences.
     * The value represents if the app is run for the first time.
     * @param context context
     */
    fun saveFirstRun(context: Context) {
        val editor = getSharedPreferences(context).edit()
        editor.putBoolean(firstRun, false)
        editor.apply()
    }

    /**
     * Returns true of the app is run for the first time.
     * @param context context
     * @return Returns true of the app is run for the first time.
     */
    fun isRunForFirstTime(context: Context): Boolean {
        val sharedPreferences = getSharedPreferences(context)
        return sharedPreferences
            .getBoolean(firstRun, true)
    }

    /**
     * Saves to the SharedPreferences if the user has chosen to
     * hide done tasks. We need to remember this option to
     * keep the UI consistent.
     */
    @SuppressLint("ApplySharedPref")
    fun saveHideDone(context: Context, hideDone: Boolean) {
        val editor = getSharedPreferences(context).edit()
        editor.putBoolean(this.hideDone, hideDone)
        editor.commit()
    }

    /**
     * Returns true if user has chosen to hide done tasks.
     */
    fun shouldHideDone(context: Context): Boolean {
        val sharedPreferences = getSharedPreferences(context)
        return sharedPreferences
            .getBoolean(hideDone, false)
    }

}