package com.kotlin.education.android.easytodo.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.kotlin.education.android.easytodo.model.Task

/**
 * The definition of the database.
 * @Database is used to define a list of database table classes
 * @TypeConverters defines the type converters class
 */
@Database(entities = [Task::class], version = 3)
@TypeConverters(TaskTypeConverters::class)
abstract class TodoDatabase : RoomDatabase() {

    /**
     * Returns the TasksDao for accessing the list of tasks.
     */
    abstract fun todoDao(): TasksDao
}