package com.anonymous.he_thong_canh_bao_chay.Models

import android.provider.ContactsContract
import android.text.TextUtils
import android.util.Patterns
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import java.io.Serializable
import java.util.regex.Pattern

class User (var email: String? = null,
            var password : String? = null,
            var idUser : String? = null,
            var roleID : Int? = 2,
            var phoneNumber: String?= null ,
            var nameDisplay : String?= "NO 1",
            var imgUrl :String?= "Default"): Serializable {
    var _email: String? = null
    var _password : String? = null
    var _idUser : String? = null
    var _roleID : Int? = 2
    var _nameDisplay : String?= "NO 1"
    var _imgUrl :String?= "Default"
    var _phoneNumber : String?= "UNKNOWN"

    constructor(email: String , pass : String , nameDisplay: String, phoneNumber: String = "UNKNOWN") : this(email, pass) {
        this._email = email
        this._password = pass
        this._nameDisplay = nameDisplay
        this._phoneNumber = phoneNumber
    }
    fun checkValidAccount() : Int {
        if (!TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches()&&
            !TextUtils.isEmpty(password) && password!!.length > 5
            ) {
            return 1
        }
        else if(TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches() == false) {
            return 2
        }
        else  {
            return 3 ;
        }
    }
}