package com.example.shiksharemastered;

import static com.example.shiksharemastered.DataValidator.validateFirebaseAddress;
import static java.lang.String.valueOf;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.ui.StyledPlayerView;
import com.google.android.exoplayer2.upstream.DefaultDataSource;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;

public class UploadExperiments extends AppCompatActivity {

    private DatabaseReference myRef;
    private StorageReference myStorageRef;
    private Uri videoUri, downloadUri;
    private StyledPlayerView playerView;
    private ExoPlayer exoPlayer;
    private ProgressBar progressBar;
    private UploadTask uploadTask;
    private String subject, title, description, address;
    private String[] userDetails;
    private Button uploadButton, chooseButton;
    private TextView titleText, descriptionText;
    private EditText titleEditText, descriptionEditText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_experiments);

        init();

        ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if (result.getResultCode() == RESULT_OK) {
                    assert result.getData() != null;
                    videoUri = result.getData().getData();
                    playerView.setVisibility(View.VISIBLE);
                    exoPlayer = new ExoPlayer.Builder(getApplicationContext()).build();
                    MediaSource mediaSource = new ProgressiveMediaSource.Factory(new DefaultDataSource.Factory(getApplicationContext()))
                            .createMediaSource(MediaItem.fromUri(videoUri));
                    exoPlayer.setMediaSource(mediaSource);
                    exoPlayer.setPlayWhenReady(true);
                    playerView.setPlayer(exoPlayer);
                }
            }
        });

        chooseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent getVideo = new Intent();
                getVideo.setType("video/*");
                getVideo.setAction(Intent.ACTION_GET_CONTENT);
                activityResultLauncher.launch(getVideo);
            }
        });

        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadVideo();
            }
        });
    }

    private void uploadVideo() {
        if (validateEntriesForUpload()) {
            titleEditText.setClickable(false);
            titleEditText.setEnabled(false);
            descriptionEditText.setClickable(false);
            descriptionEditText.setEnabled(false);
            progressBar.setVisibility(View.VISIBLE);
            address = "Experiment: " + title;

            myRef.child(subject).child("approved").child(address).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        Toast.makeText(getApplicationContext(), "Title Already Used", Toast.LENGTH_SHORT).show();
                        titleEditText.setClickable(true);
                        titleEditText.setEnabled(true);
                        descriptionEditText.setClickable(true);
                        descriptionEditText.setEnabled(true);
                        progressBar.setVisibility(View.INVISIBLE);
                    } else {
                        StorageReference refStorage = myStorageRef.child(address).child(address);

                        myRef.child(subject).child("requests").child(address).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()) {
                                    Toast.makeText(getApplicationContext(), "Request already made", Toast.LENGTH_SHORT).show();
                                    titleEditText.setClickable(true);
                                    titleEditText.setEnabled(true);
                                    descriptionEditText.setClickable(true);
                                    descriptionEditText.setEnabled(true);
                                    progressBar.setVisibility(View.INVISIBLE);
                                } else {
                                    uploadTask = refStorage.putFile(videoUri);
                                    Task<Uri> uriTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                                        @Override
                                        public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                                            if (!task.isSuccessful()) {
                                                throw task.getException();
                                            }
                                            return refStorage.getDownloadUrl();
                                        }
                                    }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Uri> task) {
                                            if (task.isSuccessful()) {
                                                downloadUri = task.getResult();
                                                upload(address);
                                            }
                                        }
                                    });
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Log.d("lectureUploadError", "onCancelled: " + error.getMessage());
                            }
                        });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.d("lectureUploadError", "onCancelled: " + error.getMessage());
                }
            });

        } else {
            Toast.makeText(getApplicationContext(), "Try Again", Toast.LENGTH_SHORT).show();
        }
    }

    private void upload(String address) {
        HashMap<String, String> mainMap = new HashMap<>();
        mainMap.put("videoUri", downloadUri.toString());
        mainMap.put("uploader", userDetails[1]);
        mainMap.put("access", "Everyone");
        mainMap.put("title", title);
        mainMap.put("description", description);

        myRef.child(subject).child("requests").child(address).setValue(mainMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(getApplicationContext(), "Request Successful", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(), "Try Again Later", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private boolean validateEntriesForUpload() {
        boolean flag = videoUri != null;

        title = valueOf(titleEditText.getText()).trim();
        if (title.equals("") || !validateFirebaseAddress(title)) {
            titleText.setText(R.string.title_compulsory);
            flag = false;
        } else {
            titleText.setText(R.string.title);
        }

        description = String.valueOf(descriptionEditText.getText()).trim();
        if (description.equals("") || !validateFirebaseAddress(title)) {
            descriptionText.setText(R.string.description_compulsory);
            flag = false;
        } else {
            descriptionText.setText(R.string.description);
        }

        return flag;
    }

    private void init() {
        uploadButton = findViewById(R.id.btn_uploadExperiments);
        chooseButton = findViewById(R.id.btn_chooseVideoExperiments);

        titleText = findViewById(R.id.experimentTitleText);
        descriptionText = findViewById(R.id.descriptionText);

        titleEditText = findViewById(R.id.experimentTitle_editText);
        descriptionEditText = findViewById(R.id.descriptionEditText);

        Intent fromMainMenu = getIntent();
        userDetails = fromMainMenu.getStringArrayExtra("UserDetailsFromMainMenuToUploadExperiments");
        subject = fromMainMenu.getStringExtra("subjectNameFromMainMenuToUploadExperiments");

        playerView = findViewById(R.id.playerViewUploadExperiments);
        progressBar = findViewById(R.id.progressBarUploadVideoExperiments);

        FirebaseDatabase database = FirebaseDatabase.getInstance("https://shiksha-37914-default-rtdb.asia-southeast1.firebasedatabase.app/");
        myRef = database.getReference().child("Experiments");
        myStorageRef = FirebaseStorage.getInstance("gs://shiksha-37914.appspot.com").getReference();
    }
}