package com.example.dexter007bot;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.os.Bundle;
import android.view.Display;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.alexvasilkov.gestures.views.GestureImageView;
import com.example.dexter007bot.R;
import com.squareup.picasso.Picasso;

import java.io.File;

import static android.os.Environment.getExternalStoragePublicDirectory;

public class ImageViewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_view);

        GestureImageView image  = (GestureImageView) findViewById(R.id.imageview);
        String path = getIntent().getStringExtra("url");
        File file = Environment.getExternalStoragePublicDirectory(path);
        int height=0,width=0;
        File f = null;
        try {
            f = Environment.getExternalStoragePublicDirectory(path);
            Bitmap bitmap = BitmapFactory.decodeFile(f.getAbsolutePath());
            height = bitmap.getHeight();
            width = bitmap.getWidth();
            if (height > 2450 || width > 2450) {
                Display display = getWindowManager().getDefaultDisplay();
                Point size = new Point();
                display.getSize(size);
                width = size.x;
                height = size.y;
            }
        }
        catch (Exception e){
            f = Environment.getExternalStoragePublicDirectory("DMS/tempMedia/"+file.getName());
            Bitmap bitmap = BitmapFactory.decodeFile(f.getAbsolutePath());
            height = bitmap.getHeight();
            width = bitmap.getWidth();
            if (height > 2450 || width > 2450) {
                Display display = getWindowManager().getDefaultDisplay();
                Point size = new Point();
                display.getSize(size);
                width = size.x;
                height = size.y;
            }
        }
        Picasso.get().load(f).resize(width,height).into(image);
    }
}
