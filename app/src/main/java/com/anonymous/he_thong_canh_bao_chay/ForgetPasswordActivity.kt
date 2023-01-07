package com.anonymous.he_thong_canh_bao_chay

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.widget.Toast
import com.anonymous.he_thong_canh_bao_chay.Helper.CustomLoading
import com.anonymous.he_thong_canh_bao_chay.databinding.ActivityForgetPasswordBinding
import com.google.firebase.auth.FirebaseAuth

class ForgetPasswordActivity : AppCompatActivity() {
    lateinit var viewBinding : ActivityForgetPasswordBinding
    lateinit var loading: CustomLoading
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityForgetPasswordBinding.inflate(LayoutInflater.from(this))
        loading = CustomLoading(this)
        setContentView(viewBinding.root)
        viewBinding.btnResendMail.setOnClickListener {
            var strEmail = viewBinding.edtPasswordRetry.text.toString().trim()
            if (Patterns.EMAIL_ADDRESS.matcher(strEmail).matches()) {
                sendMail(email = strEmail , loading = loading)
            }else {
                Toast.makeText(this," Không đúng định dạng email", Toast.LENGTH_SHORT).show()
            }
        }
    }
    fun sendMail(email : String, loading: CustomLoading) {
        loading.showDialog()
        FirebaseAuth.getInstance().sendPasswordResetEmail(email.toString()).addOnCompleteListener() {
            if (it.isSuccessful) {
                Toast.makeText(this,"Đã gửi về mail ${email} thành công" ,Toast.LENGTH_SHORT).show()
                startActivity(Intent(this@ForgetPasswordActivity, LoginActivity::class.java))
                loading.dimiss()
                viewBinding.edtPasswordRetry.setText("");
                finish()
            }
        }.addOnFailureListener {
            Toast.makeText(this,"Lỗi: ${it.message.toString()}" ,Toast.LENGTH_SHORT).show()
            Log.d("EMAIL: ", "${it.message.toString()}")
            loading.dimiss()
        }
    }
}