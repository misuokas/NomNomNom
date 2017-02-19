package com.rny925.nomnomnom;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class PlaceholderFragment extends Fragment
{
    private RecyclerView mRecyclerView;
    private CardAdapter mAdapter;

    public static PlaceholderFragment newInstance( MenuList list )
    {
        PlaceholderFragment fragment = new PlaceholderFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable( "data", list );
        fragment.setArguments( bundle );
        return fragment;
    }

    @Override
    public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState )
    {
        mRecyclerView = ( RecyclerView ) inflater.inflate( R.layout.fragment_main, container, false );
        mRecyclerView.setLayoutManager( new LinearLayoutManager( getActivity() ) );
        mAdapter = new CardAdapter( ( MenuList ) getArguments().getSerializable( "data" ) );
        mRecyclerView.setAdapter( mAdapter );
        return mRecyclerView;
    }

    public void updateItem( int row, MenuList list )
    {
        mAdapter.updateItem( list );
        mAdapter.notifyItemInserted( row );
    }

    public void removeItem( int row, MenuList list )
    {
        mAdapter.updateItem( list );
        if( row == 255 )
        {
            mAdapter.notifyDataSetChanged();
        }
        else
        {
            mAdapter.notifyItemInserted( row );
        }
    }
}
