package com.appsonair.appremark.dialogs

import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.appsonair.appremark.R
import com.appsonair.appremark.testutil.TestHostActivity
import com.appsonair.appremark.testutil.ViewTestUtils
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
class BottomSheetEmojiTest {

    private lateinit var activity: TestHostActivity
    private lateinit var sheet: BottomSheetEmoji
    private val clicked = mutableListOf<String>()

    @Before
    fun setUp() {
        activity = Robolectric.buildActivity(TestHostActivity::class.java).setup().get()
        sheet = BottomSheetEmoji()
    }

    private fun show(listener: BottomSheetEmoji.EmojiListener? = object : BottomSheetEmoji.EmojiListener {
        override fun onEmojiClick(emojiUnicode: String) { clicked += emojiUnicode }
    }): RecyclerView {
        sheet.setEmojiListener(listener)
        sheet.show(activity.supportFragmentManager, "emoji")
        ShadowLooper.idleMainLooper()
        val rv = sheet.requireDialog().findViewById<RecyclerView>(R.id.rv_emoji)
        ViewTestUtils.layout(rv)
        return rv
    }

    @Test
    fun adapter_containsEveryConfiguredEmoji() {
        val rv = show()

        val configured = activity.resources.getStringArray(R.array.photo_editor_emoji)
        assertEquals(configured.size, rv.adapter!!.itemCount)
    }

    @Test
    fun emojis_areDecodedFromUnicodeCodePoints() {
        val rv = show()

        // first entries are u+1f604, u+1f603, u+1f600
        val texts = (0..2).map {
            (rv.findViewHolderForAdapterPosition(it)!!.itemView.findViewById<TextView>(R.id.tv_emoji)).text.toString()
        }
        assertEquals(listOf("😄", "😃", "😀"), texts)
    }

    @Test
    fun emojis_fromBasicPlane_areDecoded() {
        // u+263a (☺) sits at index 4 and is a single UTF-16 char
        initializeAndAssert(4, "☺")
    }

    private fun initializeAndAssert(position: Int, expected: String) {
        val rv = show()
        rv.scrollToPosition(position)
        ViewTestUtils.layout(rv)
        val text = rv.findViewHolderForAdapterPosition(position)!!.itemView
            .findViewById<TextView>(R.id.tv_emoji).text.toString()
        assertEquals(expected, text)
    }

    @Test
    fun noEmojiDecodesToEmpty() {
        val raw = activity.resources.getStringArray(R.array.photo_editor_emoji)
        raw.forEach {
            val codePoint = it.substring(2).toInt(16)
            assertTrue("$it is a valid code point", Character.isValidCodePoint(codePoint))
        }
    }

    @Test
    fun click_reportsEmojiAndDismisses() {
        val rv = show()

        rv.findViewHolderForAdapterPosition(1)!!.itemView.performClick()
        ShadowLooper.idleMainLooper()

        assertEquals(listOf("😃"), clicked)
        assertFalse(sheet.isAdded)
    }

    @Test
    fun click_withoutListener_onlyDismisses() {
        val rv = show(listener = null)

        rv.findViewHolderForAdapterPosition(0)!!.itemView.performClick()
        ShadowLooper.idleMainLooper()

        assertTrue(clicked.isEmpty())
        assertFalse(sheet.isAdded)
    }

    @Test
    fun initializeEmojis_canBeCalledDirectly() {
        BottomSheetEmoji.initializeEmojis(activity)
        val rv = show()

        assertEquals(activity.resources.getStringArray(R.array.photo_editor_emoji).size, rv.adapter!!.itemCount)
    }
}
