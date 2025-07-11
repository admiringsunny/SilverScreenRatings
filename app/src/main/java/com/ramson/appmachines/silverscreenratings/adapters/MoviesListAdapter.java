package com.ramson.appmachines.silverscreenratings.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.ramson.appmachines.silverscreenratings.R;
import com.ramson.appmachines.silverscreenratings.models.Movie;
import com.squareup.picasso.Picasso;

import java.util.List;


public class MoviesListAdapter extends BaseAdapter {

    private static final String TAG = "MoviesAdapter";
    private Context context;
    private List<Movie> movies;
    private LayoutInflater inflater;

    public MoviesListAdapter(Context context, List<Movie> movies) {
        this.context = context;
        this.movies = movies;
    }

    @Override
    public int getCount() {
        return movies.size();
    }

    @Override
    public Object getItem(int i) {
        return movies.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {

        if (null == view) {
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.adapter_movies_list, null);
        }

        Movie movie = movies.get(i);
        ImageView backdrop_img = (ImageView) view.findViewById(R.id.backdrop_img);
        TextView movie_name = (TextView) view.findViewById(R.id.txt_m_name);
        TextView txt_stars = (TextView) view.findViewById(R.id.txt_stars);
        TextView genres = (TextView) view.findViewById(R.id.genres);
        TextView rel_date = (TextView) view.findViewById(R.id.rel_date);

        Log.d(TAG,
                "imageUrl=" + movie.getImageUrl() + "\n"
                + "Name=" + movie.getName() + "\n"
                + "Stars=" + movie.getStars() + "\n"
                + "Genre=" + movie.getGenre() + "\n"
                + "ReleaseDate=" + movie.getReleaseDate()
        );

        Picasso.with(context).load(movie.getImageUrl()).into(backdrop_img);
        movie_name.setText(movie.getName());
        txt_stars.setText(movie.getStars());
        genres.setText(movie.getGenre());
        rel_date.setText(movie.getReleaseDate());

        return view;
    }
}
