package com.kotlin.education.android.easytodo.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.core.widget.ContentLoadingProgressBar
import androidx.fragment.app.Fragment
import com.github.chrisbanes.photoview.PhotoView
import com.kotlin.education.android.easytodo.R
import com.kotlin.education.android.easytodo.TodoApplication
import com.kotlin.education.android.easytodo.constants.IntentConstants
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import java.io.File
import java.lang.Exception

/**
 * The Fragment representing a single image in ViewPager.
 * The Fragment receives the name of the image and displays the image.
 */
class ImagePreviewFragment : Fragment() {

    // the name of the image file.
    private lateinit var imageName : String

    companion object {

        /**
         * Creates a new instance of the ImagePreviewFragment.
         * It only need the name of the image.
         */
        fun newInstance(imageName: String): ImagePreviewFragment {
            // create a new instance of the fragment
            val newFragment = ImagePreviewFragment()
            // create a bundle to save image name
            val dataBundle = Bundle()
            // put the image name to bundle
            dataBundle.putString(IntentConstants.IMAGE_NAME, imageName)
            // set the arguments to bundle
            newFragment.arguments = dataBundle
            // return the fragment
            return newFragment

        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // inflate a new view from the layout
        val view: View? = inflater.inflate(R.layout.fragment_image_preview, container, false)
        // get the image name from the arguments
        imageName = arguments!!.getString(IntentConstants.IMAGE_NAME).toString()
        // find the imageview to set the image
        val imageView: PhotoView = view!!.findViewById(R.id.imagePreview)
        // find the progressbar to hide it when image is loaded
        val progressBar: ProgressBar = view.findViewById(R.id.progressBar)
        // load the image using Pisasso library. The callback part is optional, but in this
        // necessary. We must know when is the time to hide the progress bar
        Picasso.get().load(File(TodoApplication.getAppContent().filesDir, imageName))
            .into(imageView, object: Callback{
                override fun onSuccess() {
                    // the image is loaded, hide the progress bar
                    progressBar.visibility = View.GONE
                }

                override fun onError(e: Exception?) {
                    // todo Homework: Show error message when the image is not loaded.
                }
            })

        return view
    }

    /**
     * Returns an image name
     */
    fun getImageName(): String {
        return imageName
    }

}