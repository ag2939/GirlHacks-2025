package com.example.shiksharemastered;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.android.exoplayer2.ui.StyledPlayerView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class UploadRequests extends AppCompatActivity {

    private String subject, videoType;
    private DatabaseReference myRef;
    private ArrayList<Map<String, String>> requests;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_requests);

        init(savedInstanceState);
    }

    private void init(Bundle savedInstanceState) {
        requests = new ArrayList<>();

        Intent fromMainMenu = getIntent();
        videoType = fromMainMenu.getStringExtra("videoTypeFromMainMenuToUploadRequests");
        subject = fromMainMenu.getStringExtra("subjectNameFromMainMenuToUploadRequests");
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://shiksha-37914-default-rtdb.asia-southeast1.firebasedatabase.app/");
        myRef = database.getReference().child(videoType).child(subject);

        createRequestList(savedInstanceState);
    }

    private void createRequestList(Bundle savedInstanceState) {
        myRef.child("requests").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                requests.clear();

                for (DataSnapshot snapshot1: snapshot.getChildren()) {
                    if (snapshot1.exists()) {
                        HashMap<String, String> mainMap = new HashMap<>();
                        mainMap.put("access", snapshot1.child("access").getValue(String.class));
                        mainMap.put("title", snapshot1.child("title").getValue(String.class));
                        requests.add(mainMap);
                    }
                }

                ConsentRecyclerViewAdapter consentRecyclerViewAdapter = new ConsentRecyclerViewAdapter(getApplicationContext(), subject, videoType, requests, "upload");
                RecyclerView recyclerView = findViewById(R.id.recyclerViewUploadRequests);
                recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                recyclerView.setAdapter(consentRecyclerViewAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("listGenerateFail", "onCancelled: " + error.getMessage());
            }
        });
    }
}