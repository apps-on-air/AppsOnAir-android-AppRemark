package com.appsonair.appremark.testutil

import android.os.Bundle
import com.appsonair.appremark.R
import com.appsonair.appremark.base.BaseActivity

/** Bare AppCompat host for fragments/adapters; also exposes BaseActivity's protected helpers. */
class TestHostActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppRemarkTheme)
        super.onCreate(savedInstanceState)
    }

    fun callShowLoading(message: String) = showLoading(message)

    fun callHideLoading() = hideLoading()

    fun callShowSnackbar(message: String) = showSnackbar(message)
}
