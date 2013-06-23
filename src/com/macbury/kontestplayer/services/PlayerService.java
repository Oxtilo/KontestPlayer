package com.macbury.kontestplayer.services;

import java.io.IOException;

import com.macbury.kontestplayer.AppDelegate;
import com.macbury.kontestplayer.R;
import com.macbury.kontestplayer.auditions.Audition;
import com.macbury.kontestplayer.auditions.Episode;
import com.macbury.kontestplayer.player.PlayerActivity;
import com.macbury.kontestplayer.utils.Utils;

import android.media.AudioManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

public class PlayerService extends Service implements OnPreparedListener, OnBufferingUpdateListener, OnCompletionListener, OnSeekCompleteListener, OnErrorListener {
  private static final String TAG = "PlayerService";
  private MediaPlayer mediaPlayer;
  private WifiManager.WifiLock wifilock;
  private Audition currentAudition;
  private Episode currentEpisode;
  private SleepTimer sleepTimer;
  private Thread sleepThread;
  
  private boolean isPrepared;
  static final String WIFILOCK                              = "OPTION_PERM_WIFILOCK";
  public static final String EXTRA_URL                      = "EXTRA_URL";
  private static final int NOTIFICATION_ID                  = 50;
  public static final String EXTRA_AUDITION                 = "EXTRA_AUDITION";
  public static final String EXTRA_EPISODE                  = "EXTRA_EPISODE";
  public static final String EXTRA_ACTION                   = "EXTRA_ACTION";
  public static final String ACTION_START                   = "ACTION_START";
  public static final String ACTION_STOP                    = "ACTION_STOP";
  public static final String ACTION_UPDATE_PLAYBACK_INFO    = "com.macbury.kontestplayer.ACTION_UPDATE_PLAYBACK_INFO";
  public static final String EXTRA_ACTION_PLAY              = "EXTRA_ACTION_PLAY";
  public static final String EXTRA_ACTION_PAUSE             = "EXTRA_ACTION_PAUSE";
  @Override
  public void onCreate() {
    super.onCreate();
    acquireWifiLock(this);
    Log.i(TAG, "Creating player service");
    
    sleepTimer  = new SleepTimer(this, ACTION_UPDATE_PLAYBACK_INFO);
    sleepThread = new Thread(sleepTimer);
    sleepThread.start();
    registerReceiver(mUpdateBroadcast, new IntentFilter(ACTION_UPDATE_PLAYBACK_INFO));
  }
  
  private void updateNotification() {
    NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
      .setSmallIcon(com.macbury.kontestplayer.R.drawable.av_play_dark)
      .setContentTitle(currentAudition.getTitle());
    builder.setContentText(currentEpisode.getTitle());
    builder.setSubText(Utils.formatDurationToString(mediaPlayer.getCurrentPosition() / 1000));
    
    Intent intent = new Intent(this, PlayerActivity.class);
    intent.putExtra(PlayerActivity.EPISODE_ID_EXTRA, currentEpisode.getId());
    intent.putExtra(PlayerActivity.AUDITION_EXTRA, currentAudition.getId());
    intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
    PendingIntent pi = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    builder.setContentIntent(pi);
    
    if (mediaPlayer.isPlaying()) {
      intent = new Intent(this, PlayerService.class);
      intent.putExtra(EXTRA_EPISODE, currentEpisode.getId());
      intent.putExtra(EXTRA_AUDITION, currentAudition.getId());
      intent.putExtra(EXTRA_ACTION, EXTRA_ACTION_PAUSE);
      pi = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
      
      builder.addAction(R.drawable.av_pause_dark, "Pauza", pi);    
    } else {
      intent = new Intent(this, PlayerService.class);
      intent.putExtra(EXTRA_EPISODE, currentEpisode.getId());
      intent.putExtra(EXTRA_AUDITION, currentAudition.getId());
      intent.putExtra(EXTRA_ACTION, EXTRA_ACTION_PLAY);
      
      pi = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
      
      builder.addAction(R.drawable.av_play_dark, "Odtwarzaj", pi);
    }
    
    startForeground(NOTIFICATION_ID, builder.build());
  }
  
