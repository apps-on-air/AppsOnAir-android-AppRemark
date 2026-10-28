package com.appsonair.appremark.adapters

import android.widget.ImageView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.appsonair.appremark.R
import com.appsonair.appremark.enums.ToolType
import com.appsonair.appremark.testutil.TestHostActivity
import com.appsonair.appremark.testutil.ViewTestUtils
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf

@RunWith(RobolectricTestRunner::class)
class ToolsAdapterTest {

    private val selected = mutableListOf<ToolType>()
    private lateinit var adapter: ToolsAdapter
    private lateinit var recyclerView: RecyclerView

    @Before
    fun setUp() {
        val activity = Robolectric.buildActivity(TestHostActivity::class.java).setup().get()
        adapter = ToolsAdapter(object : ToolsAdapter.OnItemSelected {
            override fun onToolSelected(toolType: ToolType) {
                selected += toolType
            }
        })
        recyclerView = RecyclerView(activity).apply {
            layoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
            this.adapter = this@ToolsAdapterTest.adapter
        }
        activity.setContentView(recyclerView)
        ViewTestUtils.layout(recyclerView)
    }

    @Test
    fun itemCount_showsEnabledToolsOnly() {
        // emoji and gallery are intentionally disabled in the adapter
        assertEquals(5, adapter.itemCount)
    }

    @Test
    fun bind_setsToolIcons() {
        val expected = listOf(R.drawable.ic_oval, R.drawable.ic_text, R.drawable.ic_eraser, R.drawable.ic_undo, R.drawable.ic_redo)
        expected.forEachIndexed { index, icon ->
            val holder = recyclerView.findViewHolderForAdapterPosition(index) as ToolsAdapter.ViewHolder
            val drawable = (holder.imgToolIcon as ImageView).drawable
            assertEquals(icon, shadowOf(drawable).createdFromResId)
        }
    }

    @Test
    fun click_reportsToolTypeInOrder() {
        for (i in 0 until adapter.itemCount) {
            recyclerView.findViewHolderForAdapterPosition(i)!!.itemView.performClick()
        }

        assertEquals(
            listOf(ToolType.SHAPE, ToolType.TEXT, ToolType.ERASER, ToolType.UNDO, ToolType.REDO),
            selected
        )
    }
}
