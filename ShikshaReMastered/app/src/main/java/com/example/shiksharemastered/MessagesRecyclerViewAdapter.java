package com.example.shiksharemastered;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashMap;

public class MessagesRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final ArrayList<HashMap<String, String>> messagesList;
    private TextView messageView, senderName;
    private final String usersGeneratedId;

    MessagesRecyclerViewAdapter(ArrayList<HashMap<String, String>> messagesList, String usersGeneratedId) {
        this.messagesList = messagesList;
        this.usersGeneratedId = usersGeneratedId;
    }

    @Override
    public int getItemViewType(int position)
    {
        if (usersGeneratedId.equals(messagesList.get(position).get("sender"))) {
            return 1;
        } else {
            return 0;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        RecyclerView.ViewHolder viewHolder;

        if (viewType == 1) {
            Log.d("chat", "onCreateViewHolder: right");
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_view_messages_right, parent, false);
            viewHolder = new ViewHolder(view);
        } else {
            Log.d("chat", "onCreateViewHolder: left");
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_view_messages_left, parent, false);
            viewHolder = new ViewHolder(view);
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        senderName.setText(messagesList.get(position).get("senderName"));
        messageView.setText(messagesList.get(position).get("message"));
    }

    @Override
    public int getItemCount() {
        Log.d("list", "getItemCount: " + messagesList.size());
        return messagesList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            senderName = itemView.findViewById(R.id.senderOfText);
            messageView = itemView.findViewById(R.id.textMessage);
        }
    }
}
