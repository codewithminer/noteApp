package com.example.noteapp.adapter

import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.*
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.noteapp.R
import com.example.noteapp.model.data.Note
import com.example.noteapp.ui.dialog.DeleteDialog
import com.example.noteapp.ui.dialog.DeleteDialogListener
import com.example.noteapp.ui.dialog.FilterDialog
import com.example.noteapp.ui.viewmodel.NoteViewModel
import com.example.noteapp.utils.getBackgroundColor
import com.example.noteapp.utils.getForegroundColor
import com.example.noteapp.utils.setPersianNumber
import com.example.noteapp.utils.setTime
import kotlinx.android.synthetic.main.home_items.view.*

class MainAdapter(
    val context: Context,
    val noteViewModel: NoteViewModel,
    val activity: FragmentActivity
    ) : RecyclerView.Adapter<MainAdapter.NotesViewHolder>() {

    var isActionModeEnable = false
    var actionMode: ActionMode? = null
    var numberOfSelectedItem = 0
    var selectedItemPosition = arrayListOf<Int>()

    inner class NotesViewHolder(itemView: View): RecyclerView.ViewHolder(itemView)

    inner class ActionModeCallBack: ActionMode.Callback{
        override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
            val inflater = mode?.menuInflater
            inflater?.inflate(R.menu.menu_delete_action, menu)
            return true
        }

        override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
            when(item?.itemId){
                R.id.ic_delete_menu -> {
                    DeleteDialog(
                        context, object: DeleteDialogListener{
                            override fun onPositiveClick() {
                                for (i in selectedItemPosition){
                                    noteViewModel.deleteNote(differ.currentList[i].id)
                                    Log.i("adapter", "del")
                                }
                                mode?.finish()
                            }
                            override fun onNegativeClick() {
                                mode?.finish()
                            }
                        }
                    ).show()
                    return true
                }
                R.id.ic_select_all_menu -> {
                    noteViewModel.setNumberOfItemsSelectedToDelete(differ.currentList.size)
                    numberOfSelectedItem = differ.currentList.size
                    selectedItemPosition.clear()
                    for (i in 0 until differ.currentList.size){
                        selectedItemPosition.add(i)
                    }
                    notifyDataSetChanged()
                    Log.i("adapter", "hello")
                    return true}
            }
            return false
        }

        override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
            menu?.findItem(R.id.ic_delete_menu)?.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
            isActionModeEnable = true
            noteViewModel.selectedItemToDelete.observe(activity, Observer {
                mode?.title = setPersianNumber(it.toString())
            })

            return true
        }

        override fun onDestroyActionMode(mode: ActionMode?) {
            isActionModeEnable = false
            actionMode = null
            numberOfSelectedItem = 0
            selectedItemPosition.clear()
            notifyDataSetChanged()
        }
    }

    private fun clickItem(holder: NotesViewHolder) {
        if (holder.itemView.cb_delete.visibility == View.GONE){
            holder.itemView.cb_delete.visibility = View.VISIBLE
            selectedItemPosition.add(holder.adapterPosition)
            numberOfSelectedItem++
            noteViewModel.setNumberOfItemsSelectedToDelete(numberOfSelectedItem)
        }else{
            holder.itemView.cb_delete.visibility = View.GONE
            selectedItemPosition.remove(holder.adapterPosition)
            numberOfSelectedItem--
            noteViewModel.setNumberOfItemsSelectedToDelete(numberOfSelectedItem)
        }
    }

    private val differCallback = object: DiffUtil.ItemCallback<Note>(){
        override fun areItemsTheSame(oldItem: Note, newItem: Note): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Note, newItem: Note): Boolean {
            return oldItem == newItem
        }

    }

    val differ = AsyncListDiffer(this, differCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainAdapter.NotesViewHolder {
        return NotesViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.home_items, parent, false)
        )
    }

    private var onItemClickListener: ((Note) -> Unit)? = null

    override fun onBindViewHolder(holder: MainAdapter.NotesViewHolder, position: Int) {
//        holder.setIsRecyclable(false)
        val note = differ.currentList[position]
//        holder.itemView.layout_main_home_items.layoutParams =
//            StaggeredGridLayoutManager.LayoutParams(
//                LinearLayout.LayoutParams.MATCH_PARENT,
//                LinearLayout.LayoutParams.WRAP_CONTENT
//            )
//        val param = StaggeredGridLayoutManager.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
//            LinearLayout.LayoutParams.WRAP_CONTENT)
//        param.setMargins(10,5,10,25)
//        holder.itemView.layout_main_home_items.layoutParams = param
        holder.itemView.apply {
            cb_delete.visibility = View.GONE
            if (isActionModeEnable && selectedItemPosition.contains(position))
                holder.itemView.cb_delete.visibility = View.VISIBLE
//            if (isEnable && selectedItem == position)
//                isChecked = true
            tv_content_main.text = note.content
            tv_date_main.text = "" +
                    "${setPersianNumber(note.year.toString())}/" +
                    "${setPersianNumber((note.month+1).toString())}/" +
                    "${setPersianNumber(note.day.toString())}  " +
                    "${setPersianNumber(setTime(note.hour,note.minute))}"
            background_main.setBackgroundColor(Color.parseColor(getBackgroundColor(note.color_index)))
            when(note.color_index){
                1 ->{ tv_content_main.setTextColor(Color.parseColor(getForegroundColor(note.color_index)))
                        tv_date_main.setTextColor(Color.parseColor(getForegroundColor(note.color_index)))}
                2 ->{ tv_content_main.setTextColor(Color.parseColor(getForegroundColor(note.color_index)))
                        tv_date_main.setTextColor(Color.parseColor(getForegroundColor(note.color_index)))}
                3 ->{ tv_content_main.setTextColor(Color.parseColor(getForegroundColor(note.color_index)))
                        tv_date_main.setTextColor(Color.parseColor(getForegroundColor(note.color_index)))}
                4 ->{ tv_content_main.setTextColor(Color.parseColor(getForegroundColor(note.color_index)))
                        tv_date_main.setTextColor(Color.parseColor(getForegroundColor(note.color_index)))}
                5 ->{ tv_content_main.setTextColor(Color.parseColor(getForegroundColor(note.color_index)))
                        tv_date_main.setTextColor(Color.parseColor(getForegroundColor(note.color_index)))}
                6 ->{ tv_content_main.setTextColor(Color.parseColor(getForegroundColor(note.color_index)))
                        tv_date_main.setTextColor(Color.parseColor(getForegroundColor(note.color_index)))}
                7 ->{ tv_content_main.setTextColor(Color.parseColor(getForegroundColor(note.color_index)))
                        tv_date_main.setTextColor(Color.parseColor(getForegroundColor(note.color_index)))}
            }
            main_item_alarm.visibility = View.GONE
            if (note.alarm_id != -1){
                main_item_alarm.visibility = View.VISIBLE
                when(note.color_index){
                    1 -> main_item_alarm.setImageResource(R.drawable.ic_alarm_black)
                    2 -> main_item_alarm.setImageResource(R.drawable.ic_alarm_white)
                    3 -> main_item_alarm.setImageResource(R.drawable.ic_alarm_red)
                    4 -> main_item_alarm.setImageResource(R.drawable.ic_alarm_green)
                    5 -> main_item_alarm.setImageResource(R.drawable.ic_alarm_blue)
                    6 -> main_item_alarm.setImageResource(R.drawable.ic_alarm_yellow)
                    7 -> main_item_alarm.setImageResource(R.drawable.ic_alarm_pink)
                }
            }

            setOnClickListener {
                if (!isActionModeEnable)
                    onItemClickListener?.let { it(note) }
                else{
                    clickItem(holder)
                }
            }

            setOnLongClickListener {
                if (!isActionModeEnable){
                    isActionModeEnable = true
                    if (actionMode == null) actionMode = startActionMode(ActionModeCallBack())
                    clickItem(holder)
                }
                true
            }
        }
    }

    fun setOnItemClickListener(listener: (Note) -> Unit){
        onItemClickListener = listener
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

//    private fun showCheckBoxes(holder: NotesViewHolder) {
//        for (i in 0 until differ.currentList.size){
//        }
//    }


}