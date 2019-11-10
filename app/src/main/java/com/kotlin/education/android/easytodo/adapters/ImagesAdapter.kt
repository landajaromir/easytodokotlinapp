package com.kotlin.education.android.easytodo.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.kotlin.education.android.easytodo.R
import com.kotlin.education.android.easytodo.activities.ImageViewerActivity
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.row_image.view.*
import java.io.File

/**
 * The adapter for showing a list of images associated with a task.
 * The definition of the adapter is usually in the activity class,
 * however we will use this adapter twice.
 * When adding or edition task we have a delete option. This option is however hidden
 * when just showing images in detail.
 */
class ImagesAdapter(private val context: AppCompatActivity, private val images: ArrayList<String>, private val hideDelete: Boolean) : RecyclerView.Adapter<ImagesAdapter.ImageViewHolder>()  {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        return ImageViewHolder(LayoutInflater.from(context).inflate(R.layout.row_image, parent, false))
    }

    override fun getItemCount(): Int {
        return images.size
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val image: String = images.get(position)
        // load the image using Picasso.
        Picasso.get().load(File(context.filesDir,image)).into(holder.image)
        holder.itemView.setOnClickListener {
            context.startActivity(ImageViewerActivity.createIntent(context, images, holder.adapterPosition))
        }

        if (!hideDelete) {
            holder.delete.setOnClickListener {
                images.removeAt(holder.adapterPosition)
                notifyItemRemoved(holder.adapterPosition)
            }
        } else {
            holder.delete.visibility = View.GONE
        }
    }

    /**
     * The ViewHolder class holds all the references of the views for each row.
     * It extends RecyclerView.ViewHolder which needs only the View containing all the other elements.
     */
    inner class ImageViewHolder(view: View) : RecyclerView.ViewHolder(view){
        val image: ImageView = view.image
        val delete: ImageButton = view.deleteImage
    }
}