package com.anonymous.he_thong_canh_bao_chay

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import com.anonymous.he_thong_canh_bao_chay.Helper.CustomLoading
import com.anonymous.he_thong_canh_bao_chay.Models.User
import com.anonymous.he_thong_canh_bao_chay.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {
    lateinit var viewBindingMain : ActivityLoginBinding
    lateinit var loading: CustomLoading
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (FirebaseAuth.getInstance().currentUser != null) {
            startActivity(Intent(this, ConnectMQTTActivity::class.java))
            finish()
        }
        loading = CustomLoading(this)
        viewBindingMain = ActivityLoginBinding.inflate(LayoutInflater.from(this)) ;
        setContentView(viewBindingMain.root)
        viewBindingMain.btnLogin.setOnClickListener {
            var edtUserName : String = viewBindingMain.edtUsername.text.toString().trim()
            var edtPassword : String = viewBindingMain.edtPassword.text.toString().trim()
            var user : User = User(edtUserName,edtPassword)
            login(user)
        }
        viewBindingMain.btnForgetPassword.setOnClickListener {
            startActivity(Intent(this@LoginActivity, ForgetPasswordActivity::class.java))
            finish()
        }
        viewBindingMain.btnSignUp.setOnClickListener {
            startActivity(Intent(this@LoginActivity,SignUpActivity::class.java))
            finish()
        }
    }

    fun login(user : User) {
        loading.showDialog()
        FirebaseAuth.getInstance().signInWithEmailAndPassword(user.email !!,user.password !!)
            .addOnCompleteListener{
                if (it.isSuccessful) {
                    loading.dimiss()
                    startActivity(Intent(this@LoginActivity, ConnectMQTTActivity::class.java))
                    finish()
                }else {
                    loading.dimiss()
                }
            }
    }
}