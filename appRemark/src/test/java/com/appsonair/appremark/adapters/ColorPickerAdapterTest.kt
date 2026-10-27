package com.appsonair.appremark.adapters

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.appsonair.appremark.R
import com.appsonair.appremark.testutil.TestHostActivity
import com.appsonair.appremark.testutil.ViewTestUtils
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ColorPickerAdapterTest {

    private lateinit var activity: TestHostActivity

    @Before
    fun setUp() {
        activity = Robolectric.buildActivity(TestHostActivity::class.java).setup().get()
    }

    private fun attach(adapter: ColorPickerAdapter): RecyclerView {
        val rv = RecyclerView(activity).apply {
            layoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
            this.adapter = adapter
        }
        activity.setContentView(rv)
        ViewTestUtils.layout(rv, width = 5000)
        return rv
    }

    @Test
    fun defaultColors_areTheTwelvePaletteColors() {
        val ids = listOf(
            R.color.blue_color_picker, R.color.brown_color_picker, R.color.green_color_picker,
            R.color.orange_color_picker, R.color.red_color_picker, R.color.black,
            R.color.red_orange_color_picker, R.color.sky_blue_color_picker, R.color.violet_color_picker,
            R.color.white, R.color.yellow_color_picker, R.color.yellow_green_color_picker
        )

        assertEquals(
            ids.map { ContextCompat.getColor(activity, it) },
            ColorPickerAdapter.getDefaultColors(activity)
        )
    }

    @Test
    fun contextConstructor_usesDefaultPalette() {
        assertEquals(12, ColorPickerAdapter(activity).itemCount)
    }

    @Test
    fun customColors_areUsedAsIs() {
        val adapter = ColorPickerAdapter(activity, listOf(Color.RED, Color.GREEN))

        assertEquals(2, adapter.itemCount)
    }

    @Test
    fun emptyColorList_hasNoItems() {
        assertEquals(0, ColorPickerAdapter(activity, emptyList()).itemCount)
    }

    @Test
    fun bind_paintsSwatchWithColor() {
        val colors = listOf(Color.RED, Color.BLUE, Color.YELLOW)
        val rv = attach(ColorPickerAdapter(activity, colors))

        colors.forEachIndexed { i, color ->
            val holder = rv.findViewHolderForAdapterPosition(i) as ColorPickerAdapter.ViewHolder
            assertEquals(color, (holder.colorPickerView.background as ColorDrawable).color)
        }
    }

    @Test
    fun click_reportsSelectedColor() {
        val picked = mutableListOf<Int>()
        val adapter = ColorPickerAdapter(activity, listOf(Color.RED, Color.BLUE, Color.YELLOW))
        adapter.setOnColorPickerClickListener(object : ColorPickerAdapter.OnColorPickerClickListener {
            override fun onColorPickerClickListener(colorCode: Int) {
                picked += colorCode
            }
        })
        val rv = attach(adapter)

        rv.findViewHolderForAdapterPosition(2)!!.itemView.performClick()
        rv.findViewHolderForAdapterPosition(0)!!.itemView.performClick()

        assertEquals(listOf(Color.YELLOW, Color.RED), picked)
    }

    @Test
    fun settingListenerAgain_replacesPrevious() {
        val first = mutableListOf<Int>()
        val second = mutableListOf<Int>()
        val adapter = ColorPickerAdapter(activity, listOf(Color.RED))
        adapter.setOnColorPickerClickListener(object : ColorPickerAdapter.OnColorPickerClickListener {
            override fun onColorPickerClickListener(colorCode: Int) { first += colorCode }
        })
        adapter.setOnColorPickerClickListener(object : ColorPickerAdapter.OnColorPickerClickListener {
            override fun onColorPickerClickListener(colorCode: Int) { second += colorCode }
        })
        val rv = attach(adapter)

        rv.findViewHolderForAdapterPosition(0)!!.itemView.performClick()

        assertEquals(emptyList<Int>(), first)
        assertEquals(listOf(Color.RED), second)
    }
}
