package org.rouif.notes.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;

import org.rouif.notes.R;
import org.rouif.notes.provider.NotesContentProvider;
import org.rouif.notes.utils.AccountUtils;
import org.rouif.notes.utils.LogUtils;

/**
 * Static helper methods for working with the sync framework.
 */
public class SyncUtils {

    private static final String TAG = LogUtils.makeLogTag(SyncUtils.class);
    private static final long SYNC_FREQUENCY = 60 * 60;  // in seconds
    private static final String CONTENT_AUTHORITY = NotesContentProvider.AUTHORITY;
    private static final String PREF_SETUP_COMPLETE = "setup_complete";
    public static final String ACCOUNT_NAME = "sync";
    // Value below must match the account type specified in res/xml/syncadapter.xml

    public static void requestManualSync(Context context) {
        requestManualSync(AccountUtils.getActiveAccount(context));
    }

    public static void requestManualSync(Account mChosenAccount) {
        requestManualSync(mChosenAccount, false);
    }

    public static void requestManualSync(Account mChosenAccount, boolean userDataSyncOnly) {
        if (mChosenAccount != null) {
            LogUtils.logd(TAG, "Requesting manual sync for account " + mChosenAccount.name
                    + " userDataSyncOnly=" + userDataSyncOnly);

            Bundle b = new Bundle();
            b.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
            b.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
            if (userDataSyncOnly) {
                b.putBoolean(SyncAdapter.EXTRA_SYNC_USER_DATA_ONLY, true);
            }
            ContentResolver.setSyncAutomatically(mChosenAccount, NotesContentProvider.AUTHORITY, true);
            ContentResolver.setIsSyncable(mChosenAccount, NotesContentProvider.AUTHORITY, 1);

            boolean pending = ContentResolver.isSyncPending(mChosenAccount, NotesContentProvider.AUTHORITY);
            if (pending) {
                LogUtils.logd(TAG, "Warning: sync is PENDING. Will cancel.");
            }
            boolean active = ContentResolver.isSyncActive(mChosenAccount, NotesContentProvider.AUTHORITY);
            if (active) {
                LogUtils.logd(TAG, "Warning: sync is ACTIVE. Will cancel.");
            }

            if (pending || active) {
                LogUtils.logd(TAG, "Cancelling previously pending/active sync.");
                ContentResolver.cancelSync(mChosenAccount, NotesContentProvider.AUTHORITY);
            }

            LogUtils.logd(TAG, "Requesting sync now.");
            ContentResolver.requestSync(mChosenAccount, NotesContentProvider.AUTHORITY, b);
        } else {
            LogUtils.logd(TAG, "Can't request manual sync -- no chosen account.");
        }
    }

    /**
     * Create an entry for this application in the system account list, if it isn't already there.
     *
     * @param context Context
     */
    @TargetApi(Build.VERSION_CODES.FROYO)
    public static void createSyncAccount(Context context) {
        boolean newAccount = false;
        boolean setupComplete = PreferenceManager
                .getDefaultSharedPreferences(context).getBoolean(PREF_SETUP_COMPLETE, false);

        // Create account, if it's missing. (Either first run, or user has deleted account.)
        Account account = acccountBuilder(context);
        AccountManager accountManager = (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);
        if (accountManager.addAccountExplicitly(account, null, null)) {
            // Inform the system that this account supports sync
            ContentResolver.setIsSyncable(account, CONTENT_AUTHORITY, 1);
            // Inform the system that this account is eligible for auto sync when the network is up
            ContentResolver.setSyncAutomatically(account, CONTENT_AUTHORITY, true);
            // Recommend a schedule for automatic synchronization. The system may modify this based
            // on other scheduled syncs and network utilization.
            ContentResolver.addPeriodicSync(account, CONTENT_AUTHORITY, new Bundle(), SYNC_FREQUENCY);
            newAccount = true;
        }

        if (newAccount) {
            AccountUtils.setActiveAccount(context, account.name);
        }

        // Schedule an initial sync if we detect problems with either our account or our local
        // data has been deleted. (Note that it's possible to clear app data WITHOUT affecting
        // the account list, so wee need to check both.)
        if (newAccount || !setupComplete) {
            requestManualSync(account);
            PreferenceManager.getDefaultSharedPreferences(context).edit()
                    .putBoolean(PREF_SETUP_COMPLETE, true).commit();
        }
    }

    /**
     * Obtain a handle to the {@link Account} used for sync in this application.
     *
     * @return Handle to application's account (not guaranteed to resolve unless createSyncAccount()
     * has been called)
     */
    public static Account acccountBuilder(Context context) {
        // Note: Normally the account name is set to the user's identity (username or email
        // address). However, since we aren't actually using any user accounts, it makes more sense
        // to use a generic string in this case.
        //
        // This string should *not* be localized. If the user switches locale, we would not be
        // able to locate the old account, and may erroneously register multiple accounts.
        //TODO use the real user no or name for the account name
        final String accountName = ACCOUNT_NAME;
        return new Account(accountName, context.getResources().getString(R.string.account_type));
    }
}
