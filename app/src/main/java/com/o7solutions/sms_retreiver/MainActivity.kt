package com.o7solutions.sms_retreiver

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.o7solutions.sms_retreiver.Adapters.UserAdapter
import com.o7solutions.sms_retreiver.UI.ViewMessageActivity
import com.o7solutions.sms_retreiver.data_classes.User
import com.o7solutions.sms_retreiver.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var userAdapter: UserAdapter
    private val userList = mutableListOf<User>()
    private var db = FirebaseFirestore.getInstance()

    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        binding.pgBar.visibility = View.VISIBLE



        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Sample Data
//        userList.add(User("1", "jatin@example.com"))
//        userList.add(User("2", "rahul@example.com"))
//        userList.add(User("3", "priya@example.com"))

        userAdapter = UserAdapter(userList) { user ->
            val intent = Intent(this, ViewMessageActivity::class.java)
            intent.putExtra("id",user.email)
            startActivity(intent)
        }

        recyclerView.adapter = userAdapter

        fetchUsers()
    }



    fun fetchUsers() {
        userList.clear()

        db.collection("user").addSnapshotListener { value,error->


            if(error != null) {
                binding.pgBar.visibility = View.GONE

                Toast.makeText(this, "Unable to fetch users!", Toast.LENGTH_SHORT).show()
            }

            if(value != null) {
                for(i in value) {
                    val user = i.toObject(User::class.java)
                    userList.add(user)
                }

                userAdapter.notifyDataSetChanged()
                binding.pgBar.visibility = View.GONE

            }

        }
    }
}