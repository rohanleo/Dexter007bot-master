package com.example.dexter007bot.SummaryVector;

import android.os.Environment;
import android.util.Log;

import com.example.dexter007bot.Connection.Ip;
import com.example.dexter007bot.LoginActivity;
import com.google.gson.Gson;

import org.apache.commons.io.FilenameUtils;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;


public class FileManager {

    public FileTable fileTable;
    Gson gson = new Gson();
    final public String DATABASE_PATH;
    final String PEER_ID;
    final String userName;


    public FileManager(String databaseName){
        this.PEER_ID = Ip.ipadd();
        this.userName = LoginActivity.userName;
        this.DATABASE_PATH =String.valueOf(Environment.getExternalStoragePublicDirectory("DextorBot/Log/" + databaseName));
        //Log.e("FileManager",DATABASE_PATH);
        this.fileTable=readDB(DATABASE_PATH);
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
            Log.e("readDB",PEER_ID);
            /*for (String key : fileTable.fileMap.keySet()) {
                FileEntry fileInfo = fileTable.fileMap.get(key);
                /*Log.e("EnterFile: " , "FileID: " + fileInfo.getFileID() +
                        "\n FileName: "+  fileInfo.getFileName() +
                        "\nRelativePath: " + fileInfo.getFilePath() +
                        "\nSIZE: " + fileInfo.getFileSize()+
                        "\nPercentDownloaded: " + fileInfo.getPercentDownloaded()+
                        "\nTimestamp: " +fileInfo.getTimestamp() +
                        "\nSenderIp: " + fileInfo.getSource()+
                        "\nTypeOfFile: " + fileInfo.getTypeoffile()+
                        "\nDestinationReachStatus: " +fileInfo.getDestinationReachedStatus() +
                        "\nOrigin: " + fileInfo.getOrigin());
            }*/

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

    /**
     * Traverse the folder and add / remove files
     */

    public void updateFilesFromSubfolders(File file, String senderIp,double fileSize, double percentDownloaded ) {
        File file_path = file;
        String relative_path = file_path.getPath();
        String fileID = getFileIDFromPath(file_path);
        String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
        String typeoffile;
        if(file_path.getName().startsWith("IMG")) typeoffile="image";
        else if(file_path.getName().startsWith("VID")) typeoffile="video";
        else if(file_path.getName().startsWith("AUD")) typeoffile="audio";
        else typeoffile="kml";
        Pattern p = Pattern.compile("_");
        String origin = p.split(FilenameUtils.getBaseName(file_path.getName()))[1] + "_" + p.split(FilenameUtils.getBaseName(file_path.getName()))[2];
        Boolean status;
        if(percentDownloaded==100) status =true;
        else status=false;

        enterFile(fileID, file_path.getName(), relative_path, fileSize,percentDownloaded, timeStamp, senderIp,typeoffile,origin, status);
    }
    /**
     * Store file description
     * @param fileID
     * @param fileName
     * @param fileSize
     * @param timestamp
     * @param source
     * @param destinationReachedStatus
     */
    public void enterFile(String fileID, String fileName, String filePath, double fileSize, double percentDownloaded, String timestamp,
                           String source, String typeoffile,String origin , boolean destinationReachedStatus){
        FileEntry newFileInfo = new FileEntry( fileID, fileName, filePath, fileSize,percentDownloaded ,timestamp,
                            source,typeoffile,origin, destinationReachedStatus);
        fileTable.fileMap.put( fileID, newFileInfo);
        //logger.d("DEBUG", "FileManager Add to DB: " + fileName);
    }


    public String getFileIDFromPath(File file){
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        byte[] md5sum = md.digest(file.getName().getBytes());
        // Create Hex String
        StringBuffer hexString = new StringBuffer();

        for (int i=0; i<md5sum.length; i++) {
            hexString.append(Integer.toHexString(0xFF & md5sum[i]));
        }

        return hexString.toString();
    }

    public void destinationReachStatusTrue(String fileID){
        FileEntry fileEntry = fileTable.fileMap.get(fileID);
        if(fileEntry!=null) {
            fileEntry.setDestinationReachedStatus(true);
        }
    }

}


