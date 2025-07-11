package com.ramson.appmachines.silverscreenratings.activities;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.ramson.appmachines.silverscreenratings.R;
import com.ramson.appmachines.silverscreenratings.util.Constants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class TrailerActivity extends AppCompatActivity {

    private static final String TAG = "TrailerActivity";
    private String video_url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trailer);

        //** get intent: video_url
        video_url = getIntent().getStringExtra("video_url");
        openTrailer();
    }

    private void openTrailer() {
        super.onResume();
//        check internet connectivity
        ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            Log.d(TAG, "internet is connected");
            Toast.makeText(this, "Loading Movie Details...", Toast.LENGTH_SHORT).show();
//         if connected: load data in Background/AsyncTask
            new TrailerFromApi().execute(video_url);
        } else {
            Log.e(TAG, "internet not connected()");
            Toast.makeText(this, "Please Check Internet Connection", Toast.LENGTH_SHORT).show();
        }
    }

    private class TrailerFromApi extends AsyncTask<String, Void, String> {
        String contentAsJason = "";

        @Override
        protected String doInBackground(String... video_api) {
            Log.d(TAG, "doInBackground");

            try {
                URL url = new URL(video_api[0]);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("GET");
                httpURLConnection.connect();

                InputStream inputStream = httpURLConnection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line);
                }

                contentAsJason = stringBuilder.toString();

            } catch (IOException e) {
                e.printStackTrace();
            }

            return contentAsJason;
        }

        @Override
        protected void onPostExecute(String contentAsJason) {
            super.onPostExecute(contentAsJason);
            Log.d(TAG, "onPostExecute");
            String video_key = null;
            try {
                JSONObject jsonObject = new JSONObject(contentAsJason);
                JSONArray resultsArray = jsonObject.getJSONArray("results");
                JSONObject object_0 = resultsArray.getJSONObject(0);
                video_key = object_0.getString("key");
                Log.d(TAG, "video_key=" + video_key);


                if (video_key != null && !video_key.equals("")) {
                    //** -> set video_url = base_youtube_url + video_key
                    //* [Eg: https://www.youtube.com/watch?v=ue80QwXMRHg]
                    String trailer_url = Constants.BASE_YOUTUBE_URL + video_key;
                    Log.d(TAG, "video_url=" + trailer_url);

                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(trailer_url));
                    startActivity(intent);

                } else {
                    Toast.makeText(getApplicationContext(), R.string.no_video, Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "json property for 'key' not found with video_url: " + video_url);
                }
                finish();

            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), R.string.no_video, Toast.LENGTH_SHORT).show();
                Log.e(TAG, "json data not available with video_url: " + video_url  );
                finish();
            }

        }
    }

}
