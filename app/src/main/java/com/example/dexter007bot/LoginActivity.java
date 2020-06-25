package com.example.dexter007bot;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.dexter007bot.AIML.sample;
import com.example.dexter007bot.SummaryVector.Logger;

import org.alicebot.ab.AIMLProcessor;
import org.alicebot.ab.MagicStrings;
import org.alicebot.ab.PCAIMLProcessorExtension;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.regex.Pattern;

public class LoginActivity extends AppCompatActivity {

    private SharedPreferences sp;
    public static String userEmail;
    public static String userPhoneNum;
    public static String userName;
    private EditText email;
    private EditText phone;

    private Button submit;

    public static Logger logger;

    public static final int MULTIPLE_PERMISSIONS = 15;
    private final String[] permissions = new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.CHANGE_WIFI_STATE,
            Manifest.permission.INTERNET,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.CHANGE_NETWORK_STATE};

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Asking Permissions
        while(!hasPermissions(this,permissions)){
            ActivityCompat.requestPermissions(this,permissions,MULTIPLE_PERMISSIONS);
        }

        //Adding directories
        AddDirectory.addDirectory();

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

        sp = getSharedPreferences("First log",0);
        if(sp.getString("First login","").toString().equals("no")){
            //Not first time login
            userEmail = sp.getString("email","");
            userPhoneNum = sp.getString("phone","");
            userName = getSource(userEmail) + "_" +userPhoneNum;
            logger = new Logger();

            //Move to main activity
            Intent i = new Intent(LoginActivity.this,MainActivity.class);
            startActivity(i);
            finish();
        }else{
            //first time login
            setContentView(R.layout.activity_login);
            email = findViewById(R.id.emailText);
            phone = findViewById(R.id.phoneText);
            submit = findViewById(R.id.submitButton);
            submit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //check if user details are filled or not
                    if(email.getText().toString().matches("") || phone.getText().toString().matches("")){
                        Toast.makeText(LoginActivity.this,"Please fill the details",Toast.LENGTH_LONG).show();
                    }else{
                        //Store the details of user in sharedpreferences
                        SharedPreferences.Editor editor = sp.edit();
                        editor.putString("First login","no");
                        editor.putString("email", String.valueOf(email.getText()));
                        editor.putString("phone", String.valueOf(phone.getText()));
                        editor.commit();

                        userEmail = sp.getString("email","");
                        userPhoneNum = sp.getString("phone","");
                        userName = getSource(userEmail) + "_" +userPhoneNum;
                        logger = new Logger();
                        //Move to main activity
                        Intent i = new Intent(LoginActivity.this,MainActivity.class);
                        startActivity(i);
                        finish();

                    }
                }
            });
        }
    }

    private static String getSource(String msg){
        Pattern p = Pattern.compile("@");
        String[] s = p.split(msg,2);
        return s[0];
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

    /**
     * checking for permissions access
     */
    public boolean hasPermissions(Context context, String... permissions){
        if(context!=null && permissions!=null ){
            for(String permission:permissions){
                if(ActivityCompat.checkSelfPermission(context,permission)!= PackageManager.PERMISSION_GRANTED){
                    return false;
                }
            }
        }
        return true;
    }

}
