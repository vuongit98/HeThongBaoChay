package com.anonymous.he_thong_canh_bao_chay.Fragment

import android.Manifest
import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import com.anonymous.he_thong_canh_bao_chay.Helper.CustomLoading
import com.anonymous.he_thong_canh_bao_chay.MainHomeActivity.Companion.accountPerson
import com.anonymous.he_thong_canh_bao_chay.R
import com.anonymous.he_thong_canh_bao_chay.databinding.FragmentProfileBinding
import com.bumptech.glide.Glide
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermissionActivity
import com.gun0912.tedpermission.normal.TedPermission
import de.hdodenhof.circleimageview.CircleImageView
import gun0912.tedbottompicker.TedBottomPicker

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ProfileFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ProfileFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }
    lateinit var loadingProfile : CustomLoading
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val binding = FragmentProfileBinding.inflate(LayoutInflater.from(context))
        activity?.apply { loadingProfile = CustomLoading(this) }
        loadingProfile.showDialog()
        Handler(Looper.getMainLooper()).postDelayed( {
            if (loadingProfile.isShowing()) loadingProfile.dimiss()
        } , 2000)
        binding?.apply {
            context?.apply {
                Glide.with(this).load(accountPerson.imgUrl).error(R.drawable.ic_baseline_account_circle_24).into(binding.imgPerson)
            }
            this.addPhoto.setOnClickListener{
                openGallery(this.imgPerson)
            }
            this.txtNamePerson.setText(accountPerson.nameDisplay)
            this.btnUpdateInfo.setOnClickListener {
                var strPass = this.edtPassword.text.toString().trim()
                var strPassRetry = this.edtPasswordRetry.text.toString().trim()
                if (strPass.length == 0) {
                    Toast.makeText(activity,"Mật khẩu chưa nhập. Xin mời nhập mật khẩu", Toast.LENGTH_SHORT).show()
                }else if (strPassRetry.length == 0 ) {
                    Toast.makeText(activity,"Mật khẩu chưa nhập. Xin mời nhập mật khẩu", Toast.LENGTH_SHORT).show()

                }else {
                    Log.d("ProfileFragment", "strPass: ${strPass} - strPassRetrt : ${strPassRetry} ")
                    if(strPass.equals(strPassRetry)) {
                        var credential = EmailAuthProvider.getCredential(accountPerson.email!!, accountPerson.password!!)
                        FirebaseAuth.getInstance().currentUser!!.reauthenticate(credential)
                            .addOnCompleteListener {
                                if (it.isSuccessful) {
                                    FirebaseAuth.getInstance().currentUser!!
                                        .updatePassword(strPass).addOnCompleteListener {
                                            if (it.isSuccessful) {
                                                Toast.makeText(activity,"Mật khẩu đã được cập nhập thành công!", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                }else {
                                    Toast.makeText(activity,"Xác nhận tài khoản không đúng. Xin kiểm tra lại mật khẩu", Toast.LENGTH_SHORT).show()
                                }
                            }
                    }else {
                        Toast.makeText(activity,"Mật khẩu không khớp. Xin mời nhập mật khẩu", Toast.LENGTH_SHORT).show()

                    }
                }
            }
        }
        return binding.root
    }
    fun openGallery(imgView : CircleImageView) {
        var permissionListener = object : PermissionListener {
            override fun onPermissionGranted() {
                Toast.makeText(activity, "Permission Granted", Toast.LENGTH_SHORT).show()
                selectImage(imgView)
            }
            override fun onPermissionDenied(deniedPermissions: MutableList<String>?) {
                Toast.makeText(
                    activity,
                    "Permission Denied\n$deniedPermissions",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        TedPermission.create()
            .setPermissionListener(permissionListener)
            .setPermissions( Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA)
            .check()
    }
    fun selectImage(imgView : ImageView){
        TedBottomPicker.with(activity)
            .show {
                imgView.setImageURI(it)
                activity?.let { it1 -> uploadImage(loadingProfile,it) }
            }
    }
    fun uploadImage(progressBar: CustomLoading, imageUri: Uri) {
        val mStorageRef = FirebaseStorage.getInstance().reference
        if (progressBar.isShowing() == false)progressBar.showDialog()
        var time = System.currentTimeMillis()
        val uploadTask = mStorageRef.child("users/${time}.png").putFile(imageUri)
        uploadTask.addOnSuccessListener {
            //Log.d( TAG, "Imaged upload successfully")
            val downloadLink = mStorageRef.child("users/${time}.png").downloadUrl
            downloadLink.addOnSuccessListener {
                //Log.d(TAG, "link image : " + it.toString())
                FirebaseDatabase.getInstance().
                getReference("Users/" + FirebaseAuth.getInstance().currentUser!!.uid + "/imgUrl")
                    .setValue(it.toString())
                if (progressBar.isShowing()) progressBar.dimiss()
            }.addOnFailureListener {
                //Log.d(TAG, "failed to download")
                progressBar.dimiss()
            }
        }.addOnFailureListener{
            //Log.d(TAG, "failed to ${it.message.toString()}: ")
        }

    }
    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ProfileFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ProfileFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}