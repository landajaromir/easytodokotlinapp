package com.kotlin.education.android.easytodo.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

import kotlinx.android.synthetic.main.activity_image_viewer.*
import kotlinx.android.synthetic.main.content_image_viewer.*
import java.io.File
import android.view.WindowManager
import androidx.core.content.ContextCompat
import com.kotlin.education.android.easytodo.R
import com.kotlin.education.android.easytodo.constants.IntentConstants
import com.kotlin.education.android.easytodo.database.TaskTypeConverters
import com.kotlin.education.android.easytodo.fragments.ImagePreviewFragment


/**
 * The activity for viewing images stored for a single task.
 */
class ImageViewerActivity : AppCompatActivity() {

    // the list of image names
    private lateinit var images: ArrayList<String>
    // the position of the image user has chosen
    private var currentImagePosition: Int = 0
    // the adapter for ViewPager
    private lateinit var adapter: MyFragmentPagerAdapter

    // all the static things for the activity
    companion object {

        /**
         * The method for creating intent to run the activity. The activity should be only run
         * using this method. This way no one doesn't need to know what to put to intent
         */
        fun createIntent(context: Context, images: ArrayList<String>, currentImagePosition: Int): Intent {
            val intent = Intent(context, ImageViewerActivity::class.java)
            intent.putExtra(IntentConstants.IMAGES, TaskTypeConverters().fromImages(images))
            intent.putExtra(IntentConstants.POSITION, currentImagePosition)
            return intent
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_viewer)
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        // lets get the images from the intent
        images = TaskTypeConverters().toImages(intent.getStringExtra(IntentConstants.IMAGES))!!
        // and get the current position of the image. This way the image shown is the image clicked.
        currentImagePosition = intent.getIntExtra(IntentConstants.POSITION, 0)

        setupViewPager()
        changeStatusBarColor()

    }

    /**
     * Changes the status bar color to black
     */
    private fun changeStatusBarColor(){
        val window = this.getWindow()
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.setStatusBarColor(ContextCompat.getColor(this, android.R.color.black))
    }

    private fun setupViewPager() {

        adapter = MyFragmentPagerAdapter(supportFragmentManager)

        /*
         * Lets look at for loop. These are all functional for loops.
         * https://kotlinlang.org/docs/tutorials/kotlin-for-py/loops.html
         */
        // the basic one
        /*
        for (image in images){
            var fragment: ImagePreviewFragment = ImagePreviewFragment.newInstance(image)
            adapter.addFragment(fragment)
        }

        // with a specific type
        for (image: String in images){
            var fragment: ImagePreviewFragment = ImagePreviewFragment.newInstance(image)
            adapter.addFragment(fragment)
        }
        */

        // when we need a position and value
        /*
        for ((position, value) in images.withIndex()){
            var fragment: ImagePreviewFragment = ImagePreviewFragment.newInstance(value)
            adapter.addFragment(fragment)
        }
        */

        // Just position of the counter
        for (position in 0 until images.size){
            val fragment: ImagePreviewFragment = ImagePreviewFragment.newInstance(images.get(position))
            adapter.addFragment(fragment)
        }

        viewPager!!.adapter = adapter
        viewPager.currentItem = currentImagePosition


    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu. menu_image_preview, menu)
        return true
    }

    /**
     * Opening a standard share dialog.
     */
    fun share(item: MenuItem){
        val sharingIntent = Intent(Intent.ACTION_SEND)
        val file = File(filesDir, adapter.getCurrentImageName(viewPager.currentItem))
        // this is the important part. The image is located in our internal storage,
        // so using FileProvider, we give othe apps access to it.
        // Note: the FileProvider is defined in AndroidManifest.xml file
        // and it has a paths.xml file definition
        val imageUri = FileProvider.getUriForFile(this, "com.kotlin.education.android.easytodo.fileprovider", file)
        sharingIntent.type = "image/jpeg"
        sharingIntent.putExtra(Intent.EXTRA_STREAM, imageUri)
        sharingIntent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        startActivity(Intent.createChooser(sharingIntent, getString(R.string.share_image)))
    }

    /**
     * The adapter for the ViewPager.
     */
    class MyFragmentPagerAdapter(manager: FragmentManager) : FragmentPagerAdapter(manager) {

        // list of fragments. The idea is to add all fragments to a list
        // and than provide the already created fragment. This way we do not have to
        // create the fragment every time its needed.
        private val fragmentsList: MutableList<ImagePreviewFragment> = mutableListOf()

        override fun getItem(position: Int): ImagePreviewFragment {
            return fragmentsList.get(position)
        }

        override fun getCount(): Int {
            return fragmentsList.size
        }

        /**
         * Adds a fragment to the list.
         */
        fun addFragment(fragment: ImagePreviewFragment) {
            fragmentsList.add(fragment)
        }

        /**
         * Returns an image name for a specific position. Used for sharing the image.
         */
        fun getCurrentImageName(position: Int): String {
            return fragmentsList.get(position).getImageName()
        }

    }


}
