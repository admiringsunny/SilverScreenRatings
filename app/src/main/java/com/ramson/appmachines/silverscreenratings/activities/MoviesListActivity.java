package com.ramson.appmachines.silverscreenratings.activities;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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

import androidx.appcompat.app.AppCompatActivity;

import com.ramson.appmachines.silverscreenratings.BuildConfig;
import com.ramson.appmachines.silverscreenratings.adapters.MoviesListAdapter;
import com.ramson.appmachines.silverscreenratings.R;
import com.ramson.appmachines.silverscreenratings.api.MovieAPIs;
import com.ramson.appmachines.silverscreenratings.api.MoviesBase;
import com.ramson.appmachines.silverscreenratings.models.Movie;
import com.ramson.appmachines.silverscreenratings.models.MoviesResponse;
import com.ramson.appmachines.silverscreenratings.models.Result;
import com.ramson.appmachines.silverscreenratings.util.Constants;
import com.ramson.appmachines.silverscreenratings.util.LoadingDialog;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MoviesListActivity extends AppCompatActivity implements AdapterView.OnItemClickListener, View.OnClickListener {

    public static final String TAG = MoviesListActivity.class.getSimpleName();

    private int pageNo;
    private int total_pages;
    private ListView list_movies;
    private List<Movie> movies = new ArrayList<>();
    private MoviesListAdapter moviesAdapter;
    private TextView txt_page;
    private Button btn_prev_page;
    private Button btn_next_page;
    private String category;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movies_list);

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

// MoviesListAdapter
//         initialize a custom adapter to add movies to the list
        moviesAdapter = new MoviesListAdapter(MoviesListActivity.this, movies);

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
            showMovies();
        } else {
            Log.e(TAG, "networkInfo Not Connected()");
            Toast.makeText(this, "Please Check Internet Connection", Toast.LENGTH_LONG).show();
        }
    }

    private void showMovies() {
        Log.d(TAG, "showMovies");
        LoadingDialog.show(this, getString(R.string.loading_movies), false);
        Call<MoviesResponse> call = MoviesBase.retrofit().create(MovieAPIs.class).getMoviesList(getMoviesListUrl());
        call.enqueue(new Callback<MoviesResponse>() {
            @Override
            public void onResponse(Call<MoviesResponse> call, Response<MoviesResponse> response) {
                Log.d(TAG, "showMovies -> onResponse");
                LoadingDialog.cancel();
                if (null == response.body() || null == response.body().getResults()) {
                    Toast.makeText(MoviesListActivity.this, "No Movies", Toast.LENGTH_SHORT).show();
                    return;
                }
                movies.clear();
                List<Result> results = response.body().getResults();
                for (int i = 0; i < results.size(); i++) {
                    Result result = results.get(i);
                    pageNo = response.body().getPage();
                    total_pages = response.body().getTotalResults();

                    Movie movie = new Movie();
                    movie.setId(String.valueOf(result.getId()));
                    movie.setImageUrl(Constants.BASE_IMG_URL + result.getBackdropPath());
                    movie.setName(result.getTitle());
                    String genre = "";
                    List<Integer> genreArray = result.getGenreIds();
                    for (int j = 0; j < genreArray.size(); j++) {
                        genre += ((j == 0 ? "" : ", ") + getString(getResources().getIdentifier("genre_id_" + genreArray.get(j), "string", getPackageName())));
                    }
                    movie.setGenre(genre);
                    movie.setStars(String.valueOf(result.getVoteAverage()));
                    movie.setReleaseDate(result.getReleaseDate());

                    // add this Movie object to our Movies List
                    movies.add(movie);
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
                btn_prev_page.setOnClickListener(MoviesListActivity.this);
                btn_next_page.setOnClickListener(MoviesListActivity.this);
            }

            @Override
            public void onFailure(Call<MoviesResponse> call, Throwable t) {
                Log.e(TAG, "showMovies, onFailure: " + t.getMessage(), t);
                LoadingDialog.cancel();
            }
        });
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

    public String getMoviesListUrl() {
        if (category == null){
            category = Constants.CATEGORY_NOW;
            // set ActionBar Title
            getSupportActionBar().setTitle(getResources().getString(R.string.now_playing));
        }
        if (pageNo < 1) pageNo = 1;
        String movies_list_url = Constants.BASE_API + category + Constants.API_KEY_LABEL + BuildConfig.API_KEY + Constants.PAGE_LABEL + pageNo;
        Log.d(TAG, "movies_list_url=" + Constants.BASE_API + category + Constants.API_KEY_LABEL + "API_KEY" + Constants.PAGE_LABEL + pageNo);
        return movies_list_url;
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
                category = Constants.CATEGORY_NOW;
                pageNo = 1;
                break;
            case R.id.upcoming:
                category = Constants.CATEGORY_UPCOMING;
                pageNo = 1;
                break;
            case R.id.popular:
                category = Constants.CATEGORY_POPULAR;
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
