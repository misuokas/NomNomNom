package com.rny925.nomnomnom;

import org.json.JSONObject;

import java.util.Date;

public class Weather implements JSONDownloader.OnJSONDownloadCompleted
{
    private boolean mRunning;
    private OnWeatherCompleted mOnWeatherCompletedListener;

    public Weather()
    {
        mRunning = false;
        mOnWeatherCompletedListener = null;
    }

    public void setOnWeatherCompletedListener( OnWeatherCompleted listener )
    {
        mOnWeatherCompletedListener = listener;
    }

    public void execute()
    {
        if( !mRunning )
        {
            mRunning = true;
            JSONDownloader downloader = new JSONDownloader( "http://api.openweathermap.org/data/2.5/weather?q=Oulu&units=metric", "b71c0b87ecf1857bb4e24557a255df39" );
            downloader.setOnDownloadCompletedListener( this );
            downloader.execute();
        }
    }

    public void OnJSONDownloadCompleted( String result )
    {
        if( result != null && result != "" )
        {
            try
            {
                JSONObject data = new JSONObject( result );
                if( data.getInt( "cod" ) == 200 )
                {
                    JSONObject details = data.getJSONArray( "weather" ).getJSONObject( 0 );
                    JSONObject main = data.getJSONObject( "main" );

                    int actualId = details.getInt( "id" );
                    long sunrise = data.getJSONObject( "sys" ).getLong( "sunrise" ) * 1000;
                    long sunset = data.getJSONObject( "sys" ).getLong( "sunset" ) * 1000;
                    int id = actualId / 100;

                    String weather = String.format( "%.1f", main.getDouble( "temp" ) ) + " ℃" + "&nbsp;&nbsp;&nbsp;";
                    if( actualId == 800 )
                    {
                        long currentTime = new Date().getTime();
                        if( currentTime >= sunrise && currentTime < sunset )
                        {
                            weather += "&#xf00d;";
                        }
                        else
                        {
                            weather += "&#xf02e;";
                        }
                    }
                    else
                    {
                        switch( id )
                        {
                            case 2:
                            {
                                weather += "&#xf01e;";
                                break;
                            }
                            case 3:
                            {
                                weather += "&#xf01c;";
                                break;
                            }
                            case 7:
                            {
                                weather += "&#xf014;";
                                break;
                            }
                            case 8:
                            {
                                weather += "&#xf013;";
                                break;
                            }
                            case 6:
                            {
                                weather += "&#xf01b;";
                                break;
                            }
                            case 5:
                            {
                                weather += "&#xf019;";
                                break;
                            }
                        }
                    }
                    if( mOnWeatherCompletedListener != null )
                    {
                        mOnWeatherCompletedListener.OnWeatherCompleted( weather );
                    }
                    mRunning = false;
                }
            } catch( Exception e )
            {
                e.printStackTrace();
            }
        }
    }

    public interface OnWeatherCompleted
    {
        void OnWeatherCompleted( String weather );
    }
}
