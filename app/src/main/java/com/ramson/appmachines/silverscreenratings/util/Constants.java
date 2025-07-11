package com.ramson.appmachines.silverscreenratings.util;

public class Constants {

    // Movies List Page
    // moviesListUrl = base + category + api_key + page + page_no
    // Ex: moviesListUrl = "http://api.themoviedb.org/3/movie/" + "now_playing" + "?api_key=API_KEY" + "&page=" + "1"
    public static final String BASE_API = "http://api.themoviedb.org/3/movie/";
    public static final String CATEGORY_UPCOMING = "upcoming";
    public static final String CATEGORY_NOW = "now_playing";
    public static final String CATEGORY_POPULAR = "popular";
    public static final String API_KEY_LABEL = "?api_key=";
    public static final String PAGE_LABEL = "&page=";
    //   page_no: from api

    //    Movie Page
    //    movie_url = base + movie_id + api_key
    //    Ex: movie_url = "http://api.themoviedb.org/3/movie" + "/movie_id" + "?api_key=<API_KEY>"
    //    base: same
    //    movie_id: from api
    //    api_key: same

    //    video_url = base_api + / + movie_id + videos + api_key
    public static final String VIDEOS = "videos";

    //    IMAGE
    //    image_poster_path = base_img_url + poster_path
    public static final String BASE_IMG_URL = "https://image.tmdb.org/t/p/w500";
    //    backdrop_path: from api
    //    poster_path: from api
    public static final String POSTER_PATH = "poster_path";
    public static final String BACKDROP_PATH = "backdrop_path";

    //    trailer_url = base_youtube_url + video_key
    public static final String BASE_YOUTUBE_URL = "https://www.youtube.com/watch?v=";
    //    video_key: from api
}
