package com.example.dexter007bot.Adapter;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.dexter007bot.MainActivity;
import com.example.dexter007bot.Model.ChatMessage;
import com.example.dexter007bot.R;

import java.util.List;

public class ChatMessageAdapter extends ArrayAdapter<ChatMessage> {

    private static final int MY_MESSAGE = 0;
    private static final int BOT_MESSAGE = 1;
    private static final int BOT_BUTTON = 2;

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
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        int viewType = getItemViewType(position);
        System.out.println(viewType);

        if(viewType == MY_MESSAGE) {
            //System.out.println("USER");
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.user_query_layout,parent,false);

            TextView textView = convertView.findViewById(R.id.text);
            textView.setText(getItem(position).getContent());
        }
        else if (viewType == BOT_MESSAGE){
            //System.out.println("BOT");
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.bots_reply_layout,parent,false);

            TextView textView = convertView.findViewById(R.id.text);
            textView.setText(getItem(position).getContent());
        }
        else if(viewType == BOT_BUTTON){
            //System.out.println("BUTTON");
            convertView = LayoutInflater.from((getContext()))
                    .inflate(R.layout.bot_button,parent,false);
            Button button =convertView.findViewById(R.id.button);
            button.setText(getItem(position).getContent());
            //button.setId(View.generateViewId());
            final String s= button.getText().toString();
            //System.out.println("Button"+s);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //System.out.println(x);
                    MainActivity.fun(s);
                }
            });
        }

        convertView.findViewById(R.id.chatMessageView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(),"Clicked...",Toast.LENGTH_SHORT).show();
            }
        });

        return convertView;
    }

    @Override
    public int getViewTypeCount() {
        return 3;
    }
}
