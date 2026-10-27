package com.appsonair.appremark.adapters

import android.net.Uri
import android.widget.FrameLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.appsonair.appremark.R
import com.appsonair.appremark.interfaces.OnItemClickListener
import com.appsonair.appremark.models.ImageData
import com.appsonair.appremark.testutil.TestHostActivity
import com.appsonair.appremark.testutil.ViewTestUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ImageAdapterTest {

    private lateinit var activity: TestHostActivity
    private val clicks = mutableListOf<Int>()
    private val listener = OnItemClickListener { clicks += it }

    @Before
    fun setUp() {
        activity = Robolectric.buildActivity(TestHostActivity::class.java).setup().get()
    }

    private fun images(n: Int) = MutableList(n) {
        ImageData.Builder().setImageUri(Uri.parse("file:///cache/img$it.png")).setFileName("img$it.png").build()
    }

    private fun attach(adapter: ImageAdapter): RecyclerView {
        val rv = RecyclerView(activity).apply {
            layoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
            this.adapter = adapter
        }
        activity.setContentView(rv)
        ViewTestUtils.layout(rv, width = 5000)
        return rv
    }

    @Test
    fun itemCount_tracksBackingList() {
        val list = images(2)
        val adapter = ImageAdapter(list, listener)
        assertEquals(2, adapter.itemCount)

        list.removeAt(0)
        assertEquals(1, adapter.itemCount)
    }

    @Test
    fun bind_clipsPreviewToOutline() {
        val rv = attach(ImageAdapter(images(1), listener))

        val holder = rv.findViewHolderForAdapterPosition(0) as ImageAdapter.ViewHolder
        assertTrue(holder.imgBug.clipToOutline)
    }

    @Test
    fun removeClick_reportsAdapterPosition() {
        val rv = attach(ImageAdapter(images(2), listener))

        rv.findViewHolderForAdapterPosition(1)!!.itemView.findViewById<android.view.View>(R.id.img_remove).performClick()
        rv.findViewHolderForAdapterPosition(0)!!.itemView.findViewById<android.view.View>(R.id.img_remove).performClick()

        assertEquals(listOf(1, 0), clicks)
    }

    @Test
    fun removeClick_onDetachedHolder_isIgnored() {
        val adapter = ImageAdapter(images(1), listener)
        val holder = adapter.onCreateViewHolder(FrameLayout(activity), 0)

        holder.imgRemove.performClick()

        assertTrue(clicks.isEmpty())
    }

    @Test
    fun removeClick_withoutListener_doesNotCrash() {
        val rv = attach(ImageAdapter(images(1), null))

        val holder = rv.findViewHolderForAdapterPosition(0)
        assertNotNull(holder)
        holder!!.itemView.findViewById<android.view.View>(R.id.img_remove).performClick()
    }
}
