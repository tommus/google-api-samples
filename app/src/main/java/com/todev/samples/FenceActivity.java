package com.todev.samples;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import com.google.android.gms.awareness.Awareness;
import com.google.android.gms.awareness.fence.AwarenessFence;
import com.google.android.gms.awareness.fence.DetectedActivityFence;
import com.google.android.gms.awareness.fence.FenceState;
import com.google.android.gms.awareness.fence.FenceUpdateRequest;
import com.google.android.gms.awareness.fence.LocationFence;
import com.google.android.gms.awareness.fence.TimeFence;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import java.util.concurrent.TimeUnit;

public class FenceActivity extends AppCompatActivity {

  public static final String FENCE_RECEIVER_ACTION = "com.todev.fences.FENCE_RECEIVER_ACTION";
  public static final String FENCE_TIME = "com.todev.fences.FENCE_TIME";
  public static final String FENCE_DISTANCE = "com.todev.fences.FENCE_DISTANCE";
  public static final String FENCE_MOVING = "com.todev.fences.FENCE_MOVING";
  public static final String FENCE_HOME = "com.todev.fences.FENCE_HOME";

  private GoogleApiClient client;

  private FenceBroadcastReceiver fenceBroadcastReceiver;

  private Intent fenceReceiverIntent = new Intent(FENCE_RECEIVER_ACTION);

  private double latitude;
  private double longitude;

