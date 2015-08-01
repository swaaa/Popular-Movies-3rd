package com.example.android.popular_movies_3rd;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


public class ThumbnailFragment extends Fragment {

    private final String LOG_TAG = ThumbnailFragment.class.getSimpleName();
    private List<String> movieList = new ArrayList<>();
    ImageAdapter movieAdapter;

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
        // insert layout
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        // adapt fake thumbnails to grid
        // insert Picasso?
        GridView gridView = (GridView) rootView.findViewById(R.id.grid);
        movieAdapter = new ImageAdapter(getActivity(), movieList);
        gridView.setAdapter(movieAdapter);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // build Intent that starts DetailActivity
                Intent details = new Intent(getActivity(), DetailActivity.class);
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
        moviesTask.execute("popularity.desc"); // hardcoding before sorting feature
    }

    public class FetchMoviesTask extends AsyncTask<String, Void, String[]> {

        private String[] getMoviesDataFromJson(String moviesJsonStr, int numMovies)
                throws JSONException {

            // These are the names of the JSON objects that need to be extracted.
            final String TMDB_RESULTS = "results";
            final String TMDB_TITLE = "title";
            final String TMDB_POSTER_PATH = "poster_path";
            final String TMDB_OVERVIEW = "overview";
            final String TMDB_VOTE_AVERAGE = "vote_average";
            final String TMDB_RELEASE_DATE = "release_date";

            JSONObject moviesJson = new JSONObject(moviesJsonStr);
            JSONArray moviesArray = moviesJson.getJSONArray(TMDB_RESULTS);

            String[] resultStrs = new String[numMovies];
            for(int i = 0; i < numMovies; i++) {

                String title;
                String poster_path;
                String overview;
                String vote_average;
                String release_date;

                // Get the JSON object representing the movie
                JSONObject movieObject = moviesArray.getJSONObject(i);

                // fetch single Json elements
                title = movieObject.getString(TMDB_TITLE);
                poster_path = movieObject.getString(TMDB_POSTER_PATH);
                overview = movieObject.getString(TMDB_OVERVIEW);
                vote_average = movieObject.getString(TMDB_VOTE_AVERAGE);
                release_date = movieObject.getString(TMDB_RELEASE_DATE);

                // fetch urls for thumbnails, which will be loaded in grid
                getThumbnailUrl(poster_path, i);

                resultStrs[i] = "title: " + title + "\n"
                        + "poster_path: " + poster_path + "\n"
                        + "overview: " + overview + "\n"
                        + "vote_average: " + vote_average + "\n"
                        + "release_date: " + release_date + "\n";
            }
            for (String s : resultStrs) {
                Log.v(LOG_TAG, "Forecast entry: " + s);
            }
            return resultStrs;
        }

        private void getThumbnailUrl(String poster_path, int index) {
            try {
                URL url = new URL("http://image.tmdb.org/t/p/w185" + poster_path);
                Log.v(LOG_TAG, "Error with URL: " + url);
                movieList.add(index, url.toString());
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }

        @Override
        protected String[] doInBackground(String... params) {

            // no point in refresh with no sort order
            if (params.length == 0) {
                return null;
            }

            /**
             * sort grid by fetching data
             */
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
                Log.v(LOG_TAG, "Built URL: " + builtUri.toString());

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
                // hardcode maximum of movies in grid
                return getMoviesDataFromJson(moviesJsonStr, 10);
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
