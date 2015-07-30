package com.example.android.popular_movies_3rd;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;


/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {

    public MainActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        /**
         * create Instance of ImageAdapter class
         */
        ImageAdapter imageAdapter = new ImageAdapter(getActivity());

        // insert layout
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        // adapt thumbs to grid
        GridView gridView = (GridView) rootView.findViewById(R.id.grid);
        gridView.setAdapter(imageAdapter);

        return inflater.inflate(R.layout.fragment_main, container, false);
    }
}
