package com.rny925.nomnomnom;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class MyViewHolder extends RecyclerView.ViewHolder
{
    private TextView mLabelView;
    private TextView mTextView;
    private ImageView mImageView;

    public MyViewHolder( View view )
    {
        super( view );
        mLabelView = ( TextView ) view.findViewById( R.id.section_label );
        mTextView = ( TextView ) view.findViewById( R.id.section_content );
        mImageView = ( ImageView ) view.findViewById( R.id.section_image );
    }

    public TextView getLabelView()
    {
        return mLabelView;
    }

    public TextView getTextView()
    {
        return mTextView;
    }

    public ImageView getImageView()
    {
        return mImageView;
    }
}

