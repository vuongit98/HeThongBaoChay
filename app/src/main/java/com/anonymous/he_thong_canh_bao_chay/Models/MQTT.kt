package com.anonymous.he_thong_canh_bao_chay.Models

import java.io.Serializable

data class MQTT(var broker : String ?= null,
                var port   : String ?=  null,
                var user   : String ?= null ,
                var pass   : String ?= null,

                ) : Serializable
