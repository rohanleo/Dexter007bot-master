package com.example.dexter007bot.Chats;





import android.location.Location;

import com.example.dexter007bot.Model.Message;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Pattern;

public class ChatUtils {

    //get message for the extended data kml
    public static String getExtendedDataFormatName(String text, String version, String type, Location location){
        String latlon = location.getLatitude() + "_" + location.getLongitude();
        return getTimeStamp()+"-"+version+"-"+type+"-"+text+"-"+latlon;
    }

    public static String getTimeStamp(){
        return new SimpleDateFormat("yyyyMMddHHmmss",Locale.ENGLISH).format(new Date());
    }

    //Get a Message object from a extended data format string
   /* public static Message getMessageObject(String extendedData, Author author){
        Message message = null;
        Pattern p = Pattern.compile("-");
        String[] s = p.split(extendedData,5);
        String date = s[0];
        String type = s[1];
        String mapObjectId = s[2];
        String text = s[3];
        DateFormat df = new SimpleDateFormat("yyyyMMddHHmmss", Locale.ENGLISH);
        try {
            Date d = df.parse(date);
            message = new Message(date, author, type, d);
            switch (type) {
                case "text":
                    message.setText(text);
                    break;
                case "image": {
                    String url = "Android/data/com.example.dexter007bot/files/Pictures/" + text;
                    message.setImageurl(url);
                    break;
                }
                case "map": {
                    String url = "Android/data/com.example.dexter007bot/files/Maps/" + text;
                    message.setImageurl(url);
                    message.setMapObjectID(mapObjectId);
                    break;
                }
                case "audio": {
                    String url = "Android/data/com.example.dexter007bot/files/Audio/" + text;
                    message.setUrl(url);
                    break;
                }
                case "video": {
                    String url = "Android/data/com.example.dexter007bot/files/Movies/" + text;
                    message.setUrl(url);
                    break;
                }
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return message;
    }*/


}
