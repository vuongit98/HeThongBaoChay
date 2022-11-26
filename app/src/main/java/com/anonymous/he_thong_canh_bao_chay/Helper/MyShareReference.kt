package com.anonymous.he_thong_canh_bao_chay.Helper

import android.content.Context
import android.content.SharedPreferences
import com.anonymous.he_thong_canh_bao_chay.Models.MQTT
import com.google.gson.Gson

object MyShareReference {
    lateinit var mContext : Context

    fun init(mContext: Context) {
        this.mContext = mContext
    }

    fun putData(key : String , mqtt : MQTT) {
        val shareReference = mContext.getSharedPreferences(key,Context.MODE_PRIVATE)
        val editor = shareReference.edit()
        val gson = Gson()
        val value = gson.toJson(mqtt)
        editor.putString(key, value)
        editor.apply()
    }
    fun getData(key : String) : MQTT {
        var mqtt = MQTT()
        val shareReference = mContext.getSharedPreferences(key, Context.MODE_PRIVATE)
        val result = shareReference.getString(key, null)
        if (result == null) return MQTT()

        val gson = Gson()
        mqtt = gson.fromJson(result, MQTT::class.java)
        if (mqtt == null) mqtt = MQTT()
        return mqtt
    }

}
