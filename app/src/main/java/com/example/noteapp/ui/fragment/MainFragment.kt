package com.example.noteapp.ui.fragment

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.SearchView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.noteapp.ui.MainActivity
import com.example.noteapp.R
import com.example.noteapp.adapter.MainAdapter
import com.example.noteapp.ui.viewmodel.NoteViewModel
import com.example.noteapp.ui.dialog.FilterDialog
import com.example.noteapp.ui.dialog.FilterDialogListener
import com.example.noteapp.utils.hideKeyboard
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_home.*

class MainFragment : Fragment(R.layout.fragment_home) {

    lateinit var noteViewModel: NoteViewModel
    lateinit var mainAdapter: MainAdapter
    var selectedOption = 2

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        noteViewModel = (activity as MainActivity).noteViewModel
        setUpRecycler()
        noteViewModel.getEmptyState().observe(viewLifecycleOwner, Observer { isEmpty ->
            Log.i("viewmodel", "empty state is $isEmpty")
            if (isEmpty) {
                todo_list_frame.visibility = View.GONE
                img_filter.visibility = View.GONE
                tv_title_main.visibility = View.GONE
                empty_state_frame.visibility = View.VISIBLE
                layout_main.setBackgroundColor(Color.WHITE)

            } else {
                todo_list_frame.visibility = View.VISIBLE
                img_filter.visibility = View.VISIBLE
                tv_title_main.visibility = View.VISIBLE
                empty_state_frame.visibility = View.GONE
                layout_main.setBackgroundColor(Color.parseColor("#F7F7F7"))
            }
        })

        noteViewModel.getSelectedSortOption().observe(viewLifecycleOwner, Observer {
            selectedOption = it
        })

        noteViewModel.noteId.observe(viewLifecycleOwner, Observer {
            Log.i("viewmodel", "object id is $it")
        })


        noteViewModel.getNotesForUI().observe(viewLifecycleOwner, Observer { notes ->
            if (notes != null && notes.isNotEmpty()) {
                noteViewModel.isEmpty.postValue(false)
                mainAdapter.differ.submitList(notes)
                Log.i("viewmdodel", "get notes successfully")
            } else {
                noteViewModel.isEmpty.postValue(true)
            }
        })

        img_filter.setOnClickListener {
            view.let { activity?.hideKeyboard(it) }
            FilterDialog(requireContext(), object : FilterDialogListener {
                override fun filterOneSelected() {
                    noteViewModel.getNotesByDateLatest().observe(viewLifecycleOwner, Observer { notes ->
                        notes.let { mainAdapter.differ.submitList(notes) }
                    })
                    noteViewModel.setSelectedSortOption(1)
                }

                override fun filterTwoSelected() {
                    noteViewModel.getNotesByDateOldest().observe(viewLifecycleOwner, Observer { notes ->
                        notes.let { mainAdapter.differ.submitList(notes) }
                    })
                    noteViewModel.setSelectedSortOption(2)
                }

                override fun filterThreeSelected() {
                    noteViewModel.getNotesByColor().observe(viewLifecycleOwner, Observer { notes ->
                        notes.let { mainAdapter.differ.submitList(notes) }
                        })
                    noteViewModel.setSelectedSortOption(3)
                    }
            }, selectedOption).show()


//            val popupMenu = PopupMenu(requireContext(), it)
//            popupMenu.setOnMenuItemClickListener { item ->
//                when(item.itemId){
//                    R.id.sortByDateLatest -> {
//                        noteViewModel.getNotesByDate().observe(viewLifecycleOwner, Observer { notes ->
//                            notes.let {
//                                mainAdapter.differ.submitList(notes)
//                            }
//                        })
//                        true
//                    }
//                    R.id.sortByColor ->{
//                        noteViewModel.getNotesByColor().observe(viewLifecycleOwner, Observer { notes ->
//                            mainAdapter.differ.submitList(notes)
//                        })
//                        true
//                    }
//                    else -> false
//                }
//            }
//            popupMenu.inflate(R.menu.menu_filter)
//            popupMenu.show()
        }

        mainAdapter.setOnItemClickListener {
            view.let { v -> activity?.hideKeyboard(v) }
            val action = MainFragmentDirections.actionHomeFragmentToNoteFragment(it.id)
            findNavController().navigate(action)        }

        btn_create_todo.setOnClickListener {
            view.let { activity?.hideKeyboard(it) }
            noteViewModel.alarmId.postValue(-1)
//            noteViewModel.noteId.postValue(-1)
            val action = MainFragmentDirections.actionHomeFragmentToNoteFragment(-1)
            findNavController().navigate(action)
        }

        searchbar.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                searchbar.clearFocus()
                if (query != null) {
                    searchDatabase(query)
                }
                return true
            }

            override fun onQueryTextChange(query: String?): Boolean {
                if (query != null) {
                    searchDatabase(query)
                }
                return true
            }
        })

//        val itemTouchHelperCallBack = object: ItemTouchHelper.SimpleCallback(
//            ItemTouchHelper.UP or ItemTouchHelper.DOWN,
//            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
//        ){
//            override fun onMove(
//                recyclerView: RecyclerView,
//                viewHolder: RecyclerView.ViewHolder,
//                target: RecyclerView.ViewHolder
//            ): Boolean {
//                return true
//            }
//
//            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
//                val position = viewHolder.adapterPosition
//                val note = mainAdapter.differ.currentList[position]
//                noteViewModel.deleteNote(note.id)
//                Snackbar.make(view, "یادداشت با موفقیت حذف شد.", Snackbar.LENGTH_LONG).apply {
//                    setAction("برگشت"){
//                        noteViewModel.saveNote(note)
//                    }
//                    show()
//                }
//            }
//        }
//        ItemTouchHelper(itemTouchHelperCallBack).apply {
//            attachToRecyclerView(rv_main)
//        }

    }



    private fun searchDatabase(query: String) {
        val searchQuery = "%$query%"
        var isLock = false
        if (query != "")
            isLock = true
        noteViewModel.searchNote(searchQuery, isLock).observe(viewLifecycleOwner, Observer { notes ->
            mainAdapter.differ.submitList(notes)
        })
    }

    private fun setUpRecycler() {
        mainAdapter = MainAdapter(requireContext(),noteViewModel, requireActivity())
        rv_main.apply {
            adapter = mainAdapter
            layoutManager = StaggeredGridLayoutManager(
                2, StaggeredGridLayoutManager.VERTICAL
            )
        }
    }

    override fun onResume() {
        super.onResume()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            var flags = requireActivity().window.decorView.systemUiVisibility // get current flag
            flags = flags or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR // add LIGHT_STATUS_BAR to flag
            requireActivity().window.decorView.systemUiVisibility = flags
            requireActivity().window.statusBarColor =
                ContextCompat.getColor(requireContext(), R.color.status_white)
        } else {
            requireActivity().window.statusBarColor =
                ContextCompat.getColor(requireContext(), R.color.status_bar_color)
        }
    }
}