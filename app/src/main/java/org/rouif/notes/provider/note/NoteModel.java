package org.rouif.notes.provider.note;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.rouif.notes.provider.base.BaseModel;

import java.util.Date;

/**
 * Data model for the {@code note} table.
 */
public interface NoteModel extends BaseModel {

    /**
     * Get the {@code server_id} value.
     * Can be {@code null}.
     */
    @Nullable
    Long getServerId();

    /**
     * Get the {@code title} value.
     * Can be {@code null}.
     */
    @Nullable
    String getTitle();

    /**
     * Get the {@code content} value.
     * Can be {@code null}.
     */
    @Nullable
    String getContent();

    /**
     * Get the {@code last_update} value.
     * Can be {@code null}.
     */
    @Nullable
    Date getLastUpdate();

    /**
     * Get the {@code sync_status} value.
     * Cannot be {@code null}.
     */
    @NonNull
    SyncStatus getSyncStatus();
}
