package com.example.shiksharemastered;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;

public class QuestionsRecyclerViewAdapter extends RecyclerView.Adapter<QuestionsRecyclerViewAdapter.ViewHolder> {

    private RadioButton option1, option2, option3, option4;
    private RadioGroup radioGroup;
    private TextView questionText;
    private Context context;
    private final Map<String, String[]> questionsList;
    private InterfaceForDataTrans interfaceForDataTrans;

    QuestionsRecyclerViewAdapter(Context context, Map<String, String[]> questionsList, InterfaceForDataTrans interfaceForDataTrans) {
        this.context = context;
        this.questionsList = questionsList;
        this.interfaceForDataTrans = interfaceForDataTrans;
    }

    @NonNull
    @Override
    public QuestionsRecyclerViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.question_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull QuestionsRecyclerViewAdapter.ViewHolder holder, int position) {
        Log.d("testRecycleView", "onBindViewHolder: " + questionsList.get("Question" + (position + 1))[0]);
        Log.d("testRecycleView", "onBindViewHolder: " + questionsList.get("Question" + (position + 1))[5]);
        String[] question = questionsList.get("Question" + (position + 1));
        assert question != null;
        String questionProperFormat = "Q" + (position + 1) + ". " + question[0];
        questionText.setText(questionProperFormat);
        option1.setText(question[1]);
        option2.setText(question[2]);
        option3.setText(question[3]);
        option4.setText(question[4]);
        interfaceForDataTrans.onSubmit("oAnswerArray", position, question[5]);
    }

    @Override
    public int getItemCount() {
        Log.d("testRecycleView", "getItemCount: " + questionsList.size());
        return questionsList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            questionText = itemView.findViewById(R.id.questionText);
            option1 = itemView.findViewById(R.id.option1);
            option2 = itemView.findViewById(R.id.option2);
            option3 = itemView.findViewById(R.id.option3);
            option4 = itemView.findViewById(R.id.option4);
            radioGroup = itemView.findViewById(R.id.radioGroup);

            radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    int position = getAdapterPosition();
                    if (checkedId == R.id.option1) {
                        interfaceForDataTrans.onSubmit("answerArray", position, "A");
                    } else if (checkedId == R.id.option2) {
                        interfaceForDataTrans.onSubmit("answerArray", position, "B");
                    } else if (checkedId == R.id.option3) {
                        interfaceForDataTrans.onSubmit("answerArray", position, "C");
                    } else if (checkedId == R.id.option4) {
                        interfaceForDataTrans.onSubmit("answerArray", position, "D");
                    }
                }
            });
        }
    }
}
