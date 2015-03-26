package org.rouif.notes.sync;

/**
 * Created by rouifs on 28/01/2015.
 */

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;

import org.rouif.notes.utils.LogUtils;


/**
 * Handle the transfer of data between a server and an
 * app, using the Android sync adapter framework.
 */
public class SyncAdapter extends AbstractThreadedSyncAdapter {

    public static final String EXTRA_SYNC_USER_DATA_ONLY = "org.rouif.notes.EXTRA_SYNC_USER_DATA_ONLY";

    private static final String TAG = LogUtils.makeLogTag(SyncHelper.class);

    // Global variables
    // Define a variable to contain a content resolver instance
    ContentResolver mContentResolver;

    /**
     * Set up the sync adapter
     */
    public SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        /*
         * If your app uses a content resolver, get an instance of it
         * from the incoming Context
         */
        mContentResolver = context.getContentResolver();
    }



    /**
     * Set up the sync adapter. This form of the constructor maintains compatibility with Android
     * 3.0 and later platform versions
     */
    public SyncAdapter(
            Context context,
            boolean autoInitialize,
            boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);
        /*
         * If your app uses a content resolver, get an instance of it
         * from the incoming Context
         */
        mContentResolver = context.getContentResolver();

    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {

        final boolean uploadOnly = extras.getBoolean(ContentResolver.SYNC_EXTRAS_UPLOAD, false);
        final boolean manualSync = extras.getBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, false);
        final boolean initialize = extras.getBoolean(ContentResolver.SYNC_EXTRAS_INITIALIZE, false);
        final boolean userDataOnly = extras.getBoolean(EXTRA_SYNC_USER_DATA_ONLY, false);

        LogUtils.logi(TAG, "Beginning sync for account " + account.toString() + "," +  "\n" +
                " uploadOnly=" + uploadOnly + "\n" +
                " manualSync=" + manualSync + "\n" +
                " userDataOnly =" + userDataOnly + "\n" +
                " initialize=" + initialize);

        // Sync from bootstrap and remote data, as needed
        new SyncHelper(getContext()).performSync(syncResult, account, extras);
    }

}