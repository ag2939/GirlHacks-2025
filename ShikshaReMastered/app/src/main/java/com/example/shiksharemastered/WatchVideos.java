package com.example.shiksharemastered;

import static java.lang.String.valueOf;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.ui.StyledPlayerView;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class WatchVideos extends AppCompatActivity {
    private StyledPlayerView playerView;
    private ArrayList<HashMap<String, String>> messagesList;
    private RecyclerView recyclerView;
    private FirebaseDatabase database;
    private DatabaseReference myRef;
    private TextView videoNameText, likes, dislikes;
    private EditText queryEditText;
    private String videoName, subject, address, videoType;
    private String[] userDetails;
    private Button likeButton, dislikeButton, sendButton, giveTest;
    private int numberOfUsers;
    private final int STORAGE_ALLOTTED_CODE = 0;
    private final int INTERNET_ALLOTTED_CODE = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_watch_videos);

        init();

        videoNameText.setText(videoName);

        myRef.child(videoType).child(subject).child("approved").child(address).child("details").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ExoPlayer exoplayer = new ExoPlayer.Builder(getApplicationContext()).build();
                MediaSource mediaSource = new ProgressiveMediaSource.Factory(new DefaultHttpDataSource.Factory())
                        .createMediaSource(MediaItem.fromUri(Uri.parse(snapshot.child("videoUri").getValue(String.class))));
                exoplayer.setMediaSource(mediaSource);
                exoplayer.setPlayWhenReady(true);
                playerView.setPlayer(exoplayer);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("videoLoadUp", "onCancelled: " + error.getMessage());
            }
        });

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = queryEditText.getText().toString();
                if (!message.equals("")) {
                    SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
                    Date date = new Date();

                    HashMap<String, String> map = new HashMap<>();
                    map.put("sender", userDetails[1]);
                    map.put("senderName", userDetails[2]);
                    map.put("message", message);
                    myRef.child(videoType).child(subject).child("approved").child(address).child("forum").child(formatter.format(date)).setValue(map);
                    queryEditText.setText("");
                }

                try {
                    InputMethodManager imm = null;
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                        imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    }
                    imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                } catch (Exception e) {
                    Log.d("SoftKeyBoardDown", "onClick: " + e.getMessage());
                }
            }
        });

        likeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myRef.child("Users").child(userDetails[0]).child(userDetails[1]).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Boolean[] likeFlag = {false}, dislikeCountBalance = {false};
                        if (!snapshot.child("LikedVideos").child(address).exists()) {
                            myRef.child("Users").child(userDetails[0]).child(userDetails[1]).child("LikedVideos").child(address).setValue(0);
                            likeFlag[0] = true;
                            if (snapshot.child("DislikedVideos").child(address).exists()) {
                                myRef.child("Users").child(userDetails[0]).child(userDetails[1]).child("DislikedVideos").child(address).setValue(null);
                                dislikeCountBalance[0] = true;
                            }
                            myRef.child(videoType).child(subject).child("approved").child(address).child("details").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (likeFlag[0]) {
                                        HashMap<String, Object> map = new HashMap<>();
                                        int likesCount = snapshot.child("Likes").getValue(Integer.class) + 1;
                                        if (dislikeCountBalance[0]) {
                                            int dislikesCount = snapshot.child("Dislikes").getValue(Integer.class) - 1;
                                            map.put("Dislikes", dislikesCount);
                                            myRef.child(videoType).child(subject).child("approved").child(address).child("details").updateChildren(map);
                                            map.clear();
                                        }
                                        map.put("Likes", likesCount);
                                        myRef.child(videoType).child(subject).child("approved").child(address).child("details").updateChildren(map);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    Log.d("likes_dislikes", "onCancelled: " + error.getMessage());
                                }
                            });
                        } else {
                            myRef.child("Users").child(userDetails[0]).child(userDetails[1]).child("LikedVideos").child(address).setValue(null);
                            myRef.child(videoType).child(subject).child("approved").child(address).child("details").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    myRef.child(videoType).child(subject).child("approved").child(address).child("details").child("Likes").setValue(snapshot.child("Likes").getValue(Integer.class) - 1);
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    Log.d("likes_dislikes", "onCancelled: " + error.getMessage());
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.d("likes_dislikes", "onCancelled: " + error.getMessage());
                    }
                });
            }
        });

        dislikeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                myRef.child("Users").child(userDetails[0]).child(userDetails[1]).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Boolean[] dislikeFlag = {false}, likeCountBalance = {false};
                        if (!snapshot.child("DislikedVideos").child(address).exists()) {
                            myRef.child("Users").child(userDetails[0]).child(userDetails[1]).child("DislikedVideos").child(address).setValue(0);
                            dislikeFlag[0] = true;
                            if (snapshot.child("LikedVideos").child(address).exists()) {
                                myRef.child("Users").child(userDetails[0]).child(userDetails[1]).child("LikedVideos").child(address).setValue(null);
                                likeCountBalance[0] = true;
                            }
                            myRef.child(videoType).child(subject).child("approved").child(address).child("details").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (dislikeFlag[0]) {
                                        HashMap<String, Object> map = new HashMap<>();
                                        int likesCount, dislikesCount;
                                        dislikesCount = snapshot.child("Dislikes").getValue(Integer.class) + 1;
                                        likesCount = snapshot.child("Likes").getValue(Integer.class);
                                        if (likeCountBalance[0]) {
                                            likesCount -= 1;
                                            map.put("Likes", likesCount);
                                            myRef.child(videoType).child(subject).child("approved").child(address).child("details").updateChildren(map);
                                            map.clear();
                                        }
                                        map.put("Dislikes", dislikesCount);
                                        int finalLikesCount = likesCount;
                                        myRef.child(videoType).child(subject).child("approved").child(address).child("details").updateChildren(map).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                myRef.child("Users").child("userCount").addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                        numberOfUsers = snapshot.getValue(Integer.class);
                                                        Log.d("remOnDislike", "onDataChange: " + numberOfUsers + " " + (Math.log(numberOfUsers)) + " " + dislikesCount + " " + finalLikesCount);
                                                        if (dislikesCount > finalLikesCount && dislikesCount >= (Math.log(numberOfUsers))) {
                                                            myRef.child(videoType).child(subject).addListenerForSingleValueEvent(new ValueEventListener() {
                                                                @Override
                                                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                                    if (!snapshot.child("removeRequests").child(address).exists()) {
                                                                        HashMap<String, String> map = new HashMap<>();
                                                                        map.put("title", snapshot.child("approved").child(address).child("details").child("title").getValue(String.class));
                                                                        map.put("access", snapshot.child("approved").child(address).child("details").child("access").getValue(String.class));
                                                                        myRef.child(videoType).child(subject).child("removeRequests").child(address).setValue(map);
                                                                    }
                                                                }

                                                                @Override
                                                                public void onCancelled(@NonNull DatabaseError error) {
                                                                    Log.d("dislike", "onCancelled: " + error.getMessage());
                                                                }
                                                            });
                                                        }
                                                    }

                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError error) {
                                                        Log.d("gettingUserCount", "onCancelled: " + error.getMessage());
                                                    }
                                                });
                                            }
                                        });
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    Log.d("likes_dislikes", "onCancelled: " + error.getMessage());
                                }
                            });
                        } else {
                            myRef.child("Users").child(userDetails[0]).child(userDetails[1]).child("DislikedVideos").child(address).setValue(null);
                            myRef.child(videoType).child(subject).child("approved").child(address).child("details").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    myRef.child(videoType).child(subject).child("approved").child(address).child("details").child("Dislikes").setValue(snapshot.child("Dislikes").getValue(Integer.class) - 1);
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    Log.d("likes_dislikes", "onCancelled: " + error.getMessage());
                                }
                            });
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.d("likes_dislikes", "onCancelled: " + error.getMessage());
                    }
                });
            }
        });

        giveTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    Intent toGiveTest = new Intent(getApplicationContext(), TestPage.class);
                    toGiveTest.putExtra("userDetailsToGiveTestOnRespectiveVideo", userDetails);
                    toGiveTest.putExtra("addressOfLectureToGiveTestOnRespectiveVideo", address);
                    toGiveTest.putExtra("actionToBePerformedOnRespectiveVideo", "attempt");
                    toGiveTest.putExtra("subjectToGiveTestOnRespectiveVideo", subject);
                    startActivity(toGiveTest);
            }
        });
    }

    private void init() {
        database = FirebaseDatabase.getInstance("https://shiksha-37914-default-rtdb.asia-southeast1.firebasedatabase.app/");
        myRef = database.getReference();

        Intent fromVideos = getIntent();
        videoType = fromVideos.getStringExtra("videoTypeFromVideosToWatchVideos");
        subject = fromVideos.getStringExtra("subjectFromVideosToWatchVideos");
        userDetails = fromVideos.getStringArrayExtra("UserDetailsFromVideosToWatchVideos");
        videoName = fromVideos.getStringExtra("VideoTitleFromVideosToWatchVideos");

        if (videoType.equals("Lectures")) {
            address = "Lecture: " + videoName;
        } else {
            address = "Experiment: " + videoName;
        }

        playerView = findViewById(R.id.styledPlayerViewLectures);
        videoNameText = findViewById(R.id.videoNameText);
        likes = findViewById(R.id.likesCount);
        dislikes = findViewById(R.id.dislikesCount);
        likeButton = findViewById(R.id.likeButton);
        dislikeButton = findViewById(R.id.dislikeButton);
        queryEditText = findViewById(R.id.queryBox);
        sendButton = findViewById(R.id.btn_sendMessage);
        recyclerView = findViewById(R.id.recyclerViewMessages);
        giveTest = findViewById(R.id.giveTest);

        myRef.child("Users").child(userDetails[0]).child(userDetails[1]).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.child(subject + "VideosSeen").child(address).exists()) {
                    myRef.child("Users").child(userDetails[0]).child(userDetails[1]).child(subject + "VideosSeen").child(address).setValue(0);
                    myRef.child("Users").child(userDetails[0]).child(userDetails[1]).child("userDetails").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (!snapshot.child(subject + "VideosSeen").exists()) {
                                myRef.child("Users").child(userDetails[0]).child(userDetails[1]).child("userDetails").child(subject + "VideosSeen").setValue(1);
                            } else {
                                myRef.child("Users").child(userDetails[0]).child(userDetails[1]).child("userDetails").child(subject + "VideosSeen").setValue(snapshot.child(subject + "VideosSeen").getValue(Integer.class) + 1);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Log.d("watchVideos", "onCancelled: " + error.getMessage());
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("watchVideos", "onCancelled: " + error.getMessage());
            }
        });

        myRef.child(videoType).child(subject).child("approved").child(address).child("details").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                likes.setText(valueOf(snapshot.child("Likes").getValue(Integer.class)));
                dislikes.setText(valueOf(snapshot.child("Dislikes").getValue(Integer.class)));
                if (snapshot.child("question").exists()) {
                    giveTest.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("likes_dislikes", "onCancelled: " + error.getMessage());
            }
        });
        messagesList = new ArrayList<>();
        createMessagesList();
    }

    private void createMessagesList() {
        myRef.child(videoType).child(subject).child("approved").child(address).child("forum").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                HashMap<String, String> map = new HashMap<>();
                map.put("sender", snapshot.child("sender").getValue(String.class));
                map.put("senderName", snapshot.child("senderName").getValue(String.class));
                map.put("message", snapshot.child("message").getValue(String.class));
                messagesList.add(map);

                MessagesRecyclerViewAdapter messagesRecyclerViewAdapter = new MessagesRecyclerViewAdapter(messagesList, userDetails[1]);
                recyclerView = findViewById(R.id.recyclerViewMessages);
                recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                recyclerView.setAdapter(messagesRecyclerViewAdapter);
                recyclerView.scrollToPosition(messagesList.size() - 1);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("messageListGeneration", "onCancelled: " + error.getMessage());
            }
        });
    }
}