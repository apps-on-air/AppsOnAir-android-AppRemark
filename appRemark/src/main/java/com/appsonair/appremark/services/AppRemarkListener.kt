package com.appsonair.appremark.services

import org.json.JSONObject

internal class AppRemarkListener {
    companion object {
        fun setRemarkResponse(result:JSONObject){
            AppRemarkService.setRemarkResponse(result)
        }
    }
}