package com.example.dexter007bot;

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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
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
           // Toast.makeText(context, "Server: connection done",Toast.LENGTH_SHORT).show();

            InputStream inputStream = socket.getInputStream();
            String filename = "KMLReceive.kml";
            FileOutputStream fileOutputStream = new FileOutputStream(Environment.getExternalStoragePublicDirectory("DextorBot/DextorKml/ReceiveKml/" + filename));
            byte b[] = new byte[2048];
            inputStream.read(b,0,b.length);
            fileOutputStream.write(b,0,b.length);
            serverSocket.close();

            //File source = Environment.getExternalStoragePublicDirectory("DextorBot/DextorKml/WorkingKml/KMLCentral.kml");
            //File dummy = Environment.getExternalStoragePublicDirectory("DextorBot/DextorKml/ReceiveKml/" + filename);
            //DiffUtils.createDiff(source,dummy);
            //File delta = Environment.getExternalStoragePublicDirectory("DextorBot/DextorKml/Diff/KMLReceive.diff");
            //DiffUtils.applyPatch(source,delta);
            return null;
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
            return null;
        }
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        Toast.makeText(context, "File Copied", Toast.LENGTH_SHORT).show();
        /*if (result != null) {
            //statusText.setText("File copied - " + result);
            File recvFile = new File(result);
            Uri fileUri = FileProvider.getUriForFile(
                    context,
                    "com.example.android.wifidirect.fileprovider",
                    recvFile);
            Intent intent = new Intent();
            intent.setAction(android.content.Intent.ACTION_VIEW);
            intent.setDataAndType(fileUri, "image/*");
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            context.startActivity(intent);
        }*/
    }

    @Override
    protected void onPreExecute() {
        Toast.makeText(context, "Opening a server socket", Toast.LENGTH_SHORT).show();
    }

    public boolean copyFile(InputStream inputStream, OutputStream out) {
        byte buf[] = new byte[1024];
        int len;
        try {
            while ((len = inputStream.read(buf)) != -1) {
                out.write(buf, 0, len);
            }
            out.close();
            inputStream.close();
        } catch (IOException e) {
            Log.d(TAG, e.toString());
            return false;
        }
        return true;
    }
}