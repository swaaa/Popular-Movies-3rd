package com.example.android.popular_movies_3rd;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;


/**
 * A placeholder fragment containing a simple view.
 */
public class DetailActivityFragment extends Fragment {

    private static final String LOG_TAG = DetailActivityFragment.class.getSimpleName();

    public DetailActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

        Intent details = getActivity().getIntent();
        Bundle extras = details.getExtras();
        if (extras != null && extras.containsKey("EXTRA_TITLE")) {

            String title = extras.getString("EXTRA_TITLE");
            String thumbnailUrlStr = extras.getString("EXTRA_THUMBNAIL_URL_STRING");
            String overview = extras.getString("EXTRA_OVERVIEW");
            String rating = extras.getString("EXTRA_RATING");
            String release = extras.getString("EXTRA_RELEASE");

            Log.v(LOG_TAG, "Title: " + title);
            Log.v(LOG_TAG, "Thumbnail Url: " + thumbnailUrlStr);

            ((TextView) rootView.findViewById(R.id.details_title))
                    .setText(title);

            try {
                ImageView thumbnail = new ImageView(getActivity());
                Picasso.with(getActivity()).load(thumbnailUrlStr).into(thumbnail);
            } catch (Exception e) {
                e.printStackTrace();
            }

            ((TextView) rootView.findViewById(R.id.details_overview))
                    .setText(overview);
            ((TextView) rootView.findViewById(R.id.details_rating))
                    .setText(rating);
            ((TextView) rootView.findViewById(R.id.details_release))
                    .setText(release);
        }
        return rootView;
    }
}
