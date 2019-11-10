package com.kotlin.education.android.easytodo.database

import androidx.room.Room
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.room.migration.Migration
import com.kotlin.education.android.easytodo.TodoApplication
import com.kotlin.education.android.easytodo.model.Task
import com.kotlin.education.android.easytodo.utils.FileUtils


/**
 * The class for accessing database operations.
 * This is just a wrapper so we don't access the database directly.
 * In case we need to change the database implementation, we just change it here, at one place.
 */
class TodoLocalRepository {

    /**
     * Returns the database object.
     */
    private fun getDatabase(): TodoDatabase {
        return Room.databaseBuilder(
            TodoApplication.getAppContent(),
            TodoDatabase::class.java, "todo_database.db")
            // todo this shouldn't be here, it is a fix.
            .allowMainThreadQueries()
            // adding the migration rules for specific versions.
            // https://developer.android.com/training/data-storage/room/migrating-db-versions
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
            .build()
    }

    /**
     * Inserts a single task to the database
     */
    fun insert(task : Task):Long{
        return getDatabase().todoDao().insert(task)
    }

    /**
     * Inserts a single task to the database
     * @return the id of the newly added task
     */
    fun getById(id : Long): Task{
        return getDatabase().todoDao().findById(id)
    }

    /**
     * Updates a task in the database
     */
    fun update(task: Task){
        getDatabase().todoDao().update(task)
    }

    /**
     * Deletes a task from the database
     */
    fun delete(task: Task){
        FileUtils.deleteImages(TodoApplication.getAppContent(), task.images)
        getDatabase().todoDao().delete(task)
    }

    /**
     * Returns all tasks from the database
     */
    fun getAll(): MutableList<Task>{
        return getDatabase().todoDao().getAll()
    }

    /**
     * Returns all tasks from the database
     */
    fun getAllUndone(): MutableList<Task>{
        return getDatabase().todoDao().getAllUndone()
    }

    /**
     * Marks a single task as done.
     */
    fun markAsDone(id: Long, done: Boolean){
        getDatabase().todoDao().markAsDone(id, if (done) 1 else 0)
    }

    /**
     * The Migration rule from version 1 to version 2.
     * In version 2, we added location.
     */
    private val MIGRATION_1_2: Migration = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("ALTER TABLE tasks ADD COLUMN latitude REAL")
            database.execSQL("ALTER TABLE tasks ADD COLUMN longitude REAL")
        }
    }

    /**
     * The Migration rule from version 2 to version 3.
     * In version 3, we added notifications.
     */
    private val MIGRATION_2_3: Migration = object : Migration(2, 3) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("ALTER TABLE tasks ADD COLUMN notificationId INTEGER")
        }
    }
}