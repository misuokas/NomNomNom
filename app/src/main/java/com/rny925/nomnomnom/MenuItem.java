package com.rny925.nomnomnom;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;

public class MenuItem implements JSONDownloader.OnJSONDownloadCompleted, ImageDownloader.OnImageDownloadCompleted, Serializable
{
    private String mTitle;
    transient private Bitmap mBitmap;
    private ArrayList< String > mContent;
    private int mDayNumber;
    private int mRow;
    transient private OnBitmapUpdated mOnBitmapUpdatedListener;

    MenuItem( int dayNumber, int row )
    {
        mTitle = "";
        mBitmap = null;
        mContent = new ArrayList<>();
        mDayNumber = dayNumber;
        mRow = row;
        mOnBitmapUpdatedListener = null;
    }

    private void writeObject( ObjectOutputStream oos ) throws IOException
    {
        oos.defaultWriteObject();
        if( mBitmap != null )
        {
            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
            boolean success = mBitmap.compress( Bitmap.CompressFormat.JPEG, 75, byteStream );
            if( success )
            {
                oos.writeObject( byteStream.toByteArray() );
            }
        }
    }

    private void readObject( ObjectInputStream ois ) throws IOException, ClassNotFoundException
    {
        ois.defaultReadObject();
        byte[] image = ( byte[] ) ois.readObject();
        if( image != null && image.length > 0 )
        {
            mBitmap = BitmapFactory.decodeByteArray( image, 0, image.length );
        }
    }

    public void clear()
    {
        mTitle = "";
        mDayNumber = 0;
        mRow = 0;
        mContent.clear();
        mBitmap = null;
    }

    public void setOnBitmapUpdatedListener( MenuItem.OnBitmapUpdated listener )
    {
        mOnBitmapUpdatedListener = listener;
    }

    public void setContent( String content )
    {
        if( mContent != null )
        {
            mContent.add( content );
        }
    }

    public String getTitle()
    {
        return mTitle;
    }

    public void setTitle( String title )
    {
        mTitle = title;
    }

    public Bitmap getBitmap()
    {
        return mBitmap;
    }

    public int getContentCount()
    {
        if( mContent == null )
        {
            return 0;
        }
        return mContent.size();
    }

    public String getContent( int position )
    {
        if( mContent == null )
        {
            return "";
        }
        return mContent.get( position );
    }

    public void loadBitmap( String url )
    {
        JSONDownloader downloader = new JSONDownloader( url );
        downloader.setOnDownloadCompletedListener( this );
        downloader.execute();
    }

    public void OnJSONDownloadCompleted( String result )
    {
        try
        {
            JSONObject reader = new JSONObject( result );
            JSONArray images = reader.getJSONArray( "images" );
            if( images.length() > 0 )
            {
                JSONObject imageUrl = images.getJSONObject( 0 );

                ImageDownloader downloader = new ImageDownloader( imageUrl.getString( "imageurl" ) );
                downloader.setOnDownloadCompletedListener( this );
                downloader.execute();
            }
            else
            {
                if( mOnBitmapUpdatedListener != null )
                {
                    mOnBitmapUpdatedListener.OnBitmapUpdated( mDayNumber, mRow );
                }
            }
        } catch( Exception e )
        {
            if( mOnBitmapUpdatedListener != null )
            {
                mOnBitmapUpdatedListener.OnBitmapUpdated( mDayNumber, mRow );
            }
            e.printStackTrace();
        }
    }

    public void OnImageDownloadCompleted( Bitmap result )
    {
        mBitmap = result;
        if( mBitmap != null )
        {
            if( mBitmap.getWidth() >= mBitmap.getHeight() )
            {
                mBitmap = Bitmap.createBitmap(
                        mBitmap,
                        mBitmap.getWidth() / 2 - mBitmap.getHeight() / 2,
                        0,
                        mBitmap.getHeight(),
                        mBitmap.getHeight()
                );
            }
            else
            {
                mBitmap = Bitmap.createBitmap(
                        mBitmap,
                        0,
                        mBitmap.getHeight() / 2 - mBitmap.getWidth() / 2,
                        mBitmap.getWidth(),
                        mBitmap.getWidth()
                );
            }
            mBitmap = mBitmap.createScaledBitmap( mBitmap, 128, 128, true );
        }

        if( mOnBitmapUpdatedListener != null )
        {
            mOnBitmapUpdatedListener.OnBitmapUpdated( mDayNumber, mRow );
        }
    }

    public interface OnBitmapUpdated
    {
        void OnBitmapUpdated( int dayNumber, int row );
    }
}