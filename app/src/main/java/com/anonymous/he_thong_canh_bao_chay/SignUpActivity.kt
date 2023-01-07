package com.anonymous.he_thong_canh_bao_chay

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import com.anonymous.he_thong_canh_bao_chay.Helper.CustomLoading
import com.anonymous.he_thong_canh_bao_chay.Models.User
import com.anonymous.he_thong_canh_bao_chay.databinding.ActivitySignUpBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class SignUpActivity : AppCompatActivity() {
    lateinit var viewBinding : ActivitySignUpBinding
    lateinit var loading: CustomLoading
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loading = CustomLoading(this)
        viewBinding = ActivitySignUpBinding.inflate(LayoutInflater.from(this))
        setContentView(viewBinding.root)
        viewBinding.btnSignUp.setOnClickListener {
            var strEmail = viewBinding.edtUsername.text.toString().trim()
            var strPass = viewBinding.edtPassword.text.toString().trim()
            var strPassRetry = viewBinding.edtPasswordRetry.text.toString().trim()
            var strNameDisplay = viewBinding.edtDisplayName.text.toString().trim()
            var strPhone = viewBinding.edtPhone.text.toString().trim()

            var user = User(strEmail,strPass,strNameDisplay,strPhone)
            signUp(user, strPassRetry,loading)
        }
    }
    fun signUp(user: User, strPassRetry: String, loading: CustomLoading) {
        if (user.checkValidAccount() == 1 && user.password.equals(strPassRetry)) {
            processAccount(user, loading) ;
        }else {

        }
    }
    fun  processAccount(user: User, loading: CustomLoading) {
        FirebaseAuth.getInstance().
        createUserWithEmailAndPassword(user.email !!, user.password !!)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    if(loading.isShowing() == false) loading.showDialog()
                            it?.apply {
                                FirebaseAuth.getInstance().currentUser.let {
                                    it?.apply {
                                        user.idUser = it.uid
                                        var firebaseRef = FirebaseDatabase.getInstance()
                                        firebaseRef.getReference("Users/" + it.uid)
                                            .setValue(user).addOnCompleteListener {
                                                if (it.isSuccessful) {
                                                    loading.dimiss()
                                                    startActivity(
                                                        Intent(
                                                            this@SignUpActivity,
                                                            ConnectMQTTActivity::class.java
                                                        )
                                                    )
                                                    finish()
                                                } else {
                                                    loading.dimiss()
                                                }
                                            }
                                            .addOnFailureListener {
                                                Toast.makeText(this@SignUpActivity,"Lỗi : $${it.message}", Toast.LENGTH_SHORT).show()
                                            }
                                    }
                                }

                            }
                }
            }.addOnFailureListener {
                Toast.makeText(this,"Lỗi : ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }
}