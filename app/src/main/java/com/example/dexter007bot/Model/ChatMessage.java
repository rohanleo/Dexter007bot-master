package com.example.dexter007bot.Model;

public class ChatMessage {

    private int value;
    private String content;

    public ChatMessage(int value, String content){
        this.value=value;
        this.content=content;
    }

    public int getValue(){ return value; }

    public String getContent() {
        return content;
    }

}
