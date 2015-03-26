package org.rouif.notes.provider.note;

/**
 * Possible values for the {@code sync_status} column of the {@code note} table.
 */
public enum SyncStatus {
    /**
     * 
     */
    TO_DELETE,

    /**
     * 
     */
    TO_SYNC,

    /**
     * 
     */
    SYNCED,

}