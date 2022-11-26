package com.anonymous.he_thong_canh_bao_chay

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import com.anonymous.he_thong_canh_bao_chay.Fragment.HistoryFragment
import com.anonymous.he_thong_canh_bao_chay.Fragment.HomeFragment
import com.anonymous.he_thong_canh_bao_chay.Fragment.ProfileFragment
import com.anonymous.he_thong_canh_bao_chay.Helper.CustomLoading
import com.anonymous.he_thong_canh_bao_chay.Models.User
import com.anonymous.he_thong_canh_bao_chay.databinding.ActivityMainHomeBinding
import com.anonymous.he_thong_canh_bao_chay.databinding.CustomDialogExitBinding
import com.bumptech.glide.Glide
import com.google.android.material.navigation.NavigationBarView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MainHomeActivity : AppCompatActivity() {
    lateinit var loading: CustomLoading
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lateinit var viewBinding: ActivityMainHomeBinding
        loading = CustomLoading(this)
        viewBinding = ActivityMainHomeBinding.inflate(LayoutInflater.from(this))
        setContentView(viewBinding.root)

        val homeFragment = HomeFragment()
        getInformationAccount()
        ReplaceFragment(homeFragment)
        viewBinding?.apply{
            bottomNavigation.setOnItemSelectedListener(object : NavigationBarView.OnItemSelectedListener{
                override fun onNavigationItemSelected(item: MenuItem): Boolean {
                    when(item.itemId) {
                        R.id.page_1 -> {
                            // Respond to navigation item 1 reselection
                            ReplaceFragment(homeFragment)
                            Log.d( "page: ", "page_1")
                        }
                        R.id.page_2 -> {
                            Log.d( "page: ", "page_2")
                            // Respond to navigation item 2 reselection
                            ReplaceFragment(ProfileFragment())
                        }
                        R.id.page_3 -> {
                            Log.d( "page: ", "page_3")
                            ReplaceFragment(HistoryFragment())
                            // Respond to navigation item 2 reselection
                        }
                        R.id.page_4 -> {
                            // Respond to navigation item 2 reselection
                            openDialog()
                        }
                    }
                    return true
                }

            })
        }
    }
    companion object{
        lateinit var accountPerson : User
    }
    fun ReplaceFragment(fragment : Fragment) {
        val trans = supportFragmentManager.beginTransaction()
        trans.replace(R.id.fragment_list,fragment)
        trans.commit()
    }
    fun getInformationAccount() {
        var uid = FirebaseAuth.getInstance().currentUser!!.uid
        var ref = FirebaseDatabase.getInstance().getReference("Users/"+ uid)
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.hasChildren() && snapshot.childrenCount > 0) {
                    if (loading.isShowing() == false) {
                        loading.showDialog()
                    }
                    val user = snapshot.getValue(User::class.java)
                    user?.apply {
                        accountPerson = this
                    }
                    if (loading.isShowing()) {
                        loading.dimiss()
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })
    }
    fun openDialog() {
        val dialog = Dialog(this)
        val customDialog = CustomDialogExitBinding.inflate(LayoutInflater.from(this))

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(customDialog.root)

        val window = dialog.window
        window ?.apply {
            setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            var attriWindow = this.attributes
            attriWindow.gravity = Gravity.CENTER
            this.attributes = attriWindow
            customDialog?.apply {
                this.btnYes.setOnClickListener {

                    FirebaseAuth.getInstance().signOut()
                    startActivity(Intent(this@MainHomeActivity, LoginActivity::class.java))
                    dialog.dismiss()
                    finish()
                }
                this.btnNo.setOnClickListener {
                    dialog.dismiss()
                }
            }

            dialog.show()
        }
    }
}