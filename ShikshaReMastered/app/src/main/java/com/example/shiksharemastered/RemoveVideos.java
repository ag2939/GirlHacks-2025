package com.example.shiksharemastered;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;

import com.google.android.exoplayer2.ui.StyledPlayerView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class RemoveVideos extends AppCompatActivity {
    private RecyclerView recyclerView;
    private DatabaseReference myRef;
    private String subject, videoType;
    private ArrayList<Map<String, String>> requestsList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remove_videos);

        init();
    }

    private void init() {
        myRef = FirebaseDatabase.getInstance("https://shiksha-37914-default-rtdb.asia-southeast1.firebasedatabase.app").getReference();
        recyclerView = findViewById(R.id.recyclerViewRemoveVideos);
        requestsList = new ArrayList<>();

        Intent fromMainMenu = getIntent();
        subject = fromMainMenu.getStringExtra("subjectNameFromMainMenuToRemoveVideos");
        videoType = fromMainMenu.getStringExtra("videoTypeFromMainMenuToRemoveVideos");

        removeRequests();
    }

    private void removeRequests() {
        myRef.child(videoType).child(subject).child("removeRequests").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                requestsList.clear();
                for (DataSnapshot snapshot1: snapshot.getChildren()) {
                    HashMap<String, String> map = new HashMap<>();
                    map.put("title", snapshot1.child("title").getValue(String.class));
                    map.put("access", snapshot1.child("access").getValue(String.class));
                    requestsList.add(map);
                    Log.d("removeRequests", "onDataChange: " + requestsList.size());
                }

                ConsentRecyclerViewAdapter consentRecyclerViewAdapter = new ConsentRecyclerViewAdapter(getApplicationContext(), subject, videoType, requestsList, "delete");
                recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                recyclerView.setAdapter(consentRecyclerViewAdapter);
                recyclerView.scrollToPosition(requestsList.size() - 1);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("removeRequestList", "onCancelled: " + error.getMessage());
            }
        });
    }
}