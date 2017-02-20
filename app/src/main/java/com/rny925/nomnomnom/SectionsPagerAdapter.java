package com.rny925.nomnomnom;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.ViewGroup;

public class SectionsPagerAdapter extends FragmentPagerAdapter
{
    private LunchMenu mMenu;
    private PlaceholderFragment[] mFragment;
    private OnPageLoaded mOnPageLoadedListener;

    public SectionsPagerAdapter( FragmentManager fm )
    {
        super( fm );
        mMenu = null;
        mFragment = new PlaceholderFragment[5];
        for( int i = 0; i < 5; i++ )
        {
            mFragment[i] = null;
        }
        mOnPageLoadedListener = null;
    }

    public void setOnPageLoadedListener( OnPageLoaded listener )
    {
        mOnPageLoadedListener = listener;
    }

    public void updateItem( int dayNumber, int row, LunchMenu menu )
    {
        mMenu = menu;
        if( dayNumber != 255 && mFragment[dayNumber] != null )
        {
            mFragment[dayNumber].updateItem( row, menu.get( dayNumber ) );
        }
    }

    public void removeItem( int dayNumber, int row, LunchMenu menu )
    {
        mMenu = menu;
        if( mFragment[dayNumber] != null )
        {
            mFragment[dayNumber].removeItem( row, menu.get( dayNumber ) );
        }
    }

    @Override
    public Object instantiateItem( ViewGroup container, int position )
    {
        PlaceholderFragment fragment = ( PlaceholderFragment ) super.instantiateItem( container, position );
        mFragment[position] = fragment;
        return fragment;
    }

    @Override
    public Fragment getItem( int position )
    {
        if( mMenu == null || position >= mMenu.getCount() )
        {
            return PlaceholderFragment.newInstance( null );
        }

        return PlaceholderFragment.newInstance( mMenu.get( position ) );
    }

    @Override
    public void finishUpdate( ViewGroup viewGroup )
    {
        super.finishUpdate( viewGroup );
        if( mOnPageLoadedListener != null )
        {
            mOnPageLoadedListener.OnPageLoaded();
        }
    }

    @Override
    public int getCount()
    {
        if( mMenu == null )
        {
            return 0;
        }
        return mMenu.getCount() < 5 ? mMenu.getCount() : 5;
    }

    @Override
    public CharSequence getPageTitle( int position )
    {
        if( mMenu == null || position >= mMenu.getCount() || mMenu.get( position ) == null )
        {
            return "";
        }

        return mMenu.get( position ).getDay();
    }

    public interface OnPageLoaded
    {
        void OnPageLoaded();
    }
}