  private void createMediaPlayer() {
    mediaPlayer = new MediaPlayer();
    mediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
    mediaPlayer.setOnPreparedListener(this);
    mediaPlayer.setOnCompletionListener(this);
    mediaPlayer.setOnBufferingUpdateListener(this);
    mediaPlayer.setOnSeekCompleteListener(this);
    mediaPlayer.setOnErrorListener(this);
  }

  @Override
  public void onDestroy() {
    Log.i(TAG, "Destroying player service");
    super.onDestroy();
    releaseWifilock();
    mediaPlayer.release();
    sleepTimer.stop();
    sleepTimer = null;
    
    unregisterReceiver(mUpdateBroadcast);
  }

  @Override
  public IBinder onBind(Intent intent) {
    return mBinder;
  }
  
  @Override
  public boolean onUnbind(Intent intent) {
    // TODO Auto-generated method stub
    return super.onUnbind(intent);
  }

  private final IBinder mBinder = new LocalBinder();
  
  public class LocalBinder extends Binder {
    public PlayerService getService() {
      return PlayerService.this;
    }
  }
  
  public void acquireWifiLock(Context ctx) {
    WifiManager wifiManager = (WifiManager) ctx.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
    releaseWifilock();
    wifilock = wifiManager.createWifiLock(WifiManager.WIFI_MODE_FULL, WIFILOCK);
    wifilock.setReferenceCounted(true);
    wifilock.acquire();
    Log.d(TAG, "WifiLock " + WIFILOCK + " aquired (FULL_MODE)");
    Log.d(TAG, "Checking if Wifilock is held:" + wifilock.isHeld()); 
  }
  
  public void releaseWifilock() {
    Log.d(TAG, "releaseWifilock called");
    if ((wifilock != null) && (wifilock.isHeld()))
    {
      wifilock.release();
      Log.d(TAG, "Wifilock " + WIFILOCK + " released");
    }
  }
  
  public boolean holdsWifiLock() {
    Log.d(TAG, "holdsWifilock called");
    if (wifilock != null) {
      return (wifilock.isHeld());
    }
    return false;
  }

  @Override
  public void onPrepared(MediaPlayer mediaPlayer) {
    mediaPlayer.start();
    Log.d(TAG, "Media is ready!");
    Log.d(TAG, "Duration of file is " + mediaPlayer.getDuration());
    
    isPrepared = true;
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    Audition au     = AppDelegate.shared().getAuditionManager().findById(intent.getExtras().getInt(EXTRA_AUDITION));
    Episode ep      = au.findEpisode(intent.getExtras().getInt(EXTRA_EPISODE));

    if (ep != currentEpisode) {
      if (mediaPlayer != null) {
        mediaPlayer.release();
      }
      createMediaPlayer();
      currentEpisode  = ep;
      currentAudition = au;
      Log.d(TAG, "Preparing: " + currentEpisode.getMp3Url());
      try {
        mediaPlayer.setDataSource(currentEpisode.getMp3Url());
      } catch (IllegalArgumentException e) {
        e.printStackTrace();
        stopSelf();
      } catch (SecurityException e) {
        e.printStackTrace();
        stopSelf();
      } catch (IllegalStateException e) {
        e.printStackTrace();
        stopSelf();
      } catch (IOException e) {
        e.printStackTrace();
        stopSelf();
      }
      
      mediaPlayer.prepareAsync();
    } else if (isPrepared) {
      if (intent.getExtras().getString(EXTRA_ACTION).equals(EXTRA_ACTION_PLAY)) {
        mediaPlayer.start();
      } else {
        mediaPlayer.pause();
      }
    }
    
    updateNotification();
    return super.onStartCommand(intent, flags, startId);
  }

  @Override
  public void onBufferingUpdate(MediaPlayer mediaPlayer, int percent) {
    
  }
  
  private BroadcastReceiver mUpdateBroadcast = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {
      if (mediaPlayer != null)
        updateNotification();
    }
  };
  
  public MediaPlayer getMediaPlayer() {
    return mediaPlayer;
  }

  @Override
  public void onCompletion(MediaPlayer player) {
    if (isPrepared) {
      Log.i(TAG, "on playback completion");
      stopForeground(false);
      stopSelf();
    }
  }

  @Override
  public void onSeekComplete(MediaPlayer arg0) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public boolean onError(MediaPlayer arg0, int what, int extra) {
    //Toast.makeText(this, "B��d: "+what, Toast.LENGTH_LONG);
    //stopSelf();
    return true;
  }
}
