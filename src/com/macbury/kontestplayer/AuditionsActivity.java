package com.macbury.kontestplayer;

import com.androidquery.AQuery;
import com.astuetz.viewpager.extensions.PagerSlidingTabStrip;
import com.macbury.kontestplayer.main_screen.MainSectionPagerAdapter;
import com.macbury.kontestplayer.services.FeedSynchronizer;
import com.macbury.kontestplayer.utils.BaseColorActivity;

import android.os.Bundle;
import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.ActionBar.TabListener;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.TransitionDrawable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;

public class AuditionsActivity extends BaseColorActivity implements OnPageChangeListener {
  private static final String TAG = "AuditionsActivity";
  private static final String BUNDLE_SAVE_INSTANCE_TAB = "BUNDLE_SAVE_INSTANCE_TAB";
  private AQuery query;
  private ViewPager mViewPager;
  private MainSectionPagerAdapter mSectionsPagerAdapter;
  private PagerSlidingTabStrip tabs;
  private int currentPageIndex = 0;
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Log.i(TAG, "Created activity");
    
    setContentView(R.layout.activity_auditions);
    
    tabs                  = (PagerSlidingTabStrip) findViewById(R.id.tabs);
    mSectionsPagerAdapter = new MainSectionPagerAdapter(getSupportFragmentManager(), this.getApplicationContext());
    mViewPager            = (ViewPager) findViewById(R.id.pager);
    
    mViewPager.setAdapter(mSectionsPagerAdapter);
    tabs.setViewPager(mViewPager);
    tabs.setOnPageChangeListener(this);
    
    changeColor(mSectionsPagerAdapter.getColorForTab(0));
    tabs.setIndicatorColor(mSectionsPagerAdapter.getColorForTab(0));
    
    loadGUIFromBundle(savedInstanceState);
  }
  
  private void loadGUIFromBundle(Bundle savedInstanceState) {
    if (savedInstanceState != null) {
      currentPageIndex = savedInstanceState.getInt(BUNDLE_SAVE_INSTANCE_TAB);
      mViewPager.setCurrentItem(currentPageIndex);
    }
  }

  @Override
  protected void onRestoreInstanceState(Bundle savedInstanceState) {
    super.onRestoreInstanceState(savedInstanceState);
    loadGUIFromBundle(savedInstanceState);
  }
  
  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    outState.putInt(BUNDLE_SAVE_INSTANCE_TAB, currentPageIndex);
  }

  @Override
  protected void onStart() {
    super.onStart();
  }
  
  @Override
  protected void onStop() {
    // TODO Auto-generated method stub
    super.onStop();
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.auditions, menu);
    return true;
  }
  
  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.refresh:
        AppDelegate.shared().sync();
        return true;
    }
    return super.onOptionsItemSelected(item);
  }

  @Override
  public void onPageScrollStateChanged(int arg0) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void onPageScrolled(int arg0, float arg1, int arg2) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void onPageSelected(int index) {
    Log.d(TAG, "Switching to tab: "+index);
    changeColor(mSectionsPagerAdapter.getColorForTab(index));
    tabs.setIndicatorColor(mSectionsPagerAdapter.getColorForTab(index));
    currentPageIndex = index;
  }

}
