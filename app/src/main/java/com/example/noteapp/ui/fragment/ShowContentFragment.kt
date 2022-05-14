package com.example.noteapp.ui.fragment

import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import com.example.noteapp.ui.MainActivity
import com.example.noteapp.R
import com.example.noteapp.ui.viewmodel.NoteViewModel
import com.example.noteapp.utils.*
import kotlinx.android.synthetic.main.fragment_show_content.*

class ShowContentFragment : Fragment(R.layout.fragment_show_content) {

    private lateinit var noteViewModel: NoteViewModel


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        noteViewModel = (activity as MainActivity).noteViewModel

        btn_edit.setOnClickListener {
            NavHostFragment.findNavController(this).navigateUp()
//                val action =
//                    ShowContentFragmentDirections.actionShowContentFragmentToNoteFragment(args.id)
//                findNavController().navigate(action)
        }
        setBottomAppBarColor(COLOR_INDEX)
        tv_show_content.loadDataWithBaseURL(
            null,
            markDown(final_text, COLOR_INDEX),
            "text/html",
            "utf-8",
            null
        )


//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
//            tv_show_content.text = HtmlCompat.fromHtml(str, HtmlCompat.FROM_HTML_MODE_LEGACY)
//        else
//            tv_show_content.text = Html.fromHtml(str)
    }

    private fun setBottomAppBarColor(color_index: Int) {
        requireActivity().window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        when (color_index) {
            1 -> {
                bottom_appbar.backgroundTint =
                    AppCompatResources.getColorStateList(requireContext(), R.color.gray_black_text)
                btn_edit.supportBackgroundTintList =
                    AppCompatResources.getColorStateList(requireContext(), R.color.gray_black_text)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    var flags =
                        requireActivity().window.decorView.systemUiVisibility // get current flag
                    flags =
                        flags or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR // add LIGHT_STATUS_BAR to flag
                    requireActivity().window.decorView.systemUiVisibility = flags
                    requireActivity().window.statusBarColor =
                        ContextCompat.getColor(requireContext(), R.color.dark_white)
                } else
                    requireActivity().window.statusBarColor =
                        ContextCompat.getColor(requireContext(), R.color.status_bar_color)
                btn_edit.supportImageTintList =
                    AppCompatResources.getColorStateList(requireContext(), R.color.gray_black)
            }

            2 -> {
                bottom_appbar.backgroundTint =
                    AppCompatResources.getColorStateList(requireContext(), R.color.dark_white_text)
                btn_edit.supportBackgroundTintList =
                    AppCompatResources.getColorStateList(requireContext(), R.color.dark_white_text)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    var flag = requireActivity().window.decorView.systemUiVisibility
                    flag = flag xor View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                    requireActivity().window.decorView.systemUiVisibility = flag
                }
                requireActivity().window.statusBarColor =
                    ContextCompat.getColor(requireContext(), R.color.gray_black)
            }


            3 -> {
                bottom_appbar.backgroundTintList =
                    AppCompatResources.getColorStateList(requireContext(), R.color.red_text)
                btn_edit.supportBackgroundTintList =
                    AppCompatResources.getColorStateList(requireContext(), R.color.red_text)
                requireActivity().window.statusBarColor =
                    ContextCompat.getColor(requireContext(), R.color.red)
            }

            4 -> {
                bottom_appbar.backgroundTintList =
                    AppCompatResources.getColorStateList(requireContext(), R.color.green_text)
                btn_edit.supportBackgroundTintList =
                    AppCompatResources.getColorStateList(requireContext(), R.color.green_text)
                requireActivity().window.statusBarColor =
                    ContextCompat.getColor(requireContext(), R.color.green)
            }

            5 -> {
                bottom_appbar.backgroundTintList =
                    AppCompatResources.getColorStateList(requireContext(), R.color.blue_text)
                btn_edit.supportBackgroundTintList =
                    AppCompatResources.getColorStateList(requireContext(), R.color.blue_text)
                requireActivity().window.statusBarColor =
                    ContextCompat.getColor(requireContext(), R.color.blue)
            }

            6 -> {
                bottom_appbar.backgroundTintList =
                    AppCompatResources.getColorStateList(requireContext(), R.color.yellow_text)
                btn_edit.supportBackgroundTintList =
                    AppCompatResources.getColorStateList(requireContext(), R.color.yellow_text)
                requireActivity().window.statusBarColor =
                    ContextCompat.getColor(requireContext(), R.color.yellow)
            }

            7 -> {
                bottom_appbar.backgroundTintList =
                    AppCompatResources.getColorStateList(requireContext(), R.color.pink_text)
                btn_edit.supportBackgroundTintList =
                    AppCompatResources.getColorStateList(requireContext(), R.color.pink_text)
                requireActivity().window.statusBarColor =
                    ContextCompat.getColor(requireContext(), R.color.pink)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
//        findNavController().navigateUp()
    }
}