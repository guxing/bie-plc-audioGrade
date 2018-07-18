package com.example.winbo.audiotest6.utils;

import android.os.Environment;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Luosiwei on 2017/9/5.
 */

public class ResetAPIUtils {
    private static final String serverURL = "http://vop.baidu.com/server_api";
    private static String token = "";
    //put your own params here
    private static final String apiKey = "z5ZAtQBXvcgKqdMHqQYD4zxa";
    private static final String secretKey = "a9dvjSLzHUnjEPfLmAS43X02uU4G9nKx";
    private static final String cuid = "10078051";

    public static String getRecognizeResult() {
        getToken();
        try {
            return method2();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    private static void getToken() {
        String getTokenURL = "https://openapi.baidu.com/oauth/2.0/token?grant_type=client_credentials" +
                "&client_id=" + apiKey + "&client_secret=" + secretKey;
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) new URL(getTokenURL).openConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            token = new JSONObject(printResponse(conn)).getString("access_token");
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String printResponse(HttpURLConnection conn) throws Exception {
        if (conn.getResponseCode() != 200) {
            return "";
        }
        InputStream is = conn.getInputStream();
        BufferedReader rd = new BufferedReader(new InputStreamReader(is));
        String line;
        StringBuffer response = new StringBuffer();
        while ((line = rd.readLine()) != null) {
            response.append(line);
            response.append('\r');
        }
        rd.close();
        System.out.println(new JSONObject(response.toString()).toString(4));
        return response.toString();
    }

    private static String method2() throws Exception {
        File pcmFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/360/temp/1.wav");
        HttpURLConnection conn = (HttpURLConnection) new URL(serverURL
                + "?cuid=" + cuid + "&token=" + token).openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "audio/wav; rate=16000");

        conn.setDoInput(true);
        conn.setDoOutput(true);

        // send request
        DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
        wr.write(loadFile(pcmFile));
        wr.flush();
        wr.close();

        return printResponse(conn);
    }

    private static byte[] loadFile(File file) throws IOException {
        InputStream is = new FileInputStream(file);

        long length = file.length();
        byte[] bytes = new byte[(int) length];

        int offset = 0;
        int numRead = 0;
        while (offset < bytes.length
                && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
            offset += numRead;
        }

        if (offset < bytes.length) {
            is.close();
            throw new IOException("Could not completely read file " + file.getName());
        }

        is.close();
        return bytes;
    }
}
