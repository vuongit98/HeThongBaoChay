package com.anonymous.he_thong_canh_bao_chay.Helper

import android.content.Context
import android.util.Log
import com.anonymous.he_thong_canh_bao_chay.Interface.ConnectSubscribe
import com.anonymous.he_thong_canh_bao_chay.Models.Device
import com.anonymous.he_thong_canh_bao_chay.Models.DeviceData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import info.mqtt.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.*
import org.json.JSONObject


object MQTTHelper {
    var clientId = MqttClient.generateClientId()
    const val logger = "[LOG_MQTT]"
    lateinit var client : MqttAndroidClient
    var uid = FirebaseAuth.getInstance().currentUser!!.uid
    lateinit var token : IMqttToken
    lateinit var listener: ConnectSubscribe

    fun connect(listener: ConnectSubscribe ,MQTTBroker : String ,port : String, mContext : Context, userName : String , passWord : String) {
        var broker = "tcp://"+MQTTBroker+":"+port
        this.listener = listener
        client = MqttAndroidClient(mContext,broker, clientId)
        try {
            client.setCallback(object :  MqttCallback {
                override fun connectionLost(cause: Throwable?) {
                    Log.d(logger, "connectionLost: ")
                }

                override fun messageArrived(topic: String?, message: MqttMessage?) {
                    Log.d(logger, "messageArrived: ")
                    var jsonmsg =  JSONObject(String(message!!.payload));
                    var nameDevice = jsonmsg.getString("nameDevice")
                    var idDevice   = jsonmsg.getString("idDevice")
                    var time       = jsonmsg.getString("time")
                    var date       = jsonmsg.getString("date")
                    var data = jsonmsg.getString(nameDevice)
                    Log.d(logger, "messageArrived: ${data}" + " topic : " + topic)
                    SaveDataToFirebase(topic!!, data)
                    SaveDataHistoryFirebase(time,data,
                    nameDevice,idDevice,date)
                    Log.d(logger, "topic: ${topic} - message :  ${jsonmsg.getString(topic)}")

                }

                override fun deliveryComplete(token: IMqttDeliveryToken?) {
                    Log.d(logger, "deliveryComplete: ")
                }

            })
            val options = MqttConnectOptions()

            if (userName.length > 0 && passWord.length > 0) {
                options.userName = userName
                options.password = passWord.toCharArray()
                token = client.connect(options)
            }else {
                token = client.connect()
            }
            token.actionCallback = object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken) {
                    // We are connected
                    Log.d(logger, "onSuccess")
                    listener.getSuccess()

                }

                override fun onFailure(asyncActionToken: IMqttToken, exception: Throwable) {
                    // Something went wrong e.g. connection timeout or firewall problems
                    Log.d(logger, "onFailure")
                }
            }

        }catch (e : MqttException) {
            e.printStackTrace()
        }

    }
    fun subscribe(topic: String, qos : Int = 1, client: MqttAndroidClient) {

            client?.apply {
                try {
                    val subToken = this.subscribe(topic,qos)
                    subToken.actionCallback = object : IMqttActionListener {
                        override fun onSuccess(asyncActionToken: IMqttToken?) {
                            Log.d(logger, "Subscribed onSuccess ")
                        }
                        override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                            Log.d(logger, "Subscribed onFailure ")
                        }

                    }

                }catch (e : MqttException) {
                    e.printStackTrace()
                }
            }

    }
    fun publish(topic: String, qos: Int = 1, data : String) {
        try {
            val message = MqttMessage()
            message.payload = data.toByteArray()
            client.publish(topic,message)
        }catch (e : MqttException) {
            e.printStackTrace()
        }
    }
    fun SaveDataToFirebase(topic: String, data: String) {
       // Log.d(logger, "SaveDataToFirebase: SAVED1 ")

        //Log.d(logger, "SaveDataToFirebase: SAVED2 ")
        var urlTree = "Devices/"+uid+"/"+topic+"/"+"valueDevice"
        Log.d(logger, "urlTree: ${urlTree}")
        FirebaseDatabase.getInstance().
        getReference(urlTree)
            .setValue(data)
        //Log.d(logger, "SaveDataToFirebase: SAVED3 ")
    }
    fun SaveDataHistoryFirebase(time : String, data: String, nameDevice : String, idDevice: String,date : String) {
        var device = DeviceData(idDevice,nameDevice,data,time)
        var urlTree = "History/"+uid+"/"+idDevice+"/"+date
        FirebaseDatabase.getInstance()
            .getReference(urlTree)
            .push().setValue(device)
    }
    fun unsubscribe(topic: String , client: MqttAndroidClient) {
        client?.apply {
            try {
                val subToken = this.unsubscribe(topic)
                subToken.actionCallback = object : IMqttActionListener {
                    override fun onSuccess(asyncActionToken: IMqttToken?) {
                        Log.d(logger, "UnSubscribed onSuccess ")
                    }
                    override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                        Log.d(logger, "UnSubscribed onFailure ")
                    }
                }

            }catch (e : MqttException) {
                e.printStackTrace()
            }
        }
    }
}