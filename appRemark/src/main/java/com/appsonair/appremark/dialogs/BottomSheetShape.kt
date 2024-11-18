package com.appsonair.appremark.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioGroup
import android.widget.SeekBar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.appsonair.appremark.R
import com.appsonair.appremark.adapters.ColorPickerAdapter
import com.appsonair.appremark.adapters.ColorPickerAdapter.OnColorPickerClickListener
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import ja.burhanrashid52.photoeditor.shape.ShapeType

class BottomSheetShape : BottomSheetDialogFragment(), SeekBar.OnSeekBarChangeListener {
    private var mProperties: Properties? = null

    interface Properties {
        fun onColorChanged(colorCode: Int)
        fun onOpacityChanged(opacity: Int)
        fun onShapeSizeChanged(shapeSize: Int)
        fun onShapePicked(shapeType: ShapeType)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.bottom_sheet_shape, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val rvColor: RecyclerView = view.findViewById(R.id.rv_shape_colors)
        val sbOpacity = view.findViewById<SeekBar>(R.id.sb_opacity)
        val sbBrushSize = view.findViewById<SeekBar>(R.id.sb_brush_size)
        val shapeGroup = view.findViewById<RadioGroup>(R.id.radio_group_shape)

        // shape picker
        shapeGroup.setOnCheckedChangeListener { _: RadioGroup?, checkedId: Int ->
            when (checkedId) {
                R.id.rb_line -> {
                    mProperties!!.onShapePicked(ShapeType.Line)
                }

                R.id.rb_arrow -> {
                    mProperties!!.onShapePicked(ShapeType.Arrow())
                }

                R.id.rb_oval -> {
                    mProperties!!.onShapePicked(ShapeType.Oval)
                }

                R.id.rb_rectangle -> {
                    mProperties!!.onShapePicked(ShapeType.Rectangle)
                }

                else -> {
                    mProperties!!.onShapePicked(ShapeType.Brush)
                }
            }
        }
        sbOpacity.setOnSeekBarChangeListener(this)
        sbBrushSize.setOnSeekBarChangeListener(this)

        val activity = requireActivity()

        // TODO(lucianocheng): Move layoutManager to a xml file.
        val layoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
        rvColor.layoutManager = layoutManager
        rvColor.setHasFixedSize(true)
        val colorPickerAdapter = ColorPickerAdapter(activity)
        colorPickerAdapter.setOnColorPickerClickListener(object : OnColorPickerClickListener {
            override fun onColorPickerClickListener(colorCode: Int) {
                if (mProperties != null) {
                    dismiss()
                    mProperties!!.onColorChanged(colorCode)
                }
            }
        })
        rvColor.adapter = colorPickerAdapter
    }

    fun setPropertiesChangeListener(properties: Properties?) {
        mProperties = properties
    }

    override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
        when (seekBar.id) {
            R.id.sb_opacity -> if (mProperties != null) {
                mProperties!!.onOpacityChanged(i)
            }

            R.id.sb_brush_size -> if (mProperties != null) {
                mProperties!!.onShapeSizeChanged(i)
            }
        }
    }

    override fun onStartTrackingTouch(seekBar: SeekBar) {}
    override fun onStopTrackingTouch(seekBar: SeekBar) {}
}