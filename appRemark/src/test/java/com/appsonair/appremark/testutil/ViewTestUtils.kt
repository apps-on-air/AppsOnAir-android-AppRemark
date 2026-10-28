package com.appsonair.appremark.testutil

import android.view.View
import androidx.recyclerview.widget.RecyclerView

object ViewTestUtils {

    /** Forces a RecyclerView to lay out its children when it lives in a window Robolectric doesn't measure. */
    fun layout(recyclerView: RecyclerView, width: Int = 1080, height: Int = 1920) {
        recyclerView.measure(
            View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY)
        )
        recyclerView.layout(0, 0, width, height)
    }
}
