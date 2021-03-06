package com.example.dexter007bot.P2PConnect;

import android.os.Environment;
import android.util.Log;

import com.example.dexter007bot.DiffUtils;
import com.example.dexter007bot.LoginActivity;
import com.example.dexter007bot.MainActivity;
import com.example.dexter007bot.SummaryVector.FileManager;
import com.example.dexter007bot.SummaryVector.ReceivedDetailsLog;

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
            //client.downloadCompleted=false;
            //FileManager fileManager = new FileManager("ReceivedDetails_Log.json");
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
            LoginActivity.logger.write(IP + "::"+filename+" :Downloaded- "+perecentdownloaded+"% ");
            bout.close();
            in.close();
            Log.e("Download",out.getName());
            if(downloaded!=filesize){
                FileManager fileManager = new FileManager(FilenameUtils.getBaseName(out.getName())+ ".json");
                fileManager.updateFilesFromSubfolders(out,IP,filesize,perecentdownloaded);
                fileManager.writeDB(fileManager.fileTable,fileManager.DATABASE_PATH);
                Log.e("Download","check1");
            }else{
                Log.e("Download","check2");
                File newOut = null;
                boolean check=true;
                if(filename.startsWith("IMG")){
                    newOut = Environment.getExternalStoragePublicDirectory("DextorBot/DextorImage/ReceivedImage/" + filename);
                    if(!newOut.exists())FileUtils.copyFile(out,newOut);
                }else if(filename.startsWith("VID")){
                    newOut = Environment.getExternalStoragePublicDirectory("DextorBot/DextorVideo/ReceivedVideo/" + filename);
                    if(!newOut.exists())FileUtils.copyFile(out,newOut);
                }else if(filename.startsWith("AUD")){
                    newOut = Environment.getExternalStoragePublicDirectory("DextorBot/DextorAudio/ReceivedAudio/" + filename);
                    if(!newOut.exists())FileUtils.copyFile(out,newOut);
                }else if(filename.startsWith("KML")){
                    //String filebasename = FilenameUtils.getBaseName(out.getName());
                    //File oldFile = Environment.getExternalStoragePublicDirectory("DextorBot/DextorKml/ReceiveKml/" + filename);
                    newOut = Environment.getExternalStoragePublicDirectory("DextorBot/DextorKml/ReceiveKml/" + filename);
                    try {
                        KmlDocument recKml = new KmlDocument();
                        recKml.parseKMLFile(out);
                        Log.e("Download",out.getName() + " " + recKml.mKmlRoot.getExtendedData("total"));
                        int recVersion = Integer.parseInt(recKml.mKmlRoot.getExtendedData("total"));
                        if(!newOut.exists()){
                            FileUtils.copyFile(out,newOut);
                        }else {
                            KmlDocument myKml = new KmlDocument();
                            myKml.parseKMLFile(newOut);
                            int myVersion = Integer.parseInt(myKml.mKmlRoot.getExtendedData("total"));
                            if (recVersion > myVersion){
                                FileUtils.forceDelete(newOut);
                                FileUtils.copyFile(out,newOut);
                            } else
                                check = false;
                            /*DiffUtils.createDiff(oldFile,out);
                            check =false;
                            File deltaList = Environment.getExternalStoragePublicDirectory("DextorBot/DextorKml/.Diff");
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
                                check =true;
                            }
                            FileUtils.forceDelete(delta);*/
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                FileUtils.forceDelete(out);
                if(check){
                    FileManager fileManager = new FileManager(FilenameUtils.getBaseName(out.getName())+ ".json");
                    fileManager.updateFilesFromSubfolders(newOut,IP,filesize,100);
                    fileManager.writeDB(fileManager.fileTable,fileManager.DATABASE_PATH);
                }
            }
            new Thread(new ReceivedDetailsLog()).start();
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