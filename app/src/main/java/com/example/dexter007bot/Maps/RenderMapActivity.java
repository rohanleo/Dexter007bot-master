package com.example.dexter007bot.Maps;


import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.dexter007bot.R;

import org.osmdroid.bonuspack.kml.KmlDocument;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.FolderOverlay;

import java.io.File;

public class RenderMapActivity extends AppCompatActivity implements MapView.OnFirstLayoutListener {
    private String absPath;
    private MapView map;
    private KmlDocument kmlDocument;
    private BoundingBox mInitialBoundingBox = null;
    private BoundingBox bb = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_render);

        kmlDocument = new KmlDocument();
        map = (MapView) findViewById(R.id.render_map_view);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setBuiltInZoomControls(true);
        map.setMultiTouchControls(true);

        Intent intent = getIntent();
        absPath = intent.getStringExtra("kml_file_uri");
        Log.d("PATH",absPath);
        File file = new File(absPath);
        kmlDocument.parseKMLFile(file);

        FolderOverlay kmlOverlay = (FolderOverlay)kmlDocument.mKmlRoot.buildOverlay(map, null, null, kmlDocument);
        map.getOverlays().add(kmlOverlay);
        map.invalidate();
        try {
            bb = kmlDocument.mKmlRoot.getBoundingBox();
        }
        catch (Exception e){
            e.printStackTrace();
            Toast.makeText(getApplicationContext(),"No Polygon was drawn",Toast.LENGTH_SHORT).show();
        }

        if(bb != null){
            setInitialViewOn(bb);
        }
    }



    void setInitialViewOn(BoundingBox bb) {
        if (map.getScreenRect(null).height() == 0) {
            mInitialBoundingBox = bb;
            map.addOnFirstLayoutListener(this);
        } else
            map.zoomToBoundingBox(bb, false);
    }

    @Override
    public void onFirstLayout(View v, int left, int top, int right, int bottom) {
        if (mInitialBoundingBox != null)
            map.zoomToBoundingBox(mInitialBoundingBox, false);
    }
}
