package com.kotlin.education.android.easytodo.activities

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.*
import android.widget.DatePicker
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.FileProvider
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.kotlin.education.android.easytodo.R
import com.kotlin.education.android.easytodo.adapters.ImagesAdapter
import com.kotlin.education.android.easytodo.constants.IntentConstants
import com.kotlin.education.android.easytodo.database.TodoLocalRepository
import com.kotlin.education.android.easytodo.extensions.round
import com.kotlin.education.android.easytodo.model.Task
import com.kotlin.education.android.easytodo.notifications.NotificationManager
import com.kotlin.education.android.easytodo.utils.DateUtils
import com.kotlin.education.android.easytodo.utils.FileUtils
import com.kotlin.education.android.easytodo.utils.PermissionUtil
import kotlinx.android.synthetic.main.activity_add_edit_task.*
import kotlinx.android.synthetic.main.content_add_edit_task.*
import java.io.File
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList


/**
 * The activity for adding and editing tasks. It's a single activity that can
 * work in teo modes. If it gets an id in the intent, it means that we will be editing,
 * if no id is send, we are adding a new task.
 * TODO Homework: Keep the activity state when the phone is rotated
 */
class AddEditTaskActivity : AppCompatActivity(), ChooseImageSourceListener {

    // all the static things for the activity
    companion object {

        /**
         * The method for creating intent to run the activity. The activity should be only run
         * using this method. This way no one doesn't need to know what to put to intent
         * Note: id can be null. If its null, we are creating, if its not, we are editing.
         */
        fun createIntent(context: Context, id: Long?) : Intent {
            val intent = Intent(context, AddEditTaskActivity::class.java)
            intent.putExtra(IntentConstants.ID, id)
            return intent
        }
    }

    // the id of the task. Used to distinguish if we are editing or adding
    private var taskId: Long = -1L
    // the task. In case of the adding, will be null.
    private var task: Task? = null

    private var tempPhotoFile: File? = null

    private var images: ArrayList<String> = arrayListOf()
    private var imageAdapter: ImagesAdapter? = null
    private var layoutManager: FlexboxLayoutManager? = null

    private val REQUEST_IMAGE_CAPTURE = 100
    private val GALLERY_IMAGE_REQUEST_CODE = 101

    private val PERMISSION_SELECT_FROM_GALLERY_REQUEST_CODE = 200
    private val PERMISSION_CAMERA_REQUEST_CODE = 201

