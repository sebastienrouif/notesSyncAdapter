package org.rouif.notes.provider.note;

import android.content.ContentResolver;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.rouif.notes.provider.base.AbstractContentValues;

import java.util.Date;

/**
 * Content values wrapper for the {@code note} table.
 */
public class NoteContentValues extends AbstractContentValues {
    @Override
    public Uri uri() {
        return NoteColumns.CONTENT_URI;
    }

    /**
     * Update row(s) using the values stored by this object and the given selection.
     *
     * @param contentResolver The content resolver to use.
     * @param where The selection to use (can be {@code null}).
     */
    public int update(ContentResolver contentResolver, @Nullable NoteSelection where) {
        return contentResolver.update(uri(), values(), where == null ? null : where.sel(), where == null ? null : where.args());
    }

    public NoteContentValues putServerId(@Nullable Long value) {
        mContentValues.put(NoteColumns.SERVER_ID, value);
        return this;
    }

    public NoteContentValues putServerIdNull() {
        mContentValues.putNull(NoteColumns.SERVER_ID);
        return this;
    }

    public NoteContentValues putTitle(@Nullable String value) {
        mContentValues.put(NoteColumns.TITLE, value);
        return this;
    }

    public NoteContentValues putTitleNull() {
        mContentValues.putNull(NoteColumns.TITLE);
        return this;
    }

    public NoteContentValues putContent(@Nullable String value) {
        mContentValues.put(NoteColumns.CONTENT, value);
        return this;
    }

    public NoteContentValues putContentNull() {
        mContentValues.putNull(NoteColumns.CONTENT);
        return this;
    }

    public NoteContentValues putLastUpdate(@Nullable Date value) {
        mContentValues.put(NoteColumns.LAST_UPDATE, value == null ? null : value.getTime());
        return this;
    }

    public NoteContentValues putLastUpdateNull() {
        mContentValues.putNull(NoteColumns.LAST_UPDATE);
        return this;
    }

    public NoteContentValues putLastUpdate(@Nullable Long value) {
        mContentValues.put(NoteColumns.LAST_UPDATE, value);
        return this;
    }

    public NoteContentValues putSyncStatus(@NonNull SyncStatus value) {
        if (value == null) throw new IllegalArgumentException("syncStatus must not be null");
        mContentValues.put(NoteColumns.SYNC_STATUS, value.ordinal());
        return this;
    }

}
