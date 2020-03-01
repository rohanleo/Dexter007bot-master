package com.example.dexter007bot.Maps;


import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.dexter007bot.Maps.RenderMapActivity;
import com.example.dexter007bot.R;

import java.io.File;
import java.util.ArrayList;

public class SavedActivity extends AppCompatActivity {

    private ListView listView;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> arrayList;
    private ArrayList<String> uriArrayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved);

        listView = findViewById(R.id.list_view);
        arrayList = new ArrayList<String>();
        uriArrayList = new ArrayList<String>();
        adapter = new ArrayAdapter<String>(getApplicationContext(),android.R.layout.simple_list_item_1,arrayList);

        String path = Environment.getExternalStorageDirectory() + "/kml";
        Log.d("PATH",path);
        File directory = new File(path);
        File[] files = directory.listFiles();
        for(int i = 0 ; i < files.length ; i++){
            arrayList.add(files[i].getName());
            uriArrayList.add(files[i].getAbsolutePath());
            Log.d("NAME",files[i].getName());
        }

        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Send the url of the file to the RenderMap intent
                Intent intent = new Intent(getApplicationContext(), RenderMapActivity.class);
                intent.putExtra("kml_file_uri",uriArrayList.get(position));
                startActivity(intent);
            }
        });
    }
}
