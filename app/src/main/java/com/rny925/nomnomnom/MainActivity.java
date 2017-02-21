package com.rny925.nomnomnom;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
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
import android.view.*;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.widget.TextView;

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
    private int mMenuType;

    private void disableUpdate()
    {
        FloatingActionButton fab = ( FloatingActionButton ) findViewById( R.id.fab );
        fab.setVisibility( View.INVISIBLE );

        Animation fadeIn = new AlphaAnimation( 1, 0 );
        fadeIn.setInterpolator( new AccelerateInterpolator() );
        fadeIn.setDuration( 500 );

        AnimationSet animation = new AnimationSet( false );
        animation.addAnimation( fadeIn );
        animation.setRepeatCount( 1 );

        fab.setAnimation( animation );
    }

    private boolean isNetworkAvailable( Context context )
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

    private void setMenuTitle( int type )
    {
        TextView textView = ( TextView ) findViewById( R.id.section_title );
        if( mMenuType == 0 )
        {
            textView.setText( "Smarthouse" );
        }
        else
        {
            textView.setText( "Galaksi" );
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
        mMenuType = 0;
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

        FloatingActionButton fab = ( FloatingActionButton ) findViewById( R.id.fab );
        fab.setVisibility( View.INVISIBLE );
        fab.setOnClickListener( new View.OnClickListener()
        {
            @Override
            public void onClick( View view )
            {
                if( !mMenu.isDownloading() )
                {
                    disableUpdate();

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

        if( savedInstanceState != null )
        {
            mMenu = ( LunchMenu ) savedInstanceState.getSerializable( "data" );
            mMenu.setOnDataUpdatedListener( this );
            mMenu.setOnDataRemovedListener( this );
            SharedPreferences prefs = getSharedPreferences( "Make Lunch Great Again Settings", MODE_PRIVATE );
            Boolean sounds = prefs.getBoolean( "Sounds", false );
            mMenu.setSounds( sounds );
            mMenuType = mMenu.getType();
            setMenuTitle( mMenuType );
            mSectionsPagerAdapter.updateItem( 255, 255, mMenu );
            mSectionsPagerAdapter.notifyDataSetChanged();

            if( !mMenu.isDownloading() )
            {
                fab.setVisibility( View.VISIBLE );
            }
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
                SharedPreferences prefs = getSharedPreferences( "Make Lunch Great Again Settings", MODE_PRIVATE );
                Boolean sounds = prefs.getBoolean( "Sounds", false );
                mMenu.setSounds( sounds );
                setMenuTitle( mMenuType );

                mMenu.execute( mMenuType, mMediaPlayer );
            }
        }
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
                    mMenu = new LunchMenu( );
                    mMenu.setOnDataUpdatedListener( this );
                    mMenu.setOnDataRemovedListener( this );
                    SharedPreferences prefs = getSharedPreferences( "Make Lunch Great Again Settings", MODE_PRIVATE );
                    Boolean sounds = prefs.getBoolean( "Sounds", false );
                    mMenu.setSounds( sounds );
                    setMenuTitle( mMenuType );

                    mMenu.execute( mMenuType, mMediaPlayer );
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

    public void OnDataUpdated( int dayNumber, int row, boolean isDownloading )
    {
        mSectionsPagerAdapter.updateItem( dayNumber, row, mMenu );
        mSectionsPagerAdapter.notifyDataSetChanged();
        if( !isDownloading )
        {
            FloatingActionButton fab = ( FloatingActionButton ) findViewById( R.id.fab );
            fab.setVisibility( View.VISIBLE );

            Animation fadeIn = new AlphaAnimation( 0, 1 );
            fadeIn.setInterpolator( new AccelerateInterpolator() );
            fadeIn.setDuration( 500 );

            AnimationSet animation = new AnimationSet( false );
            animation.addAnimation( fadeIn );
            animation.setRepeatCount( 1 );

            fab.setAnimation( animation );
        }
    }

    public void OnDataRemoved( int dayNumber, int row )
    {
        mSectionsPagerAdapter.removeItem( dayNumber, row, mMenu );
        mSectionsPagerAdapter.notifyDataSetChanged();
        if( dayNumber == 0 && row == 255 )
        {
            mMenu.update( mMenuType );
        }
    }

    @Override
    public boolean onPrepareOptionsMenu (Menu menu) {
        if ( mMenu.isDownloading() )
        {
            menu.getItem( 0 ).setEnabled( false );
            menu.getItem( 1 ).setEnabled( false );
        }
        else
        {
            menu.getItem( 0 ).setEnabled( true );
            menu.getItem( 1 ).setEnabled( true );
        }
        menu.getItem( 2 ).setChecked( mMenu.getSounds() );

        return true;
    }

    @Override
    public boolean onCreateOptionsMenu( Menu menu )
    {
        getMenuInflater().inflate( R.menu.menu_main, menu );
        return true;
    }

    @Override
    public boolean onOptionsItemSelected( android.view.MenuItem item )
    {
        int id = item.getItemId();

        switch( id )
        {
            case R.id.action_smarthouse:
            {
                if( !mMenu.isDownloading() )
                {
                    disableUpdate();
                    mMenuType = 0;
                    setMenuTitle( mMenuType );

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
                break;
            }
            case R.id.action_galaksi:
            {
                if( !mMenu.isDownloading() )
                {
                    disableUpdate();
                    mMenuType = 1;
                    setMenuTitle( mMenuType );

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
                break;
            }
            case R.id.soundonoff:
            {
                if( mMenu.getSounds() )
                {
                    mMenu.setSounds( false );
                }
                else
                {
                    mMenu.setSounds( true );
                }
                SharedPreferences.Editor editor = getSharedPreferences( "Make Lunch Great Again Settings", MODE_PRIVATE ).edit();
                editor.putBoolean( "Sounds", mMenu.getSounds() );
                editor.commit();
            }
            default:
            {
                break;
            }
        }

        return super.onOptionsItemSelected( item );
    }
}
