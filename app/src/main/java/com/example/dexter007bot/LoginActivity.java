package com.example.dexter007bot;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
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
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
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
    private EditText passKey;
    private LinearLayout auth;
    private RadioGroup radioGroup;

    private Button submit;

    public static Logger logger;
    private BasicFunctionHandler handler;

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
            //recreate();
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

        sp = getApplicationContext().getSharedPreferences("user_credentials",MODE_PRIVATE);
        if(sp.getString("First login","").toString().equals("no")){
            //Not first time login
            userEmail = sp.getString("email","");
            userPhoneNum = sp.getString("phone","");
            userName = sp.getString("user_name","no_user");
            logger = new Logger();

            //Move to main activity
            Intent i = new Intent(LoginActivity.this,MainActivity.class);
            startActivity(i);
            finish();
        }else{
            //first time login
            setContentView(R.layout.activity_login);
            handler = new BasicFunctionHandler(this);
            email = findViewById(R.id.emailText);
            phone = findViewById(R.id.phoneText);
            submit = findViewById(R.id.submitButton);
            passKey = findViewById(R.id.passKey);
            auth = findViewById(R.id.auth);
            radioGroup = findViewById(R.id.radioGroup);
            radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup radioGroup, int i) {
                    switch (radioGroup.getCheckedRadioButtonId()){
                        case R.id.normalUser:
                            auth.setVisibility(View.GONE);
                            break;

                        case R.id.authUser:
                            auth.setVisibility(View.VISIBLE);
                            break;
                    }
                }
            });
            submit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //check if user details are filled or not
                    if(email.getText().toString().matches("") || phone.getText().toString().matches("")){
                        handler.showAlertDialog("","Please fill the details");
                    }else{
                        userEmail = String.valueOf(email.getText());
                        userPhoneNum = String.valueOf(phone.getText());
                        if (!handler.isEmailValid(userEmail)){
                            handler.showAlertDialog("","Please Enter a valid email");
                        } else if (userPhoneNum.length() != 10){
                            handler.showAlertDialog("","Please enter a valid Phone Number");
                        } else{
                            SharedPreferences.Editor editor = sp.edit();
                            switch (radioGroup.getCheckedRadioButtonId()){
                                case R.id.normalUser:
                                    editor.putString("First login","no");
                                    editor.putString("email", String.valueOf(email.getText()));
                                    editor.putString("phone", String.valueOf(phone.getText()));
                                    editor.putString("user_type","normal");
                                    editor.putString("user_name",getSource(userEmail) + "_" +userPhoneNum);
                                    editor.commit();
                                    logger = new Logger();
                                    //Move to main activity
                                    Intent i = new Intent(LoginActivity.this,MainActivity.class);
                                    startActivity(i);
                                    finish();
                                    break;

                                case R.id.authUser:
                                    if (passKey.getText().toString().matches("password123")){
                                        editor.putString("First login","no");
                                        editor.putString("email", String.valueOf(email.getText()));
                                        editor.putString("phone", String.valueOf(phone.getText()));
                                        editor.putString("user_type","authorised");
                                        editor.putString("user_name",getSource(userEmail) + "_" +userPhoneNum);
                                        editor.putString("pass_key","password123");
                                        editor.commit();

                                        logger = new Logger();
                                        //Move to main activity
                                        Intent ii = new Intent(LoginActivity.this,WriteSettingActivity.class);
                                        startActivity(ii);
                                        finish();
                                    } else {
                                        AlertDialog ad = new AlertDialog.Builder(LoginActivity.this)
                                                .setMessage("Please enter the Authorised Key")
                                                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialogInterface, int i) {
                                                        dialogInterface.dismiss();
                                                    }
                                                }).create();
                                        ad.show();
                                    }
                                    break;
                            }
                        }
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
