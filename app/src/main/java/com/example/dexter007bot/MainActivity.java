package com.example.dexter007bot;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;

import com.example.dexter007bot.Chats.ChatUtils;
import com.example.dexter007bot.Connection.PeerConnection;
import com.example.dexter007bot.Connection.WiFiDirect;
import com.example.dexter007bot.Maps.MapActivity;
import com.github.javiersantos.materialstyleddialogs.MaterialStyledDialog;
import com.github.javiersantos.materialstyleddialogs.enums.Duration;
import com.github.javiersantos.materialstyleddialogs.enums.Style;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.os.SystemClock;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.dexter007bot.Adapter.ChatMessageAdapter;
import com.example.dexter007bot.Model.ChatMessage;

import org.alicebot.ab.Bot;
import org.alicebot.ab.Chat;
import org.alicebot.ab.MagicStrings;
import org.osmdroid.bonuspack.kml.KmlDocument;

import java.io.File;
import java.io.IOException;
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

    FloatingActionButton btnAttach, btnWifi;
    LinearLayout atMap, atCamera, atVideo, atAudio, revealLayout;
    int cx,cy;
    Boolean hidden = true;

    //audio
    boolean isRecording, isPlaying;
    private MediaRecorder recorder;
    private MediaPlayer mediaPlayer;
    private ImageView recordButton, playButton;
    private Chronometer chronometer;
    private Button backButton, okayButton;
    private TextView recordText;

    static String mCurrentMediaPath;
    public static String tempImage=null,tempVideo=null, tempAudio=null ;
    Boolean isImage=false, isVideo=false, isAudio=false;

    public static KmlDocument kml;
    public static File kmlFile;
    String kmlName;
    private static String totalChat, lastKey,response=null;
    private static int total;

    RelativeLayout relativeLayout;

    public static WifiManager wifiManager;
    WifiP2pManager mManager;
    WifiP2pManager.Channel mChannel;
    PeerConnection peerConnection;

    WiFiDirect mReceiver;
    IntentFilter mIntentFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initialize();

        adapter = new ChatMessageAdapter(this,new ArrayList<ChatMessage>());
        listView.setAdapter(adapter);

        onclickListener();

        bot = new Bot("TBC",MagicStrings.root_path,"chat");
        chat = new Chat(bot);
    }

    private void initialize() {
        relativeLayout = findViewById(R.id.relative);
        listView = findViewById(R.id.listView);
        btnSend = findViewById(R.id.btnSend);
        edtTextMsg = findViewById(R.id.edtTextMsg);

        btnWifi = findViewById(R.id.btnWifi);
        btnAttach = findViewById(R.id.btnAttach);
        revealLayout = findViewById(R.id.reveal_items2);
        revealLayout.setVisibility(View.INVISIBLE);
        cx = revealLayout.getRight();
        cy =  revealLayout.getBottom();
        atCamera = findViewById(R.id.atCamera);
        atMap = findViewById(R.id.atMap);
        atVideo = findViewById(R.id.atVideo);
        atAudio = findViewById(R.id.atAudio);

        kmlName ="KML_" + LoginActivity.userName + ".kml";
        kmlFile = Environment.getExternalStoragePublicDirectory("DextorBot/DextorKml/SelfKml/" + kmlName);
        kml = new KmlDocument();
        if(!kmlFile.exists()){
            try {
                kmlFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            kml.parseKMLFile(kmlFile);
            kml.mKmlRoot.setExtendedData("total","0");
            kml.saveAsKML(kmlFile);
        }
        else {
            kml.parseKMLFile(kmlFile);
        }
        totalChat ="";
        response="";
        total = Integer.parseInt(kml.mKmlRoot.getExtendedData("total"));
        total++;
        lastKey = getLatestKey();

        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(this,getMainLooper(),null);
        mReceiver = new WiFiDirect(mManager,mChannel,this);
        peerConnection = new PeerConnection(mManager, mChannel, this);
        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_DISCOVERY_CHANGED_ACTION);
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
                isImage = true;
            }
        });
        atVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                makeEffect(revealLayout,cx,cy);
                RecordVideo();
                isVideo = true;
            }
        });
        atAudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                makeEffect(revealLayout,cx,cy);
                RecordAudio();
                //Log.e("atAudio listner:", "audio");
                //isAudio=true;
                //checkMediaAvailabilty();
            }
        });
        btnWifi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                peerConnection.Discover();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkMediaAvailabilty();
        registerReceiver(mReceiver,mIntentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mReceiver);
    }

    private void checkMediaAvailabilty() {
        if(isImage){
            //Log.e("checkMediaAvailabili:", "image");
            boolean check = checkPath(tempImage,"image");
            if(check){
                fun(tempImage,"image");
            }
            isImage = false;
            tempImage = null;
        }
        if(isVideo){
            boolean check = checkPath(tempVideo,"video");
            if(check) fun(tempVideo,"video");
            isVideo = false;
            tempVideo = null;
        }
        if(isAudio){
            //Log.e("checkMediaAvailabili:", "audio" + isAudio);
            //Log.e("checkMediaAvailabili:", "tempAudio:- " + tempAudio);
            boolean check = checkPath(tempAudio,"audio");
            //Log.e("checkMediaAvailabili:", "check:- " + check);
            if(check) fun(tempAudio,"audio");
            isAudio = false;
            tempAudio = null;
        }
    }

    private boolean checkPath(String fileName, String type) {
        if(type.equals("image")){
            File image=Environment.getExternalStoragePublicDirectory("DextorBot/DextorImage/Image/" + fileName);
            //System.out.print("checkPath:-image  " + tempImage + " " + fileName);
            if(!image.exists()){
                //System.out.println(" false" );
                return false;
            }
        }
        else if(type.equals("video")){
            File video=Environment.getExternalStoragePublicDirectory("DextorBot/DextorVideo/Video/" + fileName);
            //System.out.print("checkPath:-video  " + tempVideo + " " + fileName);
            if(!video.exists()){
                return false;
            }
        }
        else if(type.equals("audio")){
            File audio=Environment.getExternalStoragePublicDirectory("DextorBot/DextorAudio/Audio/" + fileName);
            //Log.e("CheckPath: ", "audio fileName:- " + fileName);
            if(!audio.exists()){
                return false;
            }
        }
        return true;
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
    private static void replyAudio() {
        ChatMessage chatMessage = new ChatMessage(5,mCurrentMediaPath);
        adapter.add(chatMessage);
    }
    public static void  fun(String message,String type){
        String dummy;
        //Log.e("fun:- ", type + message);
        if(response==null) dummy = message;
        else dummy=response + ":" + message;
        totalChat += dummy + "-";
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
            replyAudio();
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

        kml.mKmlRoot.setExtendedData("total",total + "");

        String uniqueId = generateRandomString();
        String msg = ChatUtils.getExtendedDataFormatName(totalChat,total + "",uniqueId);
        kml.mKmlRoot.setExtendedData(lastKey,msg);
        kml.saveAsKML(kmlFile);
    }

    private void   CaptureImage(){
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_SIZE_LIMIT, "262144");
        String timeStamp=new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String fileName = "IMG" + timeStamp + "_" + LoginActivity.userName + ".jpg";
        String path="DextorBot/DextorImage/Image/";
        File image = Environment.getExternalStoragePublicDirectory(path + fileName);
        Uri uri = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".provider", image);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        cameraIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivityForResult(cameraIntent, 1000);
        tempImage = image.getName();
        mCurrentMediaPath = image.getAbsolutePath();
        //Log.e("CaptureImage: ", "mCurrentMediaPath:- " + mCurrentMediaPath);
        //fun(fileName,"image");
    }

    private void RecordVideo() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        String timeStamp=new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String fileName = "VID" + timeStamp + "_" + LoginActivity.userName+ ".mp4";
        String path = "DextorBot/DextorVideo/Video/";
        File video = Environment.getExternalStoragePublicDirectory(path + fileName);
        Uri uri = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".provider", video);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        cameraIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        cameraIntent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 10);
        startActivityForResult(cameraIntent, 1001);
        tempVideo = video.getName();
        mCurrentMediaPath=video.getAbsolutePath();
        //Log.e("RecordVideo: ", "mCurrentMediaPath:- " + mCurrentMediaPath);
        //return fileName;
    }

    private void RecordAudio(){
        View view = getLayoutInflater().inflate(R.layout.audio_popup_layout, null);
        final MaterialStyledDialog materialStyledDialog = new MaterialStyledDialog.Builder(MainActivity.this)
                .setTitle("Audio")
                .setCustomView(view, 10, 20, 10, 20)
                .withDialogAnimation(true, Duration.FAST)
                .setCancelable(false)
                .setStyle(Style.HEADER_WITH_TITLE)
                .withDarkerOverlay(true)
                .build();
        recordButton = view.findViewById(R.id.record_button_dialog);
        chronometer = view.findViewById(R.id.chronometer);
        backButton = view.findViewById(R.id.record_dialog_back_button);
        okayButton = view.findViewById(R.id.record_dialog_ok_button);
        recordText = view.findViewById(R.id.record_button_text);
        playButton = view.findViewById(R.id.play_button_dialog);

        playButton.setEnabled(false);
        isRecording = false;
        isPlaying = false;
        recorder = null;

        String timeStamp=new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        final String fileName ="AUD" + "_" + timeStamp + "_" + LoginActivity.userName + ".mp3";
        String path = "DextorBot/DextorAudio/Audio/";
        final String finalFilePath = Environment.getExternalStoragePublicDirectory(path + fileName).getAbsolutePath();
        tempAudio = fileName;
        mCurrentMediaPath = finalFilePath;
        //Log.e("RecordAudio: ", "mCurrentMediaPath:- " + mCurrentMediaPath);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                materialStyledDialog.dismiss();
                stopPlaying();
                File file = new File(finalFilePath);
                if (file.exists())
                    file.delete();
            }
        });
        okayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                materialStyledDialog.dismiss();
                stopPlaying();
                isAudio=true;
                checkMediaAvailabilty();
            }
        });

        recordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isRecording) {
                    startRecording(finalFilePath);
                    backButton.setEnabled(false);
                    okayButton.setEnabled(false);
                    playButton.setEnabled(false);
                    playButton.setImageResource(R.drawable.ic_play_arrow_black_24dp);
                    recordText.setText("Recording...");
                    recordButton.setImageResource(R.drawable.ic_mic_black_24dp);
                    isRecording = true;
                } else {
                    stopRecording();
                    backButton.setEnabled(true);
                    okayButton.setEnabled(true);
                    playButton.setEnabled(true);
                    playButton.setImageResource(R.drawable.ic_play_arrow_black_24dp);
                    recordText.setText("Record");
                    recordButton.setImageResource(R.drawable.ic_mic_black_24dp);
                    isRecording = false;
                }
            }
        });

        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isPlaying) {
                    startPlaying(finalFilePath);
                    recordButton.setImageResource(R.drawable.ic_mic_off_black_24dp);
                    recordButton.setEnabled(false);
                    playButton.setImageResource(R.drawable.ic_pause_black_24dp);
                } else {
                    stopPlaying();
                    recordButton.setImageResource(R.drawable.ic_mic_black_24dp);
                    recordButton.setEnabled(true);
                    playButton.setImageResource(R.drawable.ic_play_arrow_black_24dp);
                }
            }
        });

        materialStyledDialog.show();
        //materialDialog.dismiss();
        //fun(tempAudio,"audio");
        //return fileName;
    }

    private void startPlaying(String filePath) {
        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(filePath);
            mediaPlayer.prepare();
            chronometer.setBase(SystemClock.elapsedRealtime());
            chronometer.start();
            mediaPlayer.start();
            isPlaying = true;
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    stopPlaying();
                    recordButton.setImageResource(R.drawable.ic_mic_black_24dp);
                    recordButton.setEnabled(true);
                    playButton.setImageResource(R.drawable.ic_play_arrow_black_24dp);
                }
            });
        } catch (IOException e) {
            Log.e("AUDIO_CHAT", "prepare() failed");
        }

    }

    private void stopPlaying() {
        if (mediaPlayer != null)
            mediaPlayer.release();
        isPlaying = false;
        mediaPlayer = null;
        chronometer.stop();
    }

    private void startRecording(String filePath) {

        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        recorder.setOutputFile(filePath);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        try {
            recorder.prepare();
        } catch (IOException e) {
            Log.e("AUDIO_CHAT", "prepare() failed");
        }

        recorder.start();
        chronometer.setBase(SystemClock.elapsedRealtime());
        chronometer.start();
    }

    private void stopRecording() {
        recorder.stop();
        chronometer.stop();
        recorder.release();
        recorder = null;
    }

    private static String generateRandomString(){
        SecureRandom secureRandom = new SecureRandom();
        byte[] token = new byte[8];
        secureRandom.nextBytes(token);
        return new BigInteger(1, token).toString(16);
    }

    private static String getLatestKey(){
        String nextKey = LoginActivity.userName;
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
