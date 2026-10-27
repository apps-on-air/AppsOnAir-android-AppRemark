package com.appsonair.appremark.activities

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.appsonair.appremark.R
import com.appsonair.appremark.dialogs.BottomSheetEmoji
import com.appsonair.appremark.dialogs.BottomSheetShape
import com.appsonair.appremark.dialogs.TextEditorDialog
import com.appsonair.appremark.enums.ToolType
import com.appsonair.appremark.services.ShakeDetectorService
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import ja.burhanrashid52.photoeditor.PhotoEditor
import ja.burhanrashid52.photoeditor.PhotoEditorView
import ja.burhanrashid52.photoeditor.TextStyleBuilder
import ja.burhanrashid52.photoeditor.ViewType
import ja.burhanrashid52.photoeditor.shape.ShapeBuilder
import ja.burhanrashid52.photoeditor.shape.ShapeType
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.android.controller.ActivityController
import org.robolectric.annotation.GraphicsMode
import org.robolectric.shadows.ShadowDialog
import org.robolectric.shadows.ShadowLooper
import java.io.File
import java.io.FileOutputStream

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class EditImageActivityTest {

    private val controllers = mutableListOf<ActivityController<EditImageActivity>>()
    private lateinit var editor: PhotoEditor

    @Before
    fun setUp() {
        resetOpenFlag()
        ShakeDetectorService.isProcessing = false
        editor = mockk(relaxed = true)
        every { editor.isCacheEmpty } returns true
    }

    @After
    fun tearDown() {
        controllers.forEach { runCatching { it.pause().stop().destroy() } }
        resetOpenFlag()
        ShakeDetectorService.isProcessing = false
    }

    private fun resetOpenFlag() {
        EditImageActivity::class.java.getDeclaredField("isOpen").apply {
            isAccessible = true
            setBoolean(null, false)
        }
    }

    private fun screenshot(): Uri {
        val context = androidx.test.core.app.ApplicationProvider.getApplicationContext<android.content.Context>()
        val file = File(context.cacheDir, "shot.png")
        FileOutputStream(file).use {
            Bitmap.createBitmap(120, 200, Bitmap.Config.ARGB_8888).compress(Bitmap.CompressFormat.PNG, 100, it)
        }
        return Uri.fromFile(file)
    }

    private fun launch(intent: Intent? = Intent().putExtra("IMAGE_PATH", screenshot()), mockEditor: Boolean = true): EditImageActivity {
        val controller = if (intent != null) {
            Robolectric.buildActivity(EditImageActivity::class.java, intent)
        } else {
            Robolectric.buildActivity(EditImageActivity::class.java)
        }
        controllers += controller
        val activity = controller.setup().get()
        if (mockEditor && !activity.isFinishing) activity.mPhotoEditor = editor
        return activity
    }

    private fun EditImageActivity.editorView() = findViewById<PhotoEditorView>(R.id.photo_editor_view)

    // lifecycle

    @Test
    fun create_loadsScreenshotIntoEditor() {
        val activity = launch()

        assertNotNull(activity.editorView().source.drawable)
        assertTrue(EditImageActivity.isOpen())
    }

    @Test
    fun create_withoutImageExtra_showsEmptyEditor() {
        val activity = launch(Intent())

        assertNull(activity.editorView().source.drawable)
        assertFalse(activity.isFinishing)
    }

    @Test
    fun create_whileAnotherEditorIsOpen_finishesImmediately() {
        launch()

        val second = launch()

        assertTrue(second.isFinishing)
        assertNull(second.findViewById(R.id.photo_editor_view))
    }

    @Test
    fun destroy_clearsOpenAndProcessingFlags() {
        launch()
        ShakeDetectorService.isProcessing = true

        controllers.single().pause().stop().destroy()

        assertFalse(EditImageActivity.isOpen())
        assertFalse(ShakeDetectorService.isProcessing)
    }

    @Test
    fun create_realPhotoEditorIsBuilt() {
        val activity = launch(mockEditor = false)

        assertTrue(activity.mPhotoEditor.isCacheEmpty)
    }

    @Test
    fun toolsList_showsEnabledTools() {
        val activity = launch()

        assertEquals(5, activity.findViewById<RecyclerView>(R.id.rv_tools).adapter!!.itemCount)
    }

    // tools

    @Test
    fun undoAndRedo_delegateToEditor() {
        val activity = launch()

        activity.onToolSelected(ToolType.UNDO)
        activity.onToolSelected(ToolType.REDO)

        verify { editor.undo() }
        verify { editor.redo() }
    }

    @Test
    fun eraser_enablesLargeEraser() {
        val activity = launch()

        activity.onToolSelected(ToolType.ERASER)

        verify { editor.setBrushEraserSize(100f) }
        verify { editor.brushEraser() }
    }

    @Test
    fun shape_enablesDrawingAndOpensShapeSheet() {
        val activity = launch()

        activity.onToolSelected(ToolType.SHAPE)
        ShadowLooper.idleMainLooper()

        verify { editor.setBrushDrawingMode(true) }
        verify { editor.setShape(any()) }
        assertEquals(1, activity.supportFragmentManager.fragments.count { it is BottomSheetShape })
    }

    @Test
    fun shape_selectedTwice_doesNotStackSheets() {
        val activity = launch()

        activity.onToolSelected(ToolType.SHAPE)
        ShadowLooper.idleMainLooper()
        activity.onToolSelected(ToolType.SHAPE)
        ShadowLooper.idleMainLooper()

        assertEquals(1, activity.supportFragmentManager.fragments.count { it is BottomSheetShape })
    }

    @Test
    fun shapeProperties_updateCurrentShape() {
        val activity = launch()
        activity.onToolSelected(ToolType.SHAPE)
        val shapes = mutableListOf<ShapeBuilder>()
        every { editor.setShape(capture(shapes)) } returns Unit

        activity.onColorChanged(Color.MAGENTA)
        activity.onOpacityChanged(60)
        activity.onShapeSizeChanged(33)
        activity.onShapePicked(ShapeType.Rectangle)

        val shape = shapes.last()
        assertEquals(4, shapes.size)
        assertEquals(Color.MAGENTA, shape.shapeColor)
        assertEquals(60, shape.shapeOpacity)
        assertEquals(33f, shape.shapeSize)
        assertEquals(ShapeType.Rectangle, shape.shapeType)
    }

    @Test
    fun text_opensEditorAndAddsTypedText() {
        val activity = launch()

        activity.onToolSelected(ToolType.TEXT)
        ShadowLooper.idleMainLooper()
        val dialog = activity.supportFragmentManager.fragments.filterIsInstance<TextEditorDialog>().single()
        dialog.requireView().findViewById<EditText>(R.id.et_add_text).setText("Bug here")
        dialog.requireView().findViewById<TextView>(R.id.tv_done).performClick()

        verify { editor.addText("Bug here", any<TextStyleBuilder>()) }
    }

    @Test
    fun emoji_opensEmojiSheet() {
        val activity = launch()

        activity.onToolSelected(ToolType.EMOJI)
        ShadowLooper.idleMainLooper()

        assertEquals(1, activity.supportFragmentManager.fragments.count { it is BottomSheetEmoji })
    }

    @Test
    fun emojiClick_addsEmoji() {
        val activity = launch()

        activity.onEmojiClick("😀")

        verify { editor.addEmoji("😀") }
    }

    @Test
    fun gallery_launchesImageChooser() {
        val activity = launch()

        activity.onToolSelected(ToolType.GALLERY)

        val started = shadowOf(activity).nextStartedActivityForResult
        assertEquals(53, started.requestCode)
        assertEquals(Intent.ACTION_CHOOSER, started.intent.action)
        @Suppress("DEPRECATION")
        val target = started.intent.getParcelableExtra<Intent>(Intent.EXTRA_INTENT)!!
        assertEquals(Intent.ACTION_GET_CONTENT, target.action)
        assertEquals("image/*", target.type)
    }

    @Test
    fun galleryResult_replacesSourceImage() {
        val activity = launch()
        activity.onToolSelected(ToolType.GALLERY)
        val started = shadowOf(activity).nextStartedActivityForResult

        shadowOf(activity).receiveResult(started.intent, Activity.RESULT_OK, Intent().setData(screenshot()))

        verify { editor.clearAllViews() }
        assertNotNull(activity.editorView().source.drawable)
    }

    @Test
    fun galleryResult_cancelled_keepsCurrentImage() {
        val activity = launch()
        activity.onToolSelected(ToolType.GALLERY)
        val started = shadowOf(activity).nextStartedActivityForResult

        shadowOf(activity).receiveResult(started.intent, Activity.RESULT_CANCELED, null)

        verify(exactly = 0) { editor.clearAllViews() }
    }

    // text editing of existing views

    @Test
    fun editTextOnCanvas_opensPrefilledEditorAndUpdatesView() {
        val activity = launch()
        val textView = TextView(activity)

        activity.onEditTextChangeListener(textView, "Old", Color.RED)
        ShadowLooper.idleMainLooper()
        val dialog = activity.supportFragmentManager.fragments.filterIsInstance<TextEditorDialog>().single()
        val input = dialog.requireView().findViewById<EditText>(R.id.et_add_text)
        assertEquals("Old", input.text.toString())
        assertEquals(Color.RED, input.currentTextColor)

        input.setText("New")
        dialog.requireView().findViewById<TextView>(R.id.tv_done).performClick()

        verify { editor.editText(textView, "New", any<TextStyleBuilder>()) }
    }

    @Test
    fun editTextOnCanvas_withNullText_opensNothing() {
        val activity = launch()

        activity.onEditTextChangeListener(TextView(activity), null, Color.RED)
        ShadowLooper.idleMainLooper()

        assertTrue(activity.supportFragmentManager.fragments.none { it is TextEditorDialog })
    }

    @Test
    fun editorCallbacks_areLogOnly() {
        val activity = launch()

        activity.onAddViewListener(ViewType.TEXT, 1)
        activity.onRemoveViewListener(ViewType.EMOJI, 0)
        activity.onStartViewChangeListener(ViewType.BRUSH_DRAWING)
        activity.onStopViewChangeListener(null)
        activity.onTouchSourceImage(null)

        assertFalse(activity.isFinishing)
    }

    // leaving and saving

    @Test
    fun close_withoutEdits_finishes() {
        val activity = launch()

        activity.findViewById<android.view.View>(R.id.img_close).performClick()

        assertTrue(activity.isFinishing)
    }

    @Test
    fun back_withoutEdits_finishes() {
        val activity = launch()

        activity.onBackPressedDispatcher.onBackPressed()

        assertTrue(activity.isFinishing)
    }

    private fun closeWithEdits(activity: EditImageActivity): AlertDialog {
        every { editor.isCacheEmpty } returns false
        activity.findViewById<android.view.View>(R.id.img_close).performClick()
        return ShadowDialog.getLatestDialog() as AlertDialog
    }

    @Test
    fun close_withEdits_asksBeforeLeaving() {
        val activity = launch()

        val dialog = closeWithEdits(activity)

        assertTrue(dialog.isShowing)
        assertFalse(activity.isFinishing)
        assertEquals(
            activity.getString(R.string.msg_save_image),
            dialog.findViewById<TextView>(android.R.id.message)!!.text.toString()
        )
    }

    @Test
    fun back_withEdits_asksBeforeLeaving() {
        val activity = launch()
        every { editor.isCacheEmpty } returns false

        activity.onBackPressedDispatcher.onBackPressed()

        assertTrue((ShadowDialog.getLatestDialog() as AlertDialog).isShowing)
        assertFalse(activity.isFinishing)
    }

    @Test
    fun saveDialog_discard_finishesWithoutSaving() {
        val activity = launch()

        closeWithEdits(activity).getButton(DialogInterface.BUTTON_NEUTRAL).performClick()
        ShadowLooper.idleMainLooper() // AlertDialog dispatches button clicks through a Handler

        assertTrue(activity.isFinishing)
        assertNull(shadowOf(activity).nextStartedActivity)
    }

    @Test
    fun saveDialog_cancel_staysOnEditor() {
        val activity = launch()
        val dialog = closeWithEdits(activity)

        dialog.getButton(DialogInterface.BUTTON_NEGATIVE).performClick()
        ShadowLooper.idleMainLooper()

        assertFalse(dialog.isShowing)
        assertFalse(activity.isFinishing)
    }

    @Test
    fun saveDialog_save_opensRemarkScreenWithEditedImage() {
        val activity = launch()

        closeWithEdits(activity).getButton(DialogInterface.BUTTON_POSITIVE).performClick()
        ShadowLooper.idleMainLooper()

        assertRemarkScreenOpenedWithSavedImage(activity)
    }

    @Test
    fun done_savesPngAndOpensRemarkScreen() {
        val activity = launch()

        activity.findViewById<android.view.View>(R.id.tv_done).performClick()

        verify { editor.clearHelperBox() }
        assertRemarkScreenOpenedWithSavedImage(activity)
    }

    private fun assertRemarkScreenOpenedWithSavedImage(activity: EditImageActivity) {
        val intent = shadowOf(activity).nextStartedActivity
        assertEquals(RemarkActivity::class.java.name, intent.component?.className)
        assertTrue(intent.flags and Intent.FLAG_ACTIVITY_NEW_TASK != 0)
        assertTrue(intent.flags and Intent.FLAG_ACTIVITY_CLEAR_TOP != 0)
        @Suppress("DEPRECATION")
        val uri = intent.getParcelableExtra<Uri>("IMAGE_PATH")!!
        val saved = File(uri.path!!)
        assertTrue(saved.exists())
        assertTrue(saved.name.endsWith(".png"))
        assertEquals(activity.cacheDir.absolutePath, saved.parentFile!!.absolutePath)
        assertEquals(uri, activity.mSaveImageUri)
        assertTrue(activity.isFinishing)
    }
}
