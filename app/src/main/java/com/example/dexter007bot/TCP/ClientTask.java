package com.example.dexter007bot.TCP;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import com.example.dexter007bot.Ip;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
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
        //Toast.makeText(context, "File sent", Toast.LENGTH_SHORT).show();
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
        try {
            socket.bind(null);
            socket.connect((new InetSocketAddress(host, port)), 500);

            String ip = new String(Ip.ipadd());
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(socket.getOutputStream());
            PrintWriter printWriter = new PrintWriter(outputStreamWriter);
            outputStreamWriter.write(ip);
            outputStreamWriter.flush();
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