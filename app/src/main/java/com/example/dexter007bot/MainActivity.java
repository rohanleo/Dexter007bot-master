package com.example.dexter007bot;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.Build;
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
import android.view.ViewAnimationUtils;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
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

    FloatingActionButton btnAttach;
    LinearLayout atMap, atCamera, atVideo, revealLayout;
    int cx,cy;
    Boolean hidden = true;

    static String mCurrentMediaPath;
    public static String tempImage=null,response=null;
    public static KmlDocument kml ;
    static File kmlFile;
    String kmlName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initialize();

        AddDirectory.addDirectory();

        if(!hasPermissions(this,permissions)){
            ActivityCompat.requestPermissions(this,permissions,MULTIPLE_PERMISSIONS);
        }

        adapter = new ChatMessageAdapter(this,new ArrayList<ChatMessage>());
        listView.setAdapter(adapter);

        onclickListener();

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

    private void onclickListener() {
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String message = edtTextMsg.getText().toString();
                if(TextUtils.isEmpty(message)) {
                    Toast.makeText(MainActivity.this,"Please enter a query",Toast.LENGTH_SHORT).show();
                    return;
                }
                fun(message,"text");
            }
        });
        btnAttach.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                makeEffect(revealLayout,cx,cy);
            }
        });
        atMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                makeEffect(revealLayout,cx,cy);
                Intent intent = new Intent(MainActivity.this, MapActivity.class);
                startActivity(intent);
            }
        });
        atCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                makeEffect(revealLayout,cx,cy);
                CaptureImage();
            }
        });
        atVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                makeEffect(revealLayout,cx,cy);
                RecordVideo();
            }
        });
    }

    private void makeEffect(final LinearLayout layout, int cx, int cy) {
        int radius = Math.max(layout.getWidth(), layout.getHeight());

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {


            Animator animator =
                    ViewAnimationUtils.createCircularReveal(layout, cx, cy, 0, radius);
            animator.setInterpolator(new AccelerateDecelerateInterpolator());
            animator.setDuration(800);

            //Animator animator_reverse = animator.reverse();

            if (hidden) {
                layout.setVisibility(View.VISIBLE);
                animator.start();
                hidden = false;
            } else {
                Animator anim = android.view.ViewAnimationUtils.createCircularReveal(layout, cx, cy, radius, 0);
                anim.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        layout.setVisibility(View.INVISIBLE);
                        hidden = true;
                    }
                });
                anim.start();
            }
        } else {
            if (hidden) {
                Animator anim = android.view.ViewAnimationUtils.createCircularReveal(layout, cx, cy, 0, radius);
                layout.setVisibility(View.VISIBLE);
                anim.start();
                hidden = false;

            } else {
                Animator anim = android.view.ViewAnimationUtils.createCircularReveal(layout, cx, cy, radius, 0);
                anim.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        layout.setVisibility(View.INVISIBLE);
                        hidden = true;
                    }
                });
                anim.start();

            }
        }
    }

    private void initialize() {
        listView = findViewById(R.id.listView);
        btnSend = findViewById(R.id.btnSend);
        edtTextMsg = findViewById(R.id.edtTextMsg);
        imageView = findViewById(R.id.imageView);

        btnAttach = findViewById(R.id.btnAttach);
        revealLayout = findViewById(R.id.reveal_items2);
        revealLayout.setVisibility(View.INVISIBLE);
        cx = revealLayout.getRight();
        cy =  revealLayout.getBottom();
        atCamera = findViewById(R.id.atCamera);
        atMap = findViewById(R.id.atMap);
        atVideo = findViewById(R.id.atVideo);

        kmlName ="KML" + generateRandomString() + ".kml";
        kmlFile = Environment.getExternalStoragePublicDirectory("DextorBot/DextorKml/" + kmlName);
        kml = new KmlDocument();
        if(!kmlFile.exists()){
            try {
                kmlFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        kml.parseKMLFile(kmlFile);
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
    private static void replyImage(){
        ChatMessage chatMessage = new ChatMessage(3,mCurrentMediaPath);
        adapter.add(chatMessage);
    }
    private static void replyVideo(){
        ChatMessage chatMessage = new ChatMessage(4,mCurrentMediaPath);
        adapter.add(chatMessage);
    }
    public static void  fun(String message,String type){
        String dummy;
        if(response==null) dummy = message;
        else dummy=response + "-" + message;
        if(type =="image"){
            message = "qwertyuu";
            replyImage();
        }
        else if(type == "video"){
            message = "qwertyuu";
            replyVideo();
        }
        else if(type == "audio"){
            message = "qwertyuu";

        }
        else if(type == "text"){
            sendMessage(message);
        }
        response = chat.multisentenceRespond(message);
        botsReply(response);
        for(int i=0;i<al.size();i++)
        {
            String dum =al.get(i);
            botButton(dum);
        }
        al.clear();
        //clear editText
        edtTextMsg.setText("");
        listView.setSelection(adapter.getCount()-1);
        String uniqueId = generateRandomString();
        String lastKey = getLatestKey();
        String msg = ChatUtils.getExtendedDataFormatName(dummy,type,uniqueId);
        kml.mKmlRoot.setExtendedData(lastKey,msg);
        kml.saveAsKML(kmlFile);
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

    public void CaptureImage(){
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_SIZE_LIMIT, "262144");
        String timeStamp=new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String fileName = "IMG" + timeStamp + "_" + generateRandomString() + ".jpg";
        String path="DextorBot/DextorImage/";
        File image = Environment.getExternalStoragePublicDirectory(path + fileName);
        Uri uri = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".provider", image);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        cameraIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivityForResult(cameraIntent, 1000);
        tempImage = image.getName();
        mCurrentMediaPath = image.getAbsolutePath();
        fun(fileName,"image");
    }

    private void RecordVideo() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        String timeStamp=new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String fileName = "VID" + timeStamp + "_" + generateRandomString() + ".mp4";
        String path = "DextorBot/DextorVideo/";
        File video = Environment.getExternalStoragePublicDirectory(path + fileName);
        Uri uri = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".provider", video);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        cameraIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        cameraIntent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 10);
        startActivityForResult(cameraIntent, 1001);
        mCurrentMediaPath=video.getAbsolutePath();
        fun(fileName,"video");
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
