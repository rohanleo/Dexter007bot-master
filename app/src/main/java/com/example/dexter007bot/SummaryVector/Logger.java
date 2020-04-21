package com.example.dexter007bot.SummaryVector;

import android.os.Environment;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.example.dexter007bot.LoginActivity.userName;


public class Logger {
    private String logDirPath;
    private File logFileDir = null;
    private FileWriter logFileWriter = null;
    public Logger(){
        this.logDirPath = String.valueOf(Environment.getExternalStoragePublicDirectory("DextorBot/"));
        this.logFileDir = new File(String.valueOf(Environment.getExternalStoragePublicDirectory("DextorBot/")));
        String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());

        try {
            this.logFileWriter = new FileWriter(logDirPath + "syncLog-" + userName + "-" + timeStamp + ".csv",true);
            //the true will append the new data
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void write(String logMessage) {
        String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
        String logmsg= String.format("%s, %s", timeStamp, logMessage);
        try {
            System.out.println("LOG:" + logmsg);
            logFileWriter.write(logmsg + "\n");
            logFileWriter.flush();
        }
        catch (IOException e){
            e.printStackTrace();
        }
        //appends the string to the file
    }
    public static void d(String a, String b){
        String c = a+b;
        System.out.println(c);
    }
}
