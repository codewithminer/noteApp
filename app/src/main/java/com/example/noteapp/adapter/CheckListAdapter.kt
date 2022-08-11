package com.example.noteapp.adapter

import android.content.res.ColorStateList
import android.graphics.Color
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.noteapp.R
import com.example.noteapp.model.data.CheckBoxContent
import com.example.noteapp.ui.viewmodel.NoteViewModel
import com.example.noteapp.utils.getBackgroundColor
import kotlinx.android.synthetic.main.check_box_layout.view.*

class CheckListAdapter(
    val noteViewModel: NoteViewModel
): RecyclerView.Adapter<CheckListAdapter.CheckListViewHolder>() {
    var index = 1
    var beforeText = ""
    inner class CheckListViewHolder(itemView: View): RecyclerView.ViewHolder(itemView)

    private val differCallBack = object: DiffUtil.ItemCallback<CheckBoxContent>(){
        override fun areItemsTheSame(oldItem: CheckBoxContent, newItem: CheckBoxContent): Boolean {
            return oldItem.content == newItem.content
        }

        override fun areContentsTheSame(oldItem: CheckBoxContent, newItem: CheckBoxContent): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this,differCallBack)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CheckListAdapter.CheckListViewHolder {
        return CheckListViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.check_box_layout,parent,false)
        )
    }

    override fun onBindViewHolder(holder: CheckListAdapter.CheckListViewHolder, position: Int) {
        val checkList = differ.currentList[position]

        holder.itemView.apply {
            et_checkbox_list.setText(checkList.content)
            checkbox.isChecked = checkList.check
//            et_checkbox_list.setText(noteViewModel.checkBoxContent[holder.adapterPosition])
//            Log.i("checkbox", noteViewModel.checkBoxContent[holder.adapterPosition].toString())
            if (index == 2){
                checkbox.buttonTintList = ColorStateList.valueOf(resources.getColor(R.color.white))
                et_checkbox_list.setTextColor(Color.parseColor(getBackgroundColor(1)))
                remove_checkbox.setImageResource(R.drawable.ic_cancel_white)
            }else{
                checkbox.buttonTintList = ColorStateList.valueOf(resources.getColor(R.color.gray_black))
                et_checkbox_list.setTextColor(Color.parseColor(getBackgroundColor(2)))
                remove_checkbox.setImageResource(R.drawable.ic_cancel_black)
            }
        }
        holder.itemView.et_checkbox_list.setOnFocusChangeListener { view, hasFocus ->
            if (hasFocus)
                Log.i("checkbox", holder.adapterPosition.toString())
        }
        holder.itemView.et_checkbox_list.addTextChangedListener(object: TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                beforeText = p0.toString()
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                noteViewModel.checkBoxContent[holder.adapterPosition].content = p0.toString()
            }

            override fun afterTextChanged(p0: Editable?) {
                if (beforeText != p0.toString())
                    noteViewModel.contentsChange.value = true
            }

        })
        holder.itemView.checkbox.setOnCheckedChangeListener { compoundButton, b ->
            noteViewModel.checkBoxContent[holder.adapterPosition].check = b
            noteViewModel.contentsChange.value = true
        }

        holder.itemView.remove_checkbox.setOnClickListener {
            noteViewModel.checkBoxContent.removeAt(holder.adapterPosition)
            notifyItemRemoved(holder.adapterPosition)
            noteViewModel.boxCountCheck.value = holder.adapterPosition
            noteViewModel.contentsChange.value = true
        }

    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    fun changeIndex(ind: Int){
        index = ind
    }
}