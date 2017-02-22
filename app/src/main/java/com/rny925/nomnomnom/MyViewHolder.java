package com.rny925.nomnomnom;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class MyViewHolder extends RecyclerView.ViewHolder
{
    private TextView mLabelView;
    private TextView mTextView;
    private ImageView mImageView;
    private MenuItem mItem;

    public MyViewHolder( View view )
    {
        super( view );
        mLabelView = ( TextView ) view.findViewById( R.id.section_label );
        mTextView = ( TextView ) view.findViewById( R.id.section_content );
        mImageView = ( ImageView ) view.findViewById( R.id.section_image );
        mItem = null;

        view.setOnClickListener( new View.OnClickListener()
        {
            @Override
            public void onClick( View v )
            {
                if( mItem != null )
                {
                    String share = mItem.getTitle().equals( "" ) ? "Galaksi: " : "Smarthouse: " + mItem.getTitle();
                    for( int i = 0; i < mItem.getContentCount(); i++ )
                    {
                        share += " " + mItem.getContent( i );
                    }


                    PackageManager pm = v.getContext().getPackageManager();
                    try
                    {

                        final View view = v;
                        final Intent waIntent = new Intent( Intent.ACTION_SEND );

                        waIntent.setType( "text/plain" );
                        PackageInfo info = pm.getPackageInfo( "com.whatsapp", PackageManager.GET_META_DATA );
                        waIntent.setPackage( "com.whatsapp" );
                        waIntent.putExtra( Intent.EXTRA_TEXT, share );

                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder( view.getContext() );
                        alertDialogBuilder
                                .setIcon(R.drawable.whatsapp)
                                .setTitle( "Share to WhatsApp?" )
                                .setCancelable( true )
                                .setPositiveButton( "Yes", new DialogInterface.OnClickListener()
                                {
                                    public void onClick( DialogInterface dialog, int id )
                                    {
                                        view.getContext().startActivity( Intent.createChooser( waIntent, "Share with" ) );
                                    }
                                } )
                                .setNegativeButton( "No",
                                        new DialogInterface.OnClickListener()
                                        {
                                            public void onClick( DialogInterface dialog, int id )
                                            {
                                                dialog.cancel();

                                            }
                                        } );

                        // create alert dialog
                        AlertDialog alertDialog = alertDialogBuilder.create();

                        alertDialog.show();


                    } catch( PackageManager.NameNotFoundException e )
                    {
                    } catch( Exception e )
                    {
                        e.printStackTrace();
                    }
                }
            }
        } );
    }

    public void setPosition( MenuItem menu )
    {
        mItem = menu;
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

