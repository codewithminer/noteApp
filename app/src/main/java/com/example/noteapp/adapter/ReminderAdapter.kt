package com.example.noteapp.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.noteapp.R
import com.example.noteapp.model.data.DateModel
import com.example.noteapp.model.data.Note
import com.example.noteapp.utils.getBackgroundColor
import com.example.noteapp.utils.getForegroundColor
import com.example.noteapp.utils.setPersianNumber
import com.example.noteapp.utils.setTime
import kotlinx.android.synthetic.main.date_picker_layout.view.*
import kotlinx.android.synthetic.main.home_items.view.*

class ReminderAdapter: RecyclerView.Adapter<ReminderAdapter.RemindersViewHolder>() {
    var selectedItem = 0
    inner class RemindersViewHolder(itemView: View): RecyclerView.ViewHolder(itemView)

    private val differCallback = object: DiffUtil.ItemCallback<DateModel>(){
        override fun areItemsTheSame(oldItem: DateModel, newItem: DateModel): Boolean {
            return oldItem.Day == newItem.Day
        }

        override fun areContentsTheSame(oldItem: DateModel, newItem: DateModel): Boolean {
            return oldItem == newItem
        }

    }

    val differ = AsyncListDiffer(this, differCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReminderAdapter.RemindersViewHolder {
        return RemindersViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.date_picker_layout, parent, false)
        )
    }

    private var onItemClickListener: ((DateModel) -> Unit)? = null

    override fun onBindViewHolder(holder: ReminderAdapter.RemindersViewHolder, position: Int) {
//        holder.setIsRecyclable(false)
        val reminder = differ.currentList[position]

        holder.itemView.apply {
            if (position == selectedItem){
                main_layout.setBackgroundResource(R.drawable.date_background)
                tv_month.setTextColor(Color.BLACK)
                tv_day_of_week.setTextColor(Color.BLACK)
                tv_day.setTextColor(Color.BLACK)
            }else{
                main_layout.setBackgroundColor(Color.parseColor("#3c3c3c"))
                tv_month.setTextColor(Color.WHITE)
                tv_day_of_week.setTextColor(Color.WHITE)
                tv_day.setTextColor(Color.WHITE)
            }
            tv_month.text = reminder.MonthName
            tv_day.text = reminder.Day
            tv_day_of_week.text = reminder.DayOfWeekName

            setOnClickListener {
                selectedItem = holder.adapterPosition
                notifyDataSetChanged()
                onItemClickListener?.let { it(reminder) }
            }
        }
    }

    fun setOnItemClickListener(listener: (DateModel) -> Unit){
        onItemClickListener = listener
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    fun getSelectedDate(): DateModel{
        return differ.currentList[selectedItem]
    }
}