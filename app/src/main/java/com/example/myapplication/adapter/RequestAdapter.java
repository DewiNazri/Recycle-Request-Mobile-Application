package com.example.myapplication.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.model.Request;

import java.util.List;

public class RequestAdapter extends RecyclerView.Adapter<RequestAdapter.RequestViewHolder> {

    private List<Request> requestList;
    private final OnRequestClickListener listener;

    public interface OnRequestClickListener {
        void onView(Request request);
    }

    public RequestAdapter(List<Request> requestList, OnRequestClickListener listener) {
        this.requestList = requestList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_request, parent, false);
        return new RequestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RequestViewHolder holder, int position) {
        Request request = requestList.get(position);
        holder.tvRequestId.setText("Request ID: #" + request.getRequestId());
        holder.tvItemId.setText("Item ID: " + request.getItemId());
        holder.tvPrice.setText("Price: RM " + request.getTotalPrice());
        holder.tvStatus.setText(request.getStatus());

        if ("Pending".equalsIgnoreCase(request.getStatus())) {
            holder.tvStatus.setTextColor(Color.parseColor("#FFA500")); // Orange
        } else if ("Approved".equalsIgnoreCase(request.getStatus())) {
            holder.tvStatus.setTextColor(Color.parseColor("#4CAF50")); // Green
        }

        holder.btnView.setOnClickListener(v -> listener.onView(request));
    }

    @Override
    public int getItemCount() {
        return requestList.size();
    }

    static class RequestViewHolder extends RecyclerView.ViewHolder {
        TextView tvRequestId, tvItemId, tvPrice, tvStatus;
        Button btnView;

        public RequestViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRequestId = itemView.findViewById(R.id.tvRequestId);
            tvItemId = itemView.findViewById(R.id.tvItemName);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            btnView = itemView.findViewById(R.id.btnView);
        }
    }
}
