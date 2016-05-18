package com.writing.hlyin.dicttest.util;

import android.annotation.SuppressLint;

import org.json.JSONObject;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by hlyin on 5/19/16.
 * 处理每日一句等JSON数据
 */
public class ParseJSON {
    private String english = "lalala";
    private String translation = "啦啦啦";
    private String viewCount = "100";
    private String publishTime = "2015-05-18";
    private String urlString = null;

    public volatile boolean parsingComplete = true;

    public ParseJSON(String url) {
        this.urlString = url;
    }

    public String getEnglish() {
        return english;
    }

    public String getTranslation() {
        return translation;
    }

    public String getViewCount() {
        return viewCount;
    }

    public String getPublishTime() {
        return publishTime;
    }

    @SuppressLint("NewApi")
    public void readAndParseJSON(String in) {
        try {
            JSONObject json = new JSONObject(in);
            english = json.getString("content");
            translation = json.getString("note");
            viewCount = json.getString("s_pv");
            publishTime = json.getString("dateline");

            parsingComplete = false;
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public void fetchJSON() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(urlString);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setReadTimeout(5000 /* milliseconds */);
                    conn.setConnectTimeout(5000 /* milliseconds */);
                    conn.setRequestMethod("GET");
                    conn.setDoInput(true);
                    // Starts the query
                    conn.connect();
                    InputStream stream = conn.getInputStream();

                    String data = convertStreamToString(stream);

                    readAndParseJSON(data);
                    stream.close();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        thread.start();
    }

    static String convertStreamToString(java.io.InputStream is) {
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("A");
        return s.hasNext() ? s.next() : "";
    }
}