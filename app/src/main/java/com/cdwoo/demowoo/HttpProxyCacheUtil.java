package com.cdwoo.demowoo;

import android.content.Context;

import com.danikula.videocache.HttpProxyCacheServer;

public class HttpProxyCacheUtil {
    private static HttpProxyCacheServer audioProxy;

    public static HttpProxyCacheServer getAudioProxy(Context context) {
        if (audioProxy== null) {
            audioProxy= new HttpProxyCacheServer.Builder(context)
                    .cacheDirectory(context.getExternalCacheDir())
                    .maxCacheSize(32 * 1024 * 1024) // 缓存大小
                    .fileNameGenerator(new CacheFileNameGenerator())
                    .build();
        }
        return audioProxy;
    }
}