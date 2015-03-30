/*
 * Copyright 2014 Google Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.rouif.notes.sync;

import android.accounts.Account;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SyncResult;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.googleapis.services.AbstractGoogleClientRequest;
import com.google.api.client.googleapis.services.GoogleClientRequestInitializer;
import com.google.api.client.util.DateTime;

import org.rouif.notes.backend.model.noteApi.NoteApi;
import org.rouif.notes.backend.model.noteApi.model.CollectionResponseNote;
import org.rouif.notes.backend.model.noteApi.model.Note;
import org.rouif.notes.provider.note.NoteContentValues;
import org.rouif.notes.provider.note.NoteCursor;
import org.rouif.notes.provider.note.NoteSelection;
import org.rouif.notes.provider.note.SyncStatus;
import org.rouif.notes.utils.AccountUtils;
import org.rouif.notes.utils.LogUtils;
import org.rouif.notes.utils.SharedPreferenceUtils;

import java.io.IOException;
import java.util.Date;
import java.util.List;

/**
 * A helper class for dealing with conference data synchronization. All operations occur on the
 * thread they're called from, so it's best to wrap calls in an {@link android.os.AsyncTask}, or
 * better yet, a {@link android.app.Service}.
 */
public class SyncHelper {
    private static final String TAG = LogUtils.makeLogTag(SyncHelper.class);

    // remote sync consists of these operations, which we try one by one (and tolerate
    // individual failures on each)
    private static final int OP_NOTE_PULL_SYNC = 1;
    private static final int OP_NOTE_PUSH_SYNC = 2;


    private Context mContext;
    private NoteApi mNoteApi;


    private long numInserts;
    private long numUpdates;
    private long numDeletes;
    private long numEntries;


    public SyncHelper(Context context) {
        mContext = context;
    }

    /**
     * Attempts to perform conference data synchronization. The data comes from the remote URL
     * configured in The remote URL
     * must point to a manifest file that, in turn, can reference other files. For more details
     * about conference data synchronization, refer to the documentation at
     * http://code.google.com/p/iosched.
     *
     * @param syncResult (optional) the sync result object to update with statistics.
     * @param account    the account associated with this sync
     * @return Whether or not the synchronization made any changes to the data.
     */
    public boolean performSync(SyncResult syncResult, Account account, Bundle extras) {
        ////////////////////////////////
        //        SETUP           /////
        //////////////////////////////
        boolean dataChanged = false;
//        if (!SharedPreferenceUtils.getInstance(mContext).isDataBootstrapDone()) {
//            LogUtils.logd(TAG, "Sync aborting (data bootstrap not done yet)");
//            return false;
//        }

        long lastAttemptTime = SharedPreferenceUtils.getInstance(mContext).getLastSyncAttemptedTime();
        long now = System.currentTimeMillis();
        long timeSinceAttempt = now - lastAttemptTime;
        final boolean manualSync = extras.getBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, false);
        final boolean uploadOnly = extras.getBoolean(ContentResolver.SYNC_EXTRAS_UPLOAD, false);
        final boolean userDataOnly = extras.getBoolean(SyncAdapter.EXTRA_SYNC_USER_DATA_ONLY, false);

        LogUtils.logi(TAG, "Performing sync for account: " + account);
        SharedPreferenceUtils.getInstance(mContext).markSyncAttemptedNow();
        long opStart;
        long remoteSyncDuration, choresDuration;

        opStart = System.currentTimeMillis();


        ////////////////////////////////
        //    TASKS TO PROCESS    /////
        //////////////////////////////
        // remote sync consists of these operations, which we try one by one (and tolerate
        // individual failures on each)
        //upload only will be set to true if the sync is triggered by a notify on the contentProvider
        int[] opsToPerform = uploadOnly ?
                new int[]{OP_NOTE_PUSH_SYNC} :
                new int[]{OP_NOTE_PUSH_SYNC, OP_NOTE_PULL_SYNC};


        ////////////////////////////////
        //      TASK PROCESSING   /////
        //////////////////////////////
        for (int op : opsToPerform) {
            try {
                switch (op) {
                    case OP_NOTE_PULL_SYNC:
                        dataChanged |= doNotePullSync(mContext);
                        break;
                    case OP_NOTE_PUSH_SYNC:
                        dataChanged |= doNotePushSync(mContext);
                        break;
                    default:
                        break;
                }
            } catch (AuthException ex) {
                syncResult.stats.numAuthExceptions++;

                // if we have a token, try to refresh it
                if (AccountUtils.hasToken(mContext, account.name)) {
                    AccountUtils.refreshAuthToken(mContext);
                } else {
                    LogUtils.logw(TAG, "No auth token yet for this account. Skipping remote sync.");
                }
            } catch (IOException exception) {
                exception.printStackTrace();
                LogUtils.loge(TAG, "Error performing remote sync.");
                increaseIoExceptions(syncResult);
            } catch (Throwable throwable) {
                throwable.printStackTrace();
                LogUtils.loge(TAG, "Error performing remote sync.");
                increaseIoExceptions(syncResult);
            }
        }
        remoteSyncDuration = System.currentTimeMillis() - opStart;