    private val MAP_REQUEST_CODE = 300


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_edit_task)
        setSupportActionBar(toolbar)

        // shows the back arrow on the toolbar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        // sets the listener for on arrow clicks. I that case, finshes the activity
        toolbar.setNavigationOnClickListener{finish()}

        addImageFAB.setOnClickListener({
            openAddImageBottomSheet()
        })

        // gets the id from the intent
        taskId = intent.getLongExtra(IntentConstants.ID, -1L)
        // let's check the id if it was send to the activity
        if (taskId != -1L){
            // have the id, the activity will be in edit mode
            task = TodoLocalRepository().getById(taskId)
            taskNameEditText.setText(task!!.name)
            taskNoteEditText.setText(task!!.note)
            task!!.dueDate?.let { dateButton.setValue(DateUtils.getDateString(task!!.dueDate!!))}
            task!!.images?.let { images.addAll(task!!.images!!) }
            task!!.notificationId?.let {notificationCheckbox.isChecked = true}
            setLocationText()
            supportActionBar!!.title = getString(R.string.edit_task)
        } else {
            task = Task()
        }

        initializeImageList()

        dateButton.setOnClickListener({ openDatePicker()})

        locationButton.setOnClickListener({
            startActivityForResult(MapsActivity.createIntent(this@AddEditTaskActivity, null, null), MAP_REQUEST_CODE)
        })

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_add_edit_task, menu)
        return true
    }

    private fun openDatePicker(){
        val calendar = Calendar.getInstance()
        task?.dueDate?.let {
            calendar.timeInMillis = it * 1000
        }

        val y = calendar.get(Calendar.YEAR)
        val m = calendar.get(Calendar.MONTH)
        val d = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(this, object : DatePickerDialog.OnDateSetListener {
            override fun onDateSet(view: DatePicker?, year: Int, monthOfYear: Int, dayOfMonth: Int) {
                dateButton.setValue(DateUtils.getDateString(DateUtils.getUnixTime(year, monthOfYear, dayOfMonth)))
                task?.dueDate = DateUtils.getUnixTime(year, monthOfYear, dayOfMonth)

            }
        }, y, m, d)

        datePickerDialog.show()
    }


    fun save(item : MenuItem) {

        var everythingOK = true

        if (taskNameEditText.text.toString().trim().equals("")){
            taskNameLayout.setError(getString(R.string.required_field))
            everythingOK = false
        }

        if (everythingOK){
            if (taskId == -1L) {
                task!!.name = taskNameEditText.text.toString()
                if (!taskNoteEditText.text.toString().isEmpty()) {
                    task!!.note = taskNoteEditText.text.toString()
                }
                if (images.size > 0) {
                    task!!.images = images
                }

                task!!.dueDate?.let {
                    if (notificationCheckbox.isChecked) {
                        task!!.notificationId = NotificationManager()
                            .scheduleNotification(NotificationManager().createNotificationCalendar(task!!), taskId)
                    }
                }

                val newTaskId = TodoLocalRepository().insert(task!!)
                val intent : Intent = Intent()
                intent.putExtra(IntentConstants.ID, newTaskId)
                setResult(RESULT_OK, intent)
            } else {
                task!!.name = taskNameEditText.text.toString()
                if (!taskNoteEditText.text.toString().isEmpty()) {
                    task!!.note = taskNoteEditText.text.toString()
                }
                task!!.images = images

                // TODO Homework: This is not optimal. Always canceling and rescheduling notification. Make it better!
                if (notificationCheckbox.isChecked) {
                    task!!.notificationId?.let{
                        NotificationManager().cancelScheduledNotification(task!!.notificationId!!)
                        task!!.notificationId = null
                    }
                    task!!.notificationId = NotificationManager()
                        .scheduleNotification(NotificationManager().createNotificationCalendar(task!!), taskId)
                } else {
                    task!!.notificationId?.let{
                        NotificationManager().cancelScheduledNotification(task!!.notificationId!!)
                        task!!.notificationId = null
                    }
                }

                // task in this activity can be null. However, the database operation
                // will not accept null value. So we use the method let.
                TodoLocalRepository().update(task!!)
            }
            finish()
        }



    }

    private fun openAddImageBottomSheet() {
        val fragment = ChooseImageSourceBottomSheet()
        fragment.chooseImageSourceListener = this
        fragment.show(supportFragmentManager, "choose_image_source")
    }

    override fun captureWithCamera() {
        if (PermissionUtil.checkCameraStoragePermission(this)) {
            // intent for opening image capture activity
            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            // Checks if there is an app for capturing photos.
            if (takePictureIntent.resolveActivity(packageManager) != null) {
                try {
                    // create an empty file
                    tempPhotoFile = FileUtils.createImageFile(this)
                } catch (ex: IOException) {
                    ex.printStackTrace()
                }

                // If the file is created, continue.
                if (tempPhotoFile != null) {
                    // the file provider is needed.
                    // Otherwise the photo capturing application will not have
                    // access to the file and will not be able to save photo to it.
                    // Note: the FileProvider is defined in AndroidManifest.xml file
                    // and it has a paths.xml file definition
                    val photoURI = FileProvider.getUriForFile(
                        this,
                        "com.kotlin.education.android.easytodo.fileprovider",
                        tempPhotoFile!!
                    )
                    // set the photo URI to the intent
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    // start the photo capturing activity
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
                }
            }
        } else {
            PermissionUtil.requestCameraStoragePermission(this, PERMISSION_CAMERA_REQUEST_CODE)
        }

    }

    override fun selectFromGallery() {
        // fist check if we have permission
        if (PermissionUtil.checkReadStoragePermission(this)) {
            val intent = Intent()
            intent.type = "image/*"
            intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true)
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            intent.action = Intent.ACTION_GET_CONTENT

            startActivityForResult(
                Intent.createChooser(
                    intent,
                    getString(R.string.select_image)
                ), GALLERY_IMAGE_REQUEST_CODE
            )
        } else {
            PermissionUtil.requestReadStoragePermission(
                this@AddEditTaskActivity,
                PERMISSION_SELECT_FROM_GALLERY_REQUEST_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>,
                                            grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSION_SELECT_FROM_GALLERY_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // the permission for storage is granted.
                    selectFromGallery()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // returning from the image capture activity
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            // set the image name to the task to be stored in the database
            images.add(tempPhotoFile!!.getName())
            imageAdapter!!.notifyItemInserted(images.size)
        }

        // returning from the selection of the gallery
        if (requestCode == GALLERY_IMAGE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            if (data?.getClipData() != null) {
                val count = data.clipData?.itemCount
                for (i in 0..count!! -1) {
                    val imageUri: Uri = data.clipData!!.getItemAt(i).uri
                    val path = FileUtils.getRealPath(this, imageUri)
                    val sourceFile = File(path)
                    val destinationFile = File(filesDir, sourceFile.getName())
                    try {
                        // TODO Homework: Make the copy operation in background using AsyncTask.
                        FileUtils.copy(sourceFile, destinationFile)
                        images.add(destinationFile.getName())
                        imageAdapter!!.notifyItemInserted(images.size)
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            } else if (data?.getData() != null) {
                val uri = data.data
                val path = FileUtils.getRealPath(this, uri)
                val sourceFile = File(path)
                val destinationFile = File(filesDir, sourceFile.getName())
                try {
                    FileUtils.copy(sourceFile, destinationFile)
                    images.add(destinationFile.getName())
                    imageAdapter!!.notifyItemInserted(images.size)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }

        if (resultCode == Activity.RESULT_OK && requestCode == MAP_REQUEST_CODE){
            data?.let {
                if (it.hasExtra(IntentConstants.LONGITUDE) && it.hasExtra(IntentConstants.LONGITUDE)){
                    task?.latitude = data.getDoubleExtra(IntentConstants.LATITUDE, -0.0)
                    task?.longitude = data.getDoubleExtra(IntentConstants.LONGITUDE, 0.0)
                    setLocationText()
                }
            }
        }
    }

    private fun setLocationText(){
        if (task?.latitude != null && task?.longitude != null){
            locationButton.setValue("${task?.latitude!!.round()}, ${task?.longitude!!.round()}")
        }
    }


    /**
     * The bottom sheet dialog for choosing the image source.
     * https://www.androidhive.info/2017/12/android-working-with-bottom-sheet/
     */
    class ChooseImageSourceBottomSheet : BottomSheetDialogFragment() {

        // the listener for the source options
        var chooseImageSourceListener: ChooseImageSourceListener? = null


        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                                  savedInstanceState: Bundle?): View? {
            // init the dialog with custom layout.
            val view = inflater.inflate(R.layout.dialog_choose_image_source, container, false)

            val captureWithCamera: ConstraintLayout = view.findViewById(R.id.captureWithCamera)
            val selectFromGallery: ConstraintLayout = view.findViewById(R.id.selectFromGallery)

            // set on click listeners
            captureWithCamera.setOnClickListener {
                chooseImageSourceListener?.captureWithCamera()
                dismiss()
            }
            selectFromGallery.setOnClickListener {
                chooseImageSourceListener?.selectFromGallery()
                dismiss()
            }

            return view
        }
    }

    /**
     * Initializes the list of images.
     */
    private fun initializeImageList(){
        // if the adapter was not set, lets initialize all the objects for the list of images.
        imageAdapter?.let {
            // the is already initialized and connected to the RecyclerView. Just
            // refresh it.
            imageAdapter!!.notifyDataSetChanged()
        }?:kotlin.run {
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
            imageAdapter = ImagesAdapter(this, images, false)
            // set adapter to RecyclerView
            imagesRecyclerView.adapter = imageAdapter
        }

    }


}

interface ChooseImageSourceListener{
    fun captureWithCamera()
    fun selectFromGallery()
}

