package com.example.flamepro;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.util.LruCache;
import android.widget.ImageView;

import com.example.flamepro.network.ApiClient;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ImageLoader {

    private static final ExecutorService executor = Executors.newFixedThreadPool(4);
    private static final Handler mainHandler = new Handler(Looper.getMainLooper());

    private static final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
    private static final int cacheSize = maxMemory / 8;
    private static final LruCache<String, Bitmap> memoryCache = new LruCache<String, Bitmap>(cacheSize) {
        @Override
        protected int sizeOf(String key, Bitmap bitmap) {
            return bitmap.getByteCount() / 1024;
        }
    };

    public static void load(ImageView imageView, String imageUrl, int placeholderRes) {
        if (imageView == null) return;

        // Set placeholder first
        if (placeholderRes != 0) {
            imageView.setImageResource(placeholderRes);
        }

        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            return;
        }

        final String fullUrl;
        if (imageUrl.startsWith("http://") || imageUrl.startsWith("https://")) {
            fullUrl = imageUrl;
        } else {
            String base = ApiClient.getBaseUrl();
            if (!base.endsWith("/")) {
                base += "/";
            }
            String rel = imageUrl.startsWith("/") ? imageUrl.substring(1) : imageUrl;
            fullUrl = base + rel;
        }

        // Tag view to handle RecyclerView view recycling correctly
        imageView.setTag(fullUrl);

        // Check in-memory cache
        Bitmap cached = memoryCache.get(fullUrl);
        if (cached != null) {
            imageView.setImageBitmap(cached);
            return;
        }

        // Download in background
        executor.execute(() -> {
            try {
                URL url = new URL(fullUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(8000);
                conn.setReadTimeout(8000);
                conn.setDoInput(true);
                conn.connect();

                if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    InputStream input = conn.getInputStream();
                    Bitmap bitmap = BitmapFactory.decodeStream(input);
                    input.close();

                    if (bitmap != null) {
                        memoryCache.put(fullUrl, bitmap);
                        mainHandler.post(() -> {
                            if (fullUrl.equals(imageView.getTag())) {
                                imageView.setImageBitmap(bitmap);
                            }
                        });
                    }
                }
            } catch (Exception ignored) {
                // Keep placeholder if download fails
            }
        });
    }
}
