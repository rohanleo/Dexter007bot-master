package com.example.dexter007bot;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    private SharedPreferences sp;
    public static String userEmail;
    public static String userPhoneNum;
    private EditText email;
    private EditText phone;

    private Button submit;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //AddDirectory.addDirectory();
        sp = getSharedPreferences("First log",0);
        if(sp.getString("First login","").toString().equals("no")){
            userEmail = sp.getString("email","");
            userPhoneNum = sp.getString("phone","");
            Intent i = new Intent(LoginActivity.this,MainActivity.class);
            startActivity(i);
            finish();
        }else{
            setContentView(R.layout.activity_login);
            email = findViewById(R.id.emailText);
            phone = findViewById(R.id.phoneText);
            submit = findViewById(R.id.submitButton);
            submit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(email.getText().toString().matches("") || phone.getText().toString().matches("")){
                        Toast.makeText(LoginActivity.this,"Please fill the details",Toast.LENGTH_LONG).show();
                    }else{
                        SharedPreferences.Editor editor = sp.edit();
                        editor.putString("First login","no");
                        editor.putString("email", String.valueOf(email.getText()));
                        editor.putString("phone", String.valueOf(phone.getText()));
                        editor.commit();
                        userEmail = sp.getString("email","");
                        userPhoneNum = sp.getString("phone","");
                        Intent i = new Intent(LoginActivity.this,MainActivity.class);
                        startActivity(i);
                        finish();

                    }
                }
            });
        }
    }
}
