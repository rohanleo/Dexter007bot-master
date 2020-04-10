package com.example.dexter007bot;

import android.app.Application;
import android.content.Context;
import android.os.Environment;
import android.util.Log;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.osmdroid.bonuspack.kml.KmlDocument;

import java.io.File;
import java.io.IOException;

import ie.wombat.jbdiff.JBDiff;
import ie.wombat.jbdiff.JBPatch;

public class DiffUtils {


    public static void createDiff(File source, File destination) throws IOException {
        File delta = Environment.getExternalStoragePublicDirectory("DextorBot/DextorKml/Diff/"+getDeltaName(destination));
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
                FileUtils.forceDelete(delta);
                //Log.d("DIffUtils",delta.getAbsolutePath());
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
        File diffDir = Environment.getExternalStoragePublicDirectory("DextorBot/DextorKml/Diff");
        KmlDocument kml = new KmlDocument();
        kml.parseKMLFile(source);
        //String name = FilenameUtils.getBaseName(source.getName());
        String name = FilenameUtils.getBaseName(source.getName());
        //int version = Integer.parseInt(kml.mKmlRoot.getExtendedData("total"));
        return name + ".diff";
        //return name+"_"+version+".diff";
        //return "dummy.diff";
    }

    private static File getDestinationFile(File delta){
        String deltaName = FilenameUtils.getBaseName(delta.getName());
        return Environment.getExternalStoragePublicDirectory("DextorBot/DextorKml/WorkingKml/"+deltaName+".kml");
    }
}