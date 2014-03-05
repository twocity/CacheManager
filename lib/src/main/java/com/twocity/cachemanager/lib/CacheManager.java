package com.twocity.cachemanager.lib;

import com.google.gson.Gson;

import com.jakewharton.disklrucache.DiskLruCache;

import android.content.Context;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


/**
 * @author dvy.zhang@gmail.com
 */
public class CacheManager {

    private static final long DISK_CACHE_SIZE = 10 * 1024 * 1024; // 10MB

    private static final int DISK_CACHE_INDEX = 0;

    private final String STRING_ENCODING = "UTF-8";

    private DiskLruCache diskLruCache;

    private static CacheManager mManager;

    private Context context;

    private Gson mGson = new Gson();

    public static CacheManager load(Context context) {
        if (mManager == null) {
            mManager = new CacheManager(context);
        }
        return mManager;
    }

    private CacheManager(Context context) {
        this.context = context;
        initDiskCache();
    }

    // this should not be done at main thread
    private void initDiskCache() {
        if (!isDiskCacheOK()) {
            try {
                File cacheDir = new File(context.getCacheDir(), "lru-cache");
                if (!cacheDir.exists()) {
                    cacheDir.mkdir();
                }
                diskLruCache = DiskLruCache.open(cacheDir,
                        BuildConfig.VERSION_CODE,
                        1,
                        DISK_CACHE_SIZE
                );
            } catch (IOException e) {
            }
        }
    }

    public void put(String key, final Object value, Type type) {
        if (isDiskCacheOK() && value != null) {
            try {
                String jsonString;
                if (type != null) {
                    jsonString = mGson.toJson(value, type);
                } else {
                    jsonString = mGson.toJson(value);
                }
                setKeyValue(key, jsonString);
            } catch (IOException e) {
            } catch (Exception e) {
            }
        }

    }

    public <T> T get(String key, Type type) {
        if (isDiskCacheOK()) {
            String json = null;
            try {
                json = getValue(key);
                if (json != null) {
                    return mGson.fromJson(json, type);
                }
            } catch (IOException e) {
            }
        }
        return null;
    }


    public boolean contains(String key) {
        boolean exist = false;
        if (isDiskCacheOK()) {
            try {
                exist = containsKey(key);
            } catch (IOException e) {
            }
        }
        return exist;
    }

    // should no be done at main thread.
    public void clearCache() {
        if (isDiskCacheOK()) {
            try {
                diskLruCache.delete();
            } catch (IOException e) {
            } catch (Exception e) {
                // ignore
            }
            diskLruCache = null;
            initDiskCache();
        }
    }

    public boolean isDiskCacheOK() {
        return diskLruCache != null && !diskLruCache.isClosed();
    }

    public long getDiskLruCacheSize() {
        return diskLruCache.getMaxSize();
    }

    private String getValue(String key) throws IOException {
        String value = null;
        DiskLruCache.Snapshot snapshot = null;
        try {
            snapshot = diskLruCache.get(hashKeyForDisk(key));
            if (snapshot == null) {
                return null;
            }
            value = snapshot.getString(DISK_CACHE_INDEX);
        } finally {
            if (snapshot != null) {
                snapshot.close();
            }
        }
        return value;
    }

    private boolean containsKey(String key) throws IOException {
        boolean found = false;
        DiskLruCache.Snapshot snapshot = null;
        try {
            snapshot = diskLruCache.get(hashKeyForDisk(key));
            if (snapshot != null) {
                found = true;
            }
        } finally {
            if (snapshot != null) {
                snapshot.close();
            }
        }
        return found;
    }

    private void setKeyValue(String key, String value) throws IOException {
        DiskLruCache.Editor editor = null;
        try {
            editor = diskLruCache.edit(hashKeyForDisk(key));
            if (editor == null) {
                return;
            }

            if (writeValueToCache(value, editor)) {
                diskLruCache.flush();
                editor.commit();
            } else {
                editor.abort();
            }
        } finally {
            if (editor != null) {
                editor.abort();
            }
        }
    }


    private boolean writeValueToCache(String value, DiskLruCache.Editor editor)
            throws IOException {
        OutputStream outputStream = null;
        try {
            outputStream = new BufferedOutputStream(editor.newOutputStream(DISK_CACHE_INDEX));
            outputStream.write(value.getBytes(STRING_ENCODING));
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException ignore) {

                }
            }
        }
        return true;
    }

    private static String hashKeyForDisk(String key) {
        String cacheKey;
        try {
            final MessageDigest mDigest = MessageDigest.getInstance("MD5");
            mDigest.update(key.getBytes());
            cacheKey = bytesToHexString(mDigest.digest());
        } catch (NoSuchAlgorithmException e) {
            cacheKey = String.valueOf(key.hashCode());
        }
        return cacheKey;
    }

    private static String bytesToHexString(byte[] bytes) {
        // http://stackoverflow.com/questions/332079
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(0xFF & bytes[i]);
            if (hex.length() == 1) {
                sb.append('0');
            }
            sb.append(hex);
        }
        return sb.toString();
    }

}
