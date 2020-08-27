package com.example.dexter007bot.P2PConnect;

import android.os.Environment;


import java.io.BufferedReader;
import java.io.File;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

public class client implements Runnable{

    String IP;

    public client(String ip)
    {
        this.IP=ip;
    }

    public void run() {

        System.out.println("ip is "+ IP);
        String urlString = "http:/"+IP+":8080";
        if(IP.equals("")) return;
        //Log.e("Client:- " , IP);
        Collection A = new ArrayList();

        Collection B = new ArrayList();
        String filename = new String();
        String fileURL = new String();
        String saveDir = new String();

        //File rcvkmlDir = Environment.getExternalStoragePublicDirectory("DextorBot/DextorKml/ReceiveKml");
        File selfKml=Environment.getExternalStoragePublicDirectory("DextorBot/DextorKml/SelfKml");
        File rcvimageDir=Environment.getExternalStoragePublicDirectory("DextorBot/DextorImage/ReceivedImage");
        File imageDir=Environment.getExternalStoragePublicDirectory("DextorBot/DextorImage/Image");
        File rcvvideoDir=Environment.getExternalStoragePublicDirectory("DextorBot/DextorVideo/ReceivedVideo");
        File videoDir=Environment.getExternalStoragePublicDirectory("DextorBot/DextorVideo/Video");
        File rcvaudioDir=Environment.getExternalStoragePublicDirectory("DextorBot/DextorAudio/ReceivedAudio");
        File audioDir=Environment.getExternalStoragePublicDirectory("DextorBot/DextorAudio/Audio");

        for(File file : rcvimageDir.listFiles()){
            B.add(file.getName());
        }
        for(File file : imageDir.listFiles()){
            B.add(file.getName());
        }
        for(File file : rcvvideoDir.listFiles()){
            B.add(file.getName());
        }
        for(File file : videoDir.listFiles()){
            B.add(file.getName());
        }
        for(File file : rcvaudioDir.listFiles()){
            B.add(file.getName());
        }
        for(File file : audioDir.listFiles()){
            B.add(file.getName());
        }
        /*for(File file : rcvkmlDir.listFiles()){
            B.add(file.getName());
        }*/
        for(File file : selfKml.listFiles()){
            B.add(file.getName());
        }

        System.out.println("Files in My system\n"+B);

        URL url = null;
        try {
            url = new URL(urlString);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }


        BufferedReader reader;
        try {
            reader = new BufferedReader(new InputStreamReader(url.openStream()));
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }


        String line = null;
        try {
            line = reader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }


        int i;
        for (i = 0; i < line.length(); i++) {



            if (line.charAt(i) == 'e' && line.charAt(i + 1) == '=') {

                i = i + 2;
                while (line.charAt(i) != '"') {

                    filename += line.charAt(i);
                    i++;

                }



                A.add(filename);
                filename="";
            }


        }
        System.out.println("Files in Server\n"+A);




        Collection diff = new ArrayList(A);
        diff.removeAll(B);
        System.out.println("File To be Taken from Server\n"+diff);
        Iterator it = diff.iterator();
        while (it.hasNext()) {
            String x = (String) it.next();


            fileURL = "http:/" + IP + ":8080/get?name=" + x;

            if(x.contains(".kml")){
                saveDir = String.valueOf(Environment.getExternalStoragePublicDirectory("DextorBot/.Working/.Kml/" + x));
                //Log.e("Client",x);
            }else if(x.contains(".jpg")){
                saveDir = String.valueOf(Environment.getExternalStoragePublicDirectory("DextorBot/.Working/.Image/"+ x));
            }else if(x.contains(".mp4")){
                saveDir = String.valueOf(Environment.getExternalStoragePublicDirectory("DextorBot/.Working/.Video/"+ x));
            }else if(x.contains(".mp3")){
                saveDir = String.valueOf(Environment.getExternalStoragePublicDirectory("DextorBot/.Working/.Audio/"+ x));
            }
            //saveDir = Environment.getExternalStorageDirectory().getAbsolutePath()+"/test/"+x;



            File out = new File(saveDir);

            new Thread(new Download(fileURL, out,IP)).start();


        }


        try {
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}