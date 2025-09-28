package com.example.shiksharemastered;

import static com.example.shiksharemastered.DataValidator.validateFirebaseAddress;
import static java.lang.String.valueOf;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.ui.StyledPlayerView;
import com.google.android.exoplayer2.upstream.DefaultDataSource;
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
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;

public class UploadLectures_Backup extends AppCompatActivity {
    private DatabaseReference myRef;
    private StorageReference myStorageRef;
    private Uri videoUri, fileUri, downloadUri;
    private ExoPlayer exoPlayer;
    private StyledPlayerView playerView;
    private ProgressBar progressBar;
    private UploadTask uploadTask;
    private Spinner studentTypeUser;
    private String subject, title, enteredStudentType;
    private String[] userDetails;
    private Button uploadButton, chooseVideoButton, chooseExcelButton;
    private TextView titleText, fileUploadConditions;
    private EditText titleEditText;
    private final String[] studentType = {"Enter student type: ", "Standard", "Premium"};
    private Switch switchForFile;
    private int toggle = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_lectures);

        init();

        ActivityResultLauncher<Intent> activityResultLauncherVideo = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
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

        ActivityResultLauncher<Intent> activityResultLauncherExcel = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if (result.getResultCode() == RESULT_OK) {
                    assert result.getData() != null;
                    fileUri = result.getData().getData();
                }
            }
        });

        switchForFile.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    toggle = 1;
                    fileUploadConditions.setVisibility(View.VISIBLE);
                    chooseExcelButton.setVisibility(View.VISIBLE);
                } else {
                    toggle = 0;
                    fileUploadConditions.setVisibility(View.GONE);
                    chooseExcelButton.setVisibility(View.GONE);
                }
            }
        });

        chooseVideoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent getVideo = new Intent();
                getVideo.setType("video/*");
                getVideo.setAction(Intent.ACTION_GET_CONTENT);
                activityResultLauncherVideo.launch(getVideo);
            }
        });

        chooseExcelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent getExcel = new Intent();
                getExcel.setType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet/*");
                getExcel.setAction(Intent.ACTION_GET_CONTENT);
                activityResultLauncherExcel.launch(getExcel);
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
            studentTypeUser.setEnabled(false);
            progressBar.setVisibility(View.VISIBLE);
            String address = "Lecture: " + title;

            myRef.child(subject).child("approved").child(address).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        Toast.makeText(getApplicationContext(), "Title Already Used", Toast.LENGTH_SHORT).show();
                        titleEditText.setClickable(true);
                        titleEditText.setEnabled(true);
                        studentTypeUser.setEnabled(true);
                        progressBar.setVisibility(View.INVISIBLE);
                    } else {
                        StorageReference refStorage = myStorageRef.child(address);

                        myRef.child(subject).child("requests").child(address).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()) {
                                    Toast.makeText(getApplicationContext(), "Request already made", Toast.LENGTH_SHORT).show();
                                    titleEditText.setClickable(true);
                                    titleEditText.setEnabled(true);
                                    studentTypeUser.setEnabled(true);
                                    progressBar.setVisibility(View.INVISIBLE);
                                } else {
                                    refStorage.child("Video").putFile(videoUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                            refStorage.child("Video").getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Uri> task) {
                                                    if (task.isSuccessful()) {
                                                        downloadUri = task.getResult();
                                                        if (toggle == 1) {
                                                            StorageMetadata metadata = new StorageMetadata.Builder()
                                                                    .setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                                                                    .build();
                                                            refStorage.child("Quiz").putFile(fileUri, metadata).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                                                    refStorage.child("Quiz").getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Uri> task) {
                                                                            if (task.isSuccessful()) {
                                                                                fileUri = task.getResult();
                                                                                upload(address);
                                                                            } else {
                                                                                myRef.child(subject).child("requests").child(address).setValue(null);
                                                                                refStorage.delete();
                                                                                Toast.makeText(getApplicationContext(), "Failed", Toast.LENGTH_SHORT).show();
                                                                                finish();
                                                                            }
                                                                        }
                                                                    });
                                                                }
                                                            });
                                                        } else {
                                                            upload(address);
                                                        }
                                                    } else {
                                                        Toast.makeText(getApplicationContext(), "Failed", Toast.LENGTH_SHORT).show();
                                                        finish();
                                                    }
                                                }
                                            });
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
        if (toggle == 1) {
            mainMap.put("fileUri", fileUri.toString());
        }
        mainMap.put("access", enteredStudentType);
        mainMap.put("uploader", userDetails[1]);
        mainMap.put("title", title);

        myRef.child(subject).child("requests").child(address).setValue(mainMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(getApplicationContext(), "Request Made Successfully", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        myStorageRef.child(address).delete();
                        Toast.makeText(getApplicationContext(), "Try Again Later", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private boolean validateEntriesForUpload() {
        boolean flag = videoUri != null;

        enteredStudentType = studentTypeUser.getSelectedItem().toString();
        if (enteredStudentType.equals("Enter student type: ")) {
            flag = false;
        }

        title = valueOf(titleEditText.getText());
        if (title.equals("") || !validateFirebaseAddress(title)) {
            titleText.setText(R.string.title_compulsory);
            flag = false;
        } else {
            titleText.setText(R.string.title);
        }

        return flag;
    }

    private void init() {
        uploadButton = findViewById(R.id.btn_uploadLectures);
        chooseVideoButton = findViewById(R.id.btn_chooseVideoLectures);
        chooseExcelButton = findViewById(R.id.btn_chooseExcel);
        switchForFile = findViewById(R.id.switchForFile);

        titleText = findViewById(R.id.lectureTitleText);
        fileUploadConditions = findViewById(R.id.conditionsForUpload);

        titleEditText = findViewById(R.id.lectureTitle_editText);

        Intent fromMainMenu = getIntent();
        userDetails = fromMainMenu.getStringArrayExtra("UserDetailsFromMainMenuToUploadLectures");
        subject = fromMainMenu.getStringExtra("subjectNameFromMainMenuToUploadLectures");

        playerView = findViewById(R.id.playerViewUploadLectures);
        progressBar = findViewById(R.id.progressBarUploadVideoLectures);

        studentTypeUser = findViewById(R.id.studentTypeSpinner);
        ArrayAdapter<String> userAdapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.spinner_item_text_view, studentType);
        userAdapter.setDropDownViewResource(R.layout.spinner_item_text_view);
        studentTypeUser.setAdapter(userAdapter);

        FirebaseDatabase database = FirebaseDatabase.getInstance("https://shiksha-37914-default-rtdb.asia-southeast1.firebasedatabase.app/");
        myRef = database.getReference().child("Lectures");
        myStorageRef = FirebaseStorage.getInstance("gs://shiksha-37914.appspot.com").getReference();
    }
}