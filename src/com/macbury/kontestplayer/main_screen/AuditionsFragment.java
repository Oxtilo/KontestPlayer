package com.macbury.kontestplayer.main_screen;

import java.sql.SQLException;
import java.util.ArrayList;

import com.macbury.kontestplayer.AppDelegate;
import com.macbury.kontestplayer.auditions.Audition;
import com.macbury.kontestplayer.auditions.AuditionManager;
import com.macbury.kontestplayer.auditions.AuditionsArrayAdapter;
import com.macbury.kontestplayer.episodes.EpisodesActivity;
import com.macbury.kontestplayer.services.FeedSynchronizer;
import com.macbury.kontestplayer.utils.PageTabInterface;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class AuditionsFragment extends ListFragment implements PageTabInterface {
  private AuditionsArrayAdapter auditionsArrayAdapter;
  public final static int ACTION_BAR_COLOR  = 0xFF3F9FE0;
  private static final String TAG           = "AuditionsFragment";
  
  @Override
  public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    reloadAuditions();
  }

  protected void reloadAuditions() {
    Log.i(TAG, "Reloading auditions.");
    ArrayList<Audition> auditions = null;
    try {
      auditions = AppDelegate.shared().getDBHelper().getOrderedAuditions();
    } catch (SQLException e) {
      
    }
    
    if (auditions == null || auditions.size() == 0) {
      
    } else {
      if (getListAdapter() == null) {
        auditionsArrayAdapter   = new AuditionsArrayAdapter(this.getActivity(), auditions);
        setListAdapter(auditionsArrayAdapter);
      } else {
        auditionsArrayAdapter.setAuditions(auditions);
      }
    }
  }

  @Override
  public int getTabColor() {
    return ACTION_BAR_COLOR;
  }

  @Override
  public String getTabName(Context context) {
    return "Lista audycji";//context.getString(R.string.auditions_activity_tab_auditions_list);
  }

  @Override
  public void onListItemClick(ListView l, View v, int position, long id) {
    Audition audition = auditionsArrayAdapter.getItem(position);
    Intent   intent   = new Intent(getActivity(), EpisodesActivity.class);
    intent.putExtra(EpisodesActivity.EXTRA_AUDITION, audition.getId());
    intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
    startActivity(intent);
  }
  
  @Override
  public void onStart() {
    getActivity().registerReceiver(mRefreshReciver, new IntentFilter(FeedSynchronizer.BROADCAST_ACTION_FINISHED_SYNCING));
    super.onStart();
  }

  @Override
  public void onStop() {
    getActivity().unregisterReceiver(mRefreshReciver);
    super.onStop();
  }
  
  private BroadcastReceiver mRefreshReciver = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {
      reloadAuditions();
    }
  };
}
