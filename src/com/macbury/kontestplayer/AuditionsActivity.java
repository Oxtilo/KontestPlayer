package com.macbury.kontestplayer;

import com.androidquery.AQuery;
import com.astuetz.viewpager.extensions.PagerSlidingTabStrip;
import com.macbury.kontestplayer.main_screen.MainSectionPagerAdapter;

import android.os.Bundle;
import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.ActionBar.TabListener;
import android.app.FragmentTransaction;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.TransitionDrawable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.Menu;

public class AuditionsActivity extends FragmentActivity implements TabListener, OnPageChangeListener {
  private static final String TAG = "AuditionsActivity";
  private AQuery query;
  private ViewPager mViewPager;
  private MainSectionPagerAdapter mSectionsPagerAdapter;
  private PagerSlidingTabStrip tabs;
  
  private Drawable oldBackground = null;
  private int currentColor       = 0xFF666666;
  
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
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.auditions, menu);
    return true;
  }
  
  public void changeColor(int newColor) {
    tabs.setIndicatorColor(newColor);
    Drawable colorDrawable  = new ColorDrawable(newColor);
    //Drawable bottomDrawable = getResources().getDrawable(R.drawable.actionbar_bottom);
    LayerDrawable ld        = new LayerDrawable(new Drawable[] { colorDrawable/*, bottomDrawable */});
    
    
    if (oldBackground == null) {
      getActionBar().setBackgroundDrawable(ld);
    } else {
      TransitionDrawable td = new TransitionDrawable(new Drawable[] { oldBackground, ld });
      getActionBar().setBackgroundDrawable(td);
      td.startTransition(200);
    }
    oldBackground = ld;
    currentColor  = newColor;
    
    getActionBar().setDisplayShowTitleEnabled(false);
    getActionBar().setDisplayShowTitleEnabled(true);
    getActionBar().setDisplayUseLogoEnabled(false);
    getActionBar().setDisplayShowHomeEnabled(false);
  }

  @Override
  public void onTabReselected(Tab tab, FragmentTransaction ft) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void onTabSelected(Tab tab, FragmentTransaction ft) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void onTabUnselected(Tab tab, FragmentTransaction ft) {
    // TODO Auto-generated method stub
    
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
    //mSectionsPagerAdapter.getItem(index);
    changeColor(mSectionsPagerAdapter.getColorForTab(index));
  }

}
