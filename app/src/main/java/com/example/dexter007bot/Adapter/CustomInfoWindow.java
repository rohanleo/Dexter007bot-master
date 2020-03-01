package com.example.dexter007bot.Adapter;


import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;


import com.example.dexter007bot.Maps.ImageViewActivity;
import com.example.dexter007bot.R;

import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polygon;
import org.osmdroid.views.overlay.infowindow.InfoWindow;


public class CustomInfoWindow extends InfoWindow {
    Polygon polygon;
    Marker marker;
    Context context;
    ListView lv;
    public CustomInfoWindow(int layoutResId, MapView mapView, Polygon polygon, Context context) {
        super(layoutResId, mapView);
        this.polygon = polygon;
        this.context = context;
    }

    public CustomInfoWindow(int layoutResId, MapView mapView, Marker marker, Context context) {
        super(layoutResId, mapView);
        this.marker = marker;
        this.context = context;
    }

    @Override
    public void onOpen(Object item) {
        lv = mView.findViewById(R.id.ciw_lv);
        String des;
        if(item instanceof Polygon)
            des = polygon.getSnippet();
        else
            des = marker.getSnippet();
        try {
            String[] files;
            files = new String[1];
            files[0] = des;
            final String[] allFiles = files;
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, files);
            lv.setAdapter(adapter);
            lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    String fileName = allFiles[i];
                    String folderName = "Android/data/com.example.dexter007bot/files/";
                    if (fileName.contains("jpg")) {
                        folderName = folderName + "Pictures/"+fileName;
                        Intent ii = new Intent(context, ImageViewActivity.class);
                        ii.putExtra("url",folderName);
                        context.startActivity(ii);
                    }
                   /* else if (fileName.contains("mp4")) {
                        folderName = folderName + "Movies/"+fileName;
                        //Intent ii = new Intent(context, VideoPlayer.class);
                        //ii.putExtra("url",folderName);
                        //context.startActivity(ii);
                    }*/

                }
            });
        }
        catch (Exception e){
            String[] s = new String[1];
            s[0] = "No files found";
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, s);
            lv.setAdapter(adapter);
        }
    }

    @Override
    public void onClose() {

    }
}
