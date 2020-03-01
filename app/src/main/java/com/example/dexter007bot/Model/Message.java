package com.example.dexter007bot.Model;



import com.example.dexter007bot.Chats.Author;

import java.util.Date;


public class Message  {
    Author author;
    String id,text;
    Date createdAt;
    String url;
    boolean map=false,audio=false,video=false,image=false;
    String imageurl;
    String mapObjectID;

    public Message(String id, Author author, String type){
        this.id = id;
        this.author = author;
        this.createdAt = new Date();
        if(type.equals("map")){
            map=true;
        }
        if(type.equals("audio")){
            audio=true;
        }
        if(type.equals("video")){
            video=true;
        }
        if(type.equals("image")){
            image=true;
        }
        this.setText(type);
    }

    public Message(String id,Author author,String type,Date createdAt){
        this.id = id;
        this.author = author;
        this.createdAt = createdAt;
        if(type.equals("map")){
            map=true;
        }
        if(type.equals("audio")){
            audio=true;
        }
        if(type.equals("video")){
            video=true;
        }
        if(type.equals("image")){
            image=true;
        }
        this.setText(type);
    }


    public String getId() {
        return id;
    }

    public String getText() {
        return text;
    }

    public Author getUser() {
        return author;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public String getImageUrl() {
        return imageurl;
    }

    public String getUrl() { return url; }

    public boolean isMap() { return map;}

    public boolean isAudio() { return audio;}

    public boolean isVideo() { return video; }

    public boolean isImage() { return image; }

    public void setText(String text) {
        this.text = text;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setImageurl(String url) { this.imageurl = url;}

    public String getMapObjectID() {
        return mapObjectID;
    }

    public void setMapObjectID(String mapObjectID) {
        this.mapObjectID = mapObjectID;
    }
}
