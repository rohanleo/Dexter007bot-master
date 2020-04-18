package com.example.dexter007bot.TCP;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;


public class ServerTask extends AsyncTask<Void,Void,Void> {
    private Context context;
    //private TextView statusText;
    private String TAG = "ServerTask";

    public ServerTask(Context context) {
        this.context = context;
        //this.statusText = (TextView) statusText;
    }

    @Override
    protected Void doInBackground(Void... params) {
        try {
            ServerSocket serverSocket = new ServerSocket(8988);
            Log.d(TAG, "Server: Socket opened");
            Socket socket = serverSocket.accept();

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String clientIp =  bufferedReader.readLine();
            serverSocket.close();
            return null;
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
            return null;
        }
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        //Toast.makeText(context, "File Copied", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onPreExecute() {
        Toast.makeText(context, "Opening a server socket", Toast.LENGTH_SHORT).show();
    }
}