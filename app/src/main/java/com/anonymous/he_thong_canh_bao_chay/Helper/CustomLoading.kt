package com.anonymous.he_thong_canh_bao_chay.Helper

import android.app.Activity
import android.app.AlertDialog
import android.text.Layout
import android.view.LayoutInflater
import android.view.View
import android.widget.ProgressBar
import com.anonymous.he_thong_canh_bao_chay.R
import com.github.ybq.android.spinkit.sprite.CircleSprite
import com.github.ybq.android.spinkit.sprite.Sprite

class CustomLoading {
    lateinit var activity: Activity
    var alertDialog: AlertDialog? =null
    constructor( act : Activity) {
        this.activity = act
    }
    fun showDialog() {
        var builder  = AlertDialog.Builder(this.activity)
        val layoutInflater = this.activity.layoutInflater
        builder.setView(layoutInflater.inflate(R.layout.loading_custom, null))
        builder.setCancelable(true)
        alertDialog = builder.create()
        alertDialog!!.show()
    }
    fun dimiss() {
        alertDialog?.apply {
            dismiss()
        }
    }
    fun isShowing() : Boolean {
        if (alertDialog == null ) return false
        return alertDialog!!.isShowing
    }
}