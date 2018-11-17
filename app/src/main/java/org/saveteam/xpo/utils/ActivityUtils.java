package org.saveteam.xpo.utils;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;

public class ActivityUtils {
    public static void changeActivity(AppCompatActivity app, Class<?> cls) {
        Intent changeActivity = new Intent(app.getApplicationContext(), cls);
        app.startActivity(changeActivity);
    }
}
