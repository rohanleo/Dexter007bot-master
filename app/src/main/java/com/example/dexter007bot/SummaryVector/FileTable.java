package com.example.dexter007bot.SummaryVector;

import java.util.concurrent.ConcurrentHashMap;


/**
 * File Table and File List Format:
 * ---------------------------------------------------------------------------------------------------------------------------------------------
 * |  File ID  |  File Name  |  File Path  |  File Size  | Percent Downloaded | Timestamp | Source | Type Of File | DestReachedStatus | Origin |
 * ---------------------------------------------------------------------------------------------------------------------------------------------
 */


public class FileTable {
    public String peerID;
    public String userName;
    public ConcurrentHashMap<String, FileEntry> fileMap;

    public FileTable(String peerId,String userName){
        this.peerID = peerId;
        this.userName=userName;
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