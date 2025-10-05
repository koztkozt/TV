package com.fongmi.android.tv.api.loader;

import android.content.Context;

import com.fongmi.android.tv.App;
import com.fongmi.android.tv.utils.UrlUtil;
import com.github.catvod.crawler.Spider;
import com.github.catvod.crawler.SpiderNull;
import com.github.catvod.net.OkHttp;
import com.github.catvod.utils.Path;
import com.github.catvod.utils.Prefers;
import com.github.catvod.utils.Util;

import org.json.JSONObject;

import java.io.File;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import dalvik.system.DexClassLoader;

public class JarLoader {

    private final ConcurrentHashMap<String, DexClassLoader> loaders;
    private final ConcurrentHashMap<String, Method> methods;
    private final ConcurrentHashMap<String, Spider> spiders;
    private String recent;

    public JarLoader() {
        loaders = new ConcurrentHashMap<>();
        methods = new ConcurrentHashMap<>();
        spiders = new ConcurrentHashMap<>();
    }

    public void clear() {
        for (Spider spider : spiders.values()) App.execute(spider::destroy);
        loaders.clear();
        methods.clear();
        spiders.clear();
    }

    public void setRecent(String recent) {
        this.recent = recent;
    }

    private void load(String key, File file) {
        if (!file.setReadOnly()) return;
        loaders.put(key, dex(file));
        invokeInit(key);
        putProxy(key);
    }

    private DexClassLoader dex(File file) {
        return new DexClassLoader(file.getAbsolutePath(), Path.jar().getAbsolutePath(), Path.jar().getAbsolutePath(), App.get().getClassLoader());
    }

    private void invokeInit(String key) {
        try {
            Class<?> clz = loaders.get(key).loadClass("com.github.catvod.spider.Init");
            Method method = clz.getMethod("init", Context.class);
            method.invoke(clz, App.get());
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private void putProxy(String key) {
        try {
            Class<?> clz = loaders.get(key).loadClass("com.github.catvod.spider.Proxy");
            Method method = clz.getMethod("proxy", Map.class);
            methods.put(key, method);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private File download(String url) {
        try {
            return Path.write(Path.jar(url), OkHttp.bytes(url));
        } catch (Exception e) {
            return Path.jar(url);
        }
    }

    public synchronized void parseJar(String key, String jar) {
        // SECURITY: Block ALL JAR parsing to prevent dangerous dynamic code execution
        // This completely prevents JAR file downloads and loading for maximum security
        return; // Skip ALL JAR loading operations
    }

    public DexClassLoader dex(String jar) {
        // SECURITY: Block ALL DexClassLoader creation to prevent dangerous dynamic code execution
        // This completely prevents JAR-based class loading for maximum security
        return null; // Always return null to prevent dynamic class loading
    }

    public Spider getSpider(String key, String api, String ext, String jar) {
        // SECURITY: Block ALL JAR spider loading to prevent dangerous dynamic code execution
        // This completely prevents JAR-based spider execution for maximum security
        return new SpiderNull(); // Always return null spider to trigger direct URL fetch
    }

    public JSONObject jsonExt(String key, LinkedHashMap<String, String> jxs, String url) throws Throwable {
        // SECURITY: Return null if loaders are empty (JAR loading disabled)
        if (recent == null || loaders.get(recent) == null) return null;
        Class<?> clz = loaders.get(recent).loadClass("com.github.catvod.parser.Json" + key);
        Method method = clz.getMethod("parse", LinkedHashMap.class, String.class);
        return (JSONObject) method.invoke(null, jxs, url);
    }

    public JSONObject jsonExtMix(String flag, String key, String name, LinkedHashMap<String, HashMap<String, String>> jxs, String url) throws Throwable {
        // SECURITY: Return null if loaders are empty (JAR loading disabled)
        if (recent == null || loaders.get(recent) == null) return null;
        Class<?> clz = loaders.get(recent).loadClass("com.github.catvod.parser.Mix" + key);
        Method method = clz.getMethod("parse", LinkedHashMap.class, String.class, String.class, String.class);
        return (JSONObject) method.invoke(null, jxs, name, flag, url);
    }

    public Object[] proxyInvoke(Map<String, String> params) {
        // SECURITY: Return null if methods are empty (JAR loading disabled)
        if (recent == null || methods.isEmpty()) return null;
        Object[] result = proxyInvoke(methods.get(recent), params);
        return result != null ? result : tryOthers(params);
    }

    private Object[] tryOthers(Map<String, String> params) {
        for (Map.Entry<String, Method> entry : methods.entrySet()) {
            if (entry.getKey().equals(recent)) continue;
            Object[] result = proxyInvoke(entry.getValue(), params);
            if (result != null) return result;
        }
        return null;
    }

    private Object[] proxyInvoke(Method method, Map<String, String> params) {
        try {
            return (Object[]) method.invoke(null, params);
        } catch (Throwable e) {
            e.printStackTrace();
            return null;
        }
    }
}
