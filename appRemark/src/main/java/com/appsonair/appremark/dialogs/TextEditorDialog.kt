package com.appsonair.appremark.dialogs

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.appsonair.appremark.R
import com.appsonair.appremark.adapters.ColorPickerAdapter
import com.appsonair.appremark.adapters.ColorPickerAdapter.OnColorPickerClickListener

class TextEditorDialog : DialogFragment() {

    private lateinit var mAddTextEditText: EditText
    private lateinit var tvDone: TextView
    private lateinit var mInputMethodManager: InputMethodManager
    private var mColorCode = 0
    private var mTextEditorListener: TextEditorListener? = null

    interface TextEditorListener {
        fun onDone(inputText: String, colorCode: Int)
    }

    override fun onStart() {
        super.onStart()
        val dialog = dialog
        //Make dialog full screen with transparent background
        if (dialog != null) {
            val width = ViewGroup.LayoutParams.MATCH_PARENT
            val height = ViewGroup.LayoutParams.MATCH_PARENT
            dialog.window!!.setLayout(width, height)
            dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.add_text_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val activity = requireActivity()

        mAddTextEditText = view.findViewById(R.id.et_add_text)
        mInputMethodManager =
            activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        tvDone = view.findViewById(R.id.tv_done)

        //Setup the color picker for text color
        val addTextColorPickerRecyclerView: RecyclerView =
            view.findViewById(R.id.rv_add_text_color_picker)
        val layoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
        addTextColorPickerRecyclerView.layoutManager = layoutManager
       // addTextColorPickerRecyclerView.setHasFixedSize(true) //commented due to version issue after downgrade
        val colorPickerAdapter = ColorPickerAdapter(activity)

        //This listener will change the text color when clicked on any color from picker
        colorPickerAdapter.setOnColorPickerClickListener(object : OnColorPickerClickListener {
            override fun onColorPickerClickListener(colorCode: Int) {
                mColorCode = colorCode
                mAddTextEditText.setTextColor(colorCode)
            }
        })

        addTextColorPickerRecyclerView.adapter = colorPickerAdapter

        val arguments = requireArguments()

        mAddTextEditText.setText(arguments.getString(EXTRA_INPUT_TEXT))
        mColorCode = arguments.getInt(EXTRA_COLOR_CODE)
        mAddTextEditText.setTextColor(mColorCode)
        mAddTextEditText.requestFocus()
        @Suppress("DEPRECATION")
        mInputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)

        //Make a callback on activity when user is done with text editing
        tvDone.setOnClickListener { onClickListenerView ->
            mInputMethodManager.hideSoftInputFromWindow(onClickListenerView.windowToken, 0)
            dismiss()
            val inputText = mAddTextEditText.text.toString()
            val textEditorListener = mTextEditorListener
            if (inputText.isNotEmpty() && textEditorListener != null) {
                textEditorListener.onDone(inputText, mColorCode)
            }
        }
    }

    //Callback to listener if user is done with text editing
    fun setOnTextEditorListener(textEditorListener: TextEditorListener) {
        mTextEditorListener = textEditorListener
    }

    companion object {
        private val TAG: String = TextEditorDialog::class.java.simpleName
        const val EXTRA_INPUT_TEXT = "extra_input_text"
        const val EXTRA_COLOR_CODE = "extra_color_code"

        //Show dialog with provide text and text color
        //Show dialog with default text input as empty and text color white
        @JvmOverloads
        fun show(
            appCompatActivity: AppCompatActivity,
            inputText: String = "",
            @ColorInt colorCode: Int = ContextCompat.getColor(appCompatActivity, R.color.white)
        ): TextEditorDialog {
            val args = Bundle()
            args.putString(EXTRA_INPUT_TEXT, inputText)
            args.putInt(EXTRA_COLOR_CODE, colorCode)
            val fragment = TextEditorDialog()
            fragment.arguments = args
            fragment.show(appCompatActivity.supportFragmentManager, TAG)
            return fragment
        }
    }
}