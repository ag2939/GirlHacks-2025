package com.example.shiksharemastered;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.ui.StyledPlayerView;
import com.google.android.exoplayer2.upstream.DefaultDataSource;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource;
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

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellUtil;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;

public class VideoDetailedView extends AppCompatActivity {
    private TextView titleForVideo, uploaderNameText, testPreviewText;
    private Button previewTest, chooseAnotherVideo, chooseAnotherExcel, confirmChanges, deleteLecture, back;
    private Uri fileUri, videoUri;
    private boolean isAnotherFileChosen = false, isAnotherVideoChosen = false, fileUploadPossible = true, videoUploadPossible = true;
    private StyledPlayerView playerView;
    private HashMap<String, Object> questionMap;
    private ExoPlayer exoPlayer;
    private String videoType, subject, address, title, access;
    private DatabaseReference myRef;
    private StorageReference myStorageRef;
    private Switch changeOption;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_detailed_view);

        init();

        titleForVideo.setText(title);
        myRef.child("").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                uploaderNameText.setText(snapshot.child("uploader").getValue(String.class));
                Log.d("null?", "onDataChange: " + snapshot.child("videoUri").exists());
                videoUri = Uri.parse(snapshot.child("videoUri").getValue(String.class));
                try {
                    exoPlayer = new ExoPlayer.Builder(getApplicationContext()).build();
                    MediaSource mediaSource = new ProgressiveMediaSource.Factory(new DefaultHttpDataSource.Factory())
                            .createMediaSource(MediaItem.fromUri(videoUri));
                    exoPlayer.setMediaSource(mediaSource);
                    exoPlayer.setPlayWhenReady(true);
                    playerView.setPlayer(exoPlayer);
                } catch (NullPointerException e) {
                    Log.d("setPlayer", "onDataChange: " + e.getMessage());
                }
                if (snapshot.child("fileUri").exists()) {
                    fileUri = Uri.parse(snapshot.child("fileUri").getValue(String.class));
                    previewTest.setVisibility(View.VISIBLE);
                    testPreviewText.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("initializing", "onCancelled: " + error.getMessage());
            }
        });

        ActivityResultLauncher<Intent> activityResultLauncherVideo = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if (result.getResultCode() == RESULT_OK) {
                    assert result.getData() != null;
                    isAnotherVideoChosen = true;
                    videoUri = result.getData().getData();
                    MediaSource mediaSource1 = new ProgressiveMediaSource.Factory(new DefaultDataSource.Factory(getApplicationContext())).createMediaSource(MediaItem.fromUri(videoUri));
                    exoPlayer.setMediaSource(mediaSource1);
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
                    isAnotherFileChosen = true;
                    fileUri = result.getData().getData();
                }
            }
        });

        chooseAnotherVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent getVideo = new Intent();
                getVideo.setType("video/*");
                getVideo.setAction(Intent.ACTION_GET_CONTENT);
                activityResultLauncherVideo.launch(getVideo);
            }
        });

        chooseAnotherExcel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent getExcel = new Intent();
                getExcel.setType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet/*");
                getExcel.setAction(Intent.ACTION_GET_CONTENT);
                activityResultLauncherExcel.launch(getExcel);
            }
        });

        changeOption.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    previewTest.setVisibility(View.GONE);
                    testPreviewText.setText(R.string.change);
                    chooseAnotherVideo.setVisibility(View.VISIBLE);
                    chooseAnotherExcel.setVisibility(View.VISIBLE);
                    confirmChanges.setVisibility(View.VISIBLE);
                } else {
                    previewTest.setVisibility(View.VISIBLE);
                    testPreviewText.setText(R.string.preview_btn_guideline);
                    chooseAnotherVideo.setVisibility(View.GONE);
                    chooseAnotherExcel.setVisibility(View.GONE);
                    confirmChanges.setVisibility(View.GONE);
                }
            }
        });

        deleteLecture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteRequest();
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        confirmChanges.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isAnotherFileChosen) {
                    fileChanged();
                }
                if (isAnotherVideoChosen) {
                    videoChanged();
                }
                if (videoUploadPossible && fileUploadPossible) {
                    if (isAnotherFileChosen && !isAnotherVideoChosen) {
                        myRef.child("question").setValue(questionMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Toast.makeText(getApplicationContext(), "Changed Quiz", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else if (isAnotherVideoChosen && !isAnotherFileChosen) {
                        pushVideo();
                        finish();
                    } else if (isAnotherVideoChosen && isAnotherFileChosen) {
                        myRef.child("question").setValue(questionMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                pushVideo();
                                finish();
                            }
                        });
                    } else {
                        Toast.makeText(VideoDetailedView.this, "Nothing to change", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    private void pushVideo() {
        myStorageRef.child("Video").putFile(videoUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if (task.isSuccessful()) {
                    myStorageRef.child("Video").getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            videoUri = task.getResult();
                            myRef.child("videoUri").setValue(videoUri.toString()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    Toast.makeText(getApplicationContext(), "Successful", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    });
                } else {
                    Toast.makeText(getApplicationContext(), "Change Video Failed", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void fileChanged() {
        questionMap = new HashMap<>();
        try {
            InputStream inputStream = getContentResolver().openInputStream(fileUri);
            Workbook workbook = new XSSFWorkbook(inputStream);
            Sheet sheet = workbook.getSheetAt(0);

            int index;
            String question = "";
            HashMap<String, String> optionMap;
            for (Row row : sheet) {
                if (row != CellUtil.getRow(0, sheet)) {
                    optionMap = new HashMap<>();
                    index = 0;
                    for (Cell cell : row) {
                        if (cell.getColumnIndex() >= 1) {
                            if (cell.getCellType() == Cell.CELL_TYPE_STRING) {
                                optionMap.put("Option" + index, cell.getStringCellValue());
                            } else if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
                                optionMap.put("Option" + index, String.valueOf(cell.getNumericCellValue()));
                            }
                        } else {
                            if (cell.getCellType() == Cell.CELL_TYPE_STRING) {
                                question = cell.getStringCellValue();
                            } else if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
                                question = String.valueOf(cell.getNumericCellValue());
                            }
                        }
                        index++;
                    }
                    questionMap.put(question, optionMap);
                    inputStream.close();
                }
            }
        } catch (Exception e) {
            fileUploadPossible = false;
            e.printStackTrace();
        }
    }

    private void videoChanged() {
        myStorageRef.child("Video").putFile(videoUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                myStorageRef.child("Video").getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        videoUri = task.getResult();
                        myRef.child("videoUri").setValue(videoUri.toString())
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        videoUploadPossible = false;
                                        myStorageRef.delete();
                                    }
                                });
                    }
                });
            }
        });
    }

    private void deleteRequest() {
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                access = snapshot.child("access").getValue(String.class);
                HashMap<String, String> map = new HashMap<>();
                map.put("title", title);
                map.put("access", access);
                FirebaseDatabase.getInstance("https://shiksha-37914-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference(videoType).child(subject).child("removeRequests").child(address).setValue(map).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(getApplicationContext(), "Deletion Requested", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("retrieveAccess", "onCancelled: " + error.getMessage());
            }
        });
    }

    private void init() {
        titleForVideo = findViewById(R.id.titleForVideo);
        uploaderNameText = findViewById(R.id.uploaderNameText);
        testPreviewText = findViewById(R.id.previewTest);

        previewTest = findViewById(R.id.btn_previewTest);
        chooseAnotherVideo = findViewById(R.id.btn_chooseAnotherVideo);
        chooseAnotherExcel = findViewById(R.id.btn_chooseAnotherExcel);
        back = findViewById(R.id.btn_back);
        confirmChanges = findViewById(R.id.btn_confirmChanges);
        deleteLecture = findViewById(R.id.btn_deleteLecture);

        changeOption = findViewById(R.id.changeOption);

        playerView = findViewById(R.id.styledPlayerDetailedVideoPreview);

        Intent fromVideosList = getIntent();
        videoType = fromVideosList.getStringExtra("videoTypeFromListToDetailedView");
        subject = fromVideosList.getStringExtra("subjectFromListToDetailedView");
        title = fromVideosList.getStringExtra("titleFromListToDetailedView");

        if (videoType.equals("Lectures")) {
            address = "Lecture: " + title;
        } else {
            address = "Experiment: " + title;
        }
        myRef = FirebaseDatabase.getInstance("https://shiksha-37914-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference().child(videoType).child(subject).child("approved").child(address).child("details");
        myStorageRef = FirebaseStorage.getInstance("gs://shiksha-37914.appspot.com/").getReference().child(address);
    }
}