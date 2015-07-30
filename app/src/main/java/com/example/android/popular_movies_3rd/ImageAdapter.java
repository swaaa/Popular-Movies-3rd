package com.example.android.popular_movies_3rd;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

/**
 * Created by Mike on 30.07.2015.
 */
public class ImageAdapter extends BaseAdapter {

    private Context mContext;

    Integer[] mThumbnailIds = {
            R.mipmap.ic_launcher, R.mipmap.ic_launcher,
            R.mipmap.ic_launcher, R.mipmap.ic_launcher,
            R.mipmap.ic_launcher, R.mipmap.ic_launcher,
            R.mipmap.ic_launcher, R.mipmap.ic_launcher,
    };

    public ImageAdapter(Context context){
        mContext = context;
    }

    @Override
    public int getCount() {
        return mThumbnailIds.length;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView thumbnail;
        if (convertView == null) {
            // not recycled? create a new one with properties
            thumbnail = new ImageView(mContext);
            thumbnail.setLayoutParams(new GridView.LayoutParams(85, 85));
            thumbnail.setScaleType(ImageView.ScaleType.CENTER_CROP);
            thumbnail.setPadding(8, 8, 8, 8);
        }
        else {
            // is recycled? use it
            thumbnail = (ImageView) convertView;
        }

        thumbnail.setImageResource(mThumbnailIds[position]);
        return thumbnail;
    }
}
