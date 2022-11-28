package com.example.absencemanagementapp.activities.home

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.bitmap_recycle.IntegerArrayAdapter
import com.example.absencemanagementapp.R
import com.example.absencemanagementapp.activities.auth.LoginActivity
import com.example.absencemanagementapp.activities.profile.TeacherProfileActivity
import com.example.absencemanagementapp.activities.settings.TeacherSettingsActivity
import com.example.absencemanagementapp.adapters.ModulesAdapter
import com.example.absencemanagementapp.models.Module
import com.example.absencemanagementapp.models.Teacher
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import de.hdodenhof.circleimageview.CircleImageView
import java.lang.Integer.parseInt

class TeacherActivity : AppCompatActivity() {
    private lateinit var user_name_tv: TextView
    private lateinit var teacher_image_civ: CircleImageView
    private lateinit var bottom_navigation: BottomNavigationView
    private lateinit var modules_swipe: SwipeRefreshLayout

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_teacher);

        database = FirebaseDatabase.getInstance()
        auth = FirebaseAuth.getInstance()

        //initiate views
        initViews()

        //set dashboard selected
        bottom_navigation.selectedItemId = R.id.dashboard
        //set bottom navigation listener
        bottom_navigation.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.dashboard -> {
                    true
                }
                R.id.profile -> {
                    startActivity(Intent(this, TeacherProfileActivity::class.java))
                    overridePendingTransition(0, 0)
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

        //get user name
        val user_id = auth.currentUser!!.uid
        database.getReference("teachers").child(user_id).get().addOnSuccessListener {
            if (it.exists()) {
                val teacher = it.getValue(Teacher::class.java)
                user_name_tv.text = teacher!!.last_name
            }
        }

        //get user image
        database.getReference("teachers").child(user_id).child("avatar").get()
            .addOnSuccessListener {
                if (it.exists()) {
                    //get the image
                    val image = it.value
                    Glide.with(this).load(image).into(teacher_image_civ)
                }
            }

        teacher_image_civ.setOnClickListener {
            Intent(this, TeacherProfileActivity::class.java).also {
                startActivity(it)
            }
        }
    }

    private fun getModules() {

        database.getReference("modules").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val modules = ArrayList<Module>();
                for (ds in snapshot.children) {
                    val id = parseInt(ds.key.toString())
                    val inititule = ds.child("intitule").value.toString()
                    val abrv = ds.child("abrv").value.toString()
                    val semestre = ds.child("semestre").value.toString()
                    val formation = ds.child("formation").value.toString()
                    val respo_id = ds.child("respo_id").value.toString()
                    var module = Module(id, inititule, abrv, semestre, formation, respo_id)
                    modules.add(module)
                }
                initModules(modules)
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
//        modules.add(Module(1, "Algebre 1", "ALG1", 1, "GI", ""))
//        modules.add(Module(2, "Analyse 1", "ALG1", 1, "EE", ""))
//        modules.add(Module(3, "Physique 1", "ALG1", 1, "LEA", ""))
//        modules.add(Module(4, "Probabilité statistique", "ALG1", 1, "GI", ""))
//        modules.add(Module(5, "Algorithmique et programmation 1", "ALG1", 1, "SV", ""))
//        modules.add(Module(6, "Langues et terminologie 1", "ALG1", 1, "EG", ""))
//        modules.add(Module(7, "Environnement d'entreprise", "ALG1", 1, "SGARNE", ""))
    }

    private fun initModules(modules : ArrayList<Module>) {
        val rv = findViewById<RecyclerView>(R.id.module_rv);
        rv.layoutManager = LinearLayoutManager(this);
        val modulesAdapter = ModulesAdapter(modules, this);
        rv.adapter = modulesAdapter;
    }

    private fun initViews() {
        getModules()
        bottom_navigation = findViewById(R.id.bottom_navigation)
        user_name_tv = findViewById(R.id.user_name_tv)
        teacher_image_civ = findViewById(R.id.teacher_image_civ)
        modules_swipe = this.findViewById(R.id.modules_swipe)
        modules_swipe.setOnRefreshListener {
            getModules()
            modules_swipe.isRefreshing = false
        }
    }

    //redirect to login activity
    private fun redirectToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
}
