package com.anonymous.he_thong_canh_bao_chay.Fragment

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.anonymous.he_thong_canh_bao_chay.Helper.ConvertTime
import com.anonymous.he_thong_canh_bao_chay.Helper.CustomLoading
import com.anonymous.he_thong_canh_bao_chay.Helper.MQTTHelper
import com.anonymous.he_thong_canh_bao_chay.Models.Device
import com.anonymous.he_thong_canh_bao_chay.Models.DeviceData
import com.anonymous.he_thong_canh_bao_chay.R
import com.anonymous.he_thong_canh_bao_chay.databinding.FragmentHistoryBinding
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
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
 * Use the [HistoryFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class HistoryFragment : Fragment() {
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
    lateinit var mListDevice : MutableList<Device>
    lateinit var mListDeviceHistory: MutableList<DeviceData>
    lateinit var mListEntry : MutableList<Entry>
    lateinit var mListTime : MutableList<String>
    lateinit var mListData : MutableList<String>
    lateinit var loadingHistory     : CustomLoading
     var mMin : Float = 999999f
     var mMax : Float = 0f
    var uid = FirebaseAuth.getInstance().currentUser!!.uid

    lateinit var binding    : FragmentHistoryBinding
    var mType               : String?= null
    var mDevice             : Device?= null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        activity?.apply { loadingHistory = CustomLoading(this) }
        binding = FragmentHistoryBinding.inflate(LayoutInflater.from(context))
        loadingHistory.showDialog()
        mListDevice = mutableListOf()
        mListDeviceHistory = mutableListOf()
        mListTime = mutableListOf()
        mListData = mutableListOf()
        mListEntry = mutableListOf()
        getListDevice()

        Handler(Looper.getMainLooper()).postDelayed({
            if (loadingHistory.isShowing()) loadingHistory.dimiss()
        }, 2000)

        binding?.apply {
            spinnerDevice.setOnSpinnerItemSelectedListener<String> { oldIndex, oldItem, newIndex, newItem ->
                var device = mListDevice.get(newIndex)
                mDevice = device

                Log.d("device = ", device.toString())
                if (mDevice != null && device!!.nameDevice != null) {
                    //txtNameRoom.setText(mRoomChoose!!.nameRoom)
                   // txtNameDevice.setText(device.nameDevice)
                    getListHistoryDevice(mDevice!!.idDevice!!)
                    //drawLineChart(lineChart,System.currentTimeMillis(), mListEntry)
                    //TotalHistoryDevicePresenter().getTimeHistory(uid, mRoomChoose!!.idRoom!!, device.idDevice!!,mListTime,loading)
                    Handler(Looper.getMainLooper()).postDelayed({
                        if (loadingHistory.isShowing())loadingHistory.dimiss()
                    },2000)
                }else {
                    Toast.makeText(activity, "Không thiết bị nào được chọn", Toast.LENGTH_SHORT).show()
                }
            }
            spinnerDate.setOnSpinnerItemSelectedListener<String> { oldIndex, oldItem, newIndex, newItem ->
                Log.d( "Time : ", newItem.toString())
                mDevice ?.apply {

                    if (loadingHistory.isShowing() == false)
                        loadingHistory.showDialog()
                   // TotalHistoryDevicePresenter().
                   // getListHistoryDevice(uid,mRoomChoose!!.idRoom!!,mDevice!!.idDevice !!,mListDeviceHistory,mListEntry)
                    getListDataFromDate(this.idDevice.toString(), newItem,loadingHistory);
                    Handler(Looper.getMainLooper()).postDelayed({
                        if (loadingHistory.isShowing()) loadingHistory.dimiss()
                        drawLineChart(mMin,mMax,lineChart,System.currentTimeMillis(), mListEntry)
                    }, 2000)


                }
            }
        }
        return binding.root
    }
    fun getListDataFromDate(idDevice: String, date : String ,loadingHistory: CustomLoading) {
        var urlTree = "History/"+ MQTTHelper.uid +"/"+idDevice+"/"+date
        Log.d( "getListHistoryDevice: ",urlTree)
        FirebaseDatabase.getInstance()
            .getReference(urlTree)
            .addListenerForSingleValueEvent(object :ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.hasChildren() && snapshot.childrenCount > 0){
                        mListEntry.clear()
                        var i = 0 ;
                        for (data in snapshot.children){

                            var device = data.getValue(DeviceData::class.java)
                            device?.apply {
                                Log.d("KEY_DATA", device.valueDevice.toString())
                                //mListEntry.add(Entry(i.toFloat() ,this.valueDevice!!.toFloat()))
                                mMin = Math.min(mMin, this.valueDevice!!.toFloat())
                                mMax = Math.max(mMax, this.valueDevice!!.toFloat())
                                mListDeviceHistory.add(this)
                            }
                        }
                        process(date,loadingHistory)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }

            })
    }
    fun getListDevice() {
        var urlTree = "Devices/"+ MQTTHelper.uid +"/"
        FirebaseDatabase.getInstance().getReference(urlTree)
            .addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.hasChildren() && snapshot.childrenCount > 0) {
                        mListDevice.clear()
                        for (data in snapshot.children) {
                            var device = data.getValue(Device::class.java)
                            device?.apply {
                                mListDevice.add(this)
                            }
                        }
                        if (mListDevice.size > 0){
                            var listDevice = mutableListOf<String>()
                            for (data in mListDevice) {
                                listDevice.add(data.nameDevice.toString())
                            }
                            binding.spinnerDevice.setItems(listDevice)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }

            })
    }
    fun getListHistoryDevice(idDevice : String) {
        var urlTree = "History/"+ MQTTHelper.uid +"/"+idDevice
        Log.d( "getListHistoryDevice: ",urlTree)
        FirebaseDatabase.getInstance()
            .getReference(urlTree)
            .addListenerForSingleValueEvent(object :ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.hasChildren() && snapshot.childrenCount > 0){
                        mListTime.clear()
                        for (data in snapshot.children){
                            Log.d("KEY_DATA", data.key.toString())
                            mListTime.add(data.key.toString())
                        }
                        if (mListTime.size > 0) {
                            binding.spinnerDate.setItems(mListTime)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }

            })
    }
    fun process(day : String , loadingHistory: CustomLoading) {
        val tmp = day.split("_")
        Log.d("TIMESTAMP : " ,tmp.toString())
        var timeStartDate: Long =
            ConvertTime.convertDateTimeToUnix(tmp[0].toInt(), tmp[1].toInt() - 1, tmp[2].toInt(), 0, 0, 0)
        Log.d("TIME_START", "${timeStartDate} ")
        timeStartDate = timeStartDate/1000
        val listTurn: MutableList<Float> = ArrayList()
        val listTime : MutableList<Int>  = ArrayList()
        for (i in 0..1000000)  {
            listTurn.add(0f)
            listTime.add(0)
        }
        for (item in mListDeviceHistory) {
            val currentTimeStamp = item.time!!.toLong()
//            val currentTimeOn: Int = item.getTimeOn()
//            for (i in 0 until currentTimeOn) {
                var index = Math.abs(currentTimeStamp - timeStartDate)
                for(i in 0..5) {
                    index = index + i
                    Log.d("CHART", "$currentTimeStamp-$timeStartDate = $index")
                    if (index.toInt() < 0 || index.toInt() >=1000000 ) break
                    listTurn.add(index.toInt(), item.valueDevice!!.toFloat())
                }
        }
        for (i in 0 .. 86400) {
            mListEntry.add(Entry(i.toFloat(), listTurn[i]))
        }
        if(loadingHistory.isShowing()) loadingHistory.dimiss()

    }
    fun drawLineChart(min: Float , max : Float ,lineChart : LineChart, timeStamp : Long, mListData : MutableList<Entry> ) {
        // Month start 0 - 11
        Log.d( "drawLineChart: ", "VAO")
        // Left Axis
        val left: YAxis = lineChart.getAxisLeft()
        left.setDrawLabels(true) // no axis labels
        left.setDrawAxisLine(true) // no axis line
        left.setDrawGridLines(false) // no grid lines
        left.setDrawZeroLine(false) // draw a zero line
        lineChart.getAxisRight().setEnabled(false) // no right axis
        left.textColor = Color.RED // color text label
        left.axisMinimum = 0f
        left.axisMaximum = max.toFloat()+ 1
        left.granularity = 1f // interval 1
        left.setLabelCount(0, false)
        //  left.valueFormatter = YValueFormatter()

        // Right Axis
        //
        lineChart.getAxisRight().setEnabled(false)
        lineChart.zoomIn()
        // X Axis test
        val xAxis: XAxis = lineChart.getXAxis()
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.textSize = 10f
        xAxis.textColor = Color.RED
        xAxis.setDrawGridLines(false)
        xAxis.setDrawAxisLine(true)
        var lengthTime = 86400f
        xAxis.axisMaximum = lengthTime
        xAxis.axisMinimum = 0f
        xAxis.setLabelCount(24, false)
        xAxis.granularity = 3600f
        xAxis.valueFormatter = XValueFormatter()
        // General Chart Attribute
        lineChart.setVisibleXRangeMaximum(7200F)
        lineChart.setVisibleXRangeMinimum(3600F)

        val description: Description = lineChart.getDescription()
        description.setEnabled(false)

        val lineDataSet = LineDataSet(mListData, "Data")
        lineDataSet.setDrawCircles(false)
        val dataSets: MutableList<ILineDataSet> = mutableListOf()
        dataSets.add(lineDataSet)
        val data = LineData(dataSets)
        lineChart.data = data
        lineChart.legend.isEnabled = true
        lineChart.legend.textColor = R.color.colorPrimaryDark
        lineChart.invalidate()
    }
    internal class XValueFormatter : ValueFormatter() {
        override fun getAxisLabel(value: Float, axis: AxisBase): String {
            val stamp = value.toInt() / 3600
             if (value.toInt() % 3600 == 0) {
                 return "$stamp:00"
            } else {
                 var tmp = value.toInt()%3600
                 return "$stamp:$tmp"
            }
        }
    }
    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment HistoryFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            HistoryFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}