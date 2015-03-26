package org.rouif.notes.provider.note;

import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.rouif.notes.provider.base.AbstractCursor;

import java.util.Date;

/**
 * Cursor wrapper for the {@code note} table.
 */
public class NoteCursor extends AbstractCursor implements NoteModel {
    public NoteCursor(Cursor cursor) {
        super(cursor);
    }

    /**
     * Primary key.
     */
    public long getId() {
        Long res = getLongOrNull(NoteColumns._ID);
        if (res == null)
            throw new NullPointerException("The value of '_id' in the database was null, which is not allowed according to the model definition");
        return res;
    }

    /**
     * Get the {@code server_id} value.
     * Can be {@code null}.
     */
    @Nullable
    public Long getServerId() {
        Long res = getLongOrNull(NoteColumns.SERVER_ID);
        return res;
    }

    /**
     * Get the {@code title} value.
     * Can be {@code null}.
     */
    @Nullable
    public String getTitle() {
        String res = getStringOrNull(NoteColumns.TITLE);
        return res;
    }

    /**
     * Get the {@code content} value.
     * Can be {@code null}.
     */
    @Nullable
    public String getContent() {
        String res = getStringOrNull(NoteColumns.CONTENT);
        return res;
    }

    /**
     * Get the {@code last_update} value.
     * Can be {@code null}.
     */
    @Nullable
    public Date getLastUpdate() {
        Date res = getDateOrNull(NoteColumns.LAST_UPDATE);
        return res;
    }

    /**
     * Get the {@code sync_status} value.
     * Cannot be {@code null}.
     */
    @NonNull
    public SyncStatus getSyncStatus() {
        Integer intValue = getIntegerOrNull(NoteColumns.SYNC_STATUS);
        if (intValue == null)
            throw new NullPointerException("The value of 'sync_status' in the database was null, which is not allowed according to the model definition");
        return SyncStatus.values()[intValue];
    }
}
