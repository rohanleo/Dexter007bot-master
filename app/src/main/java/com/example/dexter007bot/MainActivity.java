package com.example.dexter007bot;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;

import com.example.dexter007bot.AIML.sample;
import com.example.dexter007bot.Chats.ChatUtils;
import com.example.dexter007bot.Maps.MapActivity;
import com.example.dexter007bot.Maps.SavedActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.dexter007bot.Adapter.ChatMessageAdapter;
import com.example.dexter007bot.Model.ChatMessage;

import org.alicebot.ab.AIMLProcessor;
import org.alicebot.ab.Bot;
import org.alicebot.ab.Chat;
import org.alicebot.ab.MagicStrings;
import org.alicebot.ab.PCAIMLProcessorExtension;
import org.osmdroid.bonuspack.kml.KmlDocument;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    static ListView listView;
    FloatingActionButton btnSend;
    static EditText edtTextMsg;
    ImageView imageView;
    public static String TAG = "MainActivity";

    private Bot bot;
    public static Chat chat;
    public static ChatMessageAdapter adapter;
    public static ArrayList<String>al = new ArrayList<String>();
    //public static String id;
    public static final int MULTIPLE_PERMISSIONS = 10;
    private final String[] permissions = new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA};
    FloatingActionButton fab;
    FloatingActionButton img;
    FloatingActionButton vid;
    FloatingActionButton map;
    FloatingActionButton save;
    Boolean isOpen =false;

    String mCurrentPhotoPath;
    public static String tempImage,response=null;
    private static KmlDocument kml ;
    static File kmlFile;
    String kmlName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fab = findViewById(R.id.floatBtn);
        img = findViewById(R.id.Image);
        vid = findViewById(R.id.Video);
        map = findViewById(R.id.Map);
        save = findViewById(R.id.save);
        isOpen =false;
        kmlName = generateRandomString() + ".kml";
        File strDir=Environment.getExternalStoragePublicDirectory("KML");
        if(!strDir.exists()) strDir.mkdir();
        kmlFile = Environment.getExternalStoragePublicDirectory("KML/" + kmlName);
        kml = new KmlDocument();
        if(!kmlFile.exists()){
            try {
                kmlFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        kml.parseKMLFile(kmlFile);
        listView = findViewById(R.id.listView);
        btnSend = findViewById(R.id.btnSend);
        edtTextMsg = findViewById(R.id.edtTextMsg);
        imageView = findViewById(R.id.imageView);
        if(!hasPermissions(this,permissions)){
            ActivityCompat.requestPermissions(this,permissions,MULTIPLE_PERMISSIONS);
        }
        adapter = new ChatMessageAdapter(this,new ArrayList<ChatMessage>());
        listView.setAdapter(adapter);

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String message = edtTextMsg.getText().toString();
                if(TextUtils.isEmpty(message)) {
                    Toast.makeText(MainActivity.this,"Please enter a query",Toast.LENGTH_SHORT).show();
                    return;
                }
                fun(message);
            }
        });


        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isOpen){
                    map.hide();
                    img.hide();
                    vid.hide();
                    save.hide();
                    img.setClickable(false);
                    vid.setClickable(false);
                    map.setClickable(false);
                    save.setClickable(false);
                    isOpen=false;
                }
                else
                {
                    img.show();
                    vid.show();
                    map.show();
                    save.show();
                    img.setClickable(true);
                    vid.setClickable(true);
                    map.setClickable(true);
                    save.setClickable(true);
                    isOpen=true;
                }
            }
        });
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SavedActivity.class);
                startActivity(intent);
            }
        });
        map.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, MapActivity.class);
                startActivity(intent);
            }
        });
        img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CaptureImage(v);
            }
        });
        vid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RecordVideo(v);
            }
        });

        boolean available = isSDCardAvailable();

        AssetManager assets = getResources().getAssets();
        String out1= Environment.getExternalStorageDirectory().getAbsolutePath();
        File fileName = new File(out1 + "/TBC/bots/TBC");

        boolean makeFile = fileName.mkdirs();

        if (fileName.exists()) {

            //read the line
            try {

                for (String dir : assets.list("TBC")) {

                    File subDir = new File(fileName.getPath() + "/" + dir);
                    boolean subDir_Check = subDir.mkdirs();

                    for (String file : assets.list("TBC/" + dir)) {
                        File newFile = new File(fileName.getPath() + "/" + dir +"/" + file);

                        if(newFile.exists()){
                            continue;
                        }


                        InputStream in;
                        OutputStream out;
                        String str;
                        in = assets.open("TBC/" + dir +"/" + file);
                        out = new FileOutputStream(fileName.getPath() + "/" + dir +"/" + file);

                        // copy files from assets to the mobile or any secondary storage

                        copyFile(in,out);
                        in.close();
                        out.flush();
                        out.close();

                    }
                }
            }catch (IOException e){
                e.printStackTrace();
            }
        }

        // get the working directory
        MagicStrings.root_path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/TBC";
        AIMLProcessor.extension = new PCAIMLProcessorExtension();

        AIMLProcessor.extension= new sample();

        bot = new Bot("TBC",MagicStrings.root_path,"chat");
        chat = new Chat(bot);
    }


    private void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;

        while((read = in.read(buffer))!=-1){
            out.write(buffer,0,read);
        }
    }

    public static boolean isSDCardAvailable() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)? true: false;
    }

    private static void botsReply(String response) {
        ChatMessage chatMessage = new ChatMessage(1,response);
        adapter.add(chatMessage);
    }
    private static void sendMessage(String message) {
        ChatMessage chatMessage = new ChatMessage(0,message);
        adapter.add(chatMessage);
    }
    private static void botButton(String message) {
        ChatMessage chatMessage = new ChatMessage(2,message);
        adapter.add(chatMessage);
    }
    public static void  fun(String message){
        System.out.println(message);
        String dummy;
        if(response==null) dummy = message;
        else dummy=response + ":" + message;
        response = chat.multisentenceRespond(message);
        sendMessage(message);
        botsReply(response);
        String uniqueId = generateRandomString();
        String lastKey = getLatestKey();
        String msg = ChatUtils.getExtendedDataFormatName(dummy,"map",uniqueId);
        kml.mKmlRoot.setExtendedData(lastKey,msg);
        kml.saveAsKML(kmlFile);
        for(int i=0;i<al.size();i++)
        {
            String dum =al.get(i);
            botButton(dum);
        }
        al.clear();

        //clear editText
        edtTextMsg.setText("");
        listView.setSelection(adapter.getCount()-1);
    }

    public boolean hasPermissions(Context context, String... permissions){
        if(context!=null && permissions!=null ){
            for(String permission:permissions){
                if(ActivityCompat.checkSelfPermission(context,permission)!=PackageManager.PERMISSION_GRANTED){
                    return false;
                }
            }
        }
        return true;
    }

    static final int REQUEST_TAKE_PHOTO = 1;
    static final int REQUEST_VIDEO_CAPTURE=1;


    public void CaptureImage(View view) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
                Toast.makeText(this, "Image File Created!", Toast.LENGTH_SHORT).show();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.android.fileprovidermedia",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
                //setPic();
            }
        }
    }

    //create image name
    private File createImageFile() throws IOException {
        String timeStamp=new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName="JPG"+timeStamp+"_";
        File storgeDir= getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image=File.createTempFile(imageFileName,".jpg",storgeDir);
        mCurrentPhotoPath=image.getAbsolutePath();
        tempImage=image.getName();
        System.out.println("filename-" + tempImage);
        System.out.println("Directory" + mCurrentPhotoPath);
        return image;
    }

    public void RecordVideo(View video){
        Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        if (takeVideoIntent.resolveActivity(getPackageManager()) != null) {
            File VideoFile = null;
            try {
                VideoFile = createVideoFile();
                Toast.makeText(this, "Video File Created!", Toast.LENGTH_SHORT).show();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (VideoFile != null) {
                Uri videoURI = FileProvider.getUriForFile(this,
                        "com.example.android.fileprovidermedia",
                        VideoFile);

                takeVideoIntent.putExtra(MediaStore.EXTRA_OUTPUT, videoURI);
                startActivityForResult(takeVideoIntent, REQUEST_VIDEO_CAPTURE);
            }
        }
    }

    private File createVideoFile() throws IOException{
        String timeStamp=new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String videoFileName="VID"+timeStamp+"_";
        File storgeDir= getExternalFilesDir(Environment.DIRECTORY_MOVIES);
        File video=File.createTempFile(videoFileName,".mp4",storgeDir);
        mCurrentPhotoPath=video.getAbsolutePath();
        return video;
    }

    private static String generateRandomString(){
        SecureRandom secureRandom = new SecureRandom();
        byte[] token = new byte[8];
        secureRandom.nextBytes(token);
        return new BigInteger(1, token).toString(16);
    }

    private static String getLatestKey(){
        String nextKey = "source";
        String msg;
        while (kml.mKmlRoot.mExtendedData!=null && kml.mKmlRoot.mExtendedData.containsKey(nextKey)){
            msg = kml.mKmlRoot.getExtendedData(nextKey);
            nextKey = getTimeStampFromMsg(msg);
        }
        return nextKey;
    }
    private static String getTimeStampFromMsg(String msg){
        Pattern p = Pattern.compile("-");
        String[] s = p.split(msg,4);
        return s[0];
    }
}
