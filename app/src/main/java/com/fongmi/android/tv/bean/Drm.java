package com.fongmi.android.tv.bean;

import android.text.TextUtils;

import androidx.media3.common.C;
import androidx.media3.common.MediaItem;
import androidx.media3.common.util.Util;

import com.github.catvod.utils.Json;
import com.google.gson.JsonElement;
import com.google.gson.annotations.SerializedName;

import java.util.UUID;

public class Drm {

    @SerializedName("key")
    private String key;
    @SerializedName("type")
    private String type;
    @SerializedName("forceKey")
    private boolean forceKey;
    @SerializedName("header")
    private JsonElement header;

    public static Drm create(String key, String type) {
        return new Drm(key, type);
    }

    private Drm(String key, String type) {
        this.key = key;
        this.type = type;
    }

    public String getKey() {
        return TextUtils.isEmpty(key) ? "" : key;
    }

    private String getType() {
        return TextUtils.isEmpty(type) ? "" : type;
    }

    public boolean isForceKey() {
        return forceKey;
    }

    private JsonElement getHeader() {
        return header;
    }

    public UUID getUUID() {
        if (getType().contains("playready")) return C.PLAYREADY_UUID;
        if (getType().contains("widevine")) return C.WIDEVINE_UUID;
        if (getType().contains("clearkey")) return C.CLEARKEY_UUID;
        return C.UUID_NIL;
    }

    // New method: Build JSON from kid:key if needed (centralized here to avoid duplication)
    public String getClearKeyJson() {
        String keyStr = getKey().trim();
        if (keyStr.startsWith("{")) {
            return keyStr; // Already JSON
        } else if (keyStr.contains(":")) { // kid:key format - convert to JSON
            String[] parts = keyStr.split(":");
            if (parts.length == 2) {
                byte[] kidBytes = Util.fromHex(parts[0]);
                byte[] keyBytes = Util.fromHex(parts[1]);
                String kidBase64 = Util.base64UrlEncode(kidBytes);
                String keyBase64 = Util.base64UrlEncode(keyBytes);
                return "{\"keys\":[{\"kty\":\"oct\",\"kid\":\"" + kidBase64 + "\",\"k\":\"" + keyBase64 + "\"}],\"type\":\"temporary\"}";
            }
        }
        return ""; // Invalid - fallback
    }

    public MediaItem.DrmConfiguration get() {
        MediaItem.DrmConfiguration.Builder builder = new MediaItem.DrmConfiguration.Builder(getUUID());
        builder.setMultiSession(!C.CLEARKEY_UUID.equals(getUUID()));
        builder.setLicenseRequestHeaders(Json.toMap(getHeader()));
        builder.setForceDefaultLicenseUri(isForceKey());

        String keyStr = getKey().trim();
        if (!C.CLEARKEY_UUID.equals(getUUID()) || keyStr.startsWith("http")) { // Remote URL only
            builder.setLicenseUri(keyStr);
        } // Local ClearKey (JSON or kid:key) - skip URI; handle with LocalMediaDrmCallback later

        return builder.build();
    }
}