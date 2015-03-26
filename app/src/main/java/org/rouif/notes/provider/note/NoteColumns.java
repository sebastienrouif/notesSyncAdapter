package org.rouif.notes.provider.note;

import android.net.Uri;
import android.provider.BaseColumns;

import org.rouif.notes.provider.NotesContentProvider;

/**
 * Columns for the {@code note} table.
 */
public class NoteColumns implements BaseColumns {
    public static final String TABLE_NAME = "note";
    public static final Uri CONTENT_URI = Uri.parse(NotesContentProvider.CONTENT_URI_BASE + "/" + TABLE_NAME);

    /**
     * Primary key.
     */
    public static final String _ID = BaseColumns._ID;

    public static final String SERVER_ID = "server_id";

    public static final String TITLE = "title";

    public static final String CONTENT = "content";

    public static final String LAST_UPDATE = "last_update";

    public static final String SYNC_STATUS = "sync_status";


    public static final String DEFAULT_ORDER = TABLE_NAME + "." +_ID;

    // @formatter:off
    public static final String[] ALL_COLUMNS = new String[] {
            _ID,
            SERVER_ID,
            TITLE,
            CONTENT,
            LAST_UPDATE,
            SYNC_STATUS
    };
    // @formatter:on

    public static boolean hasColumns(String[] projection) {
        if (projection == null) return true;
        for (String c : projection) {
            if (c == SERVER_ID || c.contains("." + SERVER_ID)) return true;
            if (c == TITLE || c.contains("." + TITLE)) return true;
            if (c == CONTENT || c.contains("." + CONTENT)) return true;
            if (c == LAST_UPDATE || c.contains("." + LAST_UPDATE)) return true;
            if (c == SYNC_STATUS || c.contains("." + SYNC_STATUS)) return true;
        }
        return false;
    }

}
