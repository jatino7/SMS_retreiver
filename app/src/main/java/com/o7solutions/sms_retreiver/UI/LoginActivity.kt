package com.o7solutions.sms_retreiver.UI

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.o7solutions.sms_retreiver.R
import com.o7solutions.sms_retreiver.data_classes.User
import com.o7solutions.sms_retreiver.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var emailEt: EditText
    private lateinit var passwordEt: EditText
    private lateinit var actionBtn: Button
    private lateinit var toggleTv: TextView
    private lateinit var titleTv: TextView
    private lateinit var binding: ActivityLoginBinding

    private var db = FirebaseFirestore.getInstance()
    private var isLoginMode = true  // By default, Login mode
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        auth = FirebaseAuth.getInstance()

        emailEt = findViewById(R.id.etEmail)
        passwordEt = findViewById(R.id.etPassword)
        actionBtn = findViewById(R.id.btnAction)
        toggleTv = findViewById(R.id.tvToggle)
        titleTv = findViewById(R.id.tvTitle)

        // Handle login/signup action
        actionBtn.setOnClickListener {
            val email = emailEt.text.toString()
            val password = passwordEt.text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (isLoginMode) {
                loginUser(email, password)
            } else {
                signupUser(email, password)
            }
        }

        // Toggle between login and signup
        toggleTv.setOnClickListener {
            isLoginMode = !isLoginMode
            if (isLoginMode) {
                titleTv.text = "Login"
                actionBtn.text = "Login"
                toggleTv.text = "Don’t have an account? Sign up"
            } else {
                titleTv.text = "Sign Up"
                actionBtn.text = "Sign Up"
                toggleTv.text = "Already have an account? Login"
            }
        }
    }

    private fun loginUser(email: String, password: String) {
        binding.pgBar.visibility = View.VISIBLE

        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, QuestionsActivity::class.java))
                finish()
            } else {
                Toast.makeText(this, "Login Failed: ${task.exception?.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }

        binding.pgBar.visibility = View.GONE

    }

    private fun signupUser(email: String, password: String) {

        binding.pgBar.visibility = View.VISIBLE

        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {

                addUserToFirebase(email,auth.currentUser?.uid.toString())
                Toast.makeText(this, "Signup Successful", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, QuestionsActivity::class.java))
                finish()
            } else {
                Toast.makeText(this, "Signup Failed: ${task.exception?.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }

        binding.pgBar.visibility = View.GONE

    }

    fun addUserToFirebase(email: String,id: String) {

        val newUser = User(id = id, email = email)
        db.collection("user").document(id).set(newUser)
            .addOnSuccessListener {
                Log.d("Login Activity","User added successfully")
            }
            .addOnFailureListener { e->

                Log.e("Login Activity","Failure to add user ${e.message}")
            }

    }
}