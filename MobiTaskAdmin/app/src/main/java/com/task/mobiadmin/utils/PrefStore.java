package com.task.mobiadmin.utils;


import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.gson.Gson;

import java.util.ArrayList;

import javax.inject.Inject;

public class PrefStore {

    private Context mAct;
    @Inject
    public PrefStore(Context aAct) {
        this.mAct = aAct;
    }

    private SharedPreferences getPref() {
        return PreferenceManager.getDefaultSharedPreferences(mAct);
    }

    public void cleanPref() {
        SharedPreferences settings = getPref();
        settings.edit().clear().apply();
    }

    public boolean containValue(String key) {
        SharedPreferences settings = getPref();
        return settings.contains(key);
    }

    public boolean getBoolean(String key) {
        return getBoolean(key, false);
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        SharedPreferences settings = getPref();
        return settings.getBoolean(key, defaultValue);
    }

    public void setBoolean(String key, boolean value) {
        SharedPreferences settings = getPref();
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    public void saveString(String key, String value) {
        SharedPreferences settings = getPref();
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public String getString(String key) {
        return getString(key, null);
    }

    public String getString(String key, String defaultVal) {
        SharedPreferences settings = getPref();
        return (settings.getString(key, defaultVal));
    }


    public void saveLong(String key, long value) {
        SharedPreferences settings = getPref();
        SharedPreferences.Editor editor = settings.edit();
        editor.putLong(key, value);
        editor.apply();
    }

    public long getLong(String key) {
        return getLong(key, 0);
    }

    public long getLong(String key, long defaultVal) {
        SharedPreferences settings = getPref();
        return settings.getLong(key, defaultVal);
    }

    public void setInt(String subscription_id, int sbu_id) {
        SharedPreferences settings = getPref();
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt(subscription_id, sbu_id);
        editor.apply();
    }

    public int getInt(String key) {
        return getInt(key, 0);
    }

    public int getInt(String key, int defaultVal) {
        SharedPreferences settings = getPref();
        return settings.getInt(key, defaultVal);
    }

    public <T extends Object> void setData(String value, ArrayList<T> datas) {
        getPref().edit().putString(value, ObjectSerializer.serialize(datas)).apply();
    }

    public <T extends Object> ArrayList<T> getData(String name) {
        return (ArrayList<T>) ObjectSerializer.deserialize(getPref().
                getString(name, ObjectSerializer.serialize(new ArrayList<T>())));
    }


    public void saveUserData(String key, Object data) {
        String stringData = new Gson().toJson(data);
        SharedPreferences settings = getPref();
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(key, stringData);
        editor.apply();
    }

    public Object getUserData(String key, Class dataClass) {
        SharedPreferences settings = getPref();
        String json = settings.getString(key, null);
        return new Gson().fromJson(json, dataClass);
    }

}