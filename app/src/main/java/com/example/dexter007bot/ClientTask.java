package com.example.dexter007bot;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

public class ClientTask extends AsyncTask<Void,Void,Void> {
    Context context;
    String host;

    @Override
    protected void onPreExecute() {
        Toast.makeText(context, "Opening a client Socket", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        Toast.makeText(context, "File sent", Toast.LENGTH_SHORT).show();
    }

    public ClientTask(Context context, String host) {
        this.context = context;
        this.host = host;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        int port =8988;
        int len;
        Socket socket = new Socket();
        byte buf[] = new byte[2048];
        try {
            socket.bind(null);
            socket.connect((new InetSocketAddress(host, port)), 500);

            String filename = "KMLCentral.kml";
            FileInputStream fileInputStream = new FileInputStream(Environment.getExternalStoragePublicDirectory("DextorBot/DextorKml/WorkingKml/" + filename));
            fileInputStream.read(buf,0,buf.length);
            OutputStream outputStream = socket.getOutputStream();
            outputStream.write(buf,0,buf.length);
            outputStream.close();
        } catch (FileNotFoundException e) {
            //catch logic
        } catch (IOException e) {
            //catch logic
        }

        finally {
            if (socket != null) {
                if (socket.isConnected()) {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        //catch logic
                    }
                }
            }
            return null;
        }
    }
}