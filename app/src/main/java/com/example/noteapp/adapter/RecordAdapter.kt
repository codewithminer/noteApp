package com.example.noteapp.adapter

import android.annotation.SuppressLint
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Handler
import android.transition.TransitionManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.SeekBar
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.noteapp.R
import com.example.noteapp.model.data.CheckBoxContent
import com.example.noteapp.model.data.Recording
import com.example.noteapp.ui.fragment.NoteFragment
import kotlinx.android.synthetic.main.item_recording.view.*

class RecordAdapter(
    private var recordList: ArrayList<Recording>
): RecyclerView.Adapter<RecordAdapter.ViewHolder>() {
    
    private var onClickListener: OnClickListener? = null

    fun setListener(onClickListener: OnClickListener) {
        this.onClickListener = onClickListener
    }

//    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView)
//    private val differCallBack = object: DiffUtil.ItemCallback<Recording>(){
//        override fun areItemsTheSame(oldItem: Recording, newItem: Recording): Boolean {
//            return oldItem.uri == newItem.uri
//        }
//
//        @SuppressLint("DiffUtilEquals")
//        override fun areContentsTheSame(oldItem: Recording, newItem: Recording): Boolean {
//            return oldItem == newItem
//        }
//    }
//
//    private val differ = AsyncListDiffer(this,differCallBack)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_recording, parent,false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindItems(recordList, onClickListener)
    }

    override fun getItemCount(): Int {
        return recordList.size
    }

    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        private var mediaPlayer: MediaPlayer? = null
        private var lastProgress = 0
        private val mHandler = Handler()
        fun bindItems(recordList: ArrayList<Recording>, onClickListener: OnClickListener?) {
            Log.i("Recorder", "BindItem $adapterPosition")
            val recording: Recording = recordList[adapterPosition]
            val imgViewPlay = itemView.findViewById<ImageView>(R.id.img_view_play)
            val seekBar = itemView.findViewById<SeekBar>(R.id.seekBar)
            if (recording.isPlaying){
                imgViewPlay.setImageResource(R.drawable.ic_stop_black)
                TransitionManager.beginDelayedTransition(itemView as ViewGroup)
//                seekUpdate(itemView)
                Log.i("Recorder", "change icon to stop")
            }else{
                imgViewPlay.setImageResource(R.drawable.ic_play_black)
                TransitionManager.beginDelayedTransition(itemView as ViewGroup)
                Log.i("Recorder", "change icon to play")
            }
//            manageSeekBar(seekBar)

            imgViewPlay.setOnClickListener {
                onClickListener?.onClickPlay(itemView, recording, recordList, adapterPosition)
            }
        }

//        private fun manageSeekBar(seekBar: SeekBar?) {
//            seekBar!!.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener{
//                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
//                    if (mediaPlayer != null && fromUser)
//                        mediaPlayer!!.seekTo(progress)
//                }
//
//                override fun onStartTrackingTouch(p0: SeekBar?) {
//                }
//
//                override fun onStopTrackingTouch(p0: SeekBar?) {
//                }
//
//            })
//        }
//        private var runnable: Runnable = Runnable {seekUpdate(itemView)}
//
//        private fun seekUpdate(itemView: View) {
//            if (mediaPlayer !=null){
//                val mCurrentPosition = mediaPlayer!!.currentPosition
//                itemView.seekBar.max = mediaPlayer!!.duration
//                itemView.seekBar.progress = mCurrentPosition
//                lastProgress = mCurrentPosition
//            }
//            mHandler.postDelayed(runnable,100)
//        }
    }

    interface OnClickListener {
        fun onClickPlay(view: View, record: Recording, recordingList: ArrayList<Recording>, position: Int)
    }

    fun addItem(item: Recording){
        recordList.add(item)
        notifyItemInserted(recordList.size)
    }
}

