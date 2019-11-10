package com.kotlin.education.android.easytodo.extensions

/**
 * The Extensions allow us to attach owr own methods to already existing classes.
 * https://kotlinlang.org/docs/tutorials/android-plugin.html
 */


/**
 * The extension for the Double class. Every Double will have a method round(), which
 * will round the number and return it as a String.
 * We will use it for printing of location coordinates (Latitude, Longitude)
 */
fun Double.round(): String {
    return String.format("%.2f", this)
}