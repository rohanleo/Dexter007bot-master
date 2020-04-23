package com.example.dexter007bot;

import android.app.Application;
import android.content.Context;
import android.os.Environment;
import android.util.Log;

import org.apache.commons.io.FilenameUtils;
import org.osmdroid.bonuspack.kml.KmlDocument;

import java.io.File;
import java.io.IOException;
import java.util.regex.Pattern;

import ie.wombat.jbdiff.JBDiff;
import ie.wombat.jbdiff.JBPatch;

public class DiffUtils {


    public static void createDiff(File source, File destination) throws IOException {
        File delta = Environment.getExternalStoragePublicDirectory("DextorBot/DextorKml/.Diff/"+getDeltaName(destination));
        try {
            JBDiff.bsdiff(source, destination, delta);
            //MapFragment.parseKml(app,context);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }


    public static boolean applyPatch(File source,File delta){
        if(delta!=null){
            File destination = getDestinationFile(delta);
            try {
                JBPatch.bspatch(source, destination, delta);
                Log.d("DIffUtils",delta.getAbsolutePath());
                //MapFragment.parseKml(app,context);
                return true;
            }
            catch (Exception  e){
                e.printStackTrace();
                return false;
            }
        }
        return false;
    }

    private static String getDeltaName(File source) throws IOException {
        KmlDocument kml = new KmlDocument();
        kml.parseKMLFile(source);
        String name = FilenameUtils.getBaseName(source.getName());
        int version = Integer.parseInt(kml.mKmlRoot.getExtendedData("total"));
        return name+"_"+version+".diff";
    }

    private static File getDestinationFile(File delta){
        String deltaName = FilenameUtils.getBaseName(delta.getName());
        String name = getname(deltaName);
        return Environment.getExternalStoragePublicDirectory("DextorBot/DextorKml/"+name+".kml");
    }

    private static String getname(String deltaName) {
        Pattern p =Pattern.compile("_");
        String [] s = p.split(deltaName,4);
        return s[0] + "_" + s[1]+ "_" + s[2];
    }
}
