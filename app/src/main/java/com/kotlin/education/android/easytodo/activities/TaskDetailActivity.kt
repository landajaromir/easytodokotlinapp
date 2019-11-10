package com.kotlin.education.android.easytodo.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.kotlin.education.android.easytodo.R
import com.kotlin.education.android.easytodo.adapters.ImagesAdapter
import com.kotlin.education.android.easytodo.constants.IntentConstants
import com.kotlin.education.android.easytodo.database.TodoLocalRepository
import com.kotlin.education.android.easytodo.extensions.round
import com.kotlin.education.android.easytodo.model.Task
import com.kotlin.education.android.easytodo.utils.DateUtils

import kotlinx.android.synthetic.main.activity_task_detail.toolbar
import kotlinx.android.synthetic.main.content_task_detail.*
import kotlinx.android.synthetic.main.content_task_detail.imagesRecyclerView
import kotlin.collections.ArrayList

/**
 * The activity for showing the detail of the task.
 * TODO Homework: Add to detail a button that will mark the task as done or undone. Try to use the MaterialButton class.
 */
class TaskDetailActivity : AppCompatActivity() {

    // all the static things for the activity
    companion object {

        /**
         * The method for creating intent to run the activity. The activity should be only run
         * using this method. This way no one doesn't need to know what to put to intent
         * Note: id cannot be null. Must be send to intent.
         */
        fun createIntent(context: Context, id: Long) : Intent {
            val intent = Intent(context, TaskDetailActivity::class.java)
            intent.putExtra(IntentConstants.ID, id)
            return intent
        }
    }

    private var images: ArrayList<String> = arrayListOf()
    private var imageAdapter: ImagesAdapter? = null
    private var layoutManager: FlexboxLayoutManager? = null

    // the id of the task. Used to distinguish if we are editing or adding
    private var taskId: Long = -1L
    // the task. Cannot be null. Hovewer, we will initialize it later
    private lateinit var task: Task

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_detail)
        setSupportActionBar(toolbar)

        // shows the back arrow on the toolbar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        // sets the listener for on arrow clicks. I that case, finshes the activity
        toolbar.setNavigationOnClickListener{finishDetail(Activity.RESULT_CANCELED)}

        // We are intentionally not checking if we get valid id.
        // Because if I don't get valid id, the app better crash.
        taskId = intent.getLongExtra(IntentConstants.ID, -1)
    }

    override fun onBackPressed() {
        finishDetail(Activity.RESULT_CANCELED)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_task_detail, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_edit -> {
                startActivity(AddEditTaskActivity.createIntent(this, taskId))
                return true
            }
            R.id.action_remove -> {
                openRemoveTaskDialog()
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    /**
     * Every time, just before the activity UI will be shown we refresh the layout.
     */
    override fun onResume() {
        super.onResume()
        setValues()
    }

    /**
     * Sets all layout values.
     */
    private fun setValues(){
        // get the task from the database
        task = TodoLocalRepository().getById(taskId)
        // set the name
        taskName.setText(task.name)
        // set the note.
        task.note?.let {
            taskNote.text = task.note
        }?:kotlin.run {
            // if no note was filled, the note is hidden.
            taskNoteContainer.visibility = View.GONE
        }

        // set the date. Notice the difference with a note. With note, we checked for null.
        // With dueDate, we use the let method with Elvis operator (?:)
        task.dueDate?.let {
            // if the dueDate is not null, set it to TextView
            taskDate.text = DateUtils.getDateString(task.dueDate!!)
        }?:run{
            // using the Elvis operator to provide the else part.
            // dueDate is null, lets hide it.
            taskDateContainer.visibility = View.GONE
        }

        // set the location.
        if (task.latitude != null && task.longitude != null){
            // we are using the String interpolation.
            taskLocation.text = "${task.latitude!!.round()}, ${task.longitude!!.round()}"
        } else {
            taskLocationContainer.visibility = View.GONE
        }

        // set the list of images. Notice we are using separate list for the images.
        // This way we will not loose the reference.
        task.images?.let {
            // we are reinitializing the list every time onResume is called.
            // TODO Homework: Use the DiffUtils to set the list.
            images.clear()
            images.addAll(task.images!!)
            initializeImageList()
        }

    }

    /**
     * Opens a dialog for removing task. Lets ask the user for confirmation.
     */
    private fun openRemoveTaskDialog(){
        // If we use the AlertDialog.Builder, the dialog will respect the visual side of each
        // android version.
        val builder = AlertDialog.Builder(this)
        // set title
        builder.setTitle(getString(R.string.dialog_remove_title))
        // set message
        builder.setMessage(getString(R.string.dialog_remove_message))
        // set the positive button. The dialog can have three buttons, positive, negative and neutral.
        builder.setPositiveButton(getString(R.string.remove)){dialog, which ->
            dialog.dismiss()
            finishDetail(Activity.RESULT_OK)
        }

        // Display a negative button on alert dialog
        builder.setNegativeButton(getString(R.string.cancel)){ dialog, which ->
            dialog.cancel()
        }

        // create the dialog
        val dialog: AlertDialog = builder.create()
        // show the dialog
        dialog.show()
    }

    /**
     * Finishes the activity with a specific result.
     */
    private fun finishDetail(result: Int) {
        val intent = Intent()
        // sends back the task id
        intent.putExtra(IntentConstants.ID, taskId)
        setResult(result, intent)
        finish()
    }

    /**
     * Initializes the list of images.
     */
    private fun initializeImageList(){
        // if the adapter was not set, lets initialize all the objects for the list of images.
        imageAdapter?.notifyDataSetChanged() ?:kotlin.run {

            // In the MainActivity, we used a LinearLayoutManager.
            // This is an alternative. A nice library from google (https://github.com/google/flexbox-layout)
            // allows us to make responsive layouts.
            layoutManager = FlexboxLayoutManager(this).apply {
                flexWrap = FlexWrap.WRAP
                flexDirection = FlexDirection.ROW
            }
            // set the layout manager
            imagesRecyclerView.layoutManager = layoutManager
            // create the adapter
            imageAdapter = ImagesAdapter(this, images, true)
            // set adapter to RecyclerView
            imagesRecyclerView.adapter = imageAdapter
        }
    }
}
