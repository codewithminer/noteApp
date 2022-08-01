package com.example.noteapp.adapter

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorStateListDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.noteapp.R
import com.example.noteapp.model.data.Recording
import com.example.noteapp.ui.viewmodel.NoteViewModel
import com.example.noteapp.utils.COLOR_INDEX
import com.example.noteapp.utils.getBackgroundColor
import com.example.noteapp.utils.getForegroundColor
import kotlinx.android.synthetic.main.item_recording.view.*

class RecorderAdapter(
    val noteViewModel: NoteViewModel,
    private val recorderCallBack: RecorderCallBack
) :RecyclerView.Adapter<RecorderAdapter.ViewHolder>(){

//    private var recorderCallBack: RecorderCallBack? = null
//    fun MyAdapter(callback: RecorderCallBack) {
//        this.recorderCallBack = callback
//    }

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView)

    private val differCallBack = object: DiffUtil.ItemCallback<Recording>(){
        override fun areItemsTheSame(oldItem: Recording, newItem: Recording): Boolean {
            return oldItem.uri == newItem.uri
        }
        override fun areContentsTheSame(oldItem: Recording, newItem: Recording): Boolean {
            return oldItem == newItem
        }
    }
    val differ = AsyncListDiffer(this, differCallBack)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater
                .from(parent.context)
                .inflate(R.layout.item_recording, parent, false)
        )
    }
    private var onItemClickListener: ((Recording) -> Unit)? = null

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val record = differ.currentList[position]
        record.isPlaying = false
        holder.itemView.apply {
            seekBar.tag = holder.adapterPosition
            seekBar.progress = noteViewModel.recordPosition
//            record_background.setBackgroundColor(Color.parseColor(getBackgroundColor(COLOR_INDEX)))
//            record_background.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.drawable.record_bg_color_white))
            record_background.setBackgroundColor(Color.parseColor(getForegroundColor(COLOR_INDEX)))
            if (COLOR_INDEX == 1){
                img_remove_record.setImageResource(R.drawable.ic_delete_white)
                seekBar.thumbTintList = ColorStateList.valueOf(Color.WHITE)
                seekBar.progressTintList = ColorStateList.valueOf(Color.parseColor(
                    getBackgroundColor(1)
                ))
                seekBar.progressBackgroundTintList = ColorStateList.valueOf(Color.parseColor(
                    getBackgroundColor(1)
                ))
            }
            else{
                img_remove_record.setImageResource(R.drawable.ic_delete)
                seekBar.thumbTintList = ColorStateList.valueOf(Color.parseColor(getBackgroundColor(2)))
                seekBar.progressTintList = ColorStateList.valueOf(Color.parseColor(
                    getBackgroundColor(2)
                ))
                seekBar.progressBackgroundTintList = ColorStateList.valueOf(Color.parseColor(
                    getBackgroundColor(2)
                ))
            }
            if (record.isPlaying){
                if (COLOR_INDEX == 1)
                    img_view_play.setImageResource(R.drawable.ic_stop_white)
                else
                    img_view_play.setImageResource(R.drawable.ic_stop_black)

            }else{
                if (COLOR_INDEX == 1)
                    img_view_play.setImageResource(R.drawable.ic_play_white)
                else
                    img_view_play.setImageResource(R.drawable.ic_play_black)
            }
            img_view_play.setOnClickListener {
//                onItemClickListener?.let { it(record) }
                seekBar.tag = holder.adapterPosition
                recorderCallBack.getRecordItems(record,holder, holder.itemView, holder.adapterPosition)
            }
            img_remove_record.setOnClickListener {
                recorderCallBack.removeRecord(record, holder.adapterPosition)
            }
        }
    }


    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    fun setOnItemClickListener(listener: (Recording) -> Unit) {
        onItemClickListener = listener
    }

    interface RecorderCallBack{
        fun getRecordItems(record: Recording, holder: ViewHolder, itemView: View, position: Int)
        fun removeRecord(record: Recording, position: Int)
    }

}