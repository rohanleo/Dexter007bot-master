package com.example.dexter007bot;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;

import com.example.dexter007bot.Chats.ChatUtils;
import com.example.dexter007bot.Maps.MapActivity;
import com.example.dexter007bot.P2PConnect.ListenThread;
import com.example.dexter007bot.Service.P2PConnectService;
import com.example.dexter007bot.Service.P2PNearbyService;
import com.github.javiersantos.materialstyleddialogs.MaterialStyledDialog;
import com.github.javiersantos.materialstyleddialogs.enums.Duration;
import com.github.javiersantos.materialstyleddialogs.enums.Style;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;

import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
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
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Pattern;

import static com.example.dexter007bot.LoginActivity.logger;

public class MainActivity extends AppCompatActivity {

    private static ListView listView;
    FloatingActionButton btnSend;
    private static EditText edtTextMsg;
    public static String TAG = "MainActivity";
    private SharedPreferences preferences;
    private static String userName;

    private Bot bot;
    public static Chat chat;
    public static ChatMessageAdapter adapter;
    public static ArrayList<String>al = new ArrayList<String>();
    FloatingActionButton btnAttach;
    public static FloatingActionButton btnWifi;

    //layout
    LinearLayout atMap, atCamera, atVideo, atAudio, revealLayout;
    RelativeLayout relativeLayout;
    int cx,cy;
    Boolean hidden = true;

    //location
    private static Location currentLocation;
    private LocationManager locationManager;

    //audio
    boolean isRecording, isPlaying;
    private MediaRecorder recorder;
    private MediaPlayer mediaPlayer;
    private ImageView recordButton, playButton;
    private Chronometer chronometer;
    private Button backButton, okayButton;
    private TextView recordText;

    //Media
    static String mCurrentMediaPath;
    public static String tempImage=null,tempVideo=null, tempAudio=null ;
    Boolean isImage=false, isVideo=false, isAudio=false;

    //kml
    public static KmlDocument kml;
    public static File kmlFile;
    String kmlName;
    private static String totalChat, lastKey,response=null;
    private static int total;

    //connection
    //public static P2PConnectService myService;
    public static P2PNearbyService myService;
    public static boolean syncServiceBound = false;
    public static boolean myServiceBound = false;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //startService();
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Dextor Bot");
        preferences = getApplicationContext().getSharedPreferences("user_credentials",MODE_PRIVATE);

        initialize();
        //logger.write("Application Started : "+LoginActivity.userName);

        adapter = new ChatMessageAdapter(this,new ArrayList<ChatMessage>());
        listView.setAdapter(adapter);

        onclickListener();

        bot = new Bot("TBC",MagicStrings.root_path,"chat");
        chat = new Chat(bot);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
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
        userName = preferences.getString("user_name","no_user");

