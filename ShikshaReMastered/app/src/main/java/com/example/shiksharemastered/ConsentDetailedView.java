package com.example.shiksharemastered;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaExtractor;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.ui.StyledPlayerView;
import com.google.android.exoplayer2.upstream.DefaultDataSource;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.FileNotFoundException;
import java.util.HashMap;

public class ConsentDetailedView extends AppCompatActivity {
    private TextView titleForVideo, uploaderNameText, excelPreviewText, descriptionForExperiment;
    private Button previewTest, confirmAndProceed;
    private Uri videoUri;
    private StyledPlayerView playerView;
    private Switch toggleDecision;
    private String videoType, title, address, action, subject;
    private DatabaseReference myRef;
    private StorageReference myStorageRef;
    private int dec = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_consent_detailed_view);

        init();

        titleForVideo.setText(title);
        DatabaseReference myRef2;
        if (action.equals("upload")) {
            myRef2 = myRef.child("requests");
        } else {
            myRef2 = myRef.child("approved");
        }
        myRef2.child(address).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                uploaderNameText.setText(snapshot.child("uploader").getValue(String.class));
                if (videoType.equals("Experiments"))
                    descriptionForExperiment.setText(snapshot.child("description").getValue(String.class));
                if (action.equals("upload")) {
                    videoUri = Uri.parse(snapshot.child("videoUri").getValue(String.class));
                } else {
                    videoUri = Uri.parse(snapshot.child("details").child("videoUri").getValue(String.class));
                }
                ExoPlayer exoPlayer = new ExoPlayer.Builder(getApplicationContext()).build();
                MediaSource mediaSource = new ProgressiveMediaSource.Factory(new DefaultHttpDataSource.Factory())
                        .createMediaSource(MediaItem.fromUri(videoUri));
                exoPlayer.setMediaSource(mediaSource);
                exoPlayer.setPlayWhenReady(true);
                playerView.setPlayer(exoPlayer);
                if (snapshot.child("question").exists()) {
                    previewTest.setVisibility(View.VISIBLE);
                    excelPreviewText.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("initializing", "onCancelled: " + error.getMessage());
            }
        });

        previewTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent toPreviewTest = new Intent(getApplicationContext(), TestPage.class);
                toPreviewTest.putExtra("addressOfLectureToGiveTestOnRespectiveVideo", address);
                toPreviewTest.putExtra("actionToBePerformedOnRespectiveVideo", "attempt");
                toPreviewTest.putExtra("subjectToGiveTestOnRespectiveVideo", subject);
                startActivity(toPreviewTest);
            }
        });

        toggleDecision.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    dec = 1;
                } else {
                    dec = 0;
                }
            }
        });

        confirmAndProceed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleDecision.setEnabled(false);
                if (dec == 1) {
                    accept();
                } else {
                    deny();
                }
                finish();
            }
        });
    }

    private void accept() {
        if (action.equals("upload")) {
            myRef.child("requests").child(address).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        HashMap<String, Object> mainMap = new HashMap<>();
                        HashMap<String, Object> questionMap, optionMap;

                        mainMap.put("videoUri", snapshot.child("videoUri").getValue(String.class));
                        if (snapshot.child("question").exists()) {
                            questionMap = new HashMap<>();
                            for (DataSnapshot snapshot1 : snapshot.child("question").getChildren()) {
                                optionMap = new HashMap<>();
                                for (DataSnapshot snapshot2 : snapshot1.getChildren()) {
                                    optionMap.put(snapshot2.getKey(), snapshot2.getValue(String.class));
                                }
                                questionMap.put(snapshot1.getKey(), optionMap);
                            }
                            mainMap.put("question", questionMap);
                        }
                        mainMap.put("access", snapshot.child("access").getValue(String.class));
                        mainMap.put("title", snapshot.child("title").getValue(String.class));
                        mainMap.put("uploader", snapshot.child("uploader").getValue(String.class));
                        mainMap.put("Likes", 0);
                        mainMap.put("Dislikes", 0);
                        myRef.child("approved").child(address).child("details").setValue(mainMap)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        myRef.child("requests").child(address).setValue(null);
                                        Toast.makeText(getApplicationContext(), "Request Approved", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        Toast.makeText(getApplicationContext(), "Request already handled", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.d("requests", error.getMessage());
                }
            });
        } else {
            myRef.child("approved").child(address).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        myStorageRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                myRef.child("approved").child(address).setValue(null);
                                Toast.makeText(getApplicationContext(), "Remove Successful", Toast.LENGTH_SHORT).show();
                                myRef.child("removeRequests").child(address).setValue(null);
                            }
                        });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.d("removeVideo", "onCancelled: " + error.getMessage());
                }
            });
        }
    }

    private void deny() {
        if (action.equals("upload")) {
            myRef.child("requests").child(address).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        myRef.child("requests").child(address).setValue(null).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Toast.makeText(getApplicationContext(), "Request Denied", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        Toast.makeText(getApplicationContext(), "Request already handled", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.d("requests", error.getMessage());
                }
            });
        } else {
            myRef.child("removeRequests").child(address).setValue(null).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    Toast.makeText(getApplicationContext(), "Delete Declined", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void init() {
        titleForVideo = findViewById(R.id.titleForVideo);
        descriptionForExperiment = findViewById(R.id.descriptionOfExperiment);
        uploaderNameText = findViewById(R.id.uploaderNameText);
        excelPreviewText = findViewById(R.id.excelPreviewText);

        previewTest = findViewById(R.id.btn_excelPreview);
        confirmAndProceed = findViewById(R.id.consentDecision);
        toggleDecision = findViewById(R.id.switchForConsent);

        playerView = findViewById(R.id.styledPlayerDetailedVideoPreview);

        Intent fromConsentList = getIntent();
        videoType = fromConsentList.getStringExtra("videoTypeFromListToDetailedView");
        subject = fromConsentList.getStringExtra("subjectFromListToDetailedView");
        title = fromConsentList.getStringExtra("titleFromListToDetailedView");
        action = fromConsentList.getStringExtra("actionFromListToDetailedView");

        if (videoType.equals("Lectures")) {
            address = "Lecture: " + title;
        } else {
            address = "Experiment: " + title;
            descriptionForExperiment.setVisibility(View.VISIBLE);
        }

        myStorageRef = FirebaseStorage.getInstance("gs://shiksha-37914.appspot.com/").getReference().child(address);
        myRef = FirebaseDatabase.getInstance("https://shiksha-37914-default-rtdb.asia-southeast1.firebasedatabase.app").getReference().child(videoType).child(subject);
    }
}