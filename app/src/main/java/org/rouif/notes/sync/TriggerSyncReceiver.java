package org.rouif.notes.sync;

import android.accounts.Account;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import org.rouif.notes.provider.NotesContentProvider;
import org.rouif.notes.utils.AccountUtils;
import org.rouif.notes.utils.LogUtils;


/**
 * A simple {@link BroadcastReceiver} that triggers a sync. This is used by the GCM code to trigger
 * jittered syncs using {@link android.app.AlarmManager}.
 */
public class TriggerSyncReceiver extends BroadcastReceiver {
    private static final String TAG = LogUtils.makeLogTag(AccountUtils.class);
    public static final String EXTRA_USER_DATA_SYNC_ONLY = "org.rouif.notes.EXTRA_USER_DATA_SYNC_ONLY";

    @Override
    public void onReceive(Context context, Intent intent) {
        String accountName = AccountUtils.getActiveAccountName(context);
        if (TextUtils.isEmpty(accountName)) {
            return;
        }
        Account account = AccountUtils.getActiveAccount(context);
        if (account != null) {
            if (intent.getBooleanExtra(EXTRA_USER_DATA_SYNC_ONLY, false) ) {
                // this is a request to sync user data only, so do a manual sync right now
                // with the userDataOnly == true.
                SyncUtils.requestManualSync(account, true);
            } else {
                // this is a request to sync everything
                ContentResolver.requestSync(account, NotesContentProvider.AUTHORITY, new Bundle());
            }
        }
    }
}
