package com.ramson.appmachines.silverscreenratings;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MoviesListActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    public static final String TAG = "MoviesListActivity";

    private int pageNo;
    private ListView list_movies;
    private List<Movie> movies = new ArrayList<>();
    private MoviesAdapter moviesAdapter;
    private TextView txt_page;
    private Button btn_prev_page;
    private Button btn_next_page;
    private String category;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // initialize all view items in Activity
        initializeViews();

        // fetch data from API and set to respective views
        setMovieDataFromApi();



    }


    private void initializeViews() {
//        force set to Portrait mode
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

//        initialize movies ListView
        list_movies = (ListView) findViewById(R.id.list_movies);

//         initialize a custom adapter to add movies to the list
        moviesAdapter = new MoviesAdapter(MoviesListActivity.this, movies);

//        initialize pagination views (at the bottom): buttons and page_no
        btn_prev_page = (Button) findViewById(R.id.btn_prev_page);
        txt_page = (TextView) findViewById(R.id.txt_page);
        btn_next_page = (Button) findViewById(R.id.btn_next_page);
    }


    private void setMovieDataFromApi() {
//        check internet connectivity
        ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            Log.d(TAG, "networkInfo is Connected");
            Toast.makeText(this, "Loading Movies...", Toast.LENGTH_SHORT).show();
//         if connected: load data in Background(AsyncTask)
            new MoviesFromAPI().execute(getMoviesListUrl()); // set movies from API by executing Async class
        } else {
            Log.e(TAG, "networkInfo Not Connected()");
            Toast.makeText(this, "Please Check Internet Connection", Toast.LENGTH_SHORT).show();
        }
    }


    public String getMoviesListUrl() {
//        url = combination of strings
//        Ex: url =
                // "http://api.themoviedb.org/3/movie"   // base_url
                // + "/now_playing"                      // category
                // + "?api_key=b3e0f31d9441c58e9f2e440f0b94d04a" // api_key
                // + "&page=" // page
                // + "1"      // pageNo
        String base_url = getResources().getString(R.string.base_api);
        if (category == null){
            category = getResources().getString(R.string.category_now);
            // set ActionBar Title
            getSupportActionBar().setTitle(getResources().getString(R.string.now_playing));
        }
        String api_key = getResources().getString(R.string.api_key);
        String page = getResources().getString(R.string.page);
        if (pageNo < 1) pageNo = 1;

        // url = "http://api.themoviedb.org/3/movie/now_playing?api_key=b3e0f31d9441c58e9f2e440f0b94d04a&page=1"
        String movies_list_url = base_url + category + api_key + page + pageNo;
        Log.d(TAG, "url=" + movies_list_url);
        return movies_list_url;
    }


    // loading data from internet should be a background task, else app may throw ANR
    private class MoviesFromAPI extends AsyncTask<String, Void, String> implements View.OnClickListener {

        @Override
        protected String doInBackground(String... params) {

            String apiContentAsJason = "";
            try {

                // HttpURLConnection setup
                URL url = new URL(params[0]);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("GET");
                httpURLConnection.connect();

                // getting data
                InputStream inputStream = httpURLConnection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuilder.append(line);
                }

                apiContentAsJason = stringBuilder.toString();

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return apiContentAsJason;
        }

        @Override
        protected void onPostExecute(String apiContentAsJason) {
            super.onPostExecute(apiContentAsJason);

            JSONObject jsonObject;
            int total_pages = 0;

            movies.clear();

            try {
                jsonObject = new JSONObject(apiContentAsJason);
                pageNo = jsonObject.getInt("page");
                total_pages = jsonObject.getInt("total_pages");

                JSONArray resultsArray = jsonObject.getJSONArray("results");

                for (int i = 0; i < resultsArray.length(); i++) {
                    JSONObject properties = resultsArray.getJSONObject(i);

                    // initialize a Movie object and set all Movie variable
                    Movie movie = new Movie();
                    movie.setId(properties.getString("id"));
                    movie.setImageUrl(getResources().getString(R.string.base_img_url) + properties.getString("backdrop_path"));
                    movie.setName(properties.getString("title"));
                    String genre = "";
                    JSONArray genreArray = properties.getJSONArray("genre_ids");
                    for (int j = 0; j < genreArray.length(); j++) {
                        genre += ((j == 0 ? "" : ", ") + getString(getResources().getIdentifier("genre_id_" + genreArray.getString(j), "string", getPackageName())));
                    }
                    movie.setGenre(genre);
                    movie.setStars(properties.getString("vote_average"));
                    movie.setReleaseDate(properties.getString("release_date"));

                    // add this Movie object to our Movies List
                    movies.add(movie);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

            // put each Movie views in the movies list with Adapter
            list_movies.setAdapter(moviesAdapter);

            // for onClick of each movie item register a Listener
            list_movies.setOnItemClickListener(MoviesListActivity.this);


            // set up pagination views
            if (pageNo != 0) txt_page.setText(getString(R.string.page_no) + pageNo + "/" + total_pages);

            if (pageNo == 1) btn_prev_page.setVisibility(View.GONE);
            else btn_prev_page.setVisibility(View.VISIBLE);

            if (pageNo == total_pages) btn_next_page.setVisibility(View.GONE);
            else btn_next_page.setVisibility(View.VISIBLE);

            // register onClick of pagination buttons
            btn_prev_page.setOnClickListener(this);
            btn_next_page.setOnClickListener(this);

        }

        // onClick of pagination buttons, should go to respective page
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.btn_prev_page:
                    btn_prev_page.setVisibility(View.GONE);
                    MoviesListActivity.this.pageNo--;
                    setMovieDataFromApi();
                    break;

                case R.id.btn_next_page:
                    btn_next_page.setVisibility(View.GONE);
                    MoviesListActivity.this.pageNo++;
                    setMovieDataFromApi();
                    break;

                default:
                    break;
            }

        }
    }

    // onClick of a movie item should open respective Movie page
    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        Intent intent = new Intent(this, MovieActivity.class);
        intent.putExtra("id", movies.get(i).getId());
        startActivity(intent);
    }


    // setup OptionsMenu to select required category: now_playing, upcoming or popular
    // refresh is required in case of internet fluctuations
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.options, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.refresh:
                break;
            case R.id.now_playing:
                category = getResources().getString(R.string.category_now);
                pageNo = 1;
                break;
            case R.id.upcoming:
                category = getResources().getString(R.string.category_upcoming);
                pageNo = 1;
                break;
            case R.id.popular:
                category = getResources().getString(R.string.category_popular);
                pageNo = 1;
                break;
            default:
                break;
        }
        setMovieDataFromApi();
        if (item.getItemId() != R.id.refresh) getSupportActionBar().setTitle(item.getTitle());
        return super.onOptionsItemSelected(item);
    }
}
