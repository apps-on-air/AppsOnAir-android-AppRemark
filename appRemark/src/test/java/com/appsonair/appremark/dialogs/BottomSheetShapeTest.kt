package com.appsonair.appremark.dialogs

import android.widget.RadioGroup
import android.widget.SeekBar
import androidx.recyclerview.widget.RecyclerView
import com.appsonair.appremark.R
import com.appsonair.appremark.adapters.ColorPickerAdapter
import com.appsonair.appremark.testutil.TestHostActivity
import com.appsonair.appremark.testutil.ViewTestUtils
import ja.burhanrashid52.photoeditor.shape.ShapeType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.shadows.ShadowLooper

@RunWith(RobolectricTestRunner::class)
class BottomSheetShapeTest {

    private lateinit var activity: TestHostActivity
    private lateinit var sheet: BottomSheetShape
    private val events = mutableListOf<Any>()

    private val properties = object : BottomSheetShape.Properties {
        override fun onColorChanged(colorCode: Int) { events += "color:$colorCode" }
        override fun onOpacityChanged(opacity: Int) { events += "opacity:$opacity" }
        override fun onShapeSizeChanged(shapeSize: Int) { events += "size:$shapeSize" }
        override fun onShapePicked(shapeType: ShapeType) { events += shapeType }
    }

    @Before
    fun setUp() {
        activity = Robolectric.buildActivity(TestHostActivity::class.java).setup().get()
        sheet = BottomSheetShape()
    }

    private fun show(listener: BottomSheetShape.Properties? = properties) {
        sheet.setPropertiesChangeListener(listener)
        sheet.show(activity.supportFragmentManager, "shape")
        ShadowLooper.idleMainLooper()
    }

    private fun radioGroup() = sheet.requireView().findViewById<RadioGroup>(R.id.radio_group_shape)

    @Test
    fun pickingShapes_reportsMatchingShapeType() {
        show()

        // RadioGroup notifies once per affected button, so only the latest event matters
        radioGroup().check(R.id.rb_line)
        assertEquals(ShapeType.Line, events.last())
        radioGroup().check(R.id.rb_arrow)
        assertTrue(events.last() is ShapeType.Arrow)
        radioGroup().check(R.id.rb_oval)
        assertEquals(ShapeType.Oval, events.last())
        radioGroup().check(R.id.rb_rectangle)
        assertEquals(ShapeType.Rectangle, events.last())
        radioGroup().check(R.id.rb_brush)
        assertEquals(ShapeType.Brush, events.last())
    }

    @Test
    fun opacitySeekBar_reportsOpacity() {
        show()

        sheet.requireView().findViewById<SeekBar>(R.id.sb_opacity).progress = 42

        assertEquals(listOf<Any>("opacity:42"), events)
    }

    @Test
    fun brushSizeSeekBar_reportsSize() {
        show()

        sheet.requireView().findViewById<SeekBar>(R.id.sb_brush_size).progress = 17

        assertEquals(listOf<Any>("size:17"), events)
    }

    @Test
    fun progressFromUnknownSeekBar_isIgnored() {
        show()

        sheet.onProgressChanged(SeekBar(activity), 10, true)

        assertTrue(events.isEmpty())
    }

    @Test
    fun seekBars_withoutListener_doNotCrash() {
        show(listener = null)

        sheet.requireView().findViewById<SeekBar>(R.id.sb_opacity).progress = 10
        sheet.requireView().findViewById<SeekBar>(R.id.sb_brush_size).progress = 10
        sheet.onStartTrackingTouch(SeekBar(activity))
        sheet.onStopTrackingTouch(SeekBar(activity))
    }

    @Test
    fun pickingColor_reportsColorAndDismisses() {
        show()
        val rv = sheet.requireView().findViewById<RecyclerView>(R.id.rv_shape_colors)
        ViewTestUtils.layout(rv, width = 5000, height = 200)

        rv.findViewHolderForAdapterPosition(4)!!.itemView.performClick()
        ShadowLooper.idleMainLooper()

        assertEquals(listOf<Any>("color:${ColorPickerAdapter.getDefaultColors(activity)[4]}"), events)
        assertFalse(sheet.isAdded)
    }

    @Test
    fun pickingColor_withoutListener_keepsSheetOpen() {
        show(listener = null)
        val rv = sheet.requireView().findViewById<RecyclerView>(R.id.rv_shape_colors)
        ViewTestUtils.layout(rv, width = 5000, height = 200)

        rv.findViewHolderForAdapterPosition(0)!!.itemView.performClick()
        ShadowLooper.idleMainLooper()

        assertTrue(sheet.isAdded)
    }

    @Test
    fun colorPalette_showsDefaultColors() {
        show()

        val rv = sheet.requireView().findViewById<RecyclerView>(R.id.rv_shape_colors)
        assertEquals(12, rv.adapter!!.itemCount)
    }
}
