package com.example.shiksharemastered;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.ui.StyledPlayerView;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ConsentRecyclerViewAdapter extends RecyclerView.Adapter<ConsentRecyclerViewAdapter.ViewHolder> {
    private final Context context;
    private final ArrayList<Map<String, String>> requests;
    private TextView videoTitleDisplay, videoAccessDisplay;
    private final String videoType, action, subject;

    ConsentRecyclerViewAdapter(Context context, String subject, String videoType, ArrayList<Map<String, String>> requests, String action) {
        this.context = context;
        this.subject = subject;
        this.requests = requests;
        this.videoType = videoType;
        this.action = action;
    }

    @NonNull
    @Override
    public ConsentRecyclerViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.consent_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ConsentRecyclerViewAdapter.ViewHolder holder, int position) {
        String title = (position + 1) + ". " + requests.get(position).get("title");
        videoTitleDisplay.setText(title);
        videoAccessDisplay.setText(requests.get(position).get("access"));
    }

    @Override
    public int getItemCount() {
        return requests.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            init();

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    Intent toConsentDetails = new Intent(context, ConsentDetailedView.class);
                    toConsentDetails.putExtra("videoTypeFromListToDetailedView", videoType);
                    toConsentDetails.putExtra("subjectFromListToDetailedView", subject);
                    toConsentDetails.putExtra("titleFromListToDetailedView", requests.get(position).get("title"));
                    toConsentDetails.putExtra("actionFromListToDetailedView", action);
                    context.startActivity(toConsentDetails);
                }
            });
        }

        private void init() {
            videoTitleDisplay = itemView.findViewById(R.id.videoTitle);
            videoAccessDisplay = itemView.findViewById(R.id.videoAccess);
        }
    }
}
