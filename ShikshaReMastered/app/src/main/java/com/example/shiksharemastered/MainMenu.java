package com.example.shiksharemastered;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class MainMenu extends AppCompatActivity {

    private ImageView subject1, subject2;
    private Button videosButton, progressButton, uploadButton, removeButton;
    private String[] userDetails;
    private HorizontalScrollView horizontalScrollView;
    private Spinner typesOfVideos;
    private int tag = 0, videoTag = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        init();

        horizontalScrollView.setSmoothScrollingEnabled(true);

        videosButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (tag == 1) {
                    Intent toMathPage = new Intent(getApplicationContext(), MathPage.class);
                    if (videoTag == 0) {
                        toMathPage.putExtra("videoTypeFromMainMenuToMathPage", "Lectures");
                    } else {
                        toMathPage.putExtra("videoTypeFromMainMenuToMathPage", "Experiments");
                    }
                    toMathPage.putExtra("subjectNameFromMainMenuToMathPage", "Maths");
                    toMathPage.putExtra("UserDetailsFromMainMenuToMathPage", userDetails);
                    startActivity(toMathPage);
                } else if (tag == 2) {
                    Intent toEVSPage = new Intent(getApplicationContext(), EVSPage.class);
                    if (videoTag == 0) {
                        toEVSPage.putExtra("videoTypeFromMainMenuToEVSPage", "Lectures");
                    } else {
                        toEVSPage.putExtra("videoTypeFromMainMenuToEVSPage", "Experiments");
                    }
                    toEVSPage.putExtra("subjectNameFromMainMenuToEVSPage", "EVS");
                    toEVSPage.putExtra("UserDetailsFromMainMenuToEVSPage", userDetails);
                    startActivity(toEVSPage);
                } else {
                    Toast.makeText(getApplicationContext(), "Select Subject", Toast.LENGTH_SHORT).show();
                }
            }
        });

        progressButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent toProgressPage = new Intent(getApplicationContext(), ProgressPage.class);
                toProgressPage.putExtra("userDetailsFromMainMenuToProgressPage", userDetails);
                if (tag == 1) {
                    toProgressPage.putExtra("subjectNameFromMainMenuToProgressPage", "Maths");
                    startActivity(toProgressPage);
                } else if (tag == 2) {
                    toProgressPage.putExtra("subjectNameFromMainMenuToProgressPage", "EVS");
                    startActivity(toProgressPage);
                } else {
                    Toast.makeText(getApplicationContext(), "Select Subject", Toast.LENGTH_SHORT).show();
                }
            }
        });

        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (userDetails[0].equals("Teacher")) {
                    if (videoTag == 1) {
                        Intent toUploadExperimentsPage = new Intent(getApplicationContext(), UploadExperiments.class);
                        toUploadExperimentsPage.putExtra("UserDetailsFromMainMenuToUploadExperiments", userDetails);
                        if (tag == 1) {
                            toUploadExperimentsPage.putExtra("subjectNameFromMainMenuToUploadExperiments", "Maths");
                            startActivity(toUploadExperimentsPage);
                        } else if (tag == 2) {
                            toUploadExperimentsPage.putExtra("subjectNameFromMainMenuToUploadExperiments", "EVS");
                            startActivity(toUploadExperimentsPage);
                        } else {
                            Toast.makeText(getApplicationContext(), "Select Subject", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Intent toUploadLecturesPage = new Intent(getApplicationContext(), UploadLectures.class);
                        toUploadLecturesPage.putExtra("UserDetailsFromMainMenuToUploadLectures", userDetails);
                        if (tag == 1) {
                            toUploadLecturesPage.putExtra("subjectNameFromMainMenuToUploadLectures", "Maths");
                            startActivity(toUploadLecturesPage);
                        } else if (tag == 2) {
                            toUploadLecturesPage.putExtra("subjectNameFromMainMenuToUploadLectures", "EVS");
                            startActivity(toUploadLecturesPage);
                        } else {
                            Toast.makeText(getApplicationContext(), "Select Subject", Toast.LENGTH_SHORT).show();
                        }
                    }
                } else if (userDetails[0].equals("admin")){
                    Intent toUploadRequestSection = new Intent(getApplicationContext(), UploadRequests.class);
                    if (videoTag == 0) {
                        toUploadRequestSection.putExtra("videoTypeFromMainMenuToUploadRequests", "Lectures");
                    } else {
                        toUploadRequestSection.putExtra("videoTypeFromMainMenuToUploadRequests", "Experiments");
                    }
                    if (tag == 1) {
                        toUploadRequestSection.putExtra("subjectNameFromMainMenuToUploadRequests", "Maths");
                        startActivity(toUploadRequestSection);
                    } else if (tag == 2) {
                        toUploadRequestSection.putExtra("subjectNameFromMainMenuToUploadRequests", "EVS");
                        startActivity(toUploadRequestSection);
                    } else {
                        Toast.makeText(getApplicationContext(), "Select Subject", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Intent toUploadExperimentsPage = new Intent(getApplicationContext(), UploadExperiments.class);
                    toUploadExperimentsPage.putExtra("UserDetailsFromMainMenuToUploadExperiments", userDetails);
                    if (tag == 1) {
                        toUploadExperimentsPage.putExtra("subjectNameFromMainMenuToUploadExperiments", "Maths");
                        startActivity(toUploadExperimentsPage);
                    } else if (tag == 2) {
                        toUploadExperimentsPage.putExtra("subjectNameFromMainMenuToUploadExperiments", "EVS");
                        startActivity(toUploadExperimentsPage);
                    } else {
                        Toast.makeText(getApplicationContext(), "Select Subject", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        removeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent toRemoveVideosPage = new Intent(getApplicationContext(), RemoveVideos.class);
                if (videoTag == 0) {
                    toRemoveVideosPage.putExtra("videoTypeFromMainMenuToRemoveVideos", "Lectures");
                } else {
                    toRemoveVideosPage.putExtra("videoTypeFromMainMenuToRemoveVideos", "Experiments");
                }
                if (tag == 1) {
                    toRemoveVideosPage.putExtra("subjectNameFromMainMenuToRemoveVideos", "Maths");
                    startActivity(toRemoveVideosPage);
                } else if (tag == 2) {
                    toRemoveVideosPage.putExtra("subjectNameFromMainMenuToRemoveVideos", "EVS");
                    startActivity(toRemoveVideosPage);
                } else {
                    Toast.makeText(getApplicationContext(), "Select Subject", Toast.LENGTH_SHORT).show();
                }
            }
        });

        typesOfVideos.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    videoTag = 0;
                    if (!userDetails[0].equals("Teacher") && !userDetails[0].equals("admin")) {
                        uploadButton.setVisibility(View.GONE);
                    }
                } else if (position == 1) {
                    videoTag = 1;
                    uploadButton.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void init() {
        videosButton = findViewById(R.id.btn_videos);
        progressButton = findViewById(R.id.btn_progress);
        uploadButton = findViewById(R.id.btn_upload);
        removeButton = findViewById(R.id.btn_remove);

        subject1 = findViewById(R.id.optionImage1);
        subject2 = findViewById(R.id.optionImage2);

        horizontalScrollView = findViewById(R.id.horizontalScrollView);

        String[] availableVideoTypes = {"Lecture Videos", "Experiment Videos"};
        typesOfVideos = findViewById(R.id.videoTypeSpinner);
        ArrayAdapter<String> videoAdapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.spinner_item_text_view, availableVideoTypes);
        videoAdapter.setDropDownViewResource(R.layout.spinner_item_text_view);
        typesOfVideos.setAdapter(videoAdapter);

        Intent fromSignUpLoginPage = getIntent();
        userDetails = fromSignUpLoginPage.getStringArrayExtra("iAmAStringOfUserDetailsToGameMenu");
        if (userDetails[0].equals("Teacher")) {
            uploadButton.setText(R.string.upload);
            uploadButton.setVisibility(View.VISIBLE);
        } else if (userDetails[0].equals("admin")) {
            removeButton.setVisibility(View.VISIBLE);
            uploadButton.setText(R.string.requests);
            uploadButton.setVisibility(View.VISIBLE);
        } else {
            uploadButton.setText(R.string.upload);
            progressButton.setVisibility(View.VISIBLE);
        }
    }

    public void imageTapped(View view) {
        tag = Integer.parseInt(view.getTag().toString());
        if (tag == 1) {
            horizontalScrollView.smoothScrollTo(0, 0);
            subject1.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.subject_options_selected));
            subject2.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.subject_options_border));
        } else if (tag == 2) {
            horizontalScrollView.smoothScrollTo(horizontalScrollView.getWidth(), 0);
            subject1.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.subject_options_border));
            subject2.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.subject_options_selected));
        }
    }
}