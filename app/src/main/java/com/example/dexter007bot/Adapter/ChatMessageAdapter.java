package com.example.dexter007bot.Adapter;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.example.dexter007bot.ImageViewActivity;
import com.example.dexter007bot.MainActivity;
import com.example.dexter007bot.Model.ChatMessage;
import com.example.dexter007bot.R;

import java.io.IOException;
import java.util.List;

public class ChatMessageAdapter extends ArrayAdapter<ChatMessage> {

    private static final int MY_MESSAGE = 0;
    private static final int BOT_MESSAGE = 1;
    private static final int BOT_BUTTON = 2;
    private static final int IMAGE = 3;
    private static final int VIDEO = 4;
    private static final int AUDIO = 5;

    public ChatMessageAdapter(@NonNull Context context, List<ChatMessage> data){
        super(context, R.layout.user_query_layout,data);
    }


    @Override
    public int getItemViewType(int position) {

        ChatMessage item=getItem(position);

        return item.getValue();
    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        int viewType = getItemViewType(position);
        System.out.println(viewType);

        if(viewType == MY_MESSAGE) {
            //User message layout section

            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.user_query_layout,parent,false);

            TextView textView = convertView.findViewById(R.id.text);
            textView.setText(getItem(position).getContent());
        }
        else if (viewType == BOT_MESSAGE){
            //bot message layout section

            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.bots_reply_layout,parent,false);

            TextView textView = convertView.findViewById(R.id.text);
            textView.setText(getItem(position).getContent());
        }
        else if(viewType == BOT_BUTTON){
            //button layout section

            convertView = LayoutInflater.from((getContext()))
                    .inflate(R.layout.bot_button,parent,false);
            Button button =convertView.findViewById(R.id.button);
            button.setText(getItem(position).getContent());
            final String s= button.getText().toString();
            //System.out.println("Button"+s);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //System.out.println(x);
                    MainActivity.fun(s,"text");
                }
            });
        }
        else if(viewType == IMAGE){
            //Imageview layout section

            convertView = LayoutInflater.from((getContext())).inflate(R.layout.reply_image,parent,false);
            final ImageView imageView = convertView.findViewById(R.id.image);
            Bitmap myBitmap = BitmapFactory.decodeFile(getItem(position).getContent());
            imageView.setImageBitmap(myBitmap);
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ViewGroup.LayoutParams params=imageView.getLayoutParams();
                    ViewGroup.LayoutParams temp = params;
                    params.width=300;
                    params.height=300;
                }
            });
        }
        else if(viewType == VIDEO){
            //Video layout section

            convertView = LayoutInflater.from((getContext())).inflate(R.layout.reply_video,parent,false);
            final VideoView videoView = convertView.findViewById(R.id.video);
            videoView.setVideoPath(getItem(position).getContent());
            videoView.start();
            videoView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(videoView.isPlaying()) videoView.start();
                    else videoView.pause();
                }
            });
        }
        else if(viewType == AUDIO){
            //Audio layout section

            convertView = LayoutInflater.from((getContext())).inflate(R.layout.reply_audio,parent,false);
            // Play/pause button
            ImageButton imageButton = convertView.findViewById(R.id.outgoing_imageButton);
            //Timer
            final Chronometer chronometer = convertView.findViewById(R.id.chronometerLay);
            Boolean isPlaying = false;
            imageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Log.e("ChatmessageAdapter:- ", getItem(position).getContent());

                    final MediaPlayer[] mediaPlayer = {new MediaPlayer()};
                    try {
                        mediaPlayer[0].setDataSource(getItem(position).getContent());
                        mediaPlayer[0].prepare();
                        chronometer.setBase(SystemClock.elapsedRealtime());
                        chronometer.start();
                        mediaPlayer[0].start();
                        mediaPlayer[0].setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                            @Override
                            public void onCompletion(MediaPlayer mp) {
                                mediaPlayer[0].release();
                                mediaPlayer[0] =null;
                                chronometer.stop();
                                chronometer.setBase(SystemClock.elapsedRealtime());
                            }
                        });
                    } catch (IOException e) {
                        Log.e("AUDIO_CHAT", "prepare() failed");
                    }
                }
            });
        }

        return convertView;
    }

    @Override
    public int getViewTypeCount() {
        return 6;
    }
}
