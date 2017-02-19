package com.rny925.nomnomnom;

import android.media.MediaPlayer;
import android.net.http.HttpResponseCache;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity implements LunchMenu.OnDataUpdated, LunchMenu.OnDataRemoved, SectionsPagerAdapter.OnPageLoaded, PageLoadedListener.OnPageChange
{
    private LunchMenu mMenu = null;
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;
    private boolean mUpdateRequested;
    private boolean mPageScrollStateChanged;
    private boolean mPageSelected;
    private boolean mPageLoaded;
    private PageLoadedListener mPageLoadedListener;
    private MediaPlayer mMediaPlayer;

    @Override
    protected void onStop()
    {
        super.onStop();
        HttpResponseCache cache = HttpResponseCache.getInstalled();
        if( cache != null )
        {
            cache.flush();
        }
    }

    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_main );
        Toolbar toolbar = ( Toolbar ) findViewById( R.id.toolbar );
        setSupportActionBar( toolbar );

        mMediaPlayer = MediaPlayer.create( this, R.raw.audio );
        mUpdateRequested = false;
        mPageScrollStateChanged = false;
        mPageSelected = false;
        mPageLoaded = false;
        mPageLoadedListener = new PageLoadedListener();
        mPageLoadedListener.setOnPageChangeListener( this );
        mSectionsPagerAdapter = new SectionsPagerAdapter( getSupportFragmentManager() );
        mSectionsPagerAdapter.setOnPageLoadedListener( this );
        mViewPager = ( ViewPager ) findViewById( R.id.container );
        mViewPager.setAdapter( mSectionsPagerAdapter );
        mViewPager.addOnPageChangeListener( mPageLoadedListener );
        TabLayout tabLayout = ( TabLayout ) findViewById( R.id.tabs );
        tabLayout.setupWithViewPager( mViewPager );

        try
        {
            File httpCacheDir = new File( getBaseContext().getCacheDir(), "http" );
            long httpCacheSize = 10 * 1024 * 1024;
            HttpResponseCache.install( httpCacheDir, httpCacheSize );
        } catch( IOException e )
        {
            e.printStackTrace();
        }

        if( savedInstanceState != null )
        {
            mMenu = ( LunchMenu ) savedInstanceState.getSerializable( "data" );
            mMenu.setOnDataUpdatedListener( this );
            mMenu.setOnDataRemovedListener( this );
            mSectionsPagerAdapter.updateItem( 255, 255, mMenu );
            mSectionsPagerAdapter.notifyDataSetChanged();
        }
        else
        {
            mMenu = new LunchMenu();
            mMenu.setOnDataUpdatedListener( this );
            mMenu.setOnDataRemovedListener( this );
            mMenu.execute();
        }

        FloatingActionButton fab = ( FloatingActionButton ) findViewById( R.id.fab );
        fab.setOnClickListener( new View.OnClickListener()
        {
            @Override
            public void onClick( View view )
            {
                if( !mMenu.isDownloading() )
                {
                    if( mViewPager.getCurrentItem() == 0 )
                    {
                        mMenu.remove( mMediaPlayer );
                    }
                    else
                    {
                        mUpdateRequested = true;
                        mViewPager.setCurrentItem( 0 );
                    }
                }
            }
        } );
    }

    @Override
    public void onSaveInstanceState( Bundle savedInstanceState )
    {
        super.onSaveInstanceState( savedInstanceState );
        savedInstanceState.putSerializable( "data", mMenu );
    }

    public void OnPageLoaded( )
    {
        if( mUpdateRequested )
        {
            mPageLoaded = true;
        }
        if( mPageScrollStateChanged && mPageSelected && mPageLoaded )
        {
            mUpdateRequested = false;
            mPageScrollStateChanged = false;
            mPageSelected = false;
            mPageLoaded = false;
            mMenu.remove( mMediaPlayer );
        }
    }

    public void OnPageScrollStateChanged()
    {
        if( mUpdateRequested )
        {
            mPageScrollStateChanged = true;
        }
        if( mPageScrollStateChanged && mPageSelected && mPageLoaded )
        {
            mUpdateRequested = false;
            mPageScrollStateChanged = false;
            mPageSelected = false;
            mPageLoaded = false;
            mMenu.remove( mMediaPlayer );
        }
    }

    public void OnPageSelected()
    {
        if( mUpdateRequested )
        {
            mPageSelected = true;
        }
        if( mPageScrollStateChanged && mPageSelected && mPageLoaded )
        {
            mUpdateRequested = false;
            mPageScrollStateChanged = false;
            mPageSelected = false;
            mPageLoaded = false;
            mMenu.remove( mMediaPlayer );
        }
    }

    public void OnDataUpdated( int dayNumber, int row )
    {
        mSectionsPagerAdapter.updateItem( dayNumber, row, mMenu );
        mSectionsPagerAdapter.notifyDataSetChanged();
    }

    public void OnDataRemoved( int dayNumber, int row )
    {
        mSectionsPagerAdapter.removeItem( dayNumber, row, mMenu );
        mSectionsPagerAdapter.notifyDataSetChanged();
        if( dayNumber == 0 && row == 255 )
        {
            mMenu.update();
        }
    }
}