        //selfkml file
        kmlName ="KML_" + userName + ".kml";
        kmlFile = Environment.getExternalStoragePublicDirectory("DextorBot/DextorKml/SelfKml/" + kmlName);
        kml = new KmlDocument();
        if(!kmlFile.exists()){
            //if not existed
            try {
                kmlFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            kml.parseKMLFile(kmlFile);
            //initialize the session number
            kml.mKmlRoot.setExtendedData("total","0");
            kml.saveAsKML(kmlFile);
        }
        else {
            kml.parseKMLFile(kmlFile);
        }
        totalChat ="";
        response="";

        //get the session number
        total = Integer.parseInt(kml.mKmlRoot.getExtendedData("total"));
        total++;
        lastKey = getLatestKey();

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        //current location
        Location lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (lastLocation != null) {
            currentLocation = lastLocation;
        }
        if (preferences.getString("user_type","normal").matches("normal")){
            startService();
        } else {
            startAuthService();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void startAuthService() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        boolean isXOB = pref.getBoolean("xob_switch", false);
        MyTetheringActivity tetheringActivity = new MyTetheringActivity(MainActivity.this);
        if (isXOB){
            if (tetheringActivity.isTetherActive()){
                try {
                    new ListenThread().start();
                    Log.e("XOB","ListenerThread for XOB");
                } catch (SocketException e) {
                    e.printStackTrace();
                }
                btnWifi.setBackgroundTintList(ColorStateList.valueOf(Color.RED));
            } else {
                if (toggleXOB(tetheringActivity))
                    Log.d("XOB","xob started");
            }
        } else {
            if (tetheringActivity.isTetherActive())
                tetheringActivity.stopTethering();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private boolean toggleXOB(MyTetheringActivity tetheringActivity) {
        //tetheringActivity.configureHotspot("DisarmHotspotDB","password123");
        boolean isStarted = tetheringActivity.startTethering();
        if (isStarted){
            Log.e(TAG,"Hotspot Started");
            AlertDialog ad = new AlertDialog.Builder(this)
                    .setTitle("Alert!!")
                    .setMessage("WifiHotspot Credentials should be as:-\nSSID:- DisarmHotspotDB\npassword:- password123")
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    })
                    .create();
            ad.show();
            try {
                new ListenThread().start();
                Log.e("XOB","ListenerThread for XOB");
            } catch (SocketException e) {
                e.printStackTrace();
            }
            btnWifi.setBackgroundTintList(ColorStateList.valueOf(Color.RED));
        }
        return false;
    }

    private void onclickListener() {
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //send the user message for query
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
                //Move to map activity
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
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            if (preferences.getString("user_type","normal").matches("authorised")){
                Intent ii = new Intent(MainActivity.this, SettingActivity.class);
                startActivity(ii);
                finish();
            } else {
                AlertDialog ad = new AlertDialog.Builder(this)
                        .setTitle("Alert!!")
                        .setMessage("Only Authorised Personals have permission to change the settings")
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        })
                        .create();
                ad.show();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onResume() {
        super.onResume();
        checkMediaAvailabilty();
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, myLocationListener);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, myLocationListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        locationManager.removeUpdates(myLocationListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (preferences.getString("user_type","normal").matches("normal")){
            unbindAllService();
        }
    }

    /**
     * check if the media is available before displaying in chat
     */
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

    /**
     * check for the path of the media
     */
    private boolean checkPath(String fileName, String type) {
        if(type.equals("image")){
            File image=Environment.getExternalStoragePublicDirectory("DextorBot/DextorImage/Image/" + fileName);
            //System.out.print("checkPath:-image  " + tempImage + " " + fileName);
            if(!image.exists()){
                //System.out.println(" false" );
                return false;
            }
            logger.write("Image File Created : "+ fileName);
        }
        else if(type.equals("video")){
            File video=Environment.getExternalStoragePublicDirectory("DextorBot/DextorVideo/Video/" + fileName);
            //System.out.print("checkPath:-video  " + tempVideo + " " + fileName);
            if(!video.exists()){
                return false;
            }
            logger.write("Video File Created : "+ fileName);
        }
        else if(type.equals("audio")){
            File audio=Environment.getExternalStoragePublicDirectory("DextorBot/DextorAudio/Audio/" + fileName);
            //Log.e("CheckPath: ", "audio fileName:- " + fileName);
            if(!audio.exists()){
                return false;
            }
            logger.write("Audio File Created : "+ fileName);
        }
        return true;
    }

    /**
     * opens the attachment window
     */
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

    /**
     * Passing message to Array adapter for displaying in chat
     */
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

    /**
     * Main function for user-bot interaction
     * bot response for each user query
     */
    public static void  fun(String message,String type){
        String dummy;
        //Log.e("fun:- ", type + message);
        //check if chat is just initialized
        if(response==null) dummy = message;
        else dummy=response + ":" + message;

        //appending current message to total message
        totalChat += dummy + "-";

        //check for type of user interaction
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

        //bots reply according to the user interaction
        response = chat.multisentenceRespond(message);
        botsReply(response);

        //for Buttons
        for(int i=0;i<al.size();i++)
        {
            String dum =al.get(i);
            botButton(dum);
        }
        al.clear();

        //clear editText
        edtTextMsg.setText("");
        listView.setSelection(adapter.getCount()-1);

        //saving the chat in kml extended data
        kml.mKmlRoot.setExtendedData("total",total + "");
        String msg = ChatUtils.getExtendedDataFormatName(totalChat,total + "",currentLocation);
        kml.mKmlRoot.setExtendedData(lastKey,msg);
        kml.saveAsKML(kmlFile);
    }

    /**
     * Capture the image
     */
    private void   CaptureImage(){
        //opens system camera
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_SIZE_LIMIT, "262144");

        String timeStamp=new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        //image name and path
        String fileName = "IMG" +  "_" + userName +"_"+timeStamp + ".jpg";
        String path="DextorBot/DextorImage/Image/";
        File image = Environment.getExternalStoragePublicDirectory(path + fileName);

        Uri uri = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".provider", image);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        cameraIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivityForResult(cameraIntent,1000);

