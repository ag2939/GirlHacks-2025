package com.example.shiksharemastered;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class TestPage extends AppCompatActivity implements InterfaceForDataTrans {
    private Button ques1, ques2, ques3, ques4, ques5, submit;
    private RecyclerView recyclerView;
    private String address, subject;
    private String[] userDetails, oAnswerArray, answerArray;
    private DatabaseReference myRefToTitle, myRefToUser;
    private int score = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_page);

        init();

        try {
            ques1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    recyclerView.scrollToPosition(0);
                }
            });

            ques2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    recyclerView.scrollToPosition(1);
                }
            });

            ques3.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    recyclerView.scrollToPosition(2);
                }
            });

            ques4.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    recyclerView.scrollToPosition(3);
                }
            });

            ques5.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    recyclerView.scrollToPosition(4);
                }
            });
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "Please wait...", Toast.LENGTH_SHORT).show();
        }

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                evaluateMarks();
            }
        });
    }

    private void evaluateMarks() {
        for (int index = 0; index < 5; index++) {
            try {
                if (oAnswerArray[index].equals(answerArray[index])) {
                    score += 5;
                }
            } catch (NullPointerException e) {
                Log.d("marks", "evaluateMarks: unmarked question " + index + " " + e.getMessage());
            }
        }
        Log.d("doneEvaluating", "evaluateMarks: saving marks");
        myRefToUser.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                myRefToUser.child(subject + "TestGiven").child(address).setValue(score);
                myRefToUser.child("userDetails").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (!snapshot.child(subject + "TestGiven").exists()) {
                            myRefToUser.child("userDetails").child(subject + "TestGiven").setValue(1);
                        } else {
                            myRefToUser.child("userDetails").child(subject + "TestGiven").setValue(snapshot.child(subject + "TestGiven").getValue(Integer.class) + 1);
                        }
                        Toast.makeText(getApplicationContext(), "Submitted", Toast.LENGTH_SHORT).show();
                        finish();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.d("testGiven", "onCancelled: " + error.getMessage());
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("testGiven", "onCancelled: " + error.getMessage());
            }
        });
    }

    private void init() {
        ques1 = findViewById(R.id.ques1);
        ques2 = findViewById(R.id.ques2);
        ques3 = findViewById(R.id.ques3);
        ques4 = findViewById(R.id.ques4);
        ques5 = findViewById(R.id.ques5);
        submit = findViewById(R.id.submit);

        oAnswerArray = new String[5];
        answerArray = new String[5];

        Intent fromVideos = getIntent();
        subject = fromVideos.getStringExtra("subjectToGiveTestOnRespectiveVideo");
        address = fromVideos.getStringExtra("addressOfLectureToGiveTestOnRespectiveVideo");
        userDetails = fromVideos.getStringArrayExtra("userDetailsToGiveTestOnRespectiveVideo");

        if (userDetails != null) {
            submit.setVisibility(View.VISIBLE);
            myRefToUser = FirebaseDatabase.getInstance("https://shiksha-37914-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference().child("Users").child(userDetails[0]).child(userDetails[1]);
            myRefToTitle = FirebaseDatabase.getInstance("https://shiksha-37914-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference().child("Lectures").child(subject).child("approved").child(address).child("details");
        } else {
            myRefToTitle = FirebaseDatabase.getInstance("https://shiksha-37914-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference().child("Lectures").child(subject).child("requests").child(address);
        }

        createQuestionsList();
    }

    private void createQuestionsList() {
        myRefToTitle.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                HashMap<String, String[]> questionsList = new HashMap<>();
                String[] question;
                int count = 1, index;
                for (DataSnapshot snapshot1 : snapshot.child("question").getChildren()) {
                    if (count < 6) {
                        index = 1;
                        question = new String[6];
                        question[0] = snapshot1.getKey();
                        for (DataSnapshot snapshot2 : snapshot1.getChildren()) {
                            if (index < 6) {
                                question[index] = snapshot2.getValue(String.class);
                                index++;
                            } else {
                                break;
                            }
                        }
                        questionsList.put("Question" + count, question);
                        count++;
                    }
                }

                QuestionsRecyclerViewAdapter questionsRecyclerViewAdapter = new QuestionsRecyclerViewAdapter(getApplicationContext(), questionsList, TestPage.this);
                recyclerView = findViewById(R.id.recyclerViewQuestions);
                recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                recyclerView.setAdapter(questionsRecyclerViewAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("fileDL", "onCancelled: " + error.getMessage());
            }
        });
    }

    @Override
    public void onSubmit(String array_name, int position, String value) {
        if (array_name.equals("oAnswerArray")) {
            oAnswerArray[position] = value;
            Log.d("marking", "onSubmit: " + oAnswerArray[position]);
        } else {
            answerArray[position] = value;
            Log.d("marking", "onSubmit: " + answerArray[position]);
        }
    }
}