        ////////////////////////////////
        //      POST PROCESSING   /////
        //////////////////////////////
        // If data has changed, there are a few chores we have to do
        opStart = System.currentTimeMillis();
        if (dataChanged) {
            try {
                performPostSyncChores(mContext);
            } catch (Throwable throwable) {
                throwable.printStackTrace();
                LogUtils.loge(TAG, "Error performing post sync chores.");
            }
        }

        choresDuration = System.currentTimeMillis() - opStart;

        if (syncResult != null && syncResult.stats != null) {
            syncResult.stats.numEntries += numEntries;
            syncResult.stats.numUpdates += numUpdates;
            syncResult.stats.numDeletes += numDeletes;
            syncResult.stats.numInserts += numInserts;

            LogUtils.logd(TAG, "ACCOUNT SYNC STATS" + "\n" + syncResult.toString());
        }

        if (dataChanged) {
            long totalDuration = choresDuration + remoteSyncDuration;
            LogUtils.logd(TAG, "SYNC STATS:\n" +
                    " *  Account synced: " + (account == null ? "null" : account.name) + "\n" +
                    " *  Content provider operations: " + numEntries + "\n" +
                    " *  Remote sync took: " + remoteSyncDuration + "ms\n" +
                    " *  Post-sync chores took: " + choresDuration + "ms\n" +
                    " *  Total time: " + totalDuration + "ms\n");
        }

        LogUtils.logi(TAG, "End of sync (" + (dataChanged ? "data changed" : "no data change") + ")");

