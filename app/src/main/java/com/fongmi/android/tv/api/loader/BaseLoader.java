package com.fongmi.android.tv.api.loader;

import android.text.TextUtils;

import com.fongmi.android.tv.api.config.LiveConfig;
import com.fongmi.android.tv.api.config.VodConfig;
import com.fongmi.android.tv.bean.Live;
import com.fongmi.android.tv.bean.Site;
import com.github.catvod.crawler.Spider;
import com.github.catvod.crawler.SpiderNull;
import com.github.catvod.utils.Util;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import dalvik.system.DexClassLoader;

public class BaseLoader {

    private final JarLoader jarLoader;
    private final PyLoader pyLoader;
    private final JsLoader jsLoader;

    private static class Loader {
        static volatile BaseLoader INSTANCE = new BaseLoader();
    }

    public static BaseLoader get() {
        return Loader.INSTANCE;
    }

    private BaseLoader() {
        this.jarLoader = new JarLoader();
        this.pyLoader = null; // SECURITY: Disabled
        this.jsLoader = null; // SECURITY: Disabled
    }

    public void clear() {
        this.jarLoader.clear();
        if (this.pyLoader != null) this.pyLoader.clear();
        if (this.jsLoader != null) this.jsLoader.clear();
    }

    public Spider getSpider(String key, String api, String ext, String jar) {
        boolean js = api.contains(".js");
        boolean py = api.contains(".py");
        boolean csp = api.startsWith("csp_");
        
        // SECURITY: Block ALL JAR loading (csp_ prefix) to prevent dangerous dynamic code execution
        if (csp) {
            return new SpiderNull(); // Return null spider to trigger direct URL fetch
        }
        
        if (py) return new SpiderNull(); // Python disabled
        if (js) return new SpiderNull(); // JavaScript disabled
        else return new SpiderNull();
    }

    public Spider getSpider(Map<String, String> params) {
        if (!params.containsKey("siteKey")) return new SpiderNull();
        Live live = LiveConfig.get().getLive(params.get("siteKey"));
        Site site = VodConfig.get().getSite(params.get("siteKey"));
        if (!site.isEmpty()) return site.spider();
        if (!live.isEmpty()) return live.spider();
        return new SpiderNull();
    }

    public void setRecent(String key, String api, String jar) {
        boolean js = api.contains(".js");
        boolean py = api.contains(".py");
        boolean csp = api.startsWith("csp_");
        
        // SECURITY: Skip ALL JAR operations (csp_ prefix) to prevent dangerous operations
        if (csp) return;
        
        if (js && jsLoader != null) jsLoader.setRecent(key);
        if (py && pyLoader != null) pyLoader.setRecent(key);
        else if (csp) jarLoader.setRecent(Util.md5(jar));
    }

    public Object[] proxyLocal(Map<String, String> params) {
        // SECURITY: Disabled proxy invocation to prevent code execution
        return new Object[]{};
    }

    public void parseJar(String jar, boolean recent) {
        // SECURITY: Disabled JAR loading to prevent dynamic code execution
        return;
    }

    public DexClassLoader dex(String jar) {
        // SECURITY: Disabled dex loading to prevent dynamic code execution
        return null;
    }

    public JSONObject jsonExt(String key, LinkedHashMap<String, String> jxs, String url) throws Throwable {
        // SECURITY: Disabled JSON extension to prevent code execution
        return new JSONObject();
    }

    public JSONObject jsonExtMix(String flag, String key, String name, LinkedHashMap<String, HashMap<String, String>> jxs, String url) throws Throwable {
        // SECURITY: Disabled JSON extension to prevent code execution
        return new JSONObject();
    }
}
