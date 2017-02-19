package com.rny925.nomnomnom;

import android.os.AsyncTask;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class JSONDownloader extends AsyncTask< String, String, String >
{
    private String mUrl;
    private OnJSONDownloadCompleted mOnJSONDownloadCompletedListener;

    public JSONDownloader( String url )
    {
        mUrl = url;
        mOnJSONDownloadCompletedListener = null;
    }

    public void setOnDownloadCompletedListener( OnJSONDownloadCompleted listener )
    {
        mOnJSONDownloadCompletedListener = listener;
    }

    @Override
    protected String doInBackground( String... params )
    {
        String jsonResult = "";
        try
        {
            URL url = new URL( mUrl );
            HttpURLConnection connection = ( HttpURLConnection ) url.openConnection();
            connection.addRequestProperty( "Cache-Control", "max-age=0" );
            connection.setUseCaches( true );
            connection.setConnectTimeout( 60000 );
            try
            {
                InputStream cached = new BufferedInputStream( connection.getInputStream() );
                BufferedReader bufferedReader = new BufferedReader( new InputStreamReader( cached ) );
                String str;
                while( ( str = bufferedReader.readLine() ) != null )
                {
                    jsonResult += str;
                }
                bufferedReader.close();
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
        return jsonResult;
    }

    @Override
    protected void onPostExecute( String result )
    {
        if( mOnJSONDownloadCompletedListener != null )
        {
            mOnJSONDownloadCompletedListener.OnJSONDownloadCompleted( result );
        }
    }

    public interface OnJSONDownloadCompleted
    {
        void OnJSONDownloadCompleted( String result );
    }
}
