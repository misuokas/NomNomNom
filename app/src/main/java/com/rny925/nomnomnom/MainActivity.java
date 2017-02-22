package com.rny925.nomnomnom;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.http.HttpResponseCache;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.Menu;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity implements LunchMenu.OnDataUpdated, LunchMenu.OnDataRemoved, SectionsPagerAdapter.OnPageLoaded, PageLoadedListener.OnPageChange, ActivityCompat.OnRequestPermissionsResultCallback, Weather.OnWeatherCompleted
{
    private LunchMenu mMenu = null;
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;
    private boolean mUpdateRequested = false;
    private boolean mPageScrollStateChanged = false;
    private boolean mPageSelected = false;
    private boolean mPageLoaded = false;
    private PageLoadedListener mPageLoadedListener;
    private MediaPlayer mMediaPlayer;
    private int mMenuType = 0;
    private boolean mSounds = true;
    private Weather mWeather = null;
    private Typeface mWeatherFont = null;

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
        if( mMediaPlayer != null )
        {
            mMediaPlayer.stop();
        }
        HttpResponseCache cache = HttpResponseCache.getInstalled();
        if( cache != null )
        {
            cache.flush();
        }
    }

    private void setMenuTitle()
    {
        Toolbar toolbar = ( Toolbar ) findViewById( R.id.toolbar );
        if( mMenuType == 0 )
        {
            toolbar.setTitle( "Smarthouse" );
            getSupportActionBar().setTitle( "Smarthouse" );
        }
        else
        {
            toolbar.setTitle( "Galaksi" );
            getSupportActionBar().setTitle( "Galaksi" );
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

        mWeatherFont = Typeface.createFromAsset( this.getAssets(), "fonts/weather.ttf" );
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

        SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences( getBaseContext() );
        if( SP.getString( "preferredRestaurant", "0" ).equals( "0" ) )
        {
            mMenuType = 0;
        }
        else
        {
            mMenuType = 1;
        }

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
                disableUpdate();

                if( mViewPager.getCurrentItem() == 0 )
                {
                    mMenu.remove();
                }
                else
                {
                    mUpdateRequested = true;
                    mViewPager.setCurrentItem( 0 );
                }
            }
        } );

        if( savedInstanceState != null )
        {
            mMenu = ( LunchMenu ) savedInstanceState.getSerializable( "data" );
            mMenu.setOnDataUpdatedListener( this );
            mMenu.setOnDataRemovedListener( this );
            mMenuType = mMenu.getType();
            setMenuTitle();
            mSectionsPagerAdapter.updateItem( 255, 255, mMenu );
            mSectionsPagerAdapter.notifyDataSetChanged();

            if( !mMenu.isDownloading() )
            {
                fab.setVisibility( View.VISIBLE );
            }

            mWeather = new Weather();
            mWeather.setOnWeatherCompletedListener( this );
            mWeather.execute();
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
                mSounds = SP.getBoolean( "Sounds", true );
                if( mSounds && mMediaPlayer != null && !mMediaPlayer.isPlaying() )
                {
                    mMediaPlayer.start();
                }
                setMenuTitle();

                mMenu.execute( mMenuType );

                mWeather = new Weather();
                mWeather.setOnWeatherCompletedListener( this );
                mWeather.execute();
            }
        }
    }

    public void OnWeatherCompleted( String weather )
    {
        TextView textView = ( TextView ) findViewById( R.id.section_weather );
        if( mWeatherFont != null )
        {
            textView.setTypeface( mWeatherFont );
        }
        textView.setText( Html.fromHtml( weather ) );
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
        super.onRequestPermissionsResult( requestCode, permissions, grantResults );
        switch( requestCode )
        {
            case 10:
            {
                if( grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED )
                {
                    mMenu = new LunchMenu();
                    mMenu.setOnDataUpdatedListener( this );
                    mMenu.setOnDataRemovedListener( this );
                    SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences( getBaseContext() );
                    mSounds = SP.getBoolean( "Sounds", true );
                    if( mSounds && mMediaPlayer != null && !mMediaPlayer.isPlaying() )
                    {
                        mMediaPlayer.start();
                    }
                    setMenuTitle();

                    mMenu.execute( mMenuType );

                    mWeather = new Weather();
                    mWeather.setOnWeatherCompletedListener( this );
                    mWeather.execute();
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
            mMenu.remove();
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
            mMenu.remove();
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
            mMenu.remove();
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

            invalidateOptionsMenu();
        }
    }

    public void OnDataRemoved( int dayNumber, int row )
    {
        mSectionsPagerAdapter.removeItem( dayNumber, row, mMenu );
        mSectionsPagerAdapter.notifyDataSetChanged();
        if( dayNumber == 0 && row == 255 )
        {
            mMenu.update( mMenuType );
            mWeather.execute();
        }
    }

    @Override
    public boolean onPrepareOptionsMenu( Menu menu )
    {
        menu.getItem( 2 ).setChecked( mSounds );
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
            case R.id.action_settings:
            {
                Intent i = new Intent( this, MyPreferencesActivity.class );
                startActivity( i );
                break;
            }
            case R.id.action_smarthouse:
            {
                disableUpdate();
                mMenuType = 0;
                setMenuTitle();

                if( mViewPager.getCurrentItem() == 0 )
                {
                    mMenu.remove();
                }
                else
                {
                    mUpdateRequested = true;
                    mViewPager.setCurrentItem( 0 );
                }
                break;
            }
            case R.id.action_galaksi:
            {
                disableUpdate();
                mMenuType = 1;
                setMenuTitle();

                if( mViewPager.getCurrentItem() == 0 )
                {
                    mMenu.remove();
                }
                else
                {
                    mUpdateRequested = true;
                    mViewPager.setCurrentItem( 0 );
                }
                break;
            }
            default:
            {
                break;
            }
        }

        return super.onOptionsItemSelected( item );
    }
}
