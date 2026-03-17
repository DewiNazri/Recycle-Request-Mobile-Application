package com.example.myapplication.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.EditItem;
import com.example.myapplication.R;
import com.example.myapplication.model.RecyclableItem;

import java.util.List;

public class RecyclableItemAdapter extends RecyclerView.Adapter<RecyclableItemAdapter.ViewHolder> {

    private final Context context;
    private final List<RecyclableItem> itemList;

    public RecyclableItemAdapter(Context context, List<RecyclableItem> itemList) {
        this.context = context;
        this.itemList = itemList;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvItemId, tvItemName, tvPrice;
        Button btnEdit;

        public ViewHolder(View itemView) {
            super(itemView);
            tvItemId = itemView.findViewById(R.id.tvItemId);
            tvItemName = itemView.findViewById(R.id.tvItemName);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            btnEdit = itemView.findViewById(R.id.btnEdit);
        }
    }

    @NonNull
    @Override
    public RecyclableItemAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclableItemAdapter.ViewHolder holder, int position) {
        RecyclableItem item = itemList.get(position);
        holder.tvItemId.setText("Item ID: " + item.getItemId());
        holder.tvItemName.setText("Item: " + item.getItemName());

        // ✅ Format price with 2 decimal places
        holder.tvPrice.setText(String.format("Price: RM%.2f", item.getPricePerKg()));

        holder.btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(context, EditItem.class);
            intent.putExtra("item_id", item.getItemId());
            intent.putExtra("item_name", item.getItemName());
            intent.putExtra("item_price", item.getPricePerKg());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }
}
