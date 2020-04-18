package com.example.dexter007bot.Connection;

import android.os.Environment;

import com.example.dexter007bot.DiffUtils;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.osmdroid.bonuspack.kml.KmlDocument;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Pattern;

public class Download implements Runnable{

    String link;
    File out;

    public Download(String link, File out)
    {
        this.link=link;
        this.out=out;
    }

    @Override
    public void run()
    {
        try {

            URL url = new URL(link);
            HttpURLConnection http = (HttpURLConnection)url.openConnection();
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
            double downloaded = 0.0;
            int read = 0;
            double perecentdownloaded =0.0;
            while((read = in.read(buffer,0,1024)) >=0)
            {
                bout.write(buffer,0,read);
                downloaded+= read;

            }

            bout.close();
            in.close();

            System.out.println("Work Done");

            if(out.getName().contains(".kml")){

                String filename = out.getName();
                String filebasename = FilenameUtils.getBaseName(out.getName());
                File newFile = Environment.getExternalStoragePublicDirectory("DextorBot/DextorKml/WorkingKml/" + filename);
                File oldFile = Environment.getExternalStoragePublicDirectory("DextorBot/DextorKml/ReceiveKml/" + filename);

                try {
                    if(!oldFile.exists()){
                        FileUtils.copyFile(newFile,oldFile);
                       // System.out.println("Old file not existed" + filename);
                        FileUtils.forceDelete(newFile);
                        return;
                    }

                    DiffUtils.createDiff(oldFile,newFile);

                    //System.out.println("Diff generated");

                    File deltaList = Environment.getExternalStoragePublicDirectory("DextorBot/DextorKml/Diff");
                    File delta = null;
                    for(File file : deltaList.listFiles()){
                        //System.out.println(file.getName());
                        if(file.getName().contains(filebasename)){
                            delta = file;
                            break;
                        }
                    }

                    //System.out.println("Checking diff");
                    //KmlDocument kmlDocument = new KmlDocument();
                    //kmlDocument.parseKMLFile(newFile);
                    //int newVersion = Integer.parseInt(kmlDocument.mKmlRoot.getExtendedData("total"));
                    int newVersion = Integer.parseInt(getVersion(FilenameUtils.getBaseName(delta.getName())));
                    KmlDocument kml = new KmlDocument();
                    kml.parseKMLFile(oldFile);
                    int oldVersion = Integer.parseInt(kml.mKmlRoot.getExtendedData("total"));
                    if(oldVersion<newVersion){
                        DiffUtils.applyPatch(oldFile,delta);
                        File newVersionCre = Environment.getExternalStoragePublicDirectory("DextorBot/DextorKml/" + filename);
                        FileUtils.forceDelete(oldFile);
                        File oldFilech = Environment.getExternalStoragePublicDirectory("DextorBot/DextorKml/ReceiveKml/" + filename);
                        FileUtils.copyFile(newVersionCre,oldFilech);
                        FileUtils.forceDelete(newVersionCre);
                    }
                    FileUtils.forceDelete(delta);
                    FileUtils.forceDelete(newFile);
                    //System.out.println("Diff Applied");

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

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
