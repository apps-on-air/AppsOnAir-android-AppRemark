package com.appsonair.appremark.base

import android.app.ProgressDialog
import com.appsonair.appremark.testutil.TestHostActivity
import com.google.android.material.snackbar.Snackbar
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.shadows.ShadowDialog
import org.robolectric.Shadows.shadowOf

@Suppress("DEPRECATION")
@RunWith(RobolectricTestRunner::class)
class BaseActivityTest {

    private lateinit var activity: TestHostActivity

    @Before
    fun setUp() {
        activity = Robolectric.buildActivity(TestHostActivity::class.java).setup().get()
    }

    @Test
    fun showLoading_showsNonCancelableProgressDialog() {
        activity.callShowLoading("Please wait")

        val dialog = ShadowDialog.getLatestDialog() as ProgressDialog
        assertTrue(dialog.isShowing)
        assertFalse(shadowOf(dialog).isCancelable)
    }

    @Test
    fun hideLoading_dismissesDialog() {
        activity.callShowLoading("Please wait")
        val dialog = ShadowDialog.getLatestDialog()

        activity.callHideLoading()

        assertFalse(dialog.isShowing)
    }

    @Test
    fun hideLoading_withoutShow_isNoOp() {
        activity.callHideLoading()
    }

    @Test
    fun showSnackbar_attachesToContentView() {
        activity.callShowSnackbar("Saved")
        org.robolectric.shadows.ShadowLooper.idleMainLooper()

        val content = activity.findViewById<android.view.ViewGroup>(android.R.id.content)
        val snackbarText = findText(content, "Saved")
        assertTrue("Snackbar text should be in the content hierarchy", snackbarText)
    }

    private fun findText(view: android.view.View, text: String): Boolean {
        if (view is android.widget.TextView && view.text.toString() == text) return true
        if (view is android.view.ViewGroup) {
            for (i in 0 until view.childCount) if (findText(view.getChildAt(i), text)) return true
        }
        return false
    }
}
