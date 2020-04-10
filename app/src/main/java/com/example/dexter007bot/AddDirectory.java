package com.example.dexter007bot;

import android.os.Environment;

import java.io.File;

public class AddDirectory {
    static File dextorBot= Environment.getExternalStoragePublicDirectory("DextorBot");
    static File kmlDir=Environment.getExternalStoragePublicDirectory("DextorBot/DextorKml");
    static File receiveKml=Environment.getExternalStoragePublicDirectory("DextorBot/DextorKml/ReceiveKml");
    static File selfKml=Environment.getExternalStoragePublicDirectory("DextorBot/DextorKml/SelfKml");
    static File WorkKml=Environment.getExternalStoragePublicDirectory("DextorBot/DextorKml/WorkingKml");
    static File diff=Environment.getExternalStoragePublicDirectory("DextorBot/DextorKml/Diff");
    static File imageDir=Environment.getExternalStoragePublicDirectory("DextorBot/DextorImage");
    static File videoDir=Environment.getExternalStoragePublicDirectory("DextorBot/DextorVideo");
    static File audioDir=Environment.getExternalStoragePublicDirectory("DextorBot/DextorAudio");

    public static void addDirectory(){
        if(!dextorBot.exists()) dextorBot.mkdir();
        if(!kmlDir.exists()) kmlDir.mkdir();
        if(!imageDir.exists()) imageDir.mkdir();
        if(!videoDir.exists()) videoDir.mkdir();
        if(!audioDir.exists()) audioDir.mkdir();
        if(!receiveKml.exists()) receiveKml.mkdir();
        if(!selfKml.exists()) selfKml.mkdir();
        if(!WorkKml.exists()) WorkKml.mkdir();
        if(!diff.exists()) diff.mkdir();
    }
}
