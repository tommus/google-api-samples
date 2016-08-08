package com.todev.samples;

import com.google.android.gms.maps.model.LatLng;

public class Constants {
  // TODO: Put your home location in gradle.properties.
  public static final double HOME_LATITUDE = BuildConfig.HOME_LATITUDE;
  public static final double HOME_LONGITUDE = BuildConfig.HOME_LONGITUDE;
  public static final LatLng HOME_LOCATION = new LatLng(HOME_LATITUDE, HOME_LONGITUDE);
}
