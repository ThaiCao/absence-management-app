package com.example.absencemanagementapp.activities

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.absencemanagementapp.R
import com.example.absencemanagementapp.models.Teacher
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import com.shashank.sony.fancytoastlib.FancyToast
import de.hdodenhof.circleimageview.CircleImageView
import java.io.IOException


class TeacherProfileActivity : AppCompatActivity() {
    private lateinit var bottom_navigation: BottomNavigationView
    private lateinit var profile_image_picker_btn: ImageButton
    private lateinit var teacher_profile_image_civ: CircleImageView
    private lateinit var back_iv: ImageView
    private lateinit var user_name_tv: TextView
    private lateinit var user_email_tv: TextView
    private lateinit var first_name_et: TextInputEditText
    private lateinit var last_name_et: TextInputEditText
    private lateinit var cin_et: TextInputEditText
    private lateinit var cne_et: TextInputEditText
    private lateinit var update_btn: Button
    private lateinit var bitmap: Bitmap

    private val semesters = arrayOf("1", "2", "3", "4", "5", "6")
    private val branches = arrayOf("GI", "SV", "LAE", "ECO")

    private final val REQUEST_CODE = 100
    private lateinit var uri: Uri

    private lateinit var database: FirebaseDatabase
    private lateinit var auth: FirebaseAuth
    private lateinit var storage: FirebaseStorage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_teacher_profile)

        val upload_dialog = Dialog(this)
        upload_dialog.setContentView(R.layout.dialog_uploading)
        upload_dialog.window!!.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        upload_dialog.window!!.attributes.windowAnimations = android.R.style.Animation_Dialog

        database = FirebaseDatabase.getInstance()
        auth = FirebaseAuth.getInstance()
        storage = FirebaseStorage.getInstance()

        //get user id
        val user_id = auth.currentUser!!.uid

        database.getReference("teachers").child(user_id).child("avatar").get()
            .addOnSuccessListener {
                if (it.exists()) {
                    //get the image
                    val image = it.value.toString()
                    Glide.with(this).load(image).into(teacher_profile_image_civ)
                }
            }

        //put the code here
        initViews()
        initDropDowns()
        fillData()

        //set dashboard selected
        bottom_navigation.selectedItemId = R.id.profile
        //set bottom navigation listener
        bottom_navigation.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.dashboard -> {
                    startActivity(Intent(this, TeacherActivity::class.java))
                    overridePendingTransition(0, 0)
                    true
                }
                R.id.profile -> {
                    true
                }
                R.id.settings -> {
                    startActivity(Intent(this, TeacherSettingsActivity::class.java))
                    overridePendingTransition(0, 0)
                    true
                }
                else -> false
            }
        }

        back_iv.setOnClickListener {
            finish()
        }

        teacher_profile_image_civ.setOnClickListener {
            //show image in dialog
            val dialog = Dialog(this)
            dialog.setContentView(R.layout.dialog_user_image)
            val image = dialog.findViewById<ImageView>(R.id.user_image_iv)
            //get current user image
            database.getReference("teachers").child(user_id).child("avatar").get()
                .addOnSuccessListener {
                    if (it.exists()) {
                        //get the image
                        val image_url = it.value.toString()
                        Glide.with(this).load(image_url).into(image)
                    }
                }

            dialog.window!!.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            dialog.window!!.attributes.windowAnimations = android.R.style.Animation_Dialog
            dialog.show()
        }

        profile_image_picker_btn.setOnClickListener {
            Dexter.withContext(this)
                .withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                .withListener(object : PermissionListener {
                    override fun onPermissionGranted(response: PermissionGrantedResponse) {
                        //open gallery
                        val intent = Intent(Intent.ACTION_PICK)
                        intent.type = "image/*"
                        startActivityForResult(intent, REQUEST_CODE)
                    }

                    override fun onPermissionDenied(response: PermissionDeniedResponse) {
                    }

                    override fun onPermissionRationaleShouldBeShown(
                        permission: PermissionRequest?,
                        token: PermissionToken?
                    ) {
                        token?.continuePermissionRequest()
                    }
                }).check()
        }

        //update logic
        update_btn.setOnClickListener {
            if (validateInputs()) {
                upload_dialog.show()

                val email = getCurrentUserEmail()
                val teacher = Teacher(
                    first_name_et.text.toString(),
                    last_name_et.text.toString(),
                    cin_et.text.toString(),
                    uri.toString(),
                    email
                )

                database.reference.child("teachers").child(auth.currentUser!!.uid)
                    .setValue(teacher)
                    .addOnSuccessListener {
                        FancyToast.makeText(
                            this,
                            "Profile updated successfully",
                            FancyToast.LENGTH_SHORT,
                            FancyToast.SUCCESS,
                            false
                        ).show()
                    }
                    .addOnFailureListener {
                        FancyToast.makeText(
                            this,
                            "Error: ${it.message}",
                            FancyToast.LENGTH_SHORT,
                            FancyToast.ERROR,
                            false
                        ).show()
                    }

                //save the image
                val storageRef = storage.reference
                val imageRef = storageRef.child("profile_images/${auth.currentUser!!.uid}")
                imageRef.putFile(uri)
                    .addOnSuccessListener {
                        FancyToast.makeText(
                            this,
                            "Image uploaded successfully",
                            FancyToast.LENGTH_SHORT,
                            FancyToast.SUCCESS,
                            false
                        ).show()

                        upload_dialog.dismiss()
                    }
                    .addOnFailureListener {
                        FancyToast.makeText(
                            this,
                            "Error: ${it.message}",
                            FancyToast.LENGTH_SHORT,
                            FancyToast.ERROR,
                            false
                        ).show()

                        upload_dialog.dismiss()
                    }
            }
        }
    }

    private fun validateInputs(): Boolean {
        val first_name = first_name_et.text.toString()
        val last_name = last_name_et.text.toString()
        val cin = cin_et.text.toString()
        return when {
            first_name.isEmpty() -> {
                first_name_et.error = "First name is required"
                first_name_et.requestFocus()
                false
            }
            last_name.isEmpty() -> {
                last_name_et.error = "Last name is required"
                last_name_et.requestFocus()
                false
            }
            cin.isEmpty() -> {
                cin_et.error = "CIN is required"
                cin_et.requestFocus()
                false
            }
            else -> true
        }
    }

    private fun initDropDowns() {
    }

    private fun initViews() {
        bottom_navigation = findViewById(R.id.bottom_navigation)
        teacher_profile_image_civ = findViewById(R.id.teacher_profile_image_civ)
        profile_image_picker_btn = findViewById(R.id.profile_image_picker_btn)
        back_iv = findViewById(R.id.back_iv)
        user_name_tv = findViewById(R.id.user_name_tv)
        user_email_tv = findViewById(R.id.user_email_tv)
        first_name_et = findViewById(R.id.first_name_et)
        last_name_et = findViewById(R.id.last_name_et)
        cin_et = findViewById(R.id.cin_et)
        update_btn = findViewById(R.id.update_btn)
    }

    private fun fillData() {
        val user = auth.currentUser
        val userRef = database.getReference("teachers").child(user!!.uid)
        userRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val teacher = snapshot.getValue(Teacher::class.java)
                user_name_tv.text = teacher!!.email
                user_email_tv.text = teacher.email
                first_name_et.setText(teacher.first_name)
                last_name_et.setText(teacher.last_name)
                cin_et.setText(teacher.cin)
            }

            override fun onCancelled(error: DatabaseError) {
                FancyToast.makeText(
                    this@TeacherProfileActivity,
                    "Error: ${error.message}",
                    FancyToast.LENGTH_SHORT,
                    FancyToast.ERROR,
                    false
                ).show()
            }
        })
    }

    private fun getCurrentUserEmail(): String {
        val user = auth.currentUser
        return user!!.email.toString()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            uri = data.data!!
            try {
                bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
                teacher_profile_image_civ.setImageBitmap(bitmap)
            } catch (e: IOException) {
                FancyToast.makeText(
                    this,
                    "Error: ${e.message}",
                    FancyToast.LENGTH_SHORT,
                    FancyToast.ERROR,
                    false
                ).show()
                e.printStackTrace()
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun uploadImageToFirebaseStorage(uri: Uri?) {
        FancyToast.makeText(
            this,
            "Uploading image...",
            FancyToast.LENGTH_SHORT,
            FancyToast.INFO,
            false
        ).show()
    }
}