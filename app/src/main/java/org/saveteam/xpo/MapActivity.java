package org.saveteam.xpo;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PointF;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.here.android.mpa.common.GeoCoordinate;
import com.here.android.mpa.common.GeoPosition;
import com.here.android.mpa.common.LocationDataSourceHERE;
import com.here.android.mpa.common.OnEngineInitListener;
import com.here.android.mpa.common.PositioningManager;
import com.here.android.mpa.common.ViewObject;
import com.here.android.mpa.mapping.Map;
import com.here.android.mpa.mapping.MapContainer;
import com.here.android.mpa.mapping.MapFragment;
import com.here.android.mpa.mapping.MapGesture.OnGestureListener;
import com.here.android.mpa.mapping.MapMarker;
import com.here.android.mpa.mapping.MapState;
import com.here.android.positioning.StatusListener;

import org.saveteam.xpo.utils.ActivityUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MapActivity extends AppCompatActivity implements
        PositioningManager.OnPositionChangedListener,
        Map.OnTransformListener {

    // permissions request code
    private final static int REQUEST_CODE_ASK_PERMISSIONS = 1;

    /**
     * Permissions that need to be explicitly requested from end user.
     */
    private static final String[] REQUIRED_SDK_PERMISSIONS = new String[] {
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_WIFI_STATE};

    /**
     * Map here
     */
    private Map map = null;
    private PositioningManager positioningManager;
    private LocationDataSourceHERE locationDataSourceHERE;

    /**
     * common variables
     */
    private MapFragment mapFragment = null;
    private MapMarker marker = null;



    /**
     * if press back 2 times, then quit
     */
    boolean isQuit = false;
    boolean isGps = true;

    @BindView(R.id.btn_confirm_where_map)
    AppCompatButton btnComfirm;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        ButterKnife.bind(this);
        checkPermissions();
    }

    @OnClick(R.id.btn_confirm_where_map)
    public void clickBtnConfirm(View view) {
        Intent returnIntent = getIntent();
        returnIntent.putExtra("long", marker.getCoordinate().getLongitude());
        returnIntent.putExtra("lat", marker.getCoordinate().getLatitude());
        setResult(RESULT_OK,returnIntent);
        finish();
    }

    private void initialize() {
        /**
         * init common variables
         */
        mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map_fragment_where_map);
        mapFragment.setRetainInstance(false);

        /**
         * cache apps
         */
        boolean success = com.here.android.mpa.common.MapSettings.setIsolatedDiskCacheRootPath(
                getApplicationContext().getExternalFilesDir(null) + File.separator + ".here-maps",
                "org.saveteam.xpo.MapService");
        if (!success) {
            Toast.makeText(getApplicationContext(), "Unable to set isolated disk cache path.", Toast.LENGTH_LONG);
        } else {
            mapFragment.init(new OnEngineInitListener() {
                @Override
                public void onEngineInitializationCompleted(OnEngineInitListener.Error error) {
                    if (error == OnEngineInitListener.Error.NONE) {
                        map = mapFragment.getMap();
                        initMap();
                        mapFragment.getMapGesture().addOnGestureListener(new MyOnGestureListener(map),0,true);

                        positioningManager = PositioningManager.getInstance();
                        positioningManager.start(PositioningManager.LocationMethod.GPS);
                        locationDataSourceHERE = LocationDataSourceHERE.getInstance(new MyStatusListener());
                        if (locationDataSourceHERE == null) {
                            Toast.makeText(MapActivity.this, "LocationDataSourceHERE.getInstance(): failed, exiting", Toast.LENGTH_LONG).show();
                        }
                        positioningManager.setDataSource(locationDataSourceHERE);
                        PositioningManager.getInstance().addListener(new WeakReference<PositioningManager.OnPositionChangedListener>(
                                MapActivity.this));

                        // start position updates, accepting GPS, network or indoor positions
                        if (positioningManager.start(PositioningManager.LocationMethod.GPS)) {
                            mapFragment.getPositionIndicator().setVisible(true);
                            mapFragment.getPositionIndicator().setMarker(ActivityUtils.getMarker());
                            isGps = true;
                        } else {
                            mapFragment.getPositionIndicator().setVisible(false);
                            isGps = false;
                        }
                    } else {
                        System.out.println("ERROR: Cannot initialize Map Fragment");
                    }
                }
            });

        }
    }

    private void actionGps() {
        PositioningManager posManager;
        MapContainer placesContainer = null;
        // retrieve a reference of the map from the map fragment
        // map = mapFragment.getMap();
        // start the position manager
        posManager = PositioningManager.getInstance();
        posManager.start(PositioningManager.LocationMethod.GPS_NETWORK);

        GeoPosition position = posManager.getPosition();
        GeoCoordinate cor = position.getCoordinate();

        // Set a pedestrian friendly map scheme
        map.setMapScheme(Map.Scheme.PEDESTRIAN_DAY);

        // Display position indicator
        map.getPositionIndicator().setVisible(true);

        placesContainer = new MapContainer();
        map.addMapObject(placesContainer);

        // Set the map center coordinate to the current position
        map.setCenter(posManager.getPosition().getCoordinate(), Map.Animation.NONE);
        map.setZoomLevel(14);
    }

    private void initMap() {
        map.getPositionIndicator().setVisible(true);
        map.setCenter(new GeoCoordinate(10.7859988, 106.7340538, 0.0),
                Map.Animation.NONE);
        map.setZoomLevel(10);
    }

    @Override
    public void onBackPressed() {
        if (isQuit) {
            finishAffinity();
            System.exit(0);
        } else {
            isQuit = true;
            Toast.makeText(this, "Let's pressback 1 times again to exit application", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (positioningManager != null) {
            positioningManager.stop();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (positioningManager != null) {
            positioningManager.start(PositioningManager.LocationMethod.GPS_NETWORK);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mapFragment != null) {
            // mapFragment.getMapGesture().removeOnGestureListener(new MyOnGestureListener(map));
        }
    }

    /**
     * change position
     */
    @Override
    public void onPositionUpdated(PositioningManager.LocationMethod locationMethod, GeoPosition geoPosition, boolean b) {
        if (isGps) {
            if (geoPosition == null) return;
            final GeoCoordinate coordinate = geoPosition.getCoordinate();
            map.setCenter(coordinate, Map.Animation.BOW);
            map.removeMapObject(marker);
            marker = new MapMarker(coordinate, ActivityUtils.getMarker());
            map.addMapObject(marker);
        }
    }

    @Override public void onPositionFixChanged(PositioningManager.LocationMethod locationMethod, PositioningManager.LocationStatus locationStatus) {}

    /**
     * transform map
     */
    @Override public void onMapTransformStart() {}
    @Override public void onMapTransformEnd(MapState mapState) {}

    /**
     * Checks the dynamically controlled permissions and requests missing permissions from end user.
     */
    protected void checkPermissions() {
        final List<String> missingPermissions = new ArrayList<String>();
        // check all required dynamic permissions
        for (final String permission : REQUIRED_SDK_PERMISSIONS) {
            final int result = ContextCompat.checkSelfPermission(this, permission);
            if (result != PackageManager.PERMISSION_GRANTED) {
                missingPermissions.add(permission);
            }
        }
        if (!missingPermissions.isEmpty()) {
            // request all missing permissions
            final String[] permissions = missingPermissions
                    .toArray(new String[missingPermissions.size()]);
            ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE_ASK_PERMISSIONS);
        } else {
            final int[] grantResults = new int[REQUIRED_SDK_PERMISSIONS.length];
            Arrays.fill(grantResults, PackageManager.PERMISSION_GRANTED);
            onRequestPermissionsResult(REQUEST_CODE_ASK_PERMISSIONS, REQUIRED_SDK_PERMISSIONS,
                    grantResults);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[],@NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_PERMISSIONS:
                for (int index = permissions.length - 1; index >= 0; --index) {
                    if (grantResults[index] != PackageManager.PERMISSION_GRANTED) {
                        // exit the app if one permission is not granted
                        Toast.makeText(this, "Required permission '" + permissions[index]
                                + "' not granted, exiting", Toast.LENGTH_LONG).show();
                        finish();
                        return;
                    }
                }
                // all permissions were granted
                initialize();
                break;
        }
    }

    private class MyStatusListener implements StatusListener {

        @Override
        public void onOfflineModeChanged(boolean b) {

        }

        @Override
        public void onAirplaneModeEnabled() {

        }

        @Override
        public void onWifiScansDisabled() {

        }

        @Override
        public void onBluetoothDisabled() {

        }

        @Override
        public void onCellDisabled() {

        }

        @Override
        public void onGnssLocationDisabled() {

        }

        @Override
        public void onNetworkLocationDisabled() {

        }

        @Override
        public void onServiceError(ServiceError serviceError) {

        }

        @Override
        public void onPositioningError(PositioningError positioningError) {

        }

        @Override
        public void onWifiIndoorPositioningNotAvailable() {

        }
    }

    private class MyOnGestureListener implements OnGestureListener {

        private Map map;

        public MyOnGestureListener(Map map) {
            this.map = map;
        }

        @Override
        public void onPanStart() {

        }

        @Override
        public void onPanEnd() {

        }

        @Override
        public void onMultiFingerManipulationStart() {

        }

        @Override
        public void onMultiFingerManipulationEnd() {

        }

        @Override
        public boolean onMapObjectsSelected(List<ViewObject> list) {
            return false;
        }

        @Override
        public boolean onTapEvent(PointF pointF) {
            return false;
        }

        @Override
        public boolean onDoubleTapEvent(PointF pointF) {
            return false;
        }

        @Override
        public void onPinchLocked() {

        }

        @Override
        public boolean onPinchZoomEvent(float v, PointF pointF) {
            return false;
        }

        @Override
        public void onRotateLocked() {

        }

        @Override
        public boolean onRotateEvent(float v) {
            return false;
        }

        @Override
        public boolean onTiltEvent(float v) {
            return false;
        }

        @Override
        public boolean onLongPressEvent(PointF pointF) {
            map.removeMapObject(marker);
            marker = new MapMarker(map.pixelToGeo(pointF), ActivityUtils.getMarker());
            Log.i("thanhuit","position: " + marker.getCoordinate().getLongitude() + " - " + marker.getCoordinate().getLatitude());
            map.addMapObject(marker);
            return false;
        }

        @Override
        public void onLongPressRelease() {

        }

        @Override
        public boolean onTwoFingerTapEvent(PointF pointF) {
            return false;
        }
    }
}