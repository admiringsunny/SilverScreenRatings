package com.ramson.appmachines.silverscreenratings.api;

import com.ramson.appmachines.silverscreenratings.models.MovieResponse;
import com.ramson.appmachines.silverscreenratings.models.MoviesResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Url;

public interface MovieAPIs {

    // Movies List Page
    // moviesListUrl = base + category + api_key + &page= + page_no
    // Ex: moviesListUrl = "http://api.themoviedb.org/3/movie/" + "now_playing" + "?api_key=API_KEY" + "&page=" + "1"
    @GET
    Call<MoviesResponse> getMoviesList(@Url String url);

    @GET //(Constants.BASE_API + {movieId} + Constants.category_upcoming + "?api_key=" + API_KEY + "&page=" + "1")
    Call<MovieResponse> getMovie(@Url String url);

}
