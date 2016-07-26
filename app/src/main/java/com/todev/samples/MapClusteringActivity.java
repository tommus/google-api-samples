package com.todev.samples;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.OvershootInterpolator;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.SphericalUtil;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;

public class MapClusteringActivity extends FragmentActivity implements OnMapReadyCallback {

  private static final LatLng HOME_LOCATION = Constants.HOME_LOCATION;
  public static final double MIN_DISTANCE = 1000;
  public static final double MAX_DISTANCE = 10000;
  public static final int ITEMS = 500;

  private ClusterManager<ClusterMarker> clusterManager;

  private FloatingActionsMenu menuActions;

  private GoogleMap map;

  private boolean menuActionRevealed = false;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_map_clustering);

    SupportMapFragment fragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
    fragment.getMapAsync(this);

    menuActions = (FloatingActionsMenu) findViewById(R.id.menu_actions);

    FloatingActionButton buttonCenter = (FloatingActionButton) findViewById(R.id.button_home);
    buttonCenter.setOnClickListener(new OnButtonCenterClickedListener());

    FloatingActionButton buttonRandomize = (FloatingActionButton) findViewById(R.id.button_randomize);
    buttonRandomize.setOnClickListener(new OnButtonRandomizeClickedListener());
  }

  @Override
  public void onMapReady(GoogleMap map) throws SecurityException {
    this.map = map;
    this.map.setMyLocationEnabled(true);
    this.map.setOnMapClickListener(new OnMapClickedListener());

    clusterManager = new SimpleClusterManager(this, map);
    clusterManager.setRenderer(new SimpleClusterRenderer(this, map, clusterManager));
    map.setOnCameraChangeListener(clusterManager);
    map.setOnMarkerClickListener(clusterManager);

    loadHome(clusterManager);
    loadMarkers(clusterManager, map, HOME_LOCATION, ITEMS, MIN_DISTANCE, MAX_DISTANCE);
  }

  private void loadHome(ClusterManager<ClusterMarker> manager) {
    ClusterMarker marker = new ClusterMarker(new MarkerOptions().position(HOME_LOCATION).title("Home"));
    manager.addItem(marker);
  }

  private void loadMarkers(ClusterManager<ClusterMarker> manager, GoogleMap map, LatLng center, int count,
      double minDistance, double maxDistance) {
    double minLat = Double.MAX_VALUE;
    double maxLat = Double.MIN_VALUE;
    double minLon = Double.MAX_VALUE;
    double maxLon = Double.MIN_VALUE;

    for (int i = 0; i < count; ++i) {
      double distance = minDistance + Math.random() * maxDistance;
      double heading = Math.random() * 360 - 180;

      LatLng position = SphericalUtil.computeOffset(center, distance, heading);

      ClusterMarker marker = new ClusterMarker(new MarkerOptions().position(position).title("Item No. " + i));
      manager.addItem(marker);

      minLat = Math.min(minLat, position.latitude);
      minLon = Math.min(minLon, position.longitude);
      maxLat = Math.max(maxLat, position.latitude);
      maxLon = Math.max(maxLon, position.longitude);
    }

    LatLng min = new LatLng(minLat, minLon);
    LatLng max = new LatLng(maxLat, maxLon);
    LatLngBounds bounds = new LatLngBounds(min, max);

    map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));
  }

  private boolean revealMenu(View view, boolean reveal, long delay) {
    ViewPropertyAnimator animator = view.animate();

    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
      animator.setInterpolator(reveal ? new OvershootInterpolator() : new AnticipateInterpolator());
    }

    float offset = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 60.0f, getResources().getDisplayMetrics());
    animator.setStartDelay(delay);
    animator.translationYBy(reveal ? -offset : offset);

    return reveal;
  }

  private class OnButtonCenterClickedListener implements View.OnClickListener {

    @Override
    public void onClick(View view) {
      map.animateCamera(CameraUpdateFactory.newLatLngZoom(HOME_LOCATION, 15.0f));

      if (menuActionRevealed) {
        menuActionRevealed = revealMenu(menuActions, false, 750);
      }
    }
  }

  private class OnButtonRandomizeClickedListener implements View.OnClickListener {

    @Override
    public void onClick(View view) {
      clusterManager.clearItems();
      loadHome(clusterManager);
      loadMarkers(clusterManager, map, HOME_LOCATION, ITEMS, MIN_DISTANCE, MAX_DISTANCE);

      if (menuActionRevealed) {
        menuActionRevealed = revealMenu(menuActions, false, 1300);
      }
    }
  }

  private class OnMapClickedListener implements GoogleMap.OnMapClickListener {

    @Override
    public void onMapClick(LatLng latLng) {
      if (menuActionRevealed) {
        menuActionRevealed = revealMenu(menuActions, false, 0);
      }
    }
  }

  private class ClusterMarker implements ClusterItem {

    private final MarkerOptions options;

    public ClusterMarker(MarkerOptions options) {
      this.options = options;
    }

    @Override
    public LatLng getPosition() {
      return options.getPosition();
    }

    public String getTitle() {
      return options.getTitle();
    }
  }

  private class SimpleClusterRenderer extends DefaultClusterRenderer<ClusterMarker> {

    public SimpleClusterRenderer(Context context, GoogleMap map, ClusterManager<ClusterMarker> clusterManager) {
      super(context, map, clusterManager);
    }

    @Override
    protected void onBeforeClusterItemRendered(ClusterMarker item, MarkerOptions markerOptions) {
      super.onBeforeClusterItemRendered(item, markerOptions);

      markerOptions.title(item.getTitle());
    }
  }

  private class SimpleClusterManager extends ClusterManager<ClusterMarker> {

    public SimpleClusterManager(Context context, GoogleMap map) {
      super(context, map);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
      if (!menuActionRevealed) {
        menuActionRevealed = revealMenu(menuActions, true, 0);
      }

      return super.onMarkerClick(marker);
    }
  }
}
