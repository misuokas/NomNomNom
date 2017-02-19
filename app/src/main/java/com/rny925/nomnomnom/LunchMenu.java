package com.rny925.nomnomnom;

import android.media.MediaPlayer;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class LunchMenu implements JSONDownloader.OnJSONDownloadCompleted, MenuItem.OnBitmapUpdated, Serializable
{
    private ArrayList< MenuList > mMenu;
    private boolean mIsDownloading;
    private int mCount;
    private int mSize;
    transient private OnDataUpdated mOnDataUpdatedListener;
    transient private OnDataRemoved mOnDataRemovedListener;

    LunchMenu()
    {
        mMenu = new ArrayList<>();
        mOnDataUpdatedListener = null;
        mCount = 0;
        mSize = 0;
        mIsDownloading = false;
    }

    private void download()
    {
        JSONDownloader downloader = new JSONDownloader( "http://www.amica.fi/modules/json/json/Index?costNumber=3498&language=fi&firstDay=" + getDate() );
        downloader.setOnDownloadCompletedListener( this );
        downloader.execute();
    }

    public void execute()
    {
        if( !mIsDownloading )
        {
            mIsDownloading = true;
            download();
        }
    }

    public boolean isDownloading()
    {
        return mIsDownloading;
    }

    public void update()
    {
        download();
    }

    public void remove( MediaPlayer mediaPlayer )
    {
        if( !mIsDownloading )
        {
            mIsDownloading = true;

            if( mediaPlayer != null && !mediaPlayer.isPlaying() )
            {
                mediaPlayer.start();
            }

            if( mMenu != null )
            {
                int lastDay = mSize;
                for( int i = 0; i < lastDay; i++ )
                {
                    int lastTitle = mMenu.get( lastDay - i - 1 ).getSize();
                    for( int j = 0; j < lastTitle; j++ )
                    {
                        mMenu.get( lastDay - i - 1 ).del( lastTitle - j - 1 );
                        mMenu.get( lastDay - i - 1 ).decCount();
                        if( mOnDataRemovedListener != null )
                        {
                            mOnDataRemovedListener.OnDataRemoved( lastDay - i - 1, lastTitle - j - 1 );
                        }
                    }
                    mCount--;
                    mSize--;
                    mOnDataRemovedListener.OnDataRemoved( lastDay - i - 1, 255 );
                }
            }
        }
    }

    public void setOnDataUpdatedListener( LunchMenu.OnDataUpdated listener )
    {
        mOnDataUpdatedListener = listener;
    }

    public void setOnDataRemovedListener( LunchMenu.OnDataRemoved listener )
    {
        mOnDataRemovedListener = listener;
    }

    private String getDate()
    {
        String date = "";
        Date todayDate = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat( "yyyy-MM-dd" );
        try
        {
            date = dateFormat.format( todayDate );
        } catch( Exception e )
        {
            e.printStackTrace();
        }
        return date;
    }

    public int getCount()
    {
        if( mMenu == null )
        {
            return 0;
        }
        return mCount;
    }

    public MenuList get( int position )
    {
        if( mMenu == null )
        {
            return null;
        }
        return mMenu.get( position );
    }

    private int getDayNumber()
    {
        int page = 1;
        Date todayDate = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat( "u" );
        try
        {
            String day = dateFormat.format( todayDate );
            page = Integer.parseInt( day );
        } catch( Exception e )
        {
            e.printStackTrace();
        }
        return page - 1;
    }

    public String getDay( int day )
    {
        day += getDayNumber();
        switch( day )
        {
            case 0:
            {
                return "Monday";
            }
            case 1:
            {
                return "Tuesday";
            }
            case 2:
            {
                return "Wednesday";
            }
            case 3:
            {
                return "Thursday";
            }
            case 4:
            {
                return "Friday";
            }
            case 5:
            {
                return "Saturday";
            }
            case 6:
            {
                return "Sunday";
            }
            default:
            {
                return "";
            }
        }
    }

    private void createNotAvailable()
    {
        MenuList list = mMenu.size() != 0 ? mMenu.get( 0 ) : new MenuList( getDay( 0 ) );
        MenuItem item = new MenuItem( 0, 0 );
        item.setContent( "No menu available." );
        list.add( item );
        list.incCount();
        if( mMenu.size() == 0 )
        {
            mMenu.add( list );
        }
        else
        {
            list.setDay( getDay( 0 ) );
        }
        mCount = 1;
        mSize = 1;
    }

    public void OnJSONDownloadCompleted( String result )
    {
        if( result != null && result != "" )
        {
            try
            {
                JSONObject reader = new JSONObject( result );
                JSONArray menusForDays = reader.getJSONArray( "MenusForDays" );
                for( int k = 0; k < menusForDays.length(); k++ )
                {
                    JSONObject firstDay = menusForDays.getJSONObject( k );
                    JSONArray setMenus = firstDay.getJSONArray( "SetMenus" );
                    if( setMenus.length() > 0 )
                    {
                        mSize++;
                        MenuList list = k < mMenu.size() ? mMenu.get( k ) : new MenuList( getDay( k ) );
                        for( int i = 0; i < setMenus.length(); i++ )
                        {
                            JSONObject menu = setMenus.getJSONObject( i );
                            MenuItem item = new MenuItem( k, i );
                            item.setTitle( menu.getString( "Name" ) );

                            JSONArray components = menu.getJSONArray( "Components" );
                            for( int j = 0; j < components.length(); j++ )
                            {
                                item.setContent( components.getString( j ).replaceAll( "\\(.*\\)", "" ).trim() );
                            }
                            item.setOnBitmapUpdatedListener( this );
                            list.add( item );
                        }
                        if( k >= mMenu.size() )
                        {
                            mMenu.add( list );
                        }
                        else
                        {
                            list.setDay( getDay( k ) );
                        }
                    }
                }

                if( mSize != 0 )
                {
                    mCount++;
                    mMenu.get( 0 ).get( 0 ).loadBitmap( "http://api.pixplorer.co.uk/image?amount=1&size=tb&word=" + mMenu.get( 0 ).get( 0 ).getContent( 0 ) );
                }
                else
                {
                    if( mOnDataUpdatedListener != null )
                    {
                        createNotAvailable();
                        mIsDownloading = false;
                        mOnDataUpdatedListener.OnDataUpdated( 0, 0 );
                    }
                }
            } catch( Exception e )
            {
                e.printStackTrace();
            }
        }
        else
        {
            createNotAvailable();
            mIsDownloading = false;
            mOnDataUpdatedListener.OnDataUpdated( 0, 0 );
        }
    }

    public void OnBitmapUpdated( int dayNumber, int row )
    {
        if( mOnDataUpdatedListener != null )
        {
            mMenu.get( dayNumber ).incCount();

            if( ( row + 1 ) < mMenu.get( dayNumber ).getSize() )
            {
                mMenu.get( dayNumber ).get( row + 1 ).loadBitmap( "http://api.pixplorer.co.uk/image?amount=1&size=tb&word=" + mMenu.get( dayNumber ).get( row + 1 ).getContent( 0 ) );
            }
            else if( ( dayNumber + 1 ) < mSize )
            {
                mCount++;
                mMenu.get( dayNumber + 1 ).get( 0 ).loadBitmap( "http://api.pixplorer.co.uk/image?amount=1&size=tb&word=" + mMenu.get( dayNumber + 1 ).get( 0 ).getContent( 0 ) );
            }

            mOnDataUpdatedListener.OnDataUpdated( dayNumber, row );

            if( ( dayNumber + 1 ) == mSize && ( row + 1 ) == mMenu.get( dayNumber ).getSize() )
            {
                mIsDownloading = false;
            }
        }
    }

    public interface OnDataUpdated
    {
        void OnDataUpdated( int dayNumber, int row );
    }

    public interface OnDataRemoved
    {
        void OnDataRemoved( int dayNumber, int row );
    }
}
