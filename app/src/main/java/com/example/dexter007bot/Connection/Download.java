package com.example.dexter007bot.Connection;

import android.os.Environment;
import android.util.Log;

import com.example.dexter007bot.DiffUtils;
import com.example.dexter007bot.FileManager;
import com.google.gson.Gson;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.osmdroid.bonuspack.kml.KmlDocument;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Pattern;

public class Download implements Runnable{

    String link,IP;
    File out;

    public Download(String link, File out, String IP)
    {
        this.link=link;
        this.out=out;
        this.IP=IP;
    }

    @Override
    public void run()
    {
        try {
            FileManager fileManager = new FileManager();
            URL url = new URL(link);
            HttpURLConnection http = (HttpURLConnection)url.openConnection();
            double filesize = http.getContentLength();
            String filename = out.getName();
            BufferedInputStream in = new BufferedInputStream(http.getInputStream());
            FileOutputStream fos;


            //to resume download
            if(out.exists())
                fos = new FileOutputStream(out,true); //makes the stream append if the file exists
            else {
                fos = new FileOutputStream(out); //creates a new file.
            }

            in.skip(out.length());

            BufferedOutputStream bout = new BufferedOutputStream(fos,1024);
            byte[] buffer = new byte[1024];
            double downloaded = out.length();
            int read = 0;
            double perecentdownloaded =0.0;
            while((read = in.read(buffer,0,1024)) >=0)
            {
                bout.write(buffer,0,read);
                downloaded+= read;

            }
            perecentdownloaded = (downloaded/filesize)*100;
            bout.close();
            in.close();
            if(downloaded!=filesize){
                fileManager.updateFilesFromSubfolders(out,IP,filesize,perecentdownloaded);
                fileManager.removeDeletedFiles();
                fileManager.writeDB();
            }else{
                File newOut = null;
                if(filename.startsWith("IMG")){
                    newOut = Environment.getExternalStoragePublicDirectory("DextorBot/DextorImage/ReceivedImage/" + filename);
                    if(!newOut.exists())FileUtils.copyFile(out,newOut);
                    fileManager.updateFilesFromSubfolders(newOut,IP,filesize,100);
                    fileManager.removeDeletedFiles();
                    fileManager.writeDB();
                }else if(filename.startsWith("VID")){
                    newOut = Environment.getExternalStoragePublicDirectory("DextorBot/DextorVideo/ReceivedVideo/" + filename);
                    if(!newOut.exists())FileUtils.copyFile(out,newOut);
                    fileManager.updateFilesFromSubfolders(newOut,IP,filesize,100);
                    fileManager.removeDeletedFiles();
                    fileManager.writeDB();
                }else if(filename.startsWith("AUD")){
                    newOut = Environment.getExternalStoragePublicDirectory("DextorBot/DextorAudio/ReceivedAudio/" + filename);
                    if(!newOut.exists())FileUtils.copyFile(out,newOut);
                    fileManager.updateFilesFromSubfolders(newOut,IP,filesize,100);
                    fileManager.removeDeletedFiles();
                    fileManager.writeDB();
                }else if(filename.startsWith("KML")){

                    String filebasename = FilenameUtils.getBaseName(out.getName());
                    File oldFile = Environment.getExternalStoragePublicDirectory("DextorBot/DextorKml/ReceiveKml/" + filename);
                    newOut = Environment.getExternalStoragePublicDirectory("DextorBot/DextorKml/ReceiveKml/" + filename);
                    try {
                        if(!newOut.exists()){
                            FileUtils.copyFile(out,newOut);
                            fileManager.updateFilesFromSubfolders(newOut,IP,filesize,100);
                            fileManager.removeDeletedFiles();
                            fileManager.writeDB();
                        }else {
                            DiffUtils.createDiff(oldFile,out);

                            File deltaList = Environment.getExternalStoragePublicDirectory("DextorBot/DextorKml/Diff");
                            File delta = null;
                            for(File file : deltaList.listFiles()){
                                //System.out.println(file.getName());
                                if(file.getName().contains(filebasename)){
                                    delta = file;
                                    break;
                                }
                            }

                            int newVersion = Integer.parseInt(getVersion(FilenameUtils.getBaseName(delta.getName())));
                            KmlDocument kml = new KmlDocument();
                            kml.parseKMLFile(oldFile);
                            int oldVersion = Integer.parseInt(kml.mKmlRoot.getExtendedData("total"));
                            if(oldVersion<newVersion){
                                DiffUtils.applyPatch(oldFile,delta);
                                File newVersionCre = Environment.getExternalStoragePublicDirectory("DextorBot/DextorKml/" + filename);
                                FileUtils.forceDelete(oldFile);
                                File dummy = Environment.getExternalStoragePublicDirectory("DextorBot/DextorKml/ReceiveKml/" + filename);
                                FileUtils.copyFile(newVersionCre,dummy);
                                FileUtils.forceDelete(newVersionCre);
                            }
                            FileUtils.forceDelete(delta);

                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                FileUtils.forceDelete(out);
            }

            System.out.println("Work Done");

        }

        catch(IOException ex)
        {
            ex.printStackTrace();
        }
    }

    private String getVersion(String name){
        Pattern p =Pattern.compile("_");
        String [] s = p.split(name,4);
        return s[3];
    }
}