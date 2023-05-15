package com.example.may11dialogflowwithoutsclient;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class AccessTokenRequester {

    private static String TAG = AccessTokenRequester.class.getName();

    public static String requestAccessToken(String jwt) {
        try {
            // Set up the HTTP connection
            URL url = new URL("https://oauth2.googleapis.com/token");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setDoOutput(true);

            // Construct the request body
            String requestBody = "grant_type=urn:ietf:params:oauth:grant-type:jwt-bearer&assertion=" + jwt;

            // Send the request
            OutputStream outputStream = connection.getOutputStream();
            outputStream.write(requestBody.getBytes("UTF-8"));
            outputStream.close();

            // Read the response
            int responseCode = connection.getResponseCode();
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            JSONObject accessToken = new JSONObject(response.toString());


            if (responseCode == HttpURLConnection.HTTP_OK) {
                // Successful request
                return accessToken.getString("access_token");
            } else {
                // Error response
                Log.e(TAG ,"Error response: " + response.toString());
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }
}