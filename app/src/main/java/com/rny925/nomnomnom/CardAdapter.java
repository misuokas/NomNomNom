package com.rny925.nomnomnom;

import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;

public class CardAdapter extends RecyclerView.Adapter< MyViewHolder >
{
    private MenuList mList = null;

    public CardAdapter( MenuList list )
    {
        mList = list;
    }

    public void updateItem( MenuList list )
    {
        mList = list;
    }

    @Override
    public MyViewHolder onCreateViewHolder( ViewGroup parent, int viewType )
    {
        View itemView = LayoutInflater.from( parent.getContext() ).inflate( R.layout.card, parent, false );
        return new MyViewHolder( itemView );
    }

    @Override
    public void onBindViewHolder( MyViewHolder holder, int position )
    {
        if( mList != null && mList.get( position ) != null )
        {
            holder.getLabelView().setText( mList.get( position ).getTitle() );
            String text = "";
            for( int i = 0; i < mList.get( position ).getContentCount(); i++ )
            {
                text += mList.get( position ).getContent( i ) + "\n";
            }
            holder.getTextView().setText( text );
            if( mList.get( position ).getBitmap() != null )
            {
                Bitmap bitmap = mList.get( position ).getBitmap();

                if( bitmap != null )
                {
                    holder.getImageView().setImageBitmap( bitmap );
                }

                Animation fadeIn = new AlphaAnimation( 0, 1 );
                fadeIn.setInterpolator( new AccelerateInterpolator() );
                fadeIn.setDuration( 500 );

                AnimationSet animation = new AnimationSet( false );
                animation.addAnimation( fadeIn );
                animation.setRepeatCount( 1 );

                holder.getLabelView().setAnimation( animation );
                holder.getTextView().setAnimation( animation );
                holder.getImageView().setAnimation( animation );
            }
            else
            {
                holder.getImageView().setImageResource( android.R.color.transparent );
            }
        }
    }

    @Override
    public int getItemCount()
    {
        if( mList == null )
        {
            return 0;
        }
        return mList.getCount();
    }
}
