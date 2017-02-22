package com.rny925.nomnomnom;

import android.support.v4.view.ViewPager;

public class PageLoadedListener implements ViewPager.OnPageChangeListener
{
    private OnPageChange mOnPageChangeListener;

    public void setOnPageChangeListener( OnPageChange listener )
    {
        mOnPageChangeListener = listener;
    }

    @Override
    public void onPageScrolled( int position, float positionOffset, int positionOffsetPixels )
    {
    }

    @Override
    public void onPageScrollStateChanged( int state )
    {
        if( state == ViewPager.SCROLL_STATE_IDLE && mOnPageChangeListener != null )
        {
            mOnPageChangeListener.OnPageScrollStateChanged();
        }
    }

    @Override
    public void onPageSelected( int position )
    {
        if( mOnPageChangeListener != null )
        {
            mOnPageChangeListener.OnPageSelected();
        }
    }

    public interface OnPageChange
    {
        void OnPageSelected();

        void OnPageScrollStateChanged();
    }
}