        tempImage = image.getName();
        mCurrentMediaPath = image.getAbsolutePath();
    }

    /**
     * Record Video
     */
    private void RecordVideo() {
        //opens system camera
        Intent cameraIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        String timeStamp=new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        //video name and path
        String fileName = "VID" +  "_" + userName+"_" +timeStamp +".mp4";
        String path = "DextorBot/DextorVideo/Video/";
        File video = Environment.getExternalStoragePublicDirectory(path + fileName);

        Uri uri = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".provider", video);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        cameraIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        cameraIntent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 10);
        startActivityForResult(cameraIntent, 1001);

        tempVideo = video.getName();
        mCurrentMediaPath=video.getAbsolutePath();
    }

    /**
     * Record Audio
     */
    private void RecordAudio(){
        //view for the dialog box
        View view = getLayoutInflater().inflate(R.layout.audio_popup_layout, null);

        //opens a dialog box for audio recording
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

        //audio name and path
        final String fileName ="AUD" + "_" +  userName + "_" + timeStamp + ".mp3";
        String path = "DextorBot/DextorAudio/Audio/";
        final String finalFilePath = Environment.getExternalStoragePublicDirectory(path + fileName).getAbsolutePath();

        tempAudio = fileName;
        mCurrentMediaPath = finalFilePath;

        //closes the dialog box
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

        //saves the recorded audio
        okayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                materialStyledDialog.dismiss();
                stopPlaying();
                isAudio=true;
                checkMediaAvailabilty();
            }
        });

        //to start/stop recording
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

        //play/pause the recorded audio
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
    }

    /**
     * play the recorded audio
     * @param filePath
     */
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

    /**
     * stop playing the recorded audio
     */
    private void stopPlaying() {
        if (mediaPlayer != null) mediaPlayer.release();
        isPlaying = false;
        mediaPlayer = null;
        chronometer.stop();
    }

    /**
     * start recording
     * @param filePath
     */
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

    /**
     * stop recording
     */
    private void stopRecording() {
        recorder.stop();
        chronometer.stop();
        recorder.release();
        recorder = null;
    }

    private static String getLatestKey(){
        String nextKey = userName;
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

    /**
     * Location listener to get current location
     */
    private LocationListener myLocationListener = new LocationListener()
    {
        @Override
        public void onLocationChanged(Location location)
        {
            currentLocation = location;
        }

        @Override
        public void onProviderDisabled(String provider)
        {
        }

        @Override
        public void onProviderEnabled(String provider)
        {
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras)
        {
        }
    };

    private void startService(){
        final Intent p2pServiceIntent = new Intent(getApplicationContext(), P2PNearbyService.class);
        bindService(p2pServiceIntent, myServiceConnection, Context.BIND_AUTO_CREATE);
        startService(p2pServiceIntent);
        if(!locationServicesEnabled())
            enableGPS();
    }

    private void unbindAllService(){
        final Intent myServiceIntent = new Intent(getApplicationContext(),P2PNearbyService.class);
        if(myServiceBound){
            unbindService(myServiceConnection);
        }
        myServiceBound = false;
        stopService(myServiceIntent);
    }

    public static ServiceConnection myServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            P2PNearbyService.P2PNearbyServiceBinder binder = (P2PNearbyService.P2PNearbyServiceBinder) service;
            myService = binder.getService();
            myServiceBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            myServiceBound = false;
        }
    };

    public void enableGPS() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder
                .setMessage(R.string.gps_msg)
                .setCancelable(false)
                .setTitle("Turn on Location")
                .setPositiveButton(R.string.enable_gps,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int id) {
                                Intent callGPSSettingIntent = new Intent(
                                        Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                startActivityForResult(callGPSSettingIntent, 5);
                            }
                        });
        alertDialogBuilder.setNegativeButton(R.string.cancel,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }

    public boolean locationServicesEnabled() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            int locationMode = Settings.Secure.LOCATION_MODE_OFF;

            try {
                locationMode = Settings.Secure.getInt(getContentResolver(), Settings.Secure.LOCATION_MODE);

            } catch (Settings.SettingNotFoundException ignored) {
                // This is ignored
            }

            return locationMode != Settings.Secure.LOCATION_MODE_OFF;

        } else {
            String locationProviders = Settings.Secure.getString(getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            return !TextUtils.isEmpty(locationProviders);
        }
    }

}