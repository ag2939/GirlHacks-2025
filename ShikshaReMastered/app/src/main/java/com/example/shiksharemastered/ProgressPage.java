package com.example.shiksharemastered;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ProgressPage extends AppCompatActivity {

    private TextView subject_, videos_seen, tests_given, score_avg;
    private String[] userDetails;
    private String subject;
    private int videosSeen, testGiven;
    private float score = 0;
    private DatabaseReference myRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_progress_page);

        init();

        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                try {
                    if (snapshot.child("userDetails").child(subject + "TestGiven").exists()) {
                        testGiven = snapshot.child("userDetails").child(subject + "TestGiven").getValue(Integer.class);
                    }
                    if (snapshot.child("userDetails").child(subject + "VideosSeen").exists()) {
                        videosSeen = snapshot.child("userDetails").child(subject + "VideosSeen").getValue(Integer.class);
                    }
                    int count = (int) snapshot.child(subject + "TestGiven").getChildrenCount();
                    for (DataSnapshot snapshot1 : snapshot.child(subject + "TestGiven").getChildren()) {
                        score += (snapshot1.getValue(Integer.class) / (float) count);
                    }

                    subject_.setText(subject);
                    videos_seen.setText(String.valueOf(videosSeen));
                    tests_given.setText(String.valueOf(testGiven));
                    score_avg.setText(String.valueOf(score));
                } catch (Exception e) {
                    Log.d("progressProblem", "onDataChange: Progress Can't Be Generated");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("progressCheck", "onCancelled: " + error.getMessage());
            }
        });
    }

    private void init() {
        Intent fromMainMenu = getIntent();
        userDetails = fromMainMenu.getStringArrayExtra("userDetailsFromMainMenuToProgressPage");
        subject = fromMainMenu.getStringExtra("subjectNameFromMainMenuToProgressPage");

        myRef = FirebaseDatabase.getInstance("https://shiksha-37914-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference().child("Users").child(userDetails[0]).child(userDetails[1]);

        videos_seen = findViewById(R.id.videos_seen);
        tests_given = findViewById(R.id.tests_given);
        score_avg = findViewById(R.id.score_avg);
        subject_ = findViewById(R.id.subject);
    }
}
