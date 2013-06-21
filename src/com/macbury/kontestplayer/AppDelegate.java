package com.macbury.kontestplayer;

import com.macbury.kontestplayer.auditions.AuditionManager;
import com.macbury.kontestplayer.sync.FeedSynchronizer;

import android.app.Application;
import android.content.Intent;
import android.util.Log;

public class AppDelegate extends Application {
  private static final String TAG = "AppDelegate";
  private static AppDelegate _shared;
  private AuditionManager auditionManager;
  @Override
  public void onCreate() {
    super.onCreate();
    _shared = this;
    Log.i(TAG, "Starting app");
    sync();
  }
  
  public AuditionManager getAuditionManager() {
    if (auditionManager == null) {
      auditionManager = AuditionManager.build(getResources());
    }
    
    return auditionManager;
  }

  @Override
  public void onLowMemory() {
    Log.i(TAG, "Freeing memory");
    auditionManager = null;
    super.onLowMemory();
  }

  public static AppDelegate shared() {
    return _shared;
  }

  public void sync() {
    startService(new Intent(this, FeedSynchronizer.class));
  }
}