  private Button timeFenceButton;
  private Button distanceFenceButton;
  private Button movingFenceButton;
  private Button homeFenceButton;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_fence);

    initializeWidgets();
    initializeCallbacks();
    initializeGoogleApiClient();
    initializeLocationService();
  }

  @Override
  protected void onResume() {
    super.onResume();

    initializeBroadcastReceiver();
  }

  @Override
  protected void onPause() {
    super.onPause();

    uninitializeBroadcastReceiver();
  }

  @Override
  protected void onStart() {
    super.onStart();

    uninitializeFences();
  }

  private void initializeWidgets() {
    timeFenceButton = (Button) findViewById(R.id.time_fence_button);
    distanceFenceButton = (Button) findViewById(R.id.distance_fence_button);
    movingFenceButton = (Button) findViewById(R.id.moving_fence_button);
    homeFenceButton = (Button) findViewById(R.id.home_fence_button);
  }

  private void initializeCallbacks() {
    timeFenceButton.setOnClickListener(new OnTimeFenceButtonClicked());
    distanceFenceButton.setOnClickListener(new OnDistanceButtonClicked());
    movingFenceButton.setOnClickListener(new OnMovingButtonClicked());
    homeFenceButton.setOnClickListener(new OnHomeButtonClicked());
  }

  private void initializeGoogleApiClient() {
    client = new GoogleApiClient.Builder(this).addApi(Awareness.API).build();
    client.connect();
  }

  private void initializeLocationService() throws SecurityException {
    LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
    manager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 2000, 10, new OnLocationChangedListener());
  }

  private void initializeBroadcastReceiver() {
    fenceBroadcastReceiver = new FenceBroadcastReceiver();
    registerReceiver(fenceBroadcastReceiver, new IntentFilter(FENCE_RECEIVER_ACTION));
  }

  private void uninitializeFences() {
    Awareness.FenceApi.updateFences(client, new FenceUpdateRequest.Builder().removeFence(FENCE_TIME).build());
    Awareness.FenceApi.updateFences(client, new FenceUpdateRequest.Builder().removeFence(FENCE_DISTANCE).build());
    Awareness.FenceApi.updateFences(client, new FenceUpdateRequest.Builder().removeFence(FENCE_MOVING).build());
    Awareness.FenceApi.updateFences(client, new FenceUpdateRequest.Builder().removeFence(FENCE_HOME).build());
  }

  private void uninitializeBroadcastReceiver() {
    unregisterReceiver(fenceBroadcastReceiver);
  }

  private void notifyAware(CharSequence content) {
    NotificationCompat.Builder builder =
        new NotificationCompat.Builder(this).setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Aware")
            .setContentText(content);

    NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    manager.notify(0, builder.build());
  }

  private class FenceBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
      FenceState state = FenceState.extract(intent);

      if (TextUtils.equals(state.getFenceKey(), FENCE_TIME)) {
        if (state.getCurrentState() == FenceState.TRUE) {
          notifyAware("Time elapsed!");
        }
      }

      if (TextUtils.equals(state.getFenceKey(), FENCE_DISTANCE)) {
        if (state.getCurrentState() == FenceState.TRUE) {
          notifyAware("You left area!");
        }
      }

      if (TextUtils.equals(state.getFenceKey(), FENCE_MOVING)) {
        if (state.getCurrentState() == FenceState.TRUE) {
          notifyAware("You are started to move!");
        }
      }

      if (TextUtils.equals(state.getFenceKey(), FENCE_HOME)) {
        if (state.getCurrentState() == FenceState.TRUE) {
          notifyAware("You were entered home area!");
        }
      }
    }
  }

  private class OnTimeResultCallback implements ResultCallback<Status> {

    @Override
    public void onResult(@NonNull Status status) {
      if (status.isSuccess()) {
        Snackbar.make(timeFenceButton, "Device is now aware of time passing.", Snackbar.LENGTH_LONG).show();
      }
    }
  }

  private class OnTimeFenceButtonClicked implements View.OnClickListener {

    @Override
    public void onClick(View view) {
      long nowMillis = System.currentTimeMillis();
      long oneMinuteMillis = TimeUnit.SECONDS.toMillis(10);

      PendingIntent pendingIntent = PendingIntent.getBroadcast(FenceActivity.this, 0, fenceReceiverIntent, 0);

      AwarenessFence fence = TimeFence.inInterval(nowMillis + oneMinuteMillis, Long.MAX_VALUE);
      Awareness.FenceApi.updateFences(client,
          new FenceUpdateRequest.Builder().addFence(FENCE_TIME, fence, pendingIntent).build())
          .setResultCallback(new OnTimeResultCallback());
    }
  }

  private class OnDistanceResultCallback implements ResultCallback<Status> {

    @Override
    public void onResult(@NonNull Status status) {
      if (status.isSuccess()) {
        Snackbar.make(timeFenceButton, "Device is now aware of your area leaving.", Snackbar.LENGTH_LONG).show();
      }
    }
  }

  private class OnDistanceButtonClicked implements View.OnClickListener {

    @Override
    public void onClick(View view) throws SecurityException {
      PendingIntent pendingIntent = PendingIntent.getBroadcast(FenceActivity.this, 0, fenceReceiverIntent, 0);

      AwarenessFence fence = AwarenessFence.not(LocationFence.in(latitude, longitude, 50.0, 0L));
      Awareness.FenceApi.updateFences(client,
          new FenceUpdateRequest.Builder().addFence(FENCE_DISTANCE, fence, pendingIntent).build())
          .setResultCallback(new OnDistanceResultCallback());
    }
  }

  private class OnMovingResultCallback implements ResultCallback<Status> {

    @Override
    public void onResult(@NonNull Status status) {
      if (status.isSuccess()) {
        Snackbar.make(movingFenceButton, "Device is now aware of your moving activity.", Snackbar.LENGTH_LONG).show();
      }
    }
  }

  private class OnMovingButtonClicked implements View.OnClickListener {

    @Override
    public void onClick(View view) throws SecurityException {
      PendingIntent pendingIntent = PendingIntent.getBroadcast(FenceActivity.this, 0, fenceReceiverIntent, 0);

      AwarenessFence walking = DetectedActivityFence.during(DetectedActivityFence.WALKING);
      AwarenessFence running = DetectedActivityFence.during(DetectedActivityFence.RUNNING);
      AwarenessFence compound = AwarenessFence.or(walking, running);

      Awareness.FenceApi.updateFences(client,
          new FenceUpdateRequest.Builder().addFence(FENCE_MOVING, compound, pendingIntent).build())
          .setResultCallback(new OnMovingResultCallback());
    }
  }

  private class OnHomeResultCallback implements ResultCallback<Status> {

    @Override
    public void onResult(@NonNull Status status) {
      if (status.isSuccess()) {
        Snackbar.make(homeFenceButton, "Device is now aware of you entering home area.", Snackbar.LENGTH_LONG).show();
      }
    }
  }

  private class OnHomeButtonClicked implements View.OnClickListener {

    public static final double HOME_EPSILON = 20.0;

    @Override
    public void onClick(View view) throws SecurityException {
      PendingIntent pendingIntent = PendingIntent.getBroadcast(FenceActivity.this, 0, fenceReceiverIntent, 0);

      AwarenessFence fence = LocationFence.in(Constants.HOME_LATITUDE, Constants.HOME_LONGITUDE, HOME_EPSILON, 0L);
      Awareness.FenceApi.updateFences(client,
          new FenceUpdateRequest.Builder().addFence(FENCE_HOME, fence, pendingIntent).build())
          .setResultCallback(new OnHomeResultCallback());
    }
  }

  private class OnLocationChangedListener implements LocationListener {

    @Override
    public void onLocationChanged(Location location) {
      latitude = location.getLatitude();
      longitude = location.getLongitude();
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {
      // Do nothing.
    }

    @Override
    public void onProviderEnabled(String s) {
      // Do nothing.
    }

    @Override
    public void onProviderDisabled(String s) {
      // Do nothing.
    }
  }
}
