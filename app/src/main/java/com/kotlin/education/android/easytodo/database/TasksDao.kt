package com.kotlin.education.android.easytodo.database

import androidx.room.*
import com.kotlin.education.android.easytodo.model.Task

/**
 * The Dao definition of the database operations.
 * Any Dao definition class must start with @Dao
 */
@Dao
interface TasksDao {

    @Query("SELECT * FROM tasks")
    fun getAll(): MutableList<Task>

    @Query("SELECT * FROM tasks WHERE id = :id")
    fun findById(id : Long): Task

    @Query("SELECT * FROM tasks WHERE done = 0")
    fun getAllUndone(): MutableList<Task>

    // Note: We are returning Long value. This will be the id of the newly added task.
    @Insert
    fun insert(task: Task): Long

    @Update
    fun update(task: Task)

    @Delete
    fun delete(task: Task)

    /**
     * Updates the status for the the specific task identified by the id.
     */
    @Query("UPDATE tasks SET done = :done WHERE id = :id")
    fun markAsDone(id: Long, done: Int)

}