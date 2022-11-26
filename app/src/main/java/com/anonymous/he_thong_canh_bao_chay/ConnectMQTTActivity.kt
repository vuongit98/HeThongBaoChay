package com.anonymous.he_thong_canh_bao_chay

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.Toast
import com.anonymous.he_thong_canh_bao_chay.Helper.CustomLoading
import com.anonymous.he_thong_canh_bao_chay.Helper.MQTTHelper
import com.anonymous.he_thong_canh_bao_chay.Helper.MyShareReference
import com.anonymous.he_thong_canh_bao_chay.Interface.ConnectSubscribe
import com.anonymous.he_thong_canh_bao_chay.Models.MQTT
import com.anonymous.he_thong_canh_bao_chay.databinding.ActivityConnectMqttactivityBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ConnectMQTTActivity : AppCompatActivity() , ConnectSubscribe{
    companion object{
        var MQTT_BROKER =""
        var MQTT_PORT   ="1883"
        var MQTT_USER   =""
        var MQTT_PASS   =""
        var KEY_MQTT    ="MQTT"
    }
    lateinit var viewBindingMQTT : ActivityConnectMqttactivityBinding
    lateinit var loading : CustomLoading
    lateinit var mqttValue : MQTT
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_connect_mqttactivity)
        loading = CustomLoading(this)
        viewBindingMQTT = ActivityConnectMqttactivityBinding.inflate(LayoutInflater.from(this))
        setContentView(viewBindingMQTT.root)
       GetProtocolMQTT()
        viewBindingMQTT?.apply {
            this.btnConnect.setOnClickListener {
                loading.showDialog()
                var hostName = this.edtHostName.text.toString().trim()
                var port     = this.edtPort.text.toString().trim()
                var userName = this.edtUsername.text.toString().trim()
                var passWord = this.edtPassword.text.toString().trim()
                if (hostName.length > 0 && port.length >0 ){
                    mqttValue = MQTT(hostName,port,userName,passWord)
                    MyShareReference.putData(KEY_MQTT, MQTT(hostName,port,userName,passWord))
                    SaveProtocolMQTT(hostName,port,userName,passWord)
                    MQTT_BROKER = hostName
                    MQTT_PORT = port
                    MQTT_USER = userName
                    MQTT_PASS = passWord
                    MQTTHelper.connect(this@ConnectMQTTActivity,hostName,port,this@ConnectMQTTActivity, userName, passWord)
                }else{
                    loading.dimiss()
                    Toast.makeText(this@ConnectMQTTActivity, "Xin hay nhap day du thong tin",Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun getSuccess() {
        if (loading.isShowing()) loading.dimiss()
        val intent = Intent(this, MainHomeActivity::class.java)
        val bundle = Bundle()
        bundle.putSerializable("MQTT", mqttValue)
        intent.putExtras(bundle)
        startActivity(intent)
        finish()
    }
    fun SaveProtocolMQTT(broker : String , port : String, user : String, pass : String) {
        var mqtt = MQTT(broker,port,user,pass)
        var tmpBroker = broker.replace(".", "")
        FirebaseDatabase.getInstance().getReference("Protocols/MQTT/${tmpBroker}")
            .setValue(mqtt)
    }
    fun GetProtocolMQTT() {
        var mqtt = MyShareReference.getData(KEY_MQTT)
        mqtt?.apply {
            this.broker?.apply {
                viewBindingMQTT.edtHostName.setText(this)
            }
            this.port?.apply {
                viewBindingMQTT.edtPort.setText(this)
            }
            this.user?.apply {
                viewBindingMQTT.edtUsername.setText(this)
            }
            this.pass?.apply {
                viewBindingMQTT.edtPassword.setText(this)
            }
        }
    }
}