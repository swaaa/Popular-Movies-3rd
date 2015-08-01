package com.example.android.popular_movies_3rd;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


public class ThumbnailFragment extends Fragment {

    private final String LOG_TAG = ThumbnailFragment.class.getSimpleName();

    ImageAdapter movieAdapter;

    private List<String> thumbnailList = new ArrayList<>();
    private List<String> titleList = new ArrayList<>();
    private List<String> overviewList = new ArrayList<>();
    private List<String> ratingList = new ArrayList<>();
    private List<String> releaseList = new ArrayList<>();

    public ThumbnailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        GridView gridView = (GridView) rootView.findViewById(R.id.grid);
        movieAdapter = new ImageAdapter(getActivity(), thumbnailList);
        gridView.setAdapter(movieAdapter);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent details = new Intent(getActivity(), DetailActivity.class);

                // Adding data for Details
                Bundle extras = new Bundle();
                extras.putString("EXTRA_TITLE", titleList.get(position));
                extras.putString("EXTRA_THUMBNAIL_URL_STRING", thumbnailList.get(position));
                extras.putString("EXTRA_OVERVIEW", overviewList.get(position));
                extras.putString("EXTRA_RATING", ratingList.get(position));
                extras.putString("EXTRA_RELEASE", releaseList.get(position));
                details.putExtras(extras);

                startActivity(details);
            }
        });
        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_fragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_refresh) {
            update();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStart() {
        update();
        super.onStart();
    }

    private void update() {
        FetchMoviesTask moviesTask = new FetchMoviesTask();
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(getActivity());
        String sort_by = prefs.getString(
                getString(R.string.pref_sort_key),
                getString(R.string.pref_sort_default));
        moviesTask.execute(sort_by);
    }

    public class FetchMoviesTask extends AsyncTask<String, Void, String[]> {

        private String[] getMoviesDataFromJson(String moviesJsonStr)
                throws JSONException {

            // These are the names of the JSON objects that need to be extracted.
            final String TMDB_RESULTS = "results";
            final String TMDB_TITLE = "title";
            final String TMDB_POSTER_PATH = "poster_path";
            final String TMDB_OVERVIEW = "overview";
            final String TMDB_VOTE_AVERAGE = "vote_average";
            final String TMDB_RELEASE_DATE = "release_date";

            final int moviesPerPage = 20;

            JSONObject moviesJson = new JSONObject(moviesJsonStr);
            JSONArray moviesArray = moviesJson.getJSONArray(TMDB_RESULTS);

            String[] resultStrs = new String[moviesPerPage];

                for (int i = 0; i < moviesPerPage; i++) { // Each 20 Movies

                    String title;
                    String poster_path;
                    String overview;
                    String vote_average;
                    String release_date;

                    // Get the JSON object representing the movie
                    JSONObject movieObject = moviesArray.getJSONObject(i);

                    // Fetch single Json elements
                    title = movieObject.getString(TMDB_TITLE);
                    poster_path = movieObject.getString(TMDB_POSTER_PATH);
                    overview = movieObject.getString(TMDB_OVERVIEW);
                    vote_average = movieObject.getString(TMDB_VOTE_AVERAGE);
                    release_date = movieObject.getString(TMDB_RELEASE_DATE);

                    // Free lists from old data
                    if (i == 0) {
                        titleList.clear();
                        thumbnailList.clear();
                        overviewList.clear();
                        ratingList.clear();
                        releaseList.clear();
                    }
                    // Store relevant Data for thumbnails and DetailActivity
                    titleList.add(title);
                    thumbnailList.add("http://image.tmdb.org/t/p/" + "w185" + poster_path);
                    overviewList.add(overview);
                    ratingList.add(vote_average);
                    releaseList.add(release_date);

                    // For what should I use that?
                    // Thumbnails are already stored in list for Details
                    // resultStrs[i] = null;
                }

            for (String s : resultStrs) {
                Log.v(LOG_TAG, "Forecast entry: " + s);
            }
            return resultStrs;
        }

        @Override
        protected String[] doInBackground(String... params) {
            if (params.length == 0) {
                return null;
            }

            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String moviesJsonStr = null;
            // Insert before testing
            String api_key = "insertapikey";

            try {
                // Construct the Uri for the themoviedb query
                final String MOVIES_BASE_URL = "http://api.themoviedb.org/3/discover/movie?";
                final String SORT_PARAM = "sort_by";
                final String API_KEY_PARAM = "api_key";

                Uri builtUri = Uri.parse(MOVIES_BASE_URL).buildUpon()
                        .appendQueryParameter(SORT_PARAM, params[0])
                        .appendQueryParameter(API_KEY_PARAM, api_key)
                        .build();

                URL url = new URL(builtUri.toString());

                // Create the request to themoviedb, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                try {
                    urlConnection.connect();
                } catch (Exception e) {
                    Log.e(LOG_TAG, "URL Error: " + e);
                }

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                moviesJsonStr = buffer.toString();
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                // If the code didn't successfully get the movie list, there's no point in attempting
                // to parse it.
                return null;
            } finally{
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }
            try {
                // Hardcoded number of thumbnails in grid, 20 per page[]
                // Solution now: show only Top 100
                return getMoviesDataFromJson(moviesJsonStr);
            } catch (Exception e) {
                Log.e(LOG_TAG, "Couldn't return movie data from " + moviesJsonStr);
            }
            return null;
        }

        @Override
        protected void onPostExecute(String[] strings) {
            movieAdapter.notifyDataSetChanged();
        }
    }
}