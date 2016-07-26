package com.todev.samples;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

public class SamplesActivity extends ListActivity {

  public static final String[] TITLES = new String[] {
      "Fence API Samples", "Map Clustering Samples"
  };

  public static final Class[] ACTIVITIES = new Class[] {
      FenceActivity.class, MapClusteringActivity.class
  };

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_samples);

    setListAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, TITLES));

    getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        Intent intent = new Intent(SamplesActivity.this, ACTIVITIES[position]);
        startActivity(intent);
      }
    });
  }
}
