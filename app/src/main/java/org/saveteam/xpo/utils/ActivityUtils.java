package org.saveteam.xpo.utils;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;

import com.here.android.mpa.common.Image;

import org.saveteam.xpo.R;

import java.io.IOException;

public class ActivityUtils {
    public static void changeActivity(AppCompatActivity app, Class<?> cls) {
        Intent changeActivity = new Intent(app.getApplicationContext(), cls);
        app.startActivity(changeActivity);
    }

    public static Image getMarker() {
        Image marker_img = new Image();
        try {
            marker_img.setImageResource(R.drawable.marker);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return marker_img;
    }
}
