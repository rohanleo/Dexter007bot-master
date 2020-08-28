package com.example.dexter007bot;

import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import androidx.appcompat.app.AlertDialog;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BasicFunctionHandler {
    private Pattern emailPattern;
    private Context context;

    public BasicFunctionHandler() {
    }

    public BasicFunctionHandler(Context c) {
        context = c;
        emailPattern = Pattern.compile("^([_a-zA-Z0-9-]+(\\\\.[_a-zA-Z0-9-]+)*@[a-zA-Z0-9-]+(\\\\.[a-zA-Z0-9-]+)*(\\\\.[a-zA-Z]{1,6}))?$");
    }

    public void showAlertDialog(String title, String body) {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        dialog.setTitle(title);
        dialog.setMessage(body);
        dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        dialog.create();
        dialog.show();
    }

    public boolean isEmailValid(String email) {
        Log.e("EMail", email);
        String regex = "^([_a-zA-Z0-9-]+(\\.[_a-zA-Z0-9-]+)*@[a-zA-Z0-9-]+(\\.[a-zA-Z0-9-]+)*(\\.[a-zA-Z]{1,6}))?$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }
}
