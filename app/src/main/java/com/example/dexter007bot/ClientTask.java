package com.example.dexter007bot;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

public class ClientTask extends AsyncTask<Void,Void,Void> {
    Context context;
    String host;
    int port =8988;
    int len;
    Socket socket = new Socket();
    byte buf[] = new byte[1024];

    @Override
    protected void onPreExecute() {
        Toast.makeText(context, "Opening a client Socket", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        Toast.makeText(context, "File Sent", Toast.LENGTH_SHORT).show();
    }

    public ClientTask(Context context, String host) {
        this.context = context;
        this.host = host;
    }

    @Override
    protected Void doInBackground(Void... voids) {

        try {
            socket.bind(null);
            socket.connect((new InetSocketAddress(host, port)), 500);

            /**
             * Create a byte stream from a JPEG file and pipe it to the output stream
             * of the socket. This data is retrieved by the server device.
             */
            OutputStream outputStream = socket.getOutputStream();
            ContentResolver cr = context.getContentResolver();
            InputStream inputStream = null;
            File file = Environment.getExternalStoragePublicDirectory("DextorBot");
            inputStream = cr.openInputStream(Uri.fromFile(file));
            //inputStream = cr.openInputStream(Uri.parse(String.valueOf(Environment.getExternalStoragePublicDirectory("DextorBot"))));
            while ((len = inputStream.read(buf)) != -1) {
                outputStream.write(buf, 0, len);
            }
            outputStream.close();
            inputStream.close();
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

