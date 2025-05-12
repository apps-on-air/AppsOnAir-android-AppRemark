package com.appsonair.appremark.activities

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.annotation.VisibleForTesting
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.appsonair.appremark.R
import com.appsonair.appremark.adapters.ToolsAdapter
import com.appsonair.appremark.adapters.ToolsAdapter.OnItemSelected
import com.appsonair.appremark.base.BaseActivity
import com.appsonair.appremark.dialogs.BottomSheetEmoji
import com.appsonair.appremark.dialogs.BottomSheetShape
import com.appsonair.appremark.dialogs.TextEditorDialog
import com.appsonair.appremark.enums.ToolType
import com.appsonair.appremark.enums.ToolType.EMOJI
import com.appsonair.appremark.enums.ToolType.ERASER
import com.appsonair.appremark.enums.ToolType.GALLERY
import com.appsonair.appremark.enums.ToolType.REDO
import com.appsonair.appremark.enums.ToolType.SHAPE
import com.appsonair.appremark.enums.ToolType.TEXT
import com.appsonair.appremark.enums.ToolType.UNDO
import com.appsonair.appremark.services.ShakeDetectorService
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import ja.burhanrashid52.photoeditor.OnPhotoEditorListener
import ja.burhanrashid52.photoeditor.PhotoEditor
import ja.burhanrashid52.photoeditor.PhotoEditorView
import ja.burhanrashid52.photoeditor.SaveSettings
import ja.burhanrashid52.photoeditor.TextStyleBuilder
import ja.burhanrashid52.photoeditor.ViewType
import ja.burhanrashid52.photoeditor.shape.ShapeBuilder
import ja.burhanrashid52.photoeditor.shape.ShapeType
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class EditImageActivity : BaseActivity(), OnPhotoEditorListener, View.OnClickListener,
    BottomSheetShape.Properties, BottomSheetEmoji.EmojiListener, OnItemSelected {

    lateinit var mPhotoEditor: PhotoEditor
    private lateinit var mPhotoEditorView: PhotoEditorView
    private lateinit var mShapeBSFragment: BottomSheetShape
    private lateinit var mShapeBuilder: ShapeBuilder
    private lateinit var mEmojiBSFragment: BottomSheetEmoji
    private lateinit var mRvTools: RecyclerView
    private val mEditingToolsAdapter = ToolsAdapter(this)
    private lateinit var callback: OnBackPressedCallback

    @VisibleForTesting
    var mSaveImageUri: Uri? = null

    override fun onDestroy() {
        super.onDestroy()
        isOpen = false
        ShakeDetectorService.isProcessing = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (isOpen()) {
            finish()
            return
        }

        isOpen = true
        setContentView(R.layout.activity_edit_image)
        initViews()
        handleIntentImage(mPhotoEditorView.source)

        mEmojiBSFragment = BottomSheetEmoji()
        mShapeBSFragment = BottomSheetShape()
        mEmojiBSFragment.setEmojiListener(this)
        mShapeBSFragment.setPropertiesChangeListener(this)

        val llmTools = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        mRvTools.layoutManager = llmTools
        mRvTools.adapter = mEditingToolsAdapter

        mPhotoEditor = PhotoEditor.Builder(this, mPhotoEditorView)
            .setPinchTextScalable(true)
            .setClipSourceImage(true)
            .build()

        val layoutParams = mPhotoEditorView.layoutParams
        layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT
        layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
        mPhotoEditorView.layoutParams = layoutParams

        mPhotoEditor.setOnPhotoEditorListener(this)

        callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (!mPhotoEditor.isCacheEmpty) {
                    showSaveDialog()
                } else {
                    finish()
                }
            }
        }

        onBackPressedDispatcher.addCallback(this, callback)
    }

    private fun handleIntentImage(source: ImageView) {
        if (intent == null) {
            return
        } else if (intent != null && intent.hasExtra("IMAGE_PATH")) {
            val imagePath = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableExtra("IMAGE_PATH", Uri::class.java)
            } else {
                @Suppress("DEPRECATION")
                intent.getParcelableExtra("IMAGE_PATH")
            }
            source.setImageURI(imagePath)
        }
    }

    private fun initViews() {
        mPhotoEditorView = findViewById(R.id.photo_editor_view)
        mRvTools = findViewById(R.id.rv_tools)

        val imgClose: ImageView = findViewById(R.id.img_close)
        imgClose.setOnClickListener(this)

        val tvDone: TextView = findViewById(R.id.tv_done)
        tvDone.setOnClickListener(this)
    }

    override fun onAddViewListener(viewType: ViewType?, numberOfAddedViews: Int) {
        Log.d(
            TAG,
            "onAddViewListener() called with: viewType = [$viewType], numberOfAddedViews = [$numberOfAddedViews]"
        )
    }

    override fun onEditTextChangeListener(rootView: View?, text: String?, colorCode: Int) {
        rootView.let {
            text?.let { it1 -> TextEditorDialog.show(this, it1, colorCode) }
                ?.setOnTextEditorListener(object :
                    TextEditorDialog.TextEditorListener {
                    override fun onDone(inputText: String, colorCode: Int) {
                        val styleBuilder = TextStyleBuilder()
                        styleBuilder.withTextColor(colorCode)
                        if (it != null) {
                            mPhotoEditor.editText(it, inputText, styleBuilder)
                        }
                    }
                })
        }
    }

    override fun onRemoveViewListener(viewType: ViewType?, numberOfAddedViews: Int) {
        Log.d(
            TAG,
            "onRemoveViewListener() called with: viewType = [$viewType], numberOfAddedViews = [$numberOfAddedViews]"
        )
    }

    override fun onStartViewChangeListener(viewType: ViewType?) {
        Log.d(TAG, "onStartViewChangeListener() called with: viewType = [$viewType]")
    }

    override fun onStopViewChangeListener(viewType: ViewType?) {
        Log.d(TAG, "onStopViewChangeListener() called with: viewType = [$viewType]")
    }

    override fun onTouchSourceImage(event: MotionEvent?) {
        Log.d(TAG, "onTouchView() called with: event = [$event]")
    }

    @SuppressLint("MissingPermission")
    override fun onClick(view: View) {
        when (view.id) {
            R.id.img_close -> {
                callback.isEnabled = true
                callback.handleOnBackPressed()
            }

            R.id.tv_done -> saveImage()
        }
    }

    private fun saveImageToCache(context: Context, bitmap: Bitmap, fileName: String): File? {
        val cacheDir = context.cacheDir
        val file = File(cacheDir, fileName)
        return try {
            val fos = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
            fos.flush()
            fos.close()
            file
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    private fun getBitmapFromView(view: View): Bitmap {
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }

    private fun saveImage() {
        showLoading(getString(R.string.please_wait))
        SaveSettings.Builder().setClearViewsEnabled(true)
            .setTransparencyEnabled(true).build()
        mPhotoEditor.clearHelperBox()
        val fileName = System.currentTimeMillis().toString() + ".png"
        val bitmap = getBitmapFromView(mPhotoEditorView)
        val savedFile = saveImageToCache(this, bitmap, fileName)
        val uri = Uri.fromFile(savedFile)
        if (savedFile != null) {
            hideLoading()
            showSnackbar(getString(R.string.image_saved_successfully))
            mSaveImageUri = uri
            mPhotoEditorView.source.setImageURI(mSaveImageUri)
            goToRemarkActivity(uri)
        } else {
            hideLoading()
            showSnackbar(getString(R.string.failed_to_save_image))
            goToRemarkActivity(uri)
        }
    }

    private fun goToRemarkActivity(imageUri: Uri?) {
        val intent = Intent(this, RemarkActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.putExtra("IMAGE_PATH", imageUri)
        startActivity(intent)
        finish()
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        @Suppress("DEPRECATION")
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                PICK_REQUEST -> try {
                    mPhotoEditor.clearAllViews()
                    val uri = data?.data
                    val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        val source = ImageDecoder.createSource(contentResolver, uri!!)
                        ImageDecoder.decodeBitmap(source)
                    } else {
                        @Suppress("DEPRECATION")
                        MediaStore.Images.Media.getBitmap(contentResolver, uri)
                    }
                    mPhotoEditorView.source.setImageBitmap(bitmap)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onColorChanged(colorCode: Int) {
        mPhotoEditor.setShape(mShapeBuilder.withShapeColor(colorCode))
    }

    override fun onOpacityChanged(opacity: Int) {
        mPhotoEditor.setShape(mShapeBuilder.withShapeOpacity(opacity))
    }

    override fun onShapeSizeChanged(shapeSize: Int) {
        mPhotoEditor.setShape(mShapeBuilder.withShapeSize(shapeSize.toFloat()))
    }

    override fun onShapePicked(shapeType: ShapeType) {
        mPhotoEditor.setShape(mShapeBuilder.withShapeType(shapeType))
    }

    override fun onEmojiClick(emojiUnicode: String) {
        mPhotoEditor.addEmoji(emojiUnicode)
    }

    @SuppressLint("MissingPermission")
    private fun showSaveDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage(getString(R.string.msg_save_image))
        builder.setPositiveButton(getString(R.string.save)) { _: DialogInterface?, _: Int ->
            saveImage()
        }
        builder.setNegativeButton(getString(R.string.cancel)) { dialog: DialogInterface, _: Int -> dialog.dismiss() }
        builder.setNeutralButton(getString(R.string.discard)) { _: DialogInterface?, _: Int -> finish() }
        builder.create().show()
    }

    override fun onToolSelected(toolType: ToolType) {
        when (toolType) {
            SHAPE -> {
                mPhotoEditor.setBrushDrawingMode(true)
                mShapeBuilder = ShapeBuilder()
                mPhotoEditor.setShape(mShapeBuilder)
                showBottomSheetDialogFragment(mShapeBSFragment)
            }

            TEXT -> {
                val textEditorDialogFragment = TextEditorDialog.show(this)
                textEditorDialogFragment.setOnTextEditorListener(object :
                    TextEditorDialog.TextEditorListener {
                    override fun onDone(inputText: String, colorCode: Int) {
                        val styleBuilder = TextStyleBuilder()
                        styleBuilder.withTextColor(colorCode)
                        mPhotoEditor.addText(inputText, styleBuilder)
                    }
                })
            }

            ERASER -> {
                mPhotoEditor.setBrushEraserSize(100f)
                mPhotoEditor.brushEraser()
            }

            EMOJI -> showBottomSheetDialogFragment(mEmojiBSFragment)

            UNDO -> mPhotoEditor.undo()

            REDO -> mPhotoEditor.redo()

            GALLERY -> {
                val intent = Intent()
                intent.type = "image/*"
                intent.action = Intent.ACTION_GET_CONTENT
                @Suppress("DEPRECATION")
                startActivityForResult(
                    Intent.createChooser(
                        intent,
                        getString(R.string.select_picture)
                    ), PICK_REQUEST
                )
            }
        }
    }

    private fun showBottomSheetDialogFragment(fragment: BottomSheetDialogFragment?) {
        if (fragment == null || fragment.isAdded) {
            return
        }
        fragment.show(supportFragmentManager, fragment.tag)
    }

    companion object {
        private const val TAG = "EditImageActivity"
        private const val PICK_REQUEST = 53
        private var isOpen = false
        fun isOpen(): Boolean {
            return isOpen
        }
    }
}