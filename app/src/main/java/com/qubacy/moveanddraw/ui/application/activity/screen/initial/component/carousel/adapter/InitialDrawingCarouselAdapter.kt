package com.qubacy.moveanddraw.ui.application.activity.screen.initial.component.carousel.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.qubacy.moveanddraw.databinding.ComponentDrawingCarouselItemBinding

class InitialDrawingCarouselAdapter(

) : ListAdapter<Uri, InitialDrawingCarouselAdapter.InitialDrawingCarouselViewHolder>(
    DIFFER_ITEM_CALLBACK
) {
    companion object {
        val DIFFER_ITEM_CALLBACK = object : DiffUtil.ItemCallback<Uri>() {
            override fun areItemsTheSame(oldItem: Uri, newItem: Uri): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: Uri, newItem: Uri): Boolean {
                return oldItem == newItem
            }

        }
    }

    class InitialDrawingCarouselViewHolder(
        private val mItemBinding: ComponentDrawingCarouselItemBinding
    ) : RecyclerView.ViewHolder(mItemBinding.root) {
        fun bind(imageUri: Uri) {
            mItemBinding.componentDrawingCarouselItemImage.setImageURI(imageUri)
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): InitialDrawingCarouselViewHolder {
        val itemBinding = ComponentDrawingCarouselItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false)

        return InitialDrawingCarouselViewHolder(itemBinding)
    }

    override fun onBindViewHolder(
        holder: InitialDrawingCarouselViewHolder,
        position: Int
    ) {
        val item = getItem(position)

        holder.bind(item)
    }
}