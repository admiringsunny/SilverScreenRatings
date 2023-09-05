package com.ramson.appmachines.silverscreenratings;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

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
        String movie_url = getResources().getString(R.string.base_api) + "/" + movie_id + getResources().getString(R.string.api_key);
        Log.d(TAG, "movie_url=" + movie_url);
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
            Toast.makeText(this, "Loading Movie Details...", Toast.LENGTH_SHORT).show();
//         if connected: load data in Background/AsyncTask
            new MovieDetailsFromAPI().execute(getMovieUrl());
        } else {
            Log.e(TAG, "internet not connected()");
            Toast.makeText(this, "Please Check Internet Connection", Toast.LENGTH_SHORT).show();
        }
    }

    private class MovieDetailsFromAPI extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            String apiContentAsJason = null;

            try {
                URL url = new URL(strings[0]);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("GET");
                httpURLConnection.connect();

                InputStream is = httpURLConnection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));

                StringBuilder sb = new StringBuilder();
                String line = "";
                while ((line = reader.readLine()) != null) {
                    sb.append(line + "\n");
                }
                apiContentAsJason = sb.toString();

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return (null == apiContentAsJason ? "" : apiContentAsJason);
        }

        @Override
        protected void onPostExecute(String strings) {
            super.onPostExecute(strings);

            try {

                JSONObject json = new JSONObject(strings);

                String image_poster_path = getResources().getString(R.string.base_img_url) + json.getString("poster_path");
                Log.d(TAG, "image_poster_path=" + image_poster_path);
                Picasso.with(getApplicationContext()).load(image_poster_path).into(img_poster);

                txt_m_name.setText(json.getString("title"));
                txt_date.setText(json.getString("release_date"));
                txt_user_score.setText("User Score: " + (int) (json.getDouble("vote_average") * 10) + "%");
                txt_overview_value.setText(json.getString("overview"));
                txt_lang.setText(json.getJSONArray("spoken_languages").getJSONObject(0).getString("name"));
                txt_duration.setText((json.getInt("runtime") / 60) + "hr " + (json.getInt("runtime") % 60) + "min");
                ll_trailer.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        /** video_url = base_api + / + movie_id + videos + api_key
                         * [Eg: http://api.themoviedb.org/3/movie/284053/videos?api_key=b3e0f31d9441c58e9f2e440f0b94d04a ]**/
                        String video_url = getResources().getString(R.string.base_api) + "/" + movie_id + getResources().getString(R.string.videos) + getResources().getString(R.string.api_key);
                        Log.d(TAG, "video_url=" + video_url);

                        Intent intent = new Intent(getApplicationContext(), TrailerActivity.class);
                        intent.putExtra("video_url", video_url);
                        startActivity(intent);
                    }
                });


            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }



}
