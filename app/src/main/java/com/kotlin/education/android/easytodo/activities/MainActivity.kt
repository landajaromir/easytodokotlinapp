package com.kotlin.education.android.easytodo.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.CheckBox
import androidx.appcompat.app.AppCompatActivity
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.android.synthetic.main.row_task_item.view.*
import android.graphics.Paint
import androidx.recyclerview.widget.DiffUtil
import com.kotlin.education.android.easytodo.R
import com.kotlin.education.android.easytodo.constants.IntentConstants
import com.kotlin.education.android.easytodo.database.TodoLocalRepository
import com.kotlin.education.android.easytodo.model.Task
import com.kotlin.education.android.easytodo.shared_preferences.SharedPreferencesManager


/**
 * The main activity of the application. The activity contains the list of tasks.
 */
class MainActivity : AppCompatActivity() {

    // the layout manager is a mandatory for RecyclerView
    // takes care of showing the list and how the list is shown (vertical, horizontal,...)
    private lateinit var linearLayoutManager: LinearLayoutManager
    // the mutable list supports adding and removing.
    // normal List cannot be changed.
    private val tasks: MutableList<Task> = mutableListOf()

    private var taskAdapter : TasksAdapter? = null

    private val ADD_TASK_REQUEST_CODE = 100
    private val TASK_DETAIL_REQUEST_CODE = 200

    // all the static things for the activity
    companion object {
        /**
         * The method for creating intent to run the activity. The activity should be only run
         * using this method. This way no one doesn't need to know what to put to intent
         */
        fun createIntent(context:Context) : Intent {
            return Intent(context, MainActivity::class.java)
        }
    }

    /**
     * The first method
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        // sets the onClickListener for the FloatingActionButton
        fab.setOnClickListener { view ->
            // start the adding of a new task.
            // running it expecting result. Meaning I want to know, if
            // any task was added.
            startActivityForResult(AddEditTaskActivity.createIntent(this@MainActivity, null), ADD_TASK_REQUEST_CODE)
        }

        // sets the tasks to the list.
        if (SharedPreferencesManager().shouldHideDone(this)){
            tasks.addAll(TodoLocalRepository().getAllUndone())
        } else {
            tasks.addAll(TodoLocalRepository().getAll())
        }

        // init the layout manager
        linearLayoutManager = LinearLayoutManager(this)
        // no need to used findViewByID (perks of the Kotlin)
        recyclerView.layoutManager = linearLayoutManager
        // init the adapter
        taskAdapter = TasksAdapter(tasks)
        // set the adapter to RecyclerView
        recyclerView.adapter = taskAdapter
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        val menuItem: MenuItem? = menu?.findItem(R.id.action_hide_done)
        if (SharedPreferencesManager().shouldHideDone(this)) {
            menuItem?.setChecked(true)
        }
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
             R.id.action_about -> {
                 startActivity(AboutAppActivity.createIntent(this))
                 return true
             }
             R.id.action_hide_done -> {
                 item.setChecked(!item.isChecked)
                 filterTasks(item.isChecked)
                 SharedPreferencesManager().saveHideDone(this@MainActivity, item.isChecked)
                 return true
             }
            else -> super.onOptionsItemSelected(item)
        }
    }

    /**
     * The adapter class is used to manage and show data in RecyclerView.
     * It creates all the rows and shows them when they are visible.
     * this@MainActivity is the way of getting the context in inner class.
     * The adapter always need three methods:
     *  - onCreateViewHolder - initializes the ViewHolder from a specific layout file.
     *  - onBindViewHolder - is responsible for showing all the views and their states.
     *  - getItemCount - returns the number of items. The list cannot be endless.
     */
    inner class TasksAdapter(private val tasks: MutableList<Task>) : RecyclerView.Adapter<TasksAdapter.TaskViewHolder>()  {

        /**
         * The ViewHolder class holds all the references of the views for each row.
         * It extends RecyclerView.ViewHolder which needs only the View containing all the other elements.
         */
        inner class TaskViewHolder(view: View) : RecyclerView.ViewHolder(view){
            val name: TextView = view.name
            val note: TextView = view.note
            val checkBox: CheckBox = view.checkbox
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
            return TaskViewHolder(LayoutInflater.from(this@MainActivity).inflate(R.layout.row_task_item, parent, false))
        }

        override fun getItemCount(): Int {
            return tasks.size
        }

        override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
            // gets a single task from the list
            val task : Task = tasks.get(position)
            // sets the name to the TextView
            holder.name.text = task.name
            // sets the note to the TextView
            if (task.note != null) {
                holder.note.text = task.note
                holder.note.visibility = View.VISIBLE
            } else {
                holder.note.visibility = View.GONE
            }
            // set the checkbox
            setDone(holder, tasks.get(position))

            // sets the on click listener on the entire view
            holder.itemView.setOnClickListener {
                // task.id!! will throw an exception if is null.
                // because the id in task can be null, however in method createIntent the id cannot be null.
                startActivityForResult(TaskDetailActivity.createIntent(this@MainActivity, task.id!!), TASK_DETAIL_REQUEST_CODE)
            }
        }

