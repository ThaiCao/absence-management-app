package com.example.absencemanagementapp.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.example.absencemanagementapp.R
import com.example.absencemanagementapp.models.Student
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.shashank.sony.fancytoastlib.FancyToast

class StudentProfileActivity : AppCompatActivity() {
    private lateinit var user_name_tv: TextView
    private lateinit var user_email_tv: TextView
    private lateinit var first_name_et: TextInputEditText
    private lateinit var last_name_et: TextInputEditText
    private lateinit var cin_et: TextInputEditText
    private lateinit var cne_et: TextInputEditText
    private lateinit var filiere_dropdown: AutoCompleteTextView
    private lateinit var semester_dropdown: AutoCompleteTextView
    private lateinit var email_et: TextInputEditText
    private lateinit var update_btn: Button

    private val semesters = arrayOf("1", "2", "3", "4", "5", "6")
    private val branches = arrayOf("GI", "SV", "LAE", "ECO")

    private lateinit var database: FirebaseDatabase
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_student_profile)

        database = FirebaseDatabase.getInstance()
        auth = FirebaseAuth.getInstance()

        //put the code here
        initViews()
        initDropDowns()
        fillData()
        filiere_dropdown.setOnItemClickListener { adapterView, _, i, _ ->
            //adapterView.getItemAtPosition(i)
        }

        semester_dropdown.setOnItemClickListener { adapterView, _, i, _ ->
            //adapterView.getItemAtPosition(i)
        }

        //regsitration logic
        update_btn.setOnClickListener {
            if (validateInputs()) {
                FancyToast.makeText(
                    this,
                    "Updating...",
                    FancyToast.LENGTH_SHORT,
                    FancyToast.INFO,
                    false
                ).show()
            }
        }
    }

    private fun validateInputs(): Boolean {
        val first_name = first_name_et.text.toString()
        val last_name = last_name_et.text.toString()
        val cin = cin_et.text.toString()
        val cne = cne_et.text.toString()
        val filiere = filiere_dropdown.text.toString()
        val semester = semester_dropdown.text.toString()
        val email = email_et.text.toString()
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
            cne.isEmpty() -> {
                cne_et.error = "CNE is required"
                cne_et.requestFocus()
                false
            }
            filiere.isEmpty() -> {
                filiere_dropdown.error = "Filiere is required"
                filiere_dropdown.requestFocus()
                false
            }
            semester.isEmpty() -> {
                semester_dropdown.error = "Semester is required"
                semester_dropdown.requestFocus()
                false
            }
            email.isEmpty() -> {
                email_et.error = "Email is required"
                email_et.requestFocus()
                false
            }
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                email_et.error = "Email is not valid"
                email_et.requestFocus()
                false
            }
            else -> true
        }
    }

    private fun initDropDowns() {
        val filiere_adapter = ArrayAdapter(this, R.layout.dropdown_item, branches)
        filiere_dropdown.setAdapter(filiere_adapter)
        val semester_adapter = ArrayAdapter(this, R.layout.dropdown_item, semesters)
        semester_dropdown.setAdapter(semester_adapter)
    }

    public fun initViews() {
        user_name_tv = findViewById(R.id.user_name_tv)
        user_email_tv = findViewById(R.id.user_email_tv)
        first_name_et = findViewById(R.id.first_name_et)
        last_name_et = findViewById(R.id.last_name_et)
        cin_et = findViewById(R.id.cin_et)
        cne_et = findViewById(R.id.cne_et)
        filiere_dropdown = findViewById(R.id.filiere_dropdown)
        semester_dropdown = findViewById(R.id.semester_dropdown)
        email_et = findViewById(R.id.email_et)
        update_btn = findViewById(R.id.update_btn)
    }

    public fun fillData() {
        val user = auth.currentUser
        val userRef = database.getReference("students").child(user!!.uid)
        userRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val student = snapshot.getValue(Student::class.java)
                user_name_tv.text = student!!.email
                user_email_tv.text = student!!.email
                first_name_et.setText(student!!.first_name)
                last_name_et.setText(student.last_name)
                cin_et.setText(student.cin)
                cne_et.setText(student.cne)
                filiere_dropdown.setText(student.filiere)
                semester_dropdown.setText(student.semester)
                email_et.setText(student.email)
            }

            override fun onCancelled(error: DatabaseError) {
                FancyToast.makeText(
                    this@StudentProfileActivity,
                    error.message,
                    FancyToast.LENGTH_SHORT,
                    FancyToast.ERROR,
                    false
                ).show()
            }
        })
    }
}