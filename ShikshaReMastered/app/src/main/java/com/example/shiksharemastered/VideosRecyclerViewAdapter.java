package com.example.shiksharemastered;

import static androidx.core.content.ContextCompat.startActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class VideosRecyclerViewAdapter extends RecyclerView.Adapter<VideosRecyclerViewAdapter.ViewHolder> {
    private final Context context;
    private final ArrayList<Map<String, String>> videos;
    private ImageButton playVideo, videoInfo;
    private final String[] userDetails;
    private final String subject, videoType;
    private TextView videoTitle;
    private String address;
    private DatabaseReference myRef;

    VideosRecyclerViewAdapter(Context context, ArrayList<Map<String, String>> videos, String[] userDetails, String subject, String videoType) {
        this.context = context;
        this.videos = videos;
        this.userDetails = userDetails;
        this.subject = subject;
        this.videoType = videoType;
        myRef = FirebaseDatabase.getInstance().getReference().child(videoType).child(subject).child("approved");
    }

    @NonNull
    @Override
    public VideosRecyclerViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_view_videos, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VideosRecyclerViewAdapter.ViewHolder holder, int position) {
        Map<String, String> video = videos.get(position);
        if (userDetails[1].equals(video.get("uploader"))) {
            videoInfo.setVisibility(View.VISIBLE);
        }
        String lectureTitle = (position + 1) + ". " + video.get("title");
        videoTitle.setText(lectureTitle);
    }

    @Override
    public int getItemCount() {
        Log.d("listProblem", "getItemCount: " + videos.size());
        return videos.size();
    }

    private void redirectToWatchVideos(int position) {
        Intent watchVideo = new Intent(context, WatchVideos.class);
        watchVideo.putExtra("videoTypeFromVideosToWatchVideos", videoType);
        watchVideo.putExtra("subjectFromVideosToWatchVideos", subject);
        watchVideo.putExtra("UserDetailsFromVideosToWatchVideos", userDetails);
        watchVideo.putExtra("VideoTitleFromVideosToWatchVideos", videos.get(position).get("title"));
        watchVideo.putExtra("VideoURLFromVideosToWatchVideos", videos.get(position).get("videoUri"));
        context.startActivity(watchVideo);
    }

    private void redirectToViewInfo(int position) {
        Intent details = new Intent(context, VideoDetailedView.class);
        details.putExtra("videoTypeFromListToDetailedView", videoType);
        details.putExtra("subjectFromListToDetailedView", subject);
        details.putExtra("titleFromListToDetailedView", videos.get(position).get("title"));
        context.startActivity(details);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            videoTitle = itemView.findViewById(R.id.lectureTitle);
            playVideo = itemView.findViewById(R.id.videoPlayButton);
            videoInfo = itemView.findViewById(R.id.videoInfoButton);

            playVideo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    redirectToWatchVideos(position);
                }
            });

            videoInfo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    redirectToViewInfo(position);
                }
            });
        }
    }
}
