package org.saveteam.xpo;

import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;
import com.cloudinary.utils.ObjectUtils;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.here.android.mpa.common.GeoCoordinate;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnFocusChange;

public class HomeActivity extends AppCompatActivity implements  GoogleApiClient.OnConnectionFailedListener {

    private static final int REQUEST_CAMERA = 10000;
    private static final int REQUEST_LOCATION = 2000;

    @BindView(R.id.btn_camera_where_home)
    Button btnCamera;
    @BindView(R.id.txt_title_video_where_home)
    EditText txtTitleVideo;
    @BindView(R.id.txt_details_where_home)
    EditText txtDetails;
    @BindView(R.id.btn_location_where_home)
    Button btnLocation;
    @BindView(R.id.txt_time_where_home)
    EditText txtTime;

    @BindView(R.id.btn_shoot_camera_where_home)
    Button btnShootCamera;
    @BindView(R.id.btn_submit_where_home)
    Button btnSubmit;

    Cloudinary cloudinary = null;

    /**
     * google
     */
    GoogleApiClient mGoogleApiClient;

    Uri videoUrl = null;

    double poslong = 0;
    double poslat = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        ButterKnife.bind(this);


        txtTime.setText(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date()));
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();


    }

    @OnClick({R.id.btn_camera_where_home, R.id.btn_shoot_camera_where_home})
    public void clickButtonCamera(View view) {
        Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        if (takeVideoIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takeVideoIntent, REQUEST_CAMERA);
        }
    }

    @OnClick(R.id.btn_location_where_home)
    public void clickButtonLocation(View view) {
        Intent mapIntent = new Intent(getApplicationContext(), MapActivity.class);
        startActivityForResult(mapIntent, REQUEST_LOCATION);
    }

    @OnClick(R.id.btn_submit_where_home)
    public void clickButtonSubmit(View view)  {
        if(videoUrl==null) {
            Toast.makeText(getApplicationContext(), "Please, you must have video report", Toast.LENGTH_LONG).show();
        } else {
            String position = "https://wego.here.com/?map="+poslat+","+ poslong +",10,normal";
            Toast.makeText(getApplicationContext(), videoUrl.getPath(), Toast.LENGTH_LONG).show();
            Intent email = new Intent(Intent.ACTION_SEND);
            email.putExtra(Intent.EXTRA_EMAIL, new String[]{"thanh29695@gmail.com"});
            email.putExtra(Intent.EXTRA_SUBJECT, "Report " + txtTitleVideo.getText());
            email.putExtra(Intent.EXTRA_TEXT, txtDetails.getText() + "\n" + txtTime.getText() + "\n" + position) ;
            email.putExtra(Intent.EXTRA_STREAM, videoUrl);
            email.setType("message/rfc822");
            startActivity(Intent.createChooser(email, "Choose an Email client :"));
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode == REQUEST_CAMERA && resultCode == RESULT_OK) {
            Uri uri = data.getData();
            btnCamera.setText(uri.getPath());
            videoUrl = uri;
        }

        if (requestCode == REQUEST_LOCATION && resultCode == RESULT_OK) {
            double longtitude = data.getDoubleExtra("long", 0);
            double latitude = data.getDoubleExtra("lat", 0);
            poslat = latitude;
            poslong = longtitude;
            btnLocation.setText("(" + longtitude +"," + latitude +")");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home_menu, menu);
        return true;
    }

    private void logout() {
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(@NonNull Status status) {
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id){
            case R.id.settings_where_menu_home:
                Toast.makeText(getApplicationContext(),"settings click",Toast.LENGTH_LONG).show();
                return true;
            case R.id.logout_where_menu_home:
                logout();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
