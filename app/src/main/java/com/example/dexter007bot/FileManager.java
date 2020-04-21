package com.example.dexter007bot;

import android.os.Environment;
import android.util.Log;

import com.example.dexter007bot.Connection.Ip;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.apache.commons.io.FilenameUtils;

import java.io.*;
import java.lang.reflect.Type;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;


public class FileManager {

    public FileTable fileTable;
    Gson gson = new Gson();
    final String DATABASE_PATH;
    final String PEER_ID;
    final String userName;


    public FileManager(){
        this.PEER_ID = Ip.ipadd();
        this.userName = LoginActivity.userName;
        this.fileTable = new FileTable(PEER_ID);
        fileTable.fileMap = new ConcurrentHashMap<>();
        this.DATABASE_PATH =String.valueOf(Environment.getExternalStoragePublicDirectory("DextorBot/ReceivedFiles_Log.json"));
        //Log.e("FileManager",DATABASE_PATH);
        readDB();
    }

    /**
     * Serialize data
     */
    public void writeDB() {
        try{
            File file = new File(DATABASE_PATH);
            FileWriter fileWriter = new FileWriter(file);
            gson.toJson(fileTable,fileWriter);
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
    public void readDB() {
        //logger.d("DEBUG", "FileManager reading from fileDB");
        try{
            BufferedReader br = new BufferedReader(new FileReader(DATABASE_PATH));

            //convert the json string back to object
            fileTable = (FileTable)gson.fromJson(br, FileTable.class);
            fileTable.peerID = this.PEER_ID;
            for (String key : fileTable.fileMap.keySet()) {
                FileEntry fileInfo = fileTable.fileMap.get(key);
                Log.e("EnterFile: " , "FileID: " + fileInfo.getFileID() +
                        "\n FileName: "+  fileInfo.getFileName() +
                        "\nRelativePath: " + fileInfo.getFilePath() +
                        "\nSIZE: " + fileInfo.getFileSize()+
                        "\nPercentDownloaded: " + fileInfo.getPercentDownloaded()+
                        "\nTimestamp: " +fileInfo.getTimestamp() +
                        "\nSenderIp: " + fileInfo.getSource()+
                        "\nTypeOfFile: " + fileInfo.getTypeoffile()+
                        "\nDestinationReachStatus: " +fileInfo.getDestinationReachedStatus() +
                        "\nOrigin: " + fileInfo.getOrigin());
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        catch (NullPointerException e) {
            this.writeDB();
            this.fileTable = new FileTable(this.PEER_ID);
            this.fileTable.fileMap = new ConcurrentHashMap<>();
        }
    }

    /**
     * Traverse the folder and add / remove files
     */

    public void updateFilesFromSubfolders(File file, String senderIp,double fileSize, double percentDownloaded ) {
        File file_path = file;
        String relative_path = file_path.getPath();
        String fileID = getFileIDFromPath(file_path);
        if (fileTable.fileMap.get(fileID) == null) {
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
/**
 * File Table and File List Format:
 * ---------------------------------------------------------------------------------------------------------------------------------------------
 * |  File ID  |  File Name  |  File Path  |  File Size  | Percent Downloaded | Timestamp | Source | Type Of File | DestReachedStatus | Origin |
 * ---------------------------------------------------------------------------------------------------------------------------------------------
 */

class FileTable implements java.io.Serializable{
    public String peerID;
    public ConcurrentHashMap<String, FileEntry> fileMap;

    public FileTable(String peerId){
        this.peerID = peerId;
        this.fileMap = new ConcurrentHashMap<String, FileEntry>();
    }
}


/**
 * Class to save file description
 */
class FileEntry implements java.io.Serializable{
    private String fileID;
    private String fileName;
    private String filePath;
    private double fileSize;
    private double percentDownloaded;
    private String timestamp;
    private String source;
    private String typeoffile;
    private String origin;
    private boolean destinationReachedStatus;

    public FileEntry(String fileID, String fileName, String filePath, double fileSize, double percentDownloaded,
                     String timestamp, String source,String typeoffile,String origin, boolean destinationReachedStatus){
        this.fileID = fileID;
        this.fileName = fileName;
        this.filePath = filePath;
        this.fileSize = fileSize;
        this.percentDownloaded=percentDownloaded;
        this.timestamp = timestamp;
        this.source = source;
        this.typeoffile=typeoffile;
        this.origin=origin;
        this.destinationReachedStatus = destinationReachedStatus;
    }

    public String getFileID(){
        return this.fileID;
    }

    public String getFileName(){
        return this.fileName;
    }

    public String getFilePath(){
        return this.filePath;
    }

    public double getFileSize(){
        return this.fileSize;
    }

    public double getPercentDownloaded() {
        return percentDownloaded;
    }

    public String getTimestamp(){
        return this.timestamp;
    }

    public String getSource(){
        return this.source;
    }

    public String getTypeoffile() {
        return typeoffile;
    }

    public String getOrigin() {
        return origin;
    }

    public boolean getDestinationReachedStatus(){
        return this.destinationReachedStatus;
    }

    public void setDestinationReachedStatus(boolean status){
        this.destinationReachedStatus = status;
    }

    public void setPercentDownloaded(double percentDownloaded) {
        this.percentDownloaded = percentDownloaded;
    }
}