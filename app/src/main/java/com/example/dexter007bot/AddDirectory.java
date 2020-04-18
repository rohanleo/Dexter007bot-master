package com.example.dexter007bot;

import android.os.Environment;

import java.io.File;

public class AddDirectory {
    private static File dextorBot= Environment.getExternalStoragePublicDirectory("DextorBot");
    private static File kmlDir=Environment.getExternalStoragePublicDirectory("DextorBot/DextorKml");
    private static File receiveKml=Environment.getExternalStoragePublicDirectory("DextorBot/DextorKml/ReceiveKml");
    private static File selfKml=Environment.getExternalStoragePublicDirectory("DextorBot/DextorKml/SelfKml");
    private static File WorkKml=Environment.getExternalStoragePublicDirectory("DextorBot/DextorKml/WorkingKml");
    private static File diff=Environment.getExternalStoragePublicDirectory("DextorBot/DextorKml/Diff");
    private static File imageDir=Environment.getExternalStoragePublicDirectory("DextorBot/DextorImage/Image");
    private static File image=Environment.getExternalStoragePublicDirectory("DextorBot/DextorImage");
    private static File rcvimageDir=Environment.getExternalStoragePublicDirectory("DextorBot/DextorImage/ReceivedImage");
    private static File videoDir=Environment.getExternalStoragePublicDirectory("DextorBot/DextorVideo/Video");
    private static File video=Environment.getExternalStoragePublicDirectory("DextorBot/DextorVideo");
    private static File rcvvideoDir=Environment.getExternalStoragePublicDirectory("DextorBot/DextorVideo/ReceivedVideo");
    private static File audioDir=Environment.getExternalStoragePublicDirectory("DextorBot/DextorAudio/Audio");
    private static File audio=Environment.getExternalStoragePublicDirectory("DextorBot/DextorAudio");
    private static File rcvaudioDir=Environment.getExternalStoragePublicDirectory("DextorBot/DextorAudio/ReceivedAudio");

    public static void addDirectory(){
        if(!dextorBot.exists()) dextorBot.mkdir();
        if(!kmlDir.exists()) kmlDir.mkdir();
        if(!image.exists()) image.mkdir();
        if(!video.exists()) video.mkdir();
        if(!audio.exists()) audio.mkdir();
        if(!receiveKml.exists()) receiveKml.mkdir();
        if(!selfKml.exists()) selfKml.mkdir();
        if(!WorkKml.exists()) WorkKml.mkdir();
        if(!diff.exists()) diff.mkdir();
        if(!imageDir.exists()) imageDir.mkdir();
        if(!rcvimageDir.exists()) rcvimageDir.mkdir();
        if(!videoDir.exists()) videoDir.mkdir();
        if(!rcvvideoDir.exists()) rcvvideoDir.mkdir();
        if(!audioDir.exists()) audioDir.mkdir();
        if(!rcvaudioDir.exists()) rcvaudioDir.mkdir();
    }
}
