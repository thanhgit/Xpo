package org.saveteam.xpo.model;

import com.google.android.gms.maps.model.LatLng;
import com.here.android.mpa.common.GeoCoordinate;

import java.io.Serializable;

public class MyMarker implements Serializable {
    public GeoCoordinate geoCoordinate;

    public MyMarker() {}

    public MyMarker(GeoCoordinate geoCoordinate) {
        this.geoCoordinate = geoCoordinate;
    }

    public GeoCoordinate getGeoCoordinate() {
        return geoCoordinate;
    }

    public void setGeoCoordinate(GeoCoordinate geoCoordinate) {
        this.geoCoordinate = geoCoordinate;
    }

    @Override
    public String toString() {
        return "Coordinate (" +geoCoordinate.getLongitude() + " , " + geoCoordinate.getLatitude() + ")";
    }
}
