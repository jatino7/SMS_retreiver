package com.o7solutions.sms_retreiver.UI

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.o7solutions.sms_retreiver.Adapters.SmsAdapter
import com.o7solutions.sms_retreiver.R
import com.o7solutions.sms_retreiver.data_classes.SmsMessage

class ViewMessageActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private val smsList = arrayListOf<SmsMessage>()
    private lateinit var adapter: SmsAdapter
    var id = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_view_message)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        id = intent.getStringExtra("id").toString()

        recyclerView = findViewById(R.id.recyclerView)
        adapter = SmsAdapter(smsList)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter


        val db = FirebaseFirestore.getInstance()
        db.collection("sms_messages")
            .whereEqualTo("id", id)
            .get()
            .addOnSuccessListener { result ->
                smsList.clear()
                for (doc in result) {
                    val sms = doc.toObject(SmsMessage::class.java)
                    smsList.add(sms)
                }
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}