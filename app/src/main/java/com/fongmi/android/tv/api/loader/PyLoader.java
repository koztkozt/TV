package com.fongmi.android.tv.api.loader;

import com.fongmi.android.tv.App;
// SECURITY: Chaquo Python disabled
// import com.fongmi.chaquo.Loader;
import com.github.catvod.crawler.Spider;
import com.github.catvod.crawler.SpiderNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PyLoader {

    private final ConcurrentHashMap<String, Spider> spiders;
    // SECURITY: Chaquo Python disabled
    // private final Loader loader;
    private String recent;

    public PyLoader() {
        spiders = new ConcurrentHashMap<>();
        // SECURITY: Chaquo Python disabled
        // loader = new Loader();
    }

    public void clear() {
        for (Spider spider : spiders.values()) App.execute(spider::destroy);
        spiders.clear();
    }

    public void setRecent(String recent) {
        this.recent = recent;
    }

    public Spider getSpider(String key, String api, String ext) {
        // SECURITY: Python execution disabled
        return new SpiderNull();
    }

    public Object[] proxyInvoke(Map<String, String> params) {
        try {
            if (!params.containsKey("siteKey")) return spiders.get(recent).proxyLocal(params);
            return BaseLoader.get().getSpider(params).proxyLocal(params);
        } catch (Throwable e) {
            e.printStackTrace();
            return null;
        }
    }
}
