package com.anonymous.he_thong_canh_bao_chay

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import com.anonymous.he_thong_canh_bao_chay.Helper.MQTTHelper
import com.anonymous.he_thong_canh_bao_chay.Models.Device
import com.anonymous.he_thong_canh_bao_chay.databinding.ActivityDeviceInformationBinding
import com.bumptech.glide.Glide
import com.google.firebase.database.FirebaseDatabase

class DeviceInformationActivity : AppCompatActivity() {
    lateinit var viewBindingDevice : ActivityDeviceInformationBinding
    lateinit var mDevice : Device
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
       // setContentView(R.layout.activity_device_information)
        val data = intent.extras
        data?.apply {
            mDevice = this.getSerializable("device") as Device
        }

        viewBindingDevice = ActivityDeviceInformationBinding.inflate(LayoutInflater.from(this))
        setContentView(viewBindingDevice.root)
        getInformation()
        viewBindingDevice?.apply {
            btnEdit.setOnClickListener {
                viewBindingDevice.edtTopicDevice.isEnabled = true
                viewBindingDevice.edtLinkImage.isEnabled = true
                viewBindingDevice.edtNameDevice.isEnabled = true
                viewBindingDevice.btnUpdate.isEnabled = true
                viewBindingDevice.btnUpdate.visibility = View.VISIBLE
                viewBindingDevice.btnEdit.visibility = View.INVISIBLE
            }
            btnUpdate.setOnClickListener {
                viewBindingDevice.btnEdit.visibility = View.VISIBLE
                var strTopic = viewBindingDevice.edtTopicDevice.text.toString().trim()
                var strLinkImage = viewBindingDevice.edtLinkImage.text.toString().trim()
                val strNameDevice = viewBindingDevice.edtNameDevice.text.toString().trim()
                if (mDevice.topicMQTT != strTopic) {

                    var subURLMQTT = "Devices/"+ MQTTHelper.uid+"/"+mDevice.topicMQTT
                    var subURLMQTT2 ="Devices/"+MQTTHelper.uid+"/"+strTopic
                    Log.d("subMQTT: ", "subURLMQTT2 = ${subURLMQTT2} - subURLMQTT1 = ${subURLMQTT}")
                    FirebaseDatabase.getInstance().getReference(subURLMQTT).removeValue()
                        .addOnSuccessListener {
                            mDevice.topicMQTT = strTopic
                            mDevice.urlImage  = strLinkImage
                            mDevice.nameDevice = strNameDevice
                            Log.d( "OnSuccess: ", "Remove oke")
                            FirebaseDatabase.getInstance().getReference(subURLMQTT2).setValue(mDevice)
                        }
                    MQTTHelper.unsubscribe(mDevice.topicMQTT.toString(),MQTTHelper.client)
                    MQTTHelper.subscribe(strTopic,1,MQTTHelper.client)
                }else {
                    mDevice.urlImage  = strLinkImage
                    mDevice.nameDevice = strNameDevice
                    FirebaseDatabase.getInstance().getReference("Devices/"+MQTTHelper.uid+"/"+strTopic).setValue(mDevice)
                }
                viewBindingDevice.btnUpdate.visibility = View.INVISIBLE
            }
        }
    }

    fun getInformation() {
        mDevice?.apply {
            Glide.with(this@DeviceInformationActivity).load(mDevice.urlImage).
            error(R.drawable.img_size).into(viewBindingDevice.imgDevice)
            viewBindingDevice.edtIdDevice.setText(this.idDevice)
            viewBindingDevice.edtNameDevice.setText(this.nameDevice)
            viewBindingDevice.edtTopicDevice.setText(this.topicMQTT)
            viewBindingDevice.edtLinkImage.setText(this.urlImage)
        }
    }
}