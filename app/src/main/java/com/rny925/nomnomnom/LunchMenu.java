package com.rny925.nomnomnom;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class LunchMenu implements JSONDownloader.OnJSONDownloadCompleted, MenuItem.OnBitmapUpdated, Serializable
{
    private ArrayList< MenuList > mMenu;
    private boolean mIsDownloading;
    private int mCount;
    private int mSize;
    private int mMenuType;
    private int mId;
    transient private OnDataUpdated mOnDataUpdatedListener;
    transient private OnDataRemoved mOnDataRemovedListener;

    LunchMenu()
    {
        mMenu = new ArrayList<>();
        mOnDataUpdatedListener = null;
        mCount = 0;
        mSize = 0;
        mIsDownloading = false;
        mMenuType = 0;
        mId = 0;
    }

    public int getType()
    {
        return mMenuType;
    }

    private void download()
    {
        mId++;
        if( mMenuType == 0 )
        {
            JSONDownloader downloader = new JSONDownloader( "http://www.amica.fi/modules/json/json/Index?costNumber=3498&language=fi&firstDay=" + getDate(), "" );
            downloader.setOnDownloadCompletedListener( this );
            downloader.execute();
        }
        else
        {
            JSONDownloader downloader = new JSONDownloader( "http://www.sodexo.fi/ruokalistat/output/weekly_json/49/" + getDate2() + "/fi", "" );
            downloader.setOnDownloadCompletedListener( this );
            downloader.execute();
        }
    }

    public void execute( int menuType )
    {
        mIsDownloading = true;

        mMenuType = menuType;
        download();
    }

    public boolean isDownloading()
    {
        return mIsDownloading;
    }

    public void update( int menuType )
    {
        mMenuType = menuType;
        download();
    }

    public void remove()
    {
        mIsDownloading = true;
        mId++;

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
                if( mCount > 0 )
                {
                    mCount--;
                }
                if( mSize > 0 )
                {
                    mSize--;
                }
                mOnDataRemovedListener.OnDataRemoved( lastDay - i - 1, 255 );
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

    private String getDate2()
    {
        String date = "";
        Date todayDate = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat( "yyyy/MM/dd" );
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

    public String getDay( int day )
    {
        Calendar cal = Calendar.getInstance();
        cal.add( Calendar.DAY_OF_YEAR, day );
        Date todayDate = cal.getTime();

        SimpleDateFormat dateFormat = new SimpleDateFormat( "EEE" );
        try
        {
            return dateFormat.format( todayDate );

        } catch( Exception e )
        {
            e.printStackTrace();
        }

        return "";
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

    public int getDayNumber()
    {
        Calendar calendar = Calendar.getInstance();
        int day = calendar.get( Calendar.DAY_OF_WEEK );

        switch( day )
        {
            case Calendar.MONDAY:
            {
                return 0;
            }
            case Calendar.TUESDAY:
            {
                return 1;
            }
            case Calendar.WEDNESDAY:
            {
                return 2;
            }
            case Calendar.THURSDAY:
            {
                return 3;
            }
            case Calendar.FRIDAY:
            {
                return 4;
            }
            case Calendar.SATURDAY:
            {
                return 5;
            }
            case Calendar.SUNDAY:
            {
                return 6;
            }
            default:
            {
                return 7;
            }
        }
    }

    public void OnJSONDownloadCompleted( String result )
    {
        if( result != null && result != "" )
        {
            try
            {
                JSONObject reader = new JSONObject( result );

                if( mMenuType == 0 )
                {
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
                                item.setOnBitmapUpdatedListener( this, mId );
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
                }
                else
                {
                    String[] GALAKSI_DAYS = { "monday", "tuesday", "wednesday", "thursday", "friday" };

                    JSONObject menus = reader.getJSONObject( "menus" );

                    for( int j = 0; j < 5 - getDayNumber(); j++ )
                    {
                        JSONArray menuDay = menus.getJSONArray( GALAKSI_DAYS[j + getDayNumber()] );
                        if( menuDay != null && menuDay.length() > 0 )
                        {
                            mSize++;

                            MenuList list = j < mMenu.size() ? mMenu.get( j ) : new MenuList( getDay( j ) );

                            for( int i = 0; i < menuDay.length(); i++ )
                            {
                                JSONObject menu = menuDay.getJSONObject( i );

                                MenuItem item = new MenuItem( j, i );
                                item.setTitle( "" );
                                item.setContent( menu.getString( "title_fi" ).trim() );
                                item.setOnBitmapUpdatedListener( this, mId );
                                list.add( item );
                            }

                            if( j >= mMenu.size() )
                            {
                                mMenu.add( list );
                            }
                            else
                            {
                                list.setDay( getDay( j ) );
                            }
                        }
                    }
                }

                if( mSize != 0 )
                {
                    mCount++;
                    mMenu.get( 0 ).get( 0 ).loadBitmap( "http://api.pixplorer.co.uk/image?amount=1&size=tb&word=" + mMenu.get( 0 ).get( 0 ).getContent( 0 ).replace( " ", "+" ) );
                }
                else
                {
                    if( mOnDataUpdatedListener != null )
                    {
                        createNotAvailable();
                        mIsDownloading = false;
                        mOnDataUpdatedListener.OnDataUpdated( 0, 0, false );
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
            mOnDataUpdatedListener.OnDataUpdated( 0, 0, false );
        }
    }

    public void OnBitmapUpdated( int dayNumber, int row, int id )
    {
        if( mOnDataUpdatedListener != null && mId == id )
        {
            mMenu.get( dayNumber ).incCount();

            if( ( row + 1 ) < mMenu.get( dayNumber ).getSize() )
            {
                mMenu.get( dayNumber ).get( row + 1 ).loadBitmap( "http://api.pixplorer.co.uk/image?amount=1&size=tb&word=" + mMenu.get( dayNumber ).get( row + 1 ).getContent( 0 ).replace( " ", "+" ) );
            }
            else if( ( dayNumber + 1 ) < mSize )
            {
                mCount++;
                mMenu.get( dayNumber + 1 ).get( 0 ).loadBitmap( "http://api.pixplorer.co.uk/image?amount=1&size=tb&word=" + mMenu.get( dayNumber + 1 ).get( 0 ).getContent( 0 ).replace( " ", "+" ) );
            }

            if( ( dayNumber + 1 ) == mSize && ( row + 1 ) == mMenu.get( dayNumber ).getSize() )
            {
                mIsDownloading = false;
            }

            mOnDataUpdatedListener.OnDataUpdated( dayNumber, row, mIsDownloading );
        }
    }

    public interface OnDataUpdated
    {
        void OnDataUpdated( int dayNumber, int row, boolean isDownloading );
    }

    public interface OnDataRemoved
    {
        void OnDataRemoved( int dayNumber, int row );
    }
}
