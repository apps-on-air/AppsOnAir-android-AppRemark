package com.appsonair.appremark.dialogs

import android.graphics.Color
import android.widget.EditText
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.appsonair.appremark.R
import com.appsonair.appremark.adapters.ColorPickerAdapter
import com.appsonair.appremark.testutil.TestHostActivity
import com.appsonair.appremark.testutil.ViewTestUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.shadows.ShadowLooper

@RunWith(RobolectricTestRunner::class)
class TextEditorDialogTest {

    private lateinit var activity: TestHostActivity
    private val results = mutableListOf<Pair<String, Int>>()
    private val listener = object : TextEditorDialog.TextEditorListener {
        override fun onDone(inputText: String, colorCode: Int) {
            results += inputText to colorCode
        }
    }

    @Before
    fun setUp() {
        activity = Robolectric.buildActivity(TestHostActivity::class.java).setup().get()
    }

    private fun show(text: String? = null, color: Int? = null): TextEditorDialog {
        val dialog = when {
            text != null && color != null -> TextEditorDialog.show(activity, text, color)
            text != null -> TextEditorDialog.show(activity, text)
            else -> TextEditorDialog.show(activity)
        }
        ShadowLooper.idleMainLooper()
        return dialog
    }

    private fun TextEditorDialog.editText() = requireView().findViewById<EditText>(R.id.et_add_text)
    private fun TextEditorDialog.done() = requireView().findViewById<TextView>(R.id.tv_done).performClick()

    @Test
    fun show_addsDialogWithArguments() {
        val dialog = show("Hello", Color.RED)

        assertSame(dialog, activity.supportFragmentManager.findFragmentByTag("TextEditorDialog"))
        assertEquals("Hello", dialog.requireArguments().getString(TextEditorDialog.EXTRA_INPUT_TEXT))
        assertEquals(Color.RED, dialog.requireArguments().getInt(TextEditorDialog.EXTRA_COLOR_CODE))
    }

    @Test
    fun show_defaults_areEmptyTextInWhite() {
        val dialog = show()

        assertEquals("", dialog.editText().text.toString())
        assertEquals(ContextCompat.getColor(activity, R.color.white), dialog.editText().currentTextColor)
    }

    @Test
    fun show_withTextOnly_defaultsToWhite() {
        val dialog = show("Only text")

        assertEquals("Only text", dialog.editText().text.toString())
        assertEquals(ContextCompat.getColor(activity, R.color.white), dialog.editText().currentTextColor)
    }

    @Test
    fun show_prefillsTextAndColor() {
        val dialog = show("Existing", Color.BLUE)

        assertEquals("Existing", dialog.editText().text.toString())
        assertEquals(Color.BLUE, dialog.editText().currentTextColor)
    }

    @Test
    fun start_makesDialogFullScreen() {
        val dialog = show()

        val attrs = dialog.dialog!!.window!!.attributes
        assertEquals(android.view.ViewGroup.LayoutParams.MATCH_PARENT, attrs.width)
        assertEquals(android.view.ViewGroup.LayoutParams.MATCH_PARENT, attrs.height)
    }

    @Test
    fun done_withText_reportsTextAndColorAndDismisses() {
        val dialog = show("Start", Color.GREEN)
        dialog.setOnTextEditorListener(listener)
        dialog.editText().setText("Final text")

        dialog.done()
        ShadowLooper.idleMainLooper()

        assertEquals(listOf("Final text" to Color.GREEN), results)
        assertNull(activity.supportFragmentManager.findFragmentByTag("TextEditorDialog"))
    }

    @Test
    fun done_withEmptyText_dismissesWithoutCallback() {
        val dialog = show()
        dialog.setOnTextEditorListener(listener)

        dialog.done()
        ShadowLooper.idleMainLooper()

        assertTrue(results.isEmpty())
        assertFalse(dialog.isAdded)
    }

    @Test
    fun done_withoutListener_dismissesQuietly() {
        val dialog = show("text")

        dialog.done()
        ShadowLooper.idleMainLooper()

        assertFalse(dialog.isAdded)
    }

    @Test
    fun pickingColor_updatesTextColorAndResult() {
        val dialog = show("Paint", Color.BLACK)
        dialog.setOnTextEditorListener(listener)
        val rv = dialog.requireView().findViewById<RecyclerView>(R.id.rv_add_text_color_picker)
        ViewTestUtils.layout(rv, width = 5000, height = 200)
        val firstColor = ColorPickerAdapter.getDefaultColors(activity)[0]

        rv.findViewHolderForAdapterPosition(0)!!.itemView.performClick()
        dialog.done()

        assertEquals(firstColor, dialog.editText().currentTextColor)
        assertEquals(listOf("Paint" to firstColor), results)
    }
}
