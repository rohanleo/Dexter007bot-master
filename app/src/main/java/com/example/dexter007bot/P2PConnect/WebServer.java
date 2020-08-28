package com.example.dexter007bot.P2PConnect;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.util.Log;

import com.example.dexter007bot.LoginActivity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.activation.MimetypesFileTypeMap;

import fi.iki.elonen.NanoHTTPD;

import static android.content.Context.MODE_PRIVATE;

public class WebServer extends NanoHTTPD {

    private String userName;
    public WebServer(Context context) {
        super(8080);
        SharedPreferences preferences = context.getSharedPreferences("user_credentials",MODE_PRIVATE);
        userName = preferences.getString("user_name","no_user");
    }

    @Override
    public Response serve(String uri, Method method,
                          Map<String, String> header, Map<String, String> parameters,
                          Map<String, String> files) {

        //File folder = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/test/");
        File selfKml=Environment.getExternalStoragePublicDirectory("DextorBot/DextorKml/SelfKml");
        File rcvKml=Environment.getExternalStoragePublicDirectory("DextorBot/DextorKml/ReceiveKml");
        File imageDir=Environment.getExternalStoragePublicDirectory("DextorBot/DextorImage/Image");
        File rcvimageDir=Environment.getExternalStoragePublicDirectory("DextorBot/DextorImage/ReceivedImage");
        File videoDir=Environment.getExternalStoragePublicDirectory("DextorBot/DextorVideo/Video");
        File rcvvideoDir=Environment.getExternalStoragePublicDirectory("DextorBot/DextorVideo/ReceivedVideo");
        File audioDir=Environment.getExternalStoragePublicDirectory("DextorBot/DextorAudio/Audio");
        File rcvaudioDir=Environment.getExternalStoragePublicDirectory("DextorBot/DextorAudio/ReceivedAudio");

        Map<Integer, List<String>> prio = new HashMap<>();
        List<String> list1 = new ArrayList<>();
        List<String> list2 = new ArrayList<>();
        List<String> list3 = new ArrayList<>();
        List<String> list4 = new ArrayList<>();

        //kml
        for(File file : selfKml.listFiles()){
            list1.add(file.getName());
            //Log.e("WebServer 1",file.getName());
            prio.put(new Integer(1),list1);
        }
        for(File file : rcvKml.listFiles()){
            list1.add(file.getName());
            //Log.e("WebServer 1",file.getName());
            prio.put(new Integer(1),list1);
        }
        //image
        for(File file : imageDir.listFiles()){
            list2.add(file.getName());
            prio.put(new Integer(2),list2);
        }
        for(File file : rcvimageDir.listFiles()){
            list2.add(file.getName());
            prio.put(new Integer(2),list2);
        }
        //video
        for(File file : videoDir.listFiles()){
            list3.add(file.getName());
            prio.put(new Integer(3),list3);
        }
        for(File file : rcvvideoDir.listFiles()){
            list3.add(file.getName());
            prio.put(new Integer(3),list3);
        }
        //audio
        for(File file : audioDir.listFiles()){
            list4.add(file.getName());
            prio.put(new Integer(4),list4);
        }
        for(File file : rcvaudioDir.listFiles()){
            list4.add(file.getName());
            prio.put(new Integer(4),list4);
        }

        String filename = "";

        //TO CREATE THE WEBPAGE
        if (uri.equals("/")) {
            System.out.println(uri);

            String st = "";
            String x = "";

            for (Map.Entry<Integer, List<String>> en : prio.entrySet()) {
                for (String obj : en.getValue()) {

                    x = obj;
                    st = st + "<a href=\"/get?name=" + x + "\">" + x + "</a>";
                    st = st + "<br>";


                }
            }


            return new Response(Response.Status.OK, MIME_HTML, st);


        }

        //TO DOWNLOAD
        else if (uri.equals("/get"))

        {
//            System.out.println(uri);
//            String x = header.get("referer");
//            System.out.println("THE REFERER"+x);


            FileInputStream fis = null;
            File f = null;
            try {
                if(parameters.get("name").contains(".kml")){
                    if(parameters.get("name").contains(userName))
                        f = Environment.getExternalStoragePublicDirectory("DextorBot/DextorKml/SelfKml/" + parameters.get("name"));
                    else
                        f = Environment.getExternalStoragePublicDirectory("DextorBot/DextorKml/ReceiveKml/" + parameters.get("name"));
                }else if(parameters.get("name").contains(".jpg")){
                    if(parameters.get("name").contains(userName))
                        f = Environment.getExternalStoragePublicDirectory("DextorBot/DextorImage/Image/" + parameters.get("name"));
                    else
                        f = Environment.getExternalStoragePublicDirectory("DextorBot/DextorImage/ReceivedImage/" + parameters.get("name"));
                }else if(parameters.get("name").contains(".mp4")){
                    if(parameters.get("name").contains(userName))
                        f = Environment.getExternalStoragePublicDirectory("DextorBot/DextorVideo/Video/" + parameters.get("name"));
                    else
                        f = Environment.getExternalStoragePublicDirectory("DextorBot/DextorVideo/ReceivedVideo/" + parameters.get("name"));
                }else if(parameters.get("name").contains(".mp3")){
                    if(parameters.get("name").contains(userName))
                        f = Environment.getExternalStoragePublicDirectory("DextorBot/DextorAudio/Audio/" + parameters.get("name"));
                    else
                        f = Environment.getExternalStoragePublicDirectory("DextorBot/DextorAudio/ReceivedAudio/" + parameters.get("name"));
                }
                fis = new FileInputStream(f);
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            MimetypesFileTypeMap mimeTypesMap = new MimetypesFileTypeMap();

            String mimeType = mimeTypesMap.getContentType(filename);
            return new NanoHTTPD.Response(Response.Status.OK, mimeType, fis);


        }




        else {
//            System.out.println(uri);

            return new NanoHTTPD.Response("404 File Not Found");
        }

    }
}