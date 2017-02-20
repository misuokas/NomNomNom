package com.rny925.nomnomnom;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.http.HttpResponseCache;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity implements LunchMenu.OnDataUpdated, LunchMenu.OnDataRemoved, SectionsPagerAdapter.OnPageLoaded, PageLoadedListener.OnPageChange, ActivityCompat.OnRequestPermissionsResultCallback
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

    private static boolean isNetworkAvailable( Context context )
    {
        ConnectivityManager conMan = ( ConnectivityManager ) context.getSystemService( Context.CONNECTIVITY_SERVICE );
        if( conMan.getActiveNetworkInfo() != null && conMan.getActiveNetworkInfo().isConnected() )
        {
            return true;
        }
        else
        {
            return false;
        }
    }

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


        if( !isNetworkAvailable( this ) )
        {
            AlertDialog.Builder builder = new AlertDialog.Builder( this );
            builder.setTitle( "Make Lunch Great Again" );
            builder.setMessage( "Network not available. Exiting." );
            builder.setPositiveButton( "OK", new DialogInterface.OnClickListener()
            {
                public void onClick( DialogInterface dialog, int id )
                {
                    finish();
                }
            } );
            builder.show();
        }

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
            if( ActivityCompat.checkSelfPermission( this, Manifest.permission.INTERNET ) != PackageManager.PERMISSION_GRANTED ||
                    ActivityCompat.checkSelfPermission( this, Manifest.permission.ACCESS_NETWORK_STATE ) != PackageManager.PERMISSION_GRANTED )
            {
                final String[] permission = new String[]{
                        Manifest.permission.INTERNET,
                        Manifest.permission.ACCESS_NETWORK_STATE };

                if( ActivityCompat.shouldShowRequestPermissionRationale( this, Manifest.permission.INTERNET ) ||
                        ActivityCompat.shouldShowRequestPermissionRationale( this, Manifest.permission.ACCESS_NETWORK_STATE ) )
                {
                    AlertDialog.Builder builder = new AlertDialog.Builder( this );
                    builder.setTitle( "Make Lunch Great Again" );
                    builder.setMessage( "Permission for internet required." );
                    builder.setPositiveButton( "OK", new DialogInterface.OnClickListener()
                    {
                        public void onClick( DialogInterface dialog, int id )
                        {
                            ActivityCompat.requestPermissions( MainActivity.this, permission, 10 );
                        }
                    } );
                    builder.show();
                }
                else
                {
                    ActivityCompat.requestPermissions( this, permission, 10 );
                }
            }
            else
            {
                mMenu = new LunchMenu();
                mMenu.setOnDataUpdatedListener( this );
                mMenu.setOnDataRemovedListener( this );
                mMenu.execute();
            }
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

    @Override
    public void onRequestPermissionsResult( int requestCode,
                                            String permissions[], int[] grantResults )
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch( requestCode )
        {
            case 10:
            {
                if( grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED )
                {
                    mMenu = new LunchMenu();
                    mMenu.setOnDataUpdatedListener( this );
                    mMenu.setOnDataRemovedListener( this );
                    mMenu.execute();
                }
                else
                {
                    finish();
                }
                break;
            }
        }
    }

    public void OnPageLoaded()
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
