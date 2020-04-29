package com.example.dexter007bot.Maps;


import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Point;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.example.dexter007bot.Adapter.CustomInfoWindow;
import com.example.dexter007bot.MainActivity;
import com.example.dexter007bot.R;

import org.osmdroid.bonuspack.kml.KmlDocument;
import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.CustomZoomButtonsController;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.Polygon;
import org.osmdroid.views.overlay.ScaleBarOverlay;
import org.osmdroid.views.overlay.compass.CompassOverlay;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.example.dexter007bot.MainActivity.kml;

public class MapActivity extends AppCompatActivity implements MapEventsReceiver {

    private MapView mMap;

    private MapEventsOverlay mapEventsOverlay;
    private List<GeoPoint> geoPoints;
    private List<Marker> markerList;
    private Polygon polygon;
    private ImageView saveButton, resetButton;


    private MapController mapController;
    LocationManager locationManager;
    Marker marker = null;

    ArrayList<OverlayItem> overlayItemArray;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        setContentView(R.layout.activity_map);
        initMap();
        saveButton = findViewById(R.id.savedbtn);
        resetButton = findViewById(R.id.resetbtn);

        //save all the overlays
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                kml.mKmlRoot.addOverlay(polygon,kml);
                kml.mKmlRoot.addOverlay(marker,kml);
                Toast.makeText(getApplicationContext(),"Polygon Saved",Toast.LENGTH_SHORT).show();
            }
        });

        //clear the overlays
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                geoPoints.clear();
                polygon.setPoints(geoPoints);
                mMap.getOverlays().remove(polygon);
                for (Overlay overlay : markerList) {
                    mMap.getOverlays().remove(overlay);
                }
                mMap.getOverlays().add(0,mapEventsOverlay);
                initMap();
                mMap.invalidate();
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onResume() {
        super.onResume();
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    Activity#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for Activity#requestPermissions for more details.
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, myLocationListener);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, myLocationListener);
        mMap.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        locationManager.removeUpdates(myLocationListener);
        mMap.onPause();
    }

    /**
     * location listener for getting current location
     */
    private LocationListener myLocationListener = new LocationListener()
    {
        @Override
        public void onLocationChanged(Location location)
        {
            updateLoc(location);
        }

        @Override
        public void onProviderDisabled(String provider)
        {
        }

        @Override
        public void onProviderEnabled(String provider)
        {
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras)
        {
        }
    };

    /**
     * initialize the map
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void initMap() {
        mMap = findViewById(R.id.map);
        mMap.setTileSource(TileSourceFactory.MAPNIK);

        Display display = MapActivity.this.getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        mMap.getZoomController().setVisibility(CustomZoomButtonsController.Visibility.SHOW_AND_FADEOUT);
        mMap.setMultiTouchControls(true);
        mMap.setClickable(true);

        mapController = (MapController) mMap.getController();
        mapController.setZoom(18.0f);
        overlayItemArray = new ArrayList<>();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    Activity#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for Activity#requestPermissions for more details.
            return;
        }

        //current location
        Location lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (lastLocation != null) {
            updateLoc(lastLocation);
        }
        //Add Scale Bar
        ScaleBarOverlay myScaleBarOverlay = new ScaleBarOverlay(mMap);
        myScaleBarOverlay.setCentred(true);
        myScaleBarOverlay.setScaleBarOffset(width / 2, 10);
        mMap.getOverlays().add(myScaleBarOverlay);

        //Add Compass
        CompassOverlay mCompassOverlay = new CompassOverlay(this, mMap);
        mCompassOverlay.enableCompass();
        mMap.getOverlays().add(mCompassOverlay);

        mapEventsOverlay = new MapEventsOverlay(this, this);
        mMap.getOverlays().add(0, mapEventsOverlay);

        //Rotation Gesture
        //RotationGestureOverlay mRotationGestureOverlay = new RotationGestureOverlay(this, mMap);
        //mRotationGestureOverlay.setEnabled(true);
        mMap.setMultiTouchControls(true);
        //mMap.getOverlays().add(mRotationGestureOverlay);

        //Add polygon
        geoPoints = new ArrayList<>();
        markerList = new ArrayList<>();
        polygon = new Polygon();
        polygon.setFillColor(Color.argb(75, 255,0,0));
        //polygon.getInfoWindow();
        /*MapEventsReceiver mapEventsReceiver = new MapEventsReceiver() {
            @Override
            public boolean singleTapConfirmedHelper(GeoPoint p) {

                return false;
            }

            @Override
            public boolean longPressHelper(GeoPoint p) {
                return false;
            }
        }*/
    }

    /**
     * Add Marker on click
     */

    @Override
    public boolean singleTapConfirmedHelper(GeoPoint p) {
        GeoPoint z= new GeoPoint(p.getLatitude()+1,p.getLongitude()+1);
        geoPoints.add(p);
        polygon.setPoints(geoPoints);
        Marker mMarker = new Marker(mMap);
        mMarker.setPosition(p);

        //marker.setIcon(getResources().getDrawable(R.mipmap.marker_red_round));
        mMarker.setAnchor(Marker.ANCHOR_CENTER,Marker.ANCHOR_BOTTOM);
        mMarker.setTitle(p.getLatitude() + "," + p.getLongitude());
        markerList.add(mMarker);
        int size=markerList.size();
        mMap.getOverlays().add(markerList.get(size-1));

        /*Marker check = new Marker(mMap);
        check.setPosition(z);
        check.setAnchor(Marker.ANCHOR_CENTER,Marker.ANCHOR_BOTTOM);
        check.setTitle(z.getLatitude() + "," + z.getLongitude());
        mMap.getOverlays().add(check);*/
        mMap.getOverlayManager().add(polygon);
        mMap.invalidate();
        return true;
    }

    @Override
    public boolean longPressHelper(GeoPoint p) {
        return false;
    }

    /**
     * update current location
     * @param loc
     */
    private void updateLoc(Location loc) {
        //update my location
        GeoPoint locGeoPoint = new GeoPoint(loc.getLatitude(), loc.getLongitude());
        if(marker==null){
            mapController.setCenter(locGeoPoint);
            mapController.animateTo(locGeoPoint);
            marker = new Marker(mMap);
            marker.setPosition(locGeoPoint);
            String s= MainActivity.tempImage;
            if(s!=null) {
                //setting snippet value name of the image
                marker.setSnippet(s);
                //custom info window for the marker
                CustomInfoWindow ciw = new CustomInfoWindow(R.layout.custom_info_window, mMap, marker, MapActivity.this);
                marker.setInfoWindow(ciw);
            }
            else marker.setTitle("My Location" + "\n" + locGeoPoint.getLatitude() + "," + locGeoPoint.getLongitude());
        }
        else{
            marker.setPosition(locGeoPoint);
        }
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        marker.setIcon(getResources().getDrawable(R.drawable.ic_my_location));
        //marker.setTitle("My Location" + "\n" + locGeoPoint.getLatitude() + "," + locGeoPoint.getLongitude());
        mMap.getOverlays().add(marker);

        setOverlayLoc(loc);
        //marker.showInfoWindow();
        mMap.invalidate();
    }
    private void setOverlayLoc(Location overlayloc)
    {
        GeoPoint overlocGeoPoint = new GeoPoint(overlayloc);
        //---
        overlayItemArray.clear();

        OverlayItem newMyLocationItem = new OverlayItem("My Location", "My Location", overlocGeoPoint);
        overlayItemArray.add(newMyLocationItem);
        //---
    }
}
