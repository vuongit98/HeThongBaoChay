package com.anonymous.he_thong_canh_bao_chay.Models

import java.io.Serializable

data class Device(var idDevice : String?= null ,
                  var topicMQTT : String?= null ,
                  var nameDevice : String?= null ,
                  var valueDevice: String?= "0" ,
                  var urlImage : String?= "default"
                  ): Serializable
