package com.example.myapplication.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.model.Announcement;

import java.util.List;

public class AnnouncementAdapter extends RecyclerView.Adapter<AnnouncementAdapter.ViewHolder> {

    private List<Announcement> announcementList;
    private Context context;

    // Toggle: simple or card mode
    private boolean simpleMode = false;

    public AnnouncementAdapter(Context context, List<Announcement> announcements) {
        this.context = context;
        this.announcementList = announcements;
    }

    public void setSimpleMode(boolean mode) {
        this.simpleMode = mode;
    }

    public void updateData(List<Announcement> newList) {
        this.announcementList = newList;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView tvTitle, tvMessage, tvOlder;

        public ViewHolder(View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvAnnouncementTitle);
            tvMessage = itemView.findViewById(R.id.tvAnnouncementMessage);
            tvOlder = itemView.findViewById(R.id.tvOlderAnnouncement);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (simpleMode) {
            view = LayoutInflater.from(context).inflate(R.layout.item_announcement_simple, parent, false);
        } else {
            view = LayoutInflater.from(context).inflate(R.layout.ann_card, parent, false);
        }
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Announcement ann = announcementList.get(position);
        if (simpleMode) {
            holder.tvOlder.setText(ann.getTitle() + ": " + ann.getMessage());
        } else {
            holder.tvTitle.setText(ann.getTitle());
            holder.tvMessage.setText(ann.getMessage());
        }
    }

    @Override
    public int getItemCount() {
        return announcementList.size();
    }
}

