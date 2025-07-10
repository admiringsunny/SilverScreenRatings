package com.ramson.appmachines.silverscreenratings.activities;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.ramson.appmachines.silverscreenratings.BuildConfig;
import com.ramson.appmachines.silverscreenratings.R;
import com.ramson.appmachines.silverscreenratings.api.MovieAPIs;
import com.ramson.appmachines.silverscreenratings.api.MoviesBase;
import com.ramson.appmachines.silverscreenratings.models.MovieResponse;
import com.ramson.appmachines.silverscreenratings.util.Constants;
import com.ramson.appmachines.silverscreenratings.util.LoadingDialog;
import com.squareup.picasso.Picasso;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MovieActivity extends AppCompatActivity {

    public static final String TAG = MovieActivity.class.getSimpleName();
    private ImageView img_poster;
    private TextView txt_m_name, txt_date, txt_user_score, txt_overview_value, txt_lang, txt_duration;
    private LinearLayout ll_trailer;
    private String movie_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie);

        // initialize all view items in Activity
        initializeViews();
        // fetch data from API and set to respective views
        setMovieDataFromApi();
    }

    @NonNull
    private String getMovieUrl() {
        movie_id = MovieActivity.this.getIntent().getStringExtra("id");
        String movie_url = Constants.BASE_API+ movie_id + "?api_key=" + BuildConfig.API_KEY;
        Log.d(TAG, "movie_url=" + Constants.BASE_API + movie_id + "?api_key=");
        return movie_url;
    }

    private void initializeViews() {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        img_poster = (ImageView) findViewById(R.id.img_poster);
        txt_m_name = (TextView) findViewById(R.id.txt_m_name);
        txt_date = (TextView) findViewById(R.id.txt_date);
        txt_user_score = (TextView) findViewById(R.id.txt_user_score);
        txt_overview_value = (TextView) findViewById(R.id.txt_overview_value);
        txt_lang = (TextView) findViewById(R.id.txt_lang);
        txt_duration = (TextView) findViewById(R.id.txt_duration);

        ll_trailer = (LinearLayout) findViewById(R.id.ll_trailor);
    }

    private void setMovieDataFromApi() {
        super.onResume();
//        check internet connectivity
        ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            Log.d(TAG, "internet is connected");

            showMovie();
        } else {
            Log.e(TAG, "internet not connected()");
            Toast.makeText(this, "Please Check Internet Connection", Toast.LENGTH_SHORT).show();
        }
    }

    private void showMovie() {
        Log.d(TAG, "showMovie");
        LoadingDialog.show(this, getString(R.string.loading_movie_details), false);
        Call<MovieResponse> call = MoviesBase.retrofit().create(MovieAPIs.class).getMovie(getMovieUrl());
        call.enqueue(new Callback<MovieResponse>() {
            @Override
            public void onResponse(Call<MovieResponse> call, Response<MovieResponse> response) {
                Log.d(TAG, "showMovie -> onResponse");
                LoadingDialog.cancel();
                if (null == response.body()) {
                    Toast.makeText(MovieActivity.this, "No Details", Toast.LENGTH_SHORT).show();
                    return;
                }
                MovieResponse movieResponse = response.body();
                String image_poster_path = Constants.BASE_IMG_URL + movieResponse.getPosterPath();
                Log.d(TAG, "image_poster_path = " + image_poster_path);
                Picasso.with(getApplicationContext()).load(image_poster_path).into(img_poster);

                txt_m_name.setText(movieResponse.getTitle());
                txt_date.setText(movieResponse.getReleaseDate());
                txt_user_score.setText("User Score: " + (int) (movieResponse.getVoteAverage() * 10) + "%");
                txt_overview_value.setText(movieResponse.getOverview());
                txt_lang.setText(movieResponse.getSpokenLanguages().get(0).getName());
                txt_duration.setText((movieResponse.getRuntime() / 60) + "hr " + (movieResponse.getRuntime() % 60) + "min");
                ll_trailer.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        playTrailer();
                    }
                });

            }

            @Override
            public void onFailure(Call<MovieResponse> call, Throwable t) {
                Log.e(TAG, "showMovie, onFailure: " + t.getMessage(), t);
                LoadingDialog.cancel();

            }
        });
    }

    private void playTrailer() {
        /** video_url = base_api + / + movie_id + videos + api_key
         * [Eg: http://api.themoviedb.org/3/movie/284053/videos?api_key=API_KEY ]
         * **/
        String video_url = Constants.BASE_API + movie_id + "/" + Constants.VIDEOS + "?api_key=" + BuildConfig.API_KEY;
        Log.d(TAG, "video_url=" + Constants.BASE_API + movie_id + "/" + Constants.VIDEOS + "?api_key=");

        Intent intent = new Intent(getApplicationContext(), TrailerActivity.class);
        intent.putExtra("video_url", video_url);
        startActivity(intent);
    }
}
