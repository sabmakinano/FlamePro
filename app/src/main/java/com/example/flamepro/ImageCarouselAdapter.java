package com.example.flamepro;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class ImageCarouselAdapter extends RecyclerView.Adapter<ImageCarouselAdapter.ViewHolder> {

    private List<Integer> images;
    private String imageUrl;

    public ImageCarouselAdapter(List<Integer> images) {
        this(images, null);
    }

    public ImageCarouselAdapter(List<Integer> images, String imageUrl) {
        this.images = images != null ? images : new ArrayList<>();
        this.imageUrl = imageUrl;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_carousel, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (imageUrl != null && !imageUrl.trim().isEmpty() && position == 0) {
            int placeholder = images.isEmpty() ? R.drawable.logo : images.get(0);
            ImageLoader.load(holder.imageView, imageUrl, placeholder);
        } else if (position < images.size()) {
            holder.imageView.setImageResource(images.get(position));
        } else {
            holder.imageView.setImageResource(R.drawable.logo);
        }
    }

    @Override
    public int getItemCount() {
        if (imageUrl != null && !imageUrl.trim().isEmpty()) {
            return Math.max(1, images.size());
        }
        return images.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.ivCarouselImage);
        }
    }
}
