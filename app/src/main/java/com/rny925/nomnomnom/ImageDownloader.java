package com.rny925.nomnomnom;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class ImageDownloader extends AsyncTask< String, Bitmap, Bitmap >
{
    private String mUrl;
    private OnImageDownloadCompleted mOnImageDownloadCompletedListener;

    public ImageDownloader( String url )
    {
        mUrl = url;
        mOnImageDownloadCompletedListener = null;
    }

    public void setOnDownloadCompletedListener( OnImageDownloadCompleted listener )
    {
        mOnImageDownloadCompletedListener = listener;
    }

    @Override
    protected Bitmap doInBackground( String... params )
    {
        Bitmap bitmap = null;
        try
        {
            URL url = new URL( mUrl );
            HttpURLConnection connection = ( HttpURLConnection ) url.openConnection();
            connection.addRequestProperty( "Cache-Control", "max-age=0" );
            connection.setUseCaches( true );
            connection.setConnectTimeout( 60000 );
            try
            {
                BufferedInputStream bufferedInputStream = new BufferedInputStream( connection.getInputStream() );
                bitmap = BitmapFactory.decodeStream( bufferedInputStream );
                bufferedInputStream.close();
            } catch( IOException e )
            {
                e.printStackTrace();
            } finally
            {
                connection.disconnect();
            }
        } catch( Exception e )
        {
            e.printStackTrace();
        }
        return bitmap;
    }

    @Override
    protected void onPostExecute( Bitmap result )
    {
        if( mOnImageDownloadCompletedListener != null )
        {
            mOnImageDownloadCompletedListener.OnImageDownloadCompleted( result );
        }
    }

    public interface OnImageDownloadCompleted
    {
        void OnImageDownloadCompleted( Bitmap result );
    }
}