        /**
         * Sets one task as done.
         */
        private fun setDone(holder: TaskViewHolder, task: Task){
            if (task.done) {
                holder.name.setPaintFlags(holder.name.getPaintFlags() or Paint.STRIKE_THRU_TEXT_FLAG)
            } else {
                holder.name.setPaintFlags(holder.name.getPaintFlags() and Paint.STRIKE_THRU_TEXT_FLAG.inv())
            }
            holder.checkBox.setOnCheckedChangeListener(null)
            // sets checkbox if task is done
            holder.checkBox.setChecked(task.done)
            holder.checkBox.setOnCheckedChangeListener { buttonView, isChecked ->
                recyclerView.post {
                    if (SharedPreferencesManager().shouldHideDone(this@MainActivity)){
                        task.done = !task.done
                        TodoLocalRepository().markAsDone(task.id!!, task.done)
                        tasks.removeAt(holder.adapterPosition)
                        notifyItemRemoved(holder.adapterPosition)
                    } else {
                        task.done = !task.done
                        TodoLocalRepository().markAsDone(task.id!!, task.done)
                        taskAdapter?.notifyItemChanged(holder.adapterPosition)
                    }
                }
            }

        }

    }

    /**
     * This method is called when returning from the activity started using method
     * startActivityForResult. We will always get the request code and the result code.
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // if we are returning from adding a task and a task was added (RESULT_OK)
        if (requestCode == ADD_TASK_REQUEST_CODE && resultCode == Activity.RESULT_OK){
            var id : Long = data!!.getLongExtra(IntentConstants.ID, -1)
            if (!id.equals(-1)){
                // We are getting a new task id from the AddEditTaskActivity.
                var newTask = TodoLocalRepository().getById(id)
                // adding the task to the list
                tasks.add(newTask)
                // letting the adapter know, that a new item was added
                taskAdapter?.notifyItemInserted(tasks.size-1)
                // the alternative would be to call notifyDataSetChanged
                // however, it reloads the entire list where notifyItemInserted
                // only inserts a new task to the end of the list
                // taskAdapter!!.notifyDataSetChanged()
            }
        }

        if (requestCode == TASK_DETAIL_REQUEST_CODE){
            val id = data?.getLongExtra(IntentConstants.ID, -1L)
            when(resultCode){
                Activity.RESULT_OK -> removeTask(id)
                Activity.RESULT_CANCELED -> updateTask(id)
            }
        }
    }

    /**
     * Removes a spefic task from the database and from the list.
     */
    private fun removeTask(id: Long?){
        if (id != -1L){
            // gets the task postion
            val position = getTaskPosition(id)
            // remove it from the database
            TodoLocalRepository().delete(tasks.get(position))
            // remove from the list
            tasks.removeAt(position)
            // let the list know the task was rmeoved
            taskAdapter?.notifyItemRemoved(position)
        }
    }

    /**
     * Updates a specific task.
     */
    private fun updateTask(id: Long?){
        // get the task from database
        val task = TodoLocalRepository().getById(id!!)
        // ge te task position
        val position = getTaskPosition(id)
        // set it at specific place
        tasks.set(position, task)
        // notify the single task changed
        taskAdapter?.notifyItemChanged(position)
    }

    /**
     * Gets the position of the task
     */
    private fun getTaskPosition(id: Long?): Int {
        // a nice example of the for cycle in Kotlin.
        // I am getting not only the index or the value, but both
        for ((index, value) in tasks.withIndex()){
            if (value.id == id){
                return index
            }
        }
        return -1
    }

    /**
     * Filtering the tasks. Deciding if the done tasks should be visible or not.
     */
    private fun filterTasks(hideDone: Boolean){

        val newList: MutableList<Task>
        if (hideDone){
            // get only the undone tasks
            newList = TodoLocalRepository().getAllUndone()
        } else {
            // get all tasks
            newList = TodoLocalRepository().getAll()
        }

        /**
         * Diff util is a nice utility for finding difference between lists.
         * The main advantage is that it works in cooperation with the adapter.
         * https://developer.android.com/reference/android/support/v7/util/DiffUtil
         */
        val diffUtil = DiffUtil.calculateDiff(object : DiffUtil.Callback(){
            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return tasks[oldItemPosition].id == newList[newItemPosition].id
            }

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return tasks[oldItemPosition] == newList[newItemPosition]
            }

            override fun getOldListSize() = tasks.size

            override fun getNewListSize() = newList.size

        })
        // In the DiffUtils I get the new list and dispatch it to adapter.
        diffUtil.dispatchUpdatesTo(this.taskAdapter!!)
        // however, I also need to update the list itself
        tasks.clear()
        tasks.addAll(newList)
    }
}
