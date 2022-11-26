package com.anonymous.he_thong_canh_bao_chay.Models

import java.io.Serializable

data class DeviceData(var idDevice : String?= null ,
                      var nameDevice : String?= null ,
                      var valueDevice: String?= "0" ,
                      var time : String?= System.currentTimeMillis().toString()):Serializable
