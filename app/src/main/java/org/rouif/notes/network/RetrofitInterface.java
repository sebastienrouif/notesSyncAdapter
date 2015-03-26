package org.rouif.notes.network;


import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.Query;

/**
 * Created by palmierif on 09/01/2015.
 */
public interface RetrofitInterface {

    @GET("/example1")
    Object getObject(@Query("whatever") String whatever);

    @GET("/example2")
    void getObject(@Query("whatever") String whatever, Callback<Object> cb);

    // import rxJava if you want to use it
//    @GET("/example3")
//    Observable<Object> getObject(@Query("whatever") String whatever);

}
