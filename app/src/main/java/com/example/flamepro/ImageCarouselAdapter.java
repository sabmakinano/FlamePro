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
    private List<String> galleryImages;

    public ImageCarouselAdapter(List<Integer> images) {
        this(images, null, null);
    }

    public ImageCarouselAdapter(List<Integer> images, String imageUrl) {
        this(images, imageUrl, null);
    }

    public ImageCarouselAdapter(List<Integer> images, String imageUrl, List<String> galleryImages) {
        this.images = images != null ? images : new ArrayList<>();
        this.imageUrl = imageUrl;
        this.galleryImages = galleryImages != null ? galleryImages : new ArrayList<>();
        // If galleryImages is empty but imageUrl is present, add imageUrl to galleryImages
        if (this.galleryImages.isEmpty() && imageUrl != null && !imageUrl.trim().isEmpty()) {
            this.galleryImages.add(imageUrl);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_carousel, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (!galleryImages.isEmpty() && position < galleryImages.size()) {
            String url = galleryImages.get(position);
            int placeholder = !images.isEmpty() ? images.get(Math.min(position, images.size() - 1)) : R.drawable.logo;
            ImageLoader.load(holder.imageView, url, placeholder);
        } else if (position < images.size()) {
            holder.imageView.setImageResource(images.get(position));
        } else {
            holder.imageView.setImageResource(R.drawable.logo);
        }
    }

    @Override
    public int getItemCount() {
        if (!galleryImages.isEmpty()) {
            return galleryImages.size();
        }
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
