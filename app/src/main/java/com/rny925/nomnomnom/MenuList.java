package com.rny925.nomnomnom;

import java.io.Serializable;
import java.util.ArrayList;

public class MenuList implements Serializable
{
    private ArrayList< MenuItem > mMenuList;
    private String mDay;
    private int mCount;

    MenuList( String day )
    {
        mDay = day;
        mMenuList = new ArrayList<>();
        mCount = 0;
    }

    public String getDay()
    {
        return mDay;
    }

    public void setDay( String day )
    {
        mDay = day;
    }

    public void add( MenuItem item )
    {
        if( mMenuList != null )
        {
            mMenuList.add( item );
        }
    }

    public int getCount()
    {
        if( mMenuList == null )
        {
            return 0;
        }
        return mCount;
    }

    public int getSize()
    {
        if( mMenuList == null )
        {
            return 0;
        }
        return mMenuList.size();
    }

    public void incCount()
    {
        mCount++;
    }

    public void decCount()
    {
        if( mCount > 0 )
        {
            mCount--;
        }
    }

    public MenuItem get( int position )
    {
        if( mMenuList == null ||
                position >= mMenuList.size() )
        {
            return null;
        }
        return mMenuList.get( position );
    }

    public void del( int position )
    {
        if( mMenuList != null && position < mMenuList.size() )
        {
            mMenuList.get( position ).clear();
            mMenuList.remove( position );
        }
    }
}