package com.moviemood.moviemood;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.view.View;

import com.github.bassaer.chatmessageview.model.User;
import com.github.bassaer.chatmessageview.models.Message;
import com.github.bassaer.chatmessageview.views.ChatView;
import com.github.bassaer.chatmessageview.views.MessageView;
import com.moviemood.moviemood.services.MovieMoodService;
import com.moviemood.moviemood.services.RequestTrigger;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements RequestTrigger{

    private MainActivity mainActivity = this;
    private MovieMoodService movieMoodService;
    private MessageView messageView;
    private ChatView chatView;
    final User me = new User(0, "User", null);
    final User you = new User(1, "MovieMood", null);
    private ArrayList<Message> messages = new ArrayList<>();
    private ProgressDialog progressDialog;


    protected void setMessageListener (){
        chatView.setOnClickSendButtonListener(new View.OnClickListener() {

            public void onClick(View v) {
                String userMsg = chatView.getInputText();
                if (userMsg == null || userMsg.equals("")){
                   return;
                } else {
                    if(isNetworkAvailable()){
                        if(movieMoodService.getAuthorizationHeader().equals("")){
                            onFailureCall("Restart the application!");
                        } else {
                            Message message = new Message.Builder()
                                    .setUser(me) // Sender
                                    .setRightMessage(true) // This message Will be shown right side.
                                    .setMessageText(userMsg) //Message contents
                                    .hideIcon(true)
                                    .build();

                            messages.add(message);
                            chatView.send(message);

                            movieMoodService.sendRequest(message.getMessageText());
                            chatView.setInputText("");
                        }
                    } else {
                        onFailureCall("Check your Internet Connection!");
                    }

                }
            }
        });
    }

    protected void setHelpListener (){
        chatView.setOnClickOptionButtonListener(new View.OnClickListener() {

            public void onClick(View v) {
                if (isNetworkAvailable()){
                    if(movieMoodService.getAuthorizationHeader().equals("")){
                        onFailureCall("Restart the application!");
                    } else {
                        Message message = new Message.Builder()
                                .setUser(me) // Sender
                                .setRightMessage(true) // This message Will be shown right side.
                                .setMessageText("Actor Al Pacino") //Message contents
                                .hideIcon(true)
                                .build();
                        messages.add(message);
                        chatView.send(message);

                        movieMoodService.sendRequest(message.getMessageText());
                        chatView.setInputText("");
                    }
                } else {
                    onFailureCall("Check your Internet Connection!");
                }

            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.show();

        messageView = (MessageView) findViewById(R.id.message_view);
        chatView = (ChatView) findViewById(R.id.chat_view);
        chatView.setOptionIcon(R.drawable.ic_action_info);

        messageView.init(messages);
        setMessageListener();
        setHelpListener();

        movieMoodService = new MovieMoodService(this);

        SharedPreferences sharedPreferences = getSharedPreferences("sharedInfo", Context.MODE_PRIVATE);

        if(isNetworkAvailable()){
            movieMoodService.getUUID();
        } else {
            progressDialog.hide();
            onFailureCall("Check your Internet Connection!");
            movieMoodService.setAuthorizationHeader("");
        }
    }

    @Override
    public void onSuccessCall(String successMessage) {
        Message responseMessage = new Message.Builder()
                .setUser(you) // Sender
                .setRightMessage(false) // This message Will be shown left side.
                .setMessageText(successMessage) //Message contents
                .hideIcon(true)
                .build();
        messages.add(responseMessage);
        progressDialog.hide();
        chatView.receive(responseMessage);
    }

    @Override
    public void onFailureCall(String failureMessage) {
        Message responseMessage = new Message.Builder()
                .setUser(you) // Sender
                .setRightMessage(false) // This message Will be shown left side.
                .setMessageText(failureMessage) //Message contents
                .hideIcon(true)
                .build();
        messages.add(responseMessage);
        progressDialog.hide();
        chatView.receive(responseMessage);
    }

    @Override
    public void saveUUID(String uuid) {
        SharedPreferences sharedPreferences = getSharedPreferences("sharedInfo", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("uuid", uuid);
        editor.apply();
        movieMoodService.setAuthorizationHeader(uuid);
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

}