        return dataChanged;
    }

    public static void performPostSyncChores(final Context context) {
        LogUtils.logd(TAG, "performPostSyncChores");
    }


    /**
     * Checks if there are changes on MySchedule to sync with/from remote AppData folder.
     *
     * @return Whether or not data was changed.
     * @throws IOException if there is a problem uploading the data.
     */
    private boolean doNotePullSync(Context context) throws IOException {
        if (!isOnline()) {
            LogUtils.logd(TAG, "Not attempting doNotePullSync because device is OFFLINE");
            return false;
        }

        LogUtils.logd(TAG, "Starting doNotePullSync sync.");

        NoteApi api = getNoteApi();
        CollectionResponseNote result = api.list().execute();
        List<Note> items = result.getItems();

        if (items != null) {
            for (Note note : items) {
                LogUtils.logd(TAG, note.toPrettyString());
                NoteContentValues noteContentValues = new NoteContentValues()
                        .putContent(note.getContent())
                        .putTitle(note.getTitle())
                        .putLastUpdate(note.getLastUpdate().getValue())
                        .putServerId(note.getId())
                        .putSyncStatus(SyncStatus.SYNCED);
                Uri insertUri = noteContentValues.insert(mContext.getContentResolver());

                LogUtils.logd(TAG, "insertUri" + insertUri);
                if (insertUri != null) {
                    updateInsertCount(1);
                } else {
                    NoteSelection noteSelection = new NoteSelection()
                            .serverId(note.getId())
                            .lastUpdateBefore(new Date(note.getLastUpdate().getValue()));
                    int updateCount = noteContentValues.update(mContext.getContentResolver(), noteSelection);
                    updateUpdateCount(updateCount);
                    LogUtils.logd(TAG, "updateCount count" + updateCount);
                }
            }
        }

        return true;
    }


    private boolean doNotePushSync(Context context) throws IOException {
        if (!isOnline()) {
            LogUtils.logd(TAG, "Not attempting doNotePushSync because device is OFFLINE");
            return false;
        }

        LogUtils.logd(TAG, "Starting doNotePushSync sync.");

        boolean modified = false;
        NoteApi api = getNoteApi();

        NoteSelection noteSelectionDelete = new NoteSelection().syncStatus(SyncStatus.TO_DELETE);
        NoteCursor queryDelete = noteSelectionDelete.query(context.getContentResolver());

        queryDelete.moveToPosition(-1);
        while (queryDelete.moveToNext()) {
            Long serverId = queryDelete.getServerId();
            int deleteNoteCount = 0;
            if (serverId !=null) {
                Void execute = api.remove(serverId).execute();
                deleteNoteCount = deleteNote(serverId);

            } else {
                deleteNoteCount = deleteNoteLocal(queryDelete.getId());
            }
            updateDeleteCount(deleteNoteCount);
            LogUtils.logd(TAG, "ServerSide - Deleted Note " + String.valueOf(serverId));
        }
        queryDelete.close();



        NoteSelection noteSelectionUpdate = new NoteSelection().syncStatus(SyncStatus.TO_SYNC);
        NoteCursor queryUpdate = noteSelectionUpdate.query(context.getContentResolver());

        queryUpdate.moveToPosition(-1);
        while (queryUpdate.moveToNext()) {
            Note note = new Note()
                    .setTitle(queryUpdate.getTitle())
                    .setContent(queryUpdate.getContent())
                    .setLastUpdate(new DateTime(queryUpdate.getLastUpdate()));

            Long serverId = queryUpdate.getServerId();
            if (serverId !=null) {
                Note execute = api.update(serverId, note).execute();
                int noteSynced = setNoteSynced(serverId);
                updateUpdateCount(noteSynced);
                LogUtils.logd(TAG, "ServerSide - Updated Note " + String.valueOf(serverId));
            } else {
                Note execute = api.insert(note).execute();
                serverId = execute.getId();
                int noteSyncedLocal = setNoteSyncedLocal(serverId, queryUpdate.getId());
                updateUpdateCount(noteSyncedLocal);
                LogUtils.logd(TAG, "ServerSide - Inserted Note " + String.valueOf(serverId));
            }
        }
        queryUpdate.close();

        modified = true;
        return modified;
    }

    private int setNoteSynced(long serverId) {
        NoteSelection noteSelection = new NoteSelection().serverId(serverId);
        NoteContentValues noteContentValues = new NoteContentValues().putSyncStatus(SyncStatus.SYNCED);
        return noteContentValues.update(mContext.getContentResolver(), noteSelection);
    }

    private int setNoteSyncedLocal(long serverId, long localId) {
        NoteSelection noteSelection = new NoteSelection().id(localId);
        NoteContentValues noteContentValues = new NoteContentValues()
                .putSyncStatus(SyncStatus.SYNCED)
                .putServerId(serverId);
        return noteContentValues.update(mContext.getContentResolver(), noteSelection);
    }


    private int deleteNote(long serverId) {
        NoteSelection noteSelection = new NoteSelection().serverId(serverId);
        return noteSelection.delete(mContext.getContentResolver());
    }

    private int deleteNoteLocal(long localId) {
        NoteSelection noteSelection = new NoteSelection().id(localId);
        return noteSelection.delete(mContext.getContentResolver());
    }

    private NoteApi getNoteApi() {
        if (mNoteApi == null) { // Only do this once
            NoteApi.Builder builder = new NoteApi.Builder(AndroidHttp.newCompatibleTransport(),
                    new AndroidJsonFactory(), null);
            builder.setApplicationName("global-wharf-89614");
            //builder.setRootUrl("http://10.0.2.2:8080/_ah/api/");
            builder.setGoogleClientRequestInitializer(new GoogleClientRequestInitializer() {
                @Override
                public void initialize(AbstractGoogleClientRequest<?> abstractGoogleClientRequest) throws IOException {
                    abstractGoogleClientRequest.setDisableGZipContent(true);
                }
            });

            mNoteApi = builder.build();
        }

        return mNoteApi;
    }

    private void updateInsertCount(int executedOperations) {
        numInserts += executedOperations;
        numEntries += executedOperations;
    }

    private void updateUpdateCount(int executedOperations) {
        numUpdates += executedOperations;
        numEntries += executedOperations;
    }

    private void updateDeleteCount(int executedOperations) {
        numDeletes += executedOperations;
        numEntries += executedOperations;
    }

    // Returns whether we are connected to the internet.
    private boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) mContext.getSystemService(
                Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null &&
                cm.getActiveNetworkInfo().isConnectedOrConnecting();
    }

    private static void increaseIoExceptions(SyncResult syncResult) {
        if (syncResult != null && syncResult.stats != null) {
            ++syncResult.stats.numIoExceptions;
        }
    }


    private static void increaseParseExceptions(SyncResult syncResult) {
        if (syncResult != null && syncResult.stats != null) {
            ++syncResult.stats.numParseExceptions;
        }
    }

    public static class AuthException extends RuntimeException {
    }
}
