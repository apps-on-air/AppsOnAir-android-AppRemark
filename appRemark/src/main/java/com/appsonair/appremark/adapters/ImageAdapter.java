package com.appsonair.appremark.adapters;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.appsonair.appremark.R;
import com.appsonair.appremark.interfaces.OnItemClickListener;
import com.appsonair.appremark.models.ImageData;
import com.bumptech.glide.Glide;

import java.util.List;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ViewHolder> {
    private final List<ImageData> imageList;
    private final OnItemClickListener onItemClickListener;

    public ImageAdapter(List<ImageData> imageList, OnItemClickListener onItemClickListener) {
        this.imageList = imageList;
        this.onItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_image, parent, false);
        return new ViewHolder(view, onItemClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Uri imagePath = imageList.get(position).getImageUri();

        Glide.with(holder.itemView.getContext())
                .load(imagePath)
                .into(holder.imgBug);
        holder.imgBug.setClipToOutline(true);
    }

    @Override
    public int getItemCount() {
        return imageList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgBug;
        ImageView imgRemove;

        ViewHolder(View itemView, OnItemClickListener itemClickListener) {
            super(itemView);
            imgBug = itemView.findViewById(R.id.img_bug);
            imgRemove = itemView.findViewById(R.id.img_remove);
            imgRemove.setOnClickListener(view -> {
                if (itemClickListener != null) {
                    int position = getAbsoluteAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        itemClickListener.onItemClick(position);
                    }
                }
            });
        }
    }
}
