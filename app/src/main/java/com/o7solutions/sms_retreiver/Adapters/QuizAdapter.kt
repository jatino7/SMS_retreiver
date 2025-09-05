package com.o7solutions.sms_retreiver.Adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.o7solutions.sms_retreiver.R
import com.o7solutions.sms_retreiver.data_classes.Question


class QuizAdapter(private val questions: List<Question>) :
    RecyclerView.Adapter<QuizAdapter.QuizViewHolder>() {

    class QuizViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val questionText: TextView = itemView.findViewById(R.id.tvQuestion)
        val optionsGroup: RadioGroup = itemView.findViewById(R.id.radioGroup)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuizViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_question, parent, false)
        return QuizViewHolder(view)
    }



    override fun onBindViewHolder(holder: QuizViewHolder, position: Int) {
        val question = questions[position]

        holder.questionText.text = question.question

        // Shuffle answers (correct + incorrect)
        val allAnswers = question.incorrect_answers.toMutableList()
        allAnswers.add(question.correct_answer)
        allAnswers.shuffle()

        holder.optionsGroup.removeAllViews()
        allAnswers.forEach { answer ->
            val radio = RadioButton(holder.itemView.context)
            radio.text = answer
            holder.optionsGroup.addView(radio)
        }
    }

    override fun getItemCount() = questions.size
}