package com.moviemood.moviemood.services;

import android.annotation.SuppressLint;
import android.os.AsyncTask;

import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MovieMoodService {

    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private String url = "https://moviemood-chatbot.herokuapp.com/";
    private String errorMessage;
    private String responseMessage;
    private RequestTrigger requestTrigger;
    private String uuid;
    private String uuidMsg;
    private String authorizationHeader;

    public MovieMoodService(RequestTrigger requestTrigger) {
        this.requestTrigger = requestTrigger;
    }

    @SuppressLint("StaticFieldLeak")
    public void getUUID() {
        new AsyncTask<String, Void, String>() {

            @Override
            protected String doInBackground(String... strings) {
                OkHttpClient client = new OkHttpClient();

                try {
                    //request
                    Request request = new Request.Builder()
                            .url(url + "welcome")
                            .header("user_agent","Android")
                            .header("Content-Type","application/json")
                            .build();

                    //response
                    Response response = client.newCall(request).execute();

                    return  response.body().string();
                } catch (Exception e) {
                    errorMessage = "Server Error!";
                }
                return null;
            }

            @Override
            protected void onPostExecute(String s) {
                if(s != null){
                    try {
                        JSONObject jsonObject = new JSONObject(s);
                        uuid = jsonObject.optString("uuid");
                        uuidMsg = jsonObject.optString("message");
                        requestTrigger.saveUUID(uuid);
                        requestTrigger.onSuccessCall(uuidMsg);
                    } catch (JSONException e) {
                        requestTrigger.onFailureCall("Server Error!");
                    }

                }
            }
        }.execute();
    }

    @SuppressLint("StaticFieldLeak")
    public void sendRequest(String userMessage) {

        new AsyncTask<String, Void, String>() {

            @Override
            protected String doInBackground(String... strings) {
                OkHttpClient client = new OkHttpClient();
                JSONObject jsonBody = new JSONObject();
                try {
                    //request
                    jsonBody.put("message", strings[0]);
                    RequestBody body = RequestBody.create(JSON, jsonBody.toString());
                    Request request = new Request.Builder()
                            .url(url + "chat")
                            .header("user_agent","Android")
                            .header("Content-Type","application/json")
                            .header("Authorization",authorizationHeader)
                            .post(body)
                            .build();

                    //response
                    Response response = client.newCall(request).execute();
                    if(response.code()/100 == 2){
                        return  response.body().string();
                    }
                    else{
                        errorMessage = response.body().string();
                        return null;
                    }
                } catch (Exception e) {
                    errorMessage = "Server Error!";
                }
                return null;
            }

            @Override
            protected void onPostExecute(String s) {
                if(s != null){
                    try {
                        JSONObject jsonObject = new JSONObject(s);
                        responseMessage = jsonObject.optString("message");
                        requestTrigger.onSuccessCall(responseMessage);
                    } catch (JSONException e) {
                        errorMessage = "Server Error!";
                        requestTrigger.onFailureCall(errorMessage);
                    }

                }else{
                    requestTrigger.onFailureCall(errorMessage);
                }
            }
        }.execute(userMessage);

    }

    public void setAuthorizationHeader(String authorizationHeader) {
        this.authorizationHeader = authorizationHeader;
    }

    public String getAuthorizationHeader() {
        return authorizationHeader;
    }

    public String getResponseMessage() {
        return responseMessage;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
