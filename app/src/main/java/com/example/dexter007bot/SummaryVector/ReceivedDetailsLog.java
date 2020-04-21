package com.example.dexter007bot.SummaryVector;

import android.os.Environment;

import com.example.dexter007bot.Connection.Ip;
import com.example.dexter007bot.LoginActivity;
import com.google.gson.Gson;

import org.apache.commons.io.FileUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

public class ReceivedDetailsLog implements Runnable {

    public FileTable fileTable;
    Gson gson = new Gson();
    final String DATABASE_PATH;
    final String PEER_ID;
    final String userName;
    private boolean exit;
    private boolean isRunning;


    public ReceivedDetailsLog(){
        this.PEER_ID = Ip.ipadd();
        this.userName = LoginActivity.userName;
        this.DATABASE_PATH =String.valueOf(Environment.getExternalStoragePublicDirectory("DextorBot/ReceivedDetails_Log.json"));
        this.fileTable=readDB(DATABASE_PATH);
        this.exit = true;
        this.isRunning = false;
    }

    @Override
    public void run() {
        addDB();
        removeDeletedFiles();
        writeDB(fileTable,DATABASE_PATH);
        System.out.println("Work Done");
    }

    public void stop() {
        this.exit = true;
    }

    private void addDB() {
        File dbList = Environment.getExternalStoragePublicDirectory("DextorBot/Log/");
        for(File file : dbList.listFiles()){
            FileTable fileTable1 = readDB(String.valueOf(Environment.getExternalStoragePublicDirectory("DextorBot/Log/" + file.getName())));
            //Log.e("addDB",file.getName());
            fileTable.fileMap.putAll(fileTable1.fileMap);
            try {
                FileUtils.forceDelete(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Serialize data
     */
    public void writeDB(FileTable fileTable1,String DB_path) {
        try{
            File file = new File(DB_path);
            FileWriter fileWriter = new FileWriter(file);
            gson.toJson(fileTable1,fileWriter);
            //fileWriter.write(gson.toJson(fileTable));
            fileWriter.flush();
            fileWriter.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Deserialize data
     */
    public FileTable readDB(String DB_path) {
        FileTable fileTable1 = new FileTable(PEER_ID);
        fileTable1.fileMap = new ConcurrentHashMap<>();
        //logger.d("DEBUG", "FileManager reading from fileDB");
        try{
            BufferedReader br = new BufferedReader(new FileReader(DB_path));

            //convert the json string back to object
            fileTable1 = (FileTable)gson.fromJson(br, FileTable.class);
            fileTable1.peerID = this.PEER_ID;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        catch (NullPointerException e) {
            this.writeDB(fileTable1,DB_path);
            fileTable1 = new FileTable(this.PEER_ID);
            fileTable1.fileMap = new ConcurrentHashMap<>();
        }
        return fileTable1;
    }
    public void removeDeletedFiles(){
        for (String key : fileTable.fileMap.keySet()) {
            FileEntry fileInfo = fileTable.fileMap.get(key);
            String filePath = fileInfo.getFilePath();
            boolean check = new File( filePath).exists();
            if(!check) {
                fileTable.fileMap.remove(key);
                //logger.d("DEBUG", "FileManaager Remove from DB " + filePath);

            }
        }

    }
}
