package com.sugboaid.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.LruCache;
import android.widget.ImageView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Image loading and caching utility for efficient memory management
 */
public class ImageCacheManager {
    
    private static ImageCacheManager instance;
    private final LruCache<String, Bitmap> memoryCache;
    private final File diskCacheDir;
    private final Context context;
    
    // Cache configuration
    private static final int MAX_DISK_CACHE_SIZE = 50 * 1024 * 1024; // 50MB
    private static final int BITMAP_QUALITY = 85;
    
    private ImageCacheManager(Context context) {
        this.context = context.getApplicationContext();
        
        // Initialize memory cache
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        final int cacheSize = maxMemory / 8; // Use 1/8th of available memory
        
        memoryCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                return bitmap.getByteCount() / 1024;
            }
            
            @Override
            protected void entryRemoved(boolean evicted, String key, Bitmap oldValue, Bitmap newValue) {
                if (oldValue != null && !oldValue.isRecycled()) {
                    oldValue.recycle();
                }
            }
        };
        
        // Initialize disk cache directory
        diskCacheDir = new File(context.getCacheDir(), "image_cache");
        if (!diskCacheDir.exists()) {
            diskCacheDir.mkdirs();
        }
        
        // Clean up old cache files on startup
        cleanupDiskCache();
    }
    
    public static synchronized ImageCacheManager getInstance(Context context) {
        if (instance == null) {
            instance = new ImageCacheManager(context);
        }
        return instance;
    }
    
    /**
     * Load image into ImageView with caching
     */
    public void loadImage(String imageKey, ImageView imageView, int defaultResourceId) {
        if (imageKey == null || imageView == null) {
            if (imageView != null && defaultResourceId != 0) {
                imageView.setImageResource(defaultResourceId);
            }
            return;
        }
        
        // Check memory cache first
        Bitmap bitmap = getBitmapFromMemCache(imageKey);
        if (bitmap != null && !bitmap.isRecycled()) {
            imageView.setImageBitmap(bitmap);
            return;
        }
        
        // Set default image while loading
        if (defaultResourceId != 0) {
            imageView.setImageResource(defaultResourceId);
        }
        
        // Load from disk cache or generate
        new BitmapWorkerTask(imageView, defaultResourceId).execute(imageKey);
    }
    
    /**
     * Load drawable as bitmap and cache it
     */
    public void loadDrawableAsBitmap(String key, Drawable drawable, ImageView imageView) {
        if (key == null || drawable == null || imageView == null) return;
        
        // Check if already cached
        Bitmap cached = getBitmapFromMemCache(key);
        if (cached != null && !cached.isRecycled()) {
            imageView.setImageBitmap(cached);
            return;
        }
        
        // Convert drawable to bitmap in background
        new DrawableToBitmapTask(imageView).execute(new DrawableCacheItem(key, drawable));
    }
    
    /**
     * Cache bitmap in memory
     */
    public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (key != null && bitmap != null && !bitmap.isRecycled()) {
            if (getBitmapFromMemCache(key) == null) {
                memoryCache.put(key, bitmap);
            }
        }
    }
    
    /**
     * Get bitmap from memory cache
     */
    public Bitmap getBitmapFromMemCache(String key) {
        if (key == null) return null;
        Bitmap bitmap = memoryCache.get(key);
        return (bitmap != null && !bitmap.isRecycled()) ? bitmap : null;
    }
    
    /**
     * Save bitmap to disk cache
     */
    private void saveBitmapToDisk(String key, Bitmap bitmap) {
        if (key == null || bitmap == null || bitmap.isRecycled()) return;
        
        try {
            File cacheFile = new File(diskCacheDir, hashKeyForDisk(key));
            FileOutputStream out = new FileOutputStream(cacheFile);
            bitmap.compress(Bitmap.CompressFormat.PNG, BITMAP_QUALITY, out);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Load bitmap from disk cache
     */
    private Bitmap loadBitmapFromDisk(String key) {
        if (key == null) return null;
        
        try {
            File cacheFile = new File(diskCacheDir, hashKeyForDisk(key));
            if (cacheFile.exists()) {
                FileInputStream in = new FileInputStream(cacheFile);
                Bitmap bitmap = BitmapFactory.decodeStream(in);
                in.close();
                return bitmap;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        return null;
    }
    
    /**
     * Generate hash key for disk cache
     */
    private String hashKeyForDisk(String key) {
        String cacheKey;
        try {
            final MessageDigest digest = MessageDigest.getInstance("MD5");
            digest.update(key.getBytes());
            cacheKey = bytesToHexString(digest.digest());
        } catch (NoSuchAlgorithmException e) {
            cacheKey = String.valueOf(key.hashCode());
        }
        return cacheKey;
    }
    
    /**
     * Convert bytes to hex string
     */
    private String bytesToHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xFF & b);
            if (hex.length() == 1) {
                sb.append('0');
            }
            sb.append(hex);
        }
        return sb.toString();
    }
    
    /**
     * Clean up disk cache if it exceeds size limit
     */
    private void cleanupDiskCache() {
        if (!diskCacheDir.exists()) return;
        
        File[] files = diskCacheDir.listFiles();
        if (files == null) return;
        
        long totalSize = 0;
        for (File file : files) {
            totalSize += file.length();
        }
        
        if (totalSize > MAX_DISK_CACHE_SIZE) {
            // Delete oldest files first
            java.util.Arrays.sort(files, (f1, f2) -> Long.compare(f1.lastModified(), f2.lastModified()));
            
            for (File file : files) {
                if (totalSize <= MAX_DISK_CACHE_SIZE * 0.8) break; // Keep 80% of max size
                
                totalSize -= file.length();
                file.delete();
            }
        }
    }
    
    /**
     * Clear all caches
     */
    public void clearCache() {
        // Clear memory cache
        memoryCache.evictAll();
        
        // Clear disk cache
        if (diskCacheDir.exists()) {
            File[] files = diskCacheDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    file.delete();
                }
            }
        }
    }
    
    /**
     * Get cache size information
     */
    public CacheInfo getCacheInfo() {
        long diskSize = 0;
        if (diskCacheDir.exists()) {
            File[] files = diskCacheDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    diskSize += file.length();
                }
            }
        }
        
        int memorySize = memoryCache.size();
        int memoryMaxSize = memoryCache.maxSize();
        
        return new CacheInfo(memorySize, memoryMaxSize, diskSize);
    }
    
    /**
     * Optimize bitmap for memory efficiency
     */
    public static Bitmap optimizeBitmap(Bitmap original, int maxWidth, int maxHeight) {
        if (original == null || original.isRecycled()) return null;
        
        int width = original.getWidth();
        int height = original.getHeight();
        
        // Calculate scale factor
        float scaleWidth = (float) maxWidth / width;
        float scaleHeight = (float) maxHeight / height;
        float scale = Math.min(scaleWidth, scaleHeight);
        
        if (scale >= 1.0f) {
            return original; // No scaling needed
        }
        
        // Create scaled bitmap
        int newWidth = Math.round(width * scale);
        int newHeight = Math.round(height * scale);
        
        return Bitmap.createScaledBitmap(original, newWidth, newHeight, true);
    }
    
    /**
     * Convert drawable to bitmap efficiently
     */
    public static Bitmap drawableToBitmap(Drawable drawable) {
        if (drawable == null) return null;
        
        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if (bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }
        
        Bitmap bitmap;
        if (drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
        } else {
            bitmap = Bitmap.createBitmap(
                drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(),
                Bitmap.Config.ARGB_8888
            );
        }
        
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        
        return bitmap;
    }
    
    /**
     * AsyncTask for loading bitmaps in background
     */
    private class BitmapWorkerTask extends AsyncTask<String, Void, Bitmap> {
        private final WeakReference<ImageView> imageViewReference;
        private final int defaultResourceId;
        private String imageKey;
        
        public BitmapWorkerTask(ImageView imageView, int defaultResourceId) {
            imageViewReference = new WeakReference<>(imageView);
            this.defaultResourceId = defaultResourceId;
        }
        
        @Override
        protected Bitmap doInBackground(String... params) {
            imageKey = params[0];
            
            // Try to load from disk cache
            Bitmap bitmap = loadBitmapFromDisk(imageKey);
            
            if (bitmap == null) {
                // Generate bitmap (placeholder for actual image loading logic)
                bitmap = generatePlaceholderBitmap(imageKey);
                
                if (bitmap != null) {
                    // Save to disk cache
                    saveBitmapToDisk(imageKey, bitmap);
                }
            }
            
            if (bitmap != null) {
                // Add to memory cache
                addBitmapToMemoryCache(imageKey, bitmap);
            }
            
            return bitmap;
        }
        
        @Override
        protected void onPostExecute(Bitmap bitmap) {
            ImageView imageView = imageViewReference.get();
            if (imageView != null) {
                if (bitmap != null && !bitmap.isRecycled()) {
                    imageView.setImageBitmap(bitmap);
                } else if (defaultResourceId != 0) {
                    imageView.setImageResource(defaultResourceId);
                }
            }
        }
        
        private Bitmap generatePlaceholderBitmap(String key) {
            // This is a placeholder - in a real app, you would load actual images
            // For now, return null to use default resource
            return null;
        }
    }
    
    /**
     * AsyncTask for converting drawable to bitmap
     */
    private class DrawableToBitmapTask extends AsyncTask<DrawableCacheItem, Void, Bitmap> {
        private final WeakReference<ImageView> imageViewReference;
        private String cacheKey;
        
        public DrawableToBitmapTask(ImageView imageView) {
            imageViewReference = new WeakReference<>(imageView);
        }
        
        @Override
        protected Bitmap doInBackground(DrawableCacheItem... params) {
            DrawableCacheItem item = params[0];
            cacheKey = item.key;
            
            return drawableToBitmap(item.drawable);
        }
        
        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (bitmap != null && !bitmap.isRecycled()) {
                // Cache the bitmap
                addBitmapToMemoryCache(cacheKey, bitmap);
                
                // Set to ImageView
                ImageView imageView = imageViewReference.get();
                if (imageView != null) {
                    imageView.setImageBitmap(bitmap);
                }
            }
        }
    }
    
    /**
     * Helper class for drawable caching
     */
    private static class DrawableCacheItem {
        final String key;
        final Drawable drawable;
        
        DrawableCacheItem(String key, Drawable drawable) {
            this.key = key;
            this.drawable = drawable;
        }
    }
    
    /**
     * Cache information holder
     */
    public static class CacheInfo {
        public final int memorySize;
        public final int memoryMaxSize;
        public final long diskSize;
        
        public CacheInfo(int memorySize, int memoryMaxSize, long diskSize) {
            this.memorySize = memorySize;
            this.memoryMaxSize = memoryMaxSize;
            this.diskSize = diskSize;
        }
        
        public double getMemoryUsagePercentage() {
            return (double) memorySize / memoryMaxSize * 100;
        }
        
        @Override
        public String toString() {
            return String.format(
                "Cache Info - Memory: %d/%d (%.1f%%), Disk: %.2f MB",
                memorySize, memoryMaxSize, getMemoryUsagePercentage(),
                diskSize / (1024.0 * 1024.0)
            );
        }
    }
}