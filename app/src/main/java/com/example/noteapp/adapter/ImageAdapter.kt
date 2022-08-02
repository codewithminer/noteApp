package com.example.noteapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.noteapp.R
import com.example.noteapp.model.data.Image
import kotlinx.android.synthetic.main.item_image.view.*


class ImageAdapter(
    private val onPhotoClick: (Image, position: Int) -> Unit
) : ListAdapter<Image, ImageAdapter.ImageViewHolder>(Companion) {

    inner class ImageViewHolder(itemView: View): RecyclerView.ViewHolder(itemView)
    companion object: DiffUtil.ItemCallback<Image>(){
        override fun areItemsTheSame(oldItem: Image, newItem: Image): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Image, newItem: Image): Boolean {
            return oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        return ImageViewHolder(
            LayoutInflater
                .from(parent.context)
                .inflate(R.layout.item_image, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val photo = currentList[position]
        holder.itemView.apply {
//            iv_photo.setImageURI(photo.contentUri)
            iv_photo.setImageBitmap(photo.bitmap)

            iv_photo.setOnLongClickListener {
                onPhotoClick(photo,holder.adapterPosition)
                true
            }

            iv_photo.setOnClickListener {
                onPhotoClick(photo,-1)
            }
        }
    }
}