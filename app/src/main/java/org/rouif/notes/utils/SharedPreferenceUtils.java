package org.rouif.notes.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Utility class to allow ease of access to SharedPreferences.
 */
public class SharedPreferenceUtils {

    /**
     * Class to store all the Preference Keys in.
     */
    public static class PrefKeys {
        public static final String IS_FIRST_RUN = "_is_first_run";
        public static final String IS_CONTACTS_FETCHED = "_is_contacts_fetched";

        /**
         * Boolean preference that indicates whether we installed the boostrap data or not.
         */
        public static final String PREF_DATA_BOOTSTRAP_DONE = "pref_data_bootstrap_done";

        /** Long indicating when a sync was last ATTEMPTED (not necessarily succeeded) */
        public static final String PREF_LAST_SYNC_ATTEMPTED = "pref_last_sync_attempted";

        /** Long indicating when a sync last SUCCEEDED */
        public static final String PREF_LAST_SYNC_SUCCEEDED = "pref_last_sync_succeeded";
    }

    private static final String PREFS_NAME = "_prefs";


    private static SharedPreferenceUtils sInstance;
    private SharedPreferences mPreferences;


    private SharedPreferenceUtils(Context context) {
        mPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public static SharedPreferenceUtils getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new SharedPreferenceUtils(context);
        }
        return sInstance;
    }

    public boolean isFirstRun() {
        return mPreferences.getBoolean(PrefKeys.IS_FIRST_RUN, true);
    }

    public void setFirstRun(boolean firstRun) {
        mPreferences.edit().putBoolean(PrefKeys.IS_FIRST_RUN, firstRun).apply();
    }

    public boolean isContactsFetched() {
        return mPreferences.getBoolean(PrefKeys.IS_CONTACTS_FETCHED, true);
    }

    public void setContactsFetched(boolean firstRun) {
        mPreferences.edit().putBoolean(PrefKeys.IS_CONTACTS_FETCHED, firstRun).apply();
    }

    public void markDataBootstrapDone() {
        mPreferences.edit().putBoolean(PrefKeys.PREF_DATA_BOOTSTRAP_DONE, true).apply();
    }

    public boolean isDataBootstrapDone() {
        return mPreferences.getBoolean(PrefKeys.PREF_DATA_BOOTSTRAP_DONE, false);
    }

    public long getLastSyncAttemptedTime() {
        return mPreferences.getLong(PrefKeys.PREF_LAST_SYNC_ATTEMPTED, 0L);
    }

    public void markSyncAttemptedNow() {
        mPreferences.edit().putLong(PrefKeys.PREF_LAST_SYNC_ATTEMPTED, System.currentTimeMillis()).apply();
    }

    public long getLastSyncSucceededTime() {
        return mPreferences.getLong(PrefKeys.PREF_LAST_SYNC_SUCCEEDED, 0L);
    }

    public void markSyncSucceededNow() {
        mPreferences.edit().putLong(PrefKeys.PREF_LAST_SYNC_SUCCEEDED, System.currentTimeMillis()).apply();
    }


}
