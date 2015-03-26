package org.rouif.notes;

import android.app.Application;

import com.squareup.okhttp.OkHttpClient;

import org.rouif.notes.network.RetrofitInterface;

import java.util.concurrent.TimeUnit;

import retrofit.RestAdapter;
import retrofit.client.OkClient;

/**
 * Add in cross application code here. Could provide access to API etc.
 */
public class ExampleApplication extends Application {

    private static ExampleApplication sApplication;
    private RetrofitInterface mWebApi;

    @Override
    public void onCreate() {
        super.onCreate();
        sApplication = this;
        mWebApi = buildAdapter().create(RetrofitInterface.class);
    }

    private RestAdapter buildAdapter() {
        OkHttpClient okHttpClient = new OkHttpClient();
        int readTimeout = getResources().getInteger(R.integer.read_timeout);
        int connectTimeout = getResources().getInteger(R.integer.connection_timeout);

        okHttpClient.setReadTimeout(readTimeout, TimeUnit.MILLISECONDS);
        okHttpClient.setConnectTimeout(connectTimeout, TimeUnit.MILLISECONDS);
        OkClient client = new OkClient(okHttpClient);

        return new RestAdapter.Builder().setEndpoint(getString(R.string.base_url))
                .setLogLevel(RestAdapter.LogLevel.FULL).setClient(client).build();
    }

    private static ExampleApplication getInstance() {
        return sApplication;
    }

    private RetrofitInterface getWebApi() {
        return mWebApi;
    }

}
