package com.example.shiksharemastered;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MathPage extends AppCompatActivity {
    private DatabaseReference myRef;
    private String[] userDetails;
    private ArrayList<Map<String, String>> videosList;
    private String subject, videoType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_math_page);

        init(savedInstanceState);
    }

    private void init(Bundle savedInstanceState) {
        videosList = new ArrayList<>();

        Intent fromMainMenu = getIntent();
        videoType = fromMainMenu.getStringExtra("videoTypeFromMainMenuToMathPage");
        subject = fromMainMenu.getStringExtra("subjectNameFromMainMenuToMathPage");
        userDetails = fromMainMenu.getStringArrayExtra("UserDetailsFromMainMenuToMathPage");
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://shiksha-37914-default-rtdb.asia-southeast1.firebasedatabase.app/");
        myRef = database.getReference().child(videoType).child(subject).child("approved");

        createVideosList(savedInstanceState);
    }

    private void createVideosList(Bundle savedInstanceState) {
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snapshot1: snapshot.getChildren()) {
                    if (snapshot1.exists()) {
                        HashMap<String, String> mainMap = new HashMap<>();
                        mainMap.put("title", snapshot1.child("details").child("title").getValue(String.class));
                        mainMap.put("uploader", snapshot1.child("details").child("uploader").getValue(String.class));
                        if (videoType.equals("Lectures") && userDetails[0].startsWith(snapshot1.child("details").child("access").getValue(String.class)) || userDetails[0].equals("admin") || userDetails[0].equals("Teacher")) {
                            videosList.add(mainMap);
                        }
                    }
                }

                VideosRecyclerViewAdapter videosRecyclerViewAdapter = new VideosRecyclerViewAdapter(getApplicationContext(), videosList, userDetails, subject, videoType);
                RecyclerView recyclerView = findViewById(R.id.recyclerViewMath);
                recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                recyclerView.setAdapter(videosRecyclerViewAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("listGenerateFail", "onCancelled: " + error.getMessage());
            }
        });
    }
}