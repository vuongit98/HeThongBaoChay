package com.anonymous.he_thong_canh_bao_chay.Fragment

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.anonymous.he_thong_canh_bao_chay.Adapter.DeviceAdapter
import com.anonymous.he_thong_canh_bao_chay.ConnectMQTTActivity
import com.anonymous.he_thong_canh_bao_chay.DeviceInformationActivity
import com.anonymous.he_thong_canh_bao_chay.Helper.MQTTHelper
import com.anonymous.he_thong_canh_bao_chay.Interface.ItemClickListener
import com.anonymous.he_thong_canh_bao_chay.Models.Device
import com.anonymous.he_thong_canh_bao_chay.R
import com.anonymous.he_thong_canh_bao_chay.databinding.ActivityMainHomeBinding
import com.anonymous.he_thong_canh_bao_chay.databinding.CustomAddDeviceBinding
import com.anonymous.he_thong_canh_bao_chay.databinding.FragmentHomeBinding
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [HomeFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class HomeFragment : Fragment(), ItemClickListener {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }
    lateinit var viewBinding: ActivityMainHomeBinding
    lateinit var deviceAdapter: DeviceAdapter
    lateinit var mListDevice : MutableList<Device>
    lateinit var  mUriImage : Uri
    var mAuth = FirebaseAuth.getInstance().currentUser
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val fragmentHome = FragmentHomeBinding.inflate(inflater!!,container,false)
        fragmentHome?.apply {
            mListDevice = mutableListOf()
            getDeviceFromFirebase()
            deviceAdapter = context?.let { DeviceAdapter(mListDevice,this@HomeFragment, it) }!!
            recyclerviewDevice?.apply {
               adapter = deviceAdapter
                layoutManager = GridLayoutManager(activity, 2 )
                val spacing = 10
                setPadding(spacing,spacing,spacing,spacing)
                addItemDecoration(object  : RecyclerView.ItemDecoration() {
                    override fun getItemOffsets(
                        outRect: Rect,
                        view: View,
                        parent: RecyclerView,
                        state: RecyclerView.State
                    ) {
                        outRect.set(spacing,spacing,spacing,spacing)
                    }
                })
                hasFixedSize()
            }
            this.btnAddDevice.setOnClickListener {
                OpenDialog(Gravity.CENTER);
            }
        }
        return fragmentHome.root
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment HomeFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            HomeFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
    fun OpenDialog(gravity: Int) {
        var dialogMainBinding = CustomAddDeviceBinding.inflate(LayoutInflater.from(context))
        var dialog = activity?.let { Dialog(it) };
        dialog?.apply {
            this.requestWindowFeature(Window.FEATURE_NO_TITLE)
            this.setContentView(dialogMainBinding.root)
            var window = this.window

            window?.apply {

                setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
                setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            }
            var attribute = window!!.attributes
            attribute.gravity = gravity
            window.attributes = attribute
            dialogMainBinding?.apply {
                val time = System.currentTimeMillis().toString()
                this.imgRoom.setBackgroundResource(R.drawable.img_size)
                this.edtIdDevice.setText(System.currentTimeMillis().toString())
                this.edtIdDevice.isEnabled = false
                this.btnAddRoom.setOnClickListener {
                    var strNameDevice = this.edtNameDevice.text.toString().trim()
                    var strTopicMqtt = this.edtTopicMqtt.text.toString().trim()
                    var linkImage = this.edtImgDevice.text.toString().trim()
                    if (strNameDevice.length == 0) {
                        Toast.makeText(activity, "Xin hãy nhập tên thiết bị", Toast.LENGTH_SHORT).show()
                    }else if(strTopicMqtt.length == 0 ) {
                        Toast.makeText(activity, "Xin hãy nhập số topic ", Toast.LENGTH_SHORT).show()
                    } else if(linkImage.length == 0) {
                        Toast.makeText(activity, "Xin hãy nhập link ảnh", Toast.LENGTH_SHORT).show()
                    } else {
                        val device = Device(time,strTopicMqtt,strNameDevice,"0",linkImage)
                        Glide.with(this@HomeFragment).load(linkImage).error(R.drawable.img_size).into(this.imgRoom)
                       var ref = FirebaseDatabase.getInstance().getReference("Devices/${FirebaseAuth.getInstance().currentUser!!.uid}")
                        //var tmpBroker = ConnectMQTTActivity.MQTT_BROKER.replace(".", "")
                        ref.child(strTopicMqtt).setValue(device)
                        mListDevice.add(device)
                        MQTTHelper.subscribe(strTopicMqtt,1,MQTTHelper.client)
                        deviceAdapter.notifyDataSetChanged()
                        dialog.dismiss()
                    }
                }
                dialog.show()
            }
        }
    }
    fun getDeviceFromFirebase() {

        FirebaseDatabase.getInstance().getReference("Devices/${FirebaseAuth.getInstance().currentUser!!.uid}").
        addValueEventListener(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot != null ) {
                    mListDevice.clear()
                    for (data in snapshot.children){
                        var device = data.getValue(Device::class.java)
                        device?.apply {
                            mListDevice.add(device)
                            Log.d("DATA F:", this.toString())
                            MQTTHelper.subscribe(this.topicMQTT.toString(),1,MQTTHelper.client)
                        }
                    }
                    deviceAdapter.notifyDataSetChanged()
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }
    override fun clickItem(position: Int) {
        Log.d("CLICK_ITEM:" , mListDevice.get(position).toString())
        val intent = Intent(activity, DeviceInformationActivity::class.java)
        val bundle = Bundle()
        bundle.putSerializable("device", mListDevice.get(position))
        intent.putExtras(bundle)
        startActivity(intent)
    }

    override fun onStop() {
        super.onStop()
        mListDevice.clear()
        getDeviceFromFirebase()
    }
    override fun onDestroy() {
        super.onDestroy()
        Log.d("LOGGER", "onDestroy: ")
    }
}