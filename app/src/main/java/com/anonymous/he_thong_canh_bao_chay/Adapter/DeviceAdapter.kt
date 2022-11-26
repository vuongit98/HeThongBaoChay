package com.anonymous.he_thong_canh_bao_chay.Adapter

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.Log
import android.view.*
import androidx.recyclerview.widget.RecyclerView
import com.anonymous.he_thong_canh_bao_chay.ConnectMQTTActivity
import com.anonymous.he_thong_canh_bao_chay.Helper.MQTTHelper
import com.anonymous.he_thong_canh_bao_chay.Interface.ItemClickListener
import com.anonymous.he_thong_canh_bao_chay.Models.Device
import com.anonymous.he_thong_canh_bao_chay.R
import com.anonymous.he_thong_canh_bao_chay.databinding.CustomPopUpBinding
import com.anonymous.he_thong_canh_bao_chay.databinding.ItemDeviceBinding
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class DeviceAdapter (var mList: MutableList<Device>, var mListener: ItemClickListener, var mContext : Context) : RecyclerView.Adapter<DeviceAdapter.DeviceViewHolder>() {

    inner class DeviceViewHolder(var bindingRoom: ItemDeviceBinding) :
        RecyclerView.ViewHolder(bindingRoom.root) {

        fun setData(device: Device) {
            // bindingRoom.txtTitleDevice = room.nameRoom
            Log.d("DATA: ", device.toString())
            Glide.with(mContext).load(device.urlImage).error(R.drawable.img_size)
                .into(bindingRoom.imgDeviceIcon)
            bindingRoom.txtTitleDevice.text = device.nameDevice
            bindingRoom.txtValueDevice.text = device.valueDevice

            bindingRoom.btnCloseDevice.setOnClickListener {
                openPopUp(mContext, device);
            }
        }
    }

    fun openPopUp(context: Context, device: Device) {
        var dialog = Dialog(context)
        var customDialog = CustomPopUpBinding.inflate(LayoutInflater.from(context))
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(customDialog.root)

        var window = dialog.window
        window?.apply {
            setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT
            )
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            var attWindow = this.attributes
            attWindow.gravity = Gravity.CENTER
            this.attributes = attWindow
            customDialog?.apply {
                btnYes.setOnClickListener {
                    // Xu ly xoá
                    var uid = FirebaseAuth.getInstance().currentUser!!.uid
                    Log.d("Remove : ", uid.toString() )
                    removeDevice(uid, device.topicMQTT.toString().trim())
                    MQTTHelper.unsubscribe(device.topicMQTT.toString(), MQTTHelper.client)
                    dialog.dismiss()
                    mList.remove(device)
                    notifyDataSetChanged()
                }
                btnNo.setOnClickListener {
                    Log.d("Remove current user: ", "No")
                    dialog.dismiss()
                }
            }
            dialog.show()
        }
    }
    fun removeDevice(uid : String,  topic : String) {
        var subURL = "Devices/"+uid+"/"+topic
        Log.d("removeDevice: ", subURL)
        var ref = FirebaseDatabase.getInstance().getReference(subURL)
        ref.removeValue()
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {
        var view = ItemDeviceBinding.inflate(LayoutInflater.from(parent.context))
        return DeviceViewHolder(view)
    }

    override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
        val room = mList.get(position)
        room?.apply {
            holder.setData(this)
        }
        holder.itemView.setOnClickListener {
            mListener.clickItem(position)
        }
    }

    override fun getItemCount(): Int {
        return mList.size
    }
}