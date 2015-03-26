package org.rouif.notes.provider.note;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;

import org.rouif.notes.provider.base.AbstractSelection;

import java.util.Date;

/**
 * Selection for the {@code note} table.
 */
public class NoteSelection extends AbstractSelection<NoteSelection> {
    @Override
    protected Uri baseUri() {
        return NoteColumns.CONTENT_URI;
    }

    /**
     * Query the given content resolver using this selection.
     *
     * @param contentResolver The content resolver to query.
     * @param projection A list of which columns to return. Passing null will return all columns, which is inefficient.
     * @param sortOrder How to order the rows, formatted as an SQL ORDER BY clause (excluding the ORDER BY itself). Passing null will use the default sort
     *            order, which may be unordered.
     * @return A {@code NoteCursor} object, which is positioned before the first entry, or null.
     */
    public NoteCursor query(ContentResolver contentResolver, String[] projection, String sortOrder) {
        Cursor cursor = contentResolver.query(uri(), projection, sel(), args(), sortOrder);
        if (cursor == null) return null;
        return new NoteCursor(cursor);
    }

    /**
     * Equivalent of calling {@code query(contentResolver, projection, null)}.
     */
    public NoteCursor query(ContentResolver contentResolver, String[] projection) {
        return query(contentResolver, projection, null);
    }

    /**
     * Equivalent of calling {@code query(contentResolver, projection, null, null)}.
     */
    public NoteCursor query(ContentResolver contentResolver) {
        return query(contentResolver, null, null);
    }


    public NoteSelection id(long... value) {
        addEquals("note." + NoteColumns._ID, toObjectArray(value));
        return this;
    }

    public NoteSelection serverId(Long... value) {
        addEquals(NoteColumns.SERVER_ID, value);
        return this;
    }

    public NoteSelection serverIdNot(Long... value) {
        addNotEquals(NoteColumns.SERVER_ID, value);
        return this;
    }

    public NoteSelection serverIdGt(long value) {
        addGreaterThan(NoteColumns.SERVER_ID, value);
        return this;
    }

    public NoteSelection serverIdGtEq(long value) {
        addGreaterThanOrEquals(NoteColumns.SERVER_ID, value);
        return this;
    }

    public NoteSelection serverIdLt(long value) {
        addLessThan(NoteColumns.SERVER_ID, value);
        return this;
    }

    public NoteSelection serverIdLtEq(long value) {
        addLessThanOrEquals(NoteColumns.SERVER_ID, value);
        return this;
    }

    public NoteSelection title(String... value) {
        addEquals(NoteColumns.TITLE, value);
        return this;
    }

    public NoteSelection titleNot(String... value) {
        addNotEquals(NoteColumns.TITLE, value);
        return this;
    }

    public NoteSelection titleLike(String... value) {
        addLike(NoteColumns.TITLE, value);
        return this;
    }

    public NoteSelection titleContains(String... value) {
        addContains(NoteColumns.TITLE, value);
        return this;
    }

    public NoteSelection titleStartsWith(String... value) {
        addStartsWith(NoteColumns.TITLE, value);
        return this;
    }

    public NoteSelection titleEndsWith(String... value) {
        addEndsWith(NoteColumns.TITLE, value);
        return this;
    }

    public NoteSelection content(String... value) {
        addEquals(NoteColumns.CONTENT, value);
        return this;
    }

    public NoteSelection contentNot(String... value) {
        addNotEquals(NoteColumns.CONTENT, value);
        return this;
    }

    public NoteSelection contentLike(String... value) {
        addLike(NoteColumns.CONTENT, value);
        return this;
    }

    public NoteSelection contentContains(String... value) {
        addContains(NoteColumns.CONTENT, value);
        return this;
    }

    public NoteSelection contentStartsWith(String... value) {
        addStartsWith(NoteColumns.CONTENT, value);
        return this;
    }

    public NoteSelection contentEndsWith(String... value) {
        addEndsWith(NoteColumns.CONTENT, value);
        return this;
    }

    public NoteSelection lastUpdate(Date... value) {
        addEquals(NoteColumns.LAST_UPDATE, value);
        return this;
    }

    public NoteSelection lastUpdateNot(Date... value) {
        addNotEquals(NoteColumns.LAST_UPDATE, value);
        return this;
    }

    public NoteSelection lastUpdate(Long... value) {
        addEquals(NoteColumns.LAST_UPDATE, value);
        return this;
    }

    public NoteSelection lastUpdateAfter(Date value) {
        addGreaterThan(NoteColumns.LAST_UPDATE, value);
        return this;
    }

    public NoteSelection lastUpdateAfterEq(Date value) {
        addGreaterThanOrEquals(NoteColumns.LAST_UPDATE, value);
        return this;
    }

    public NoteSelection lastUpdateBefore(Date value) {
        addLessThan(NoteColumns.LAST_UPDATE, value);
        return this;
    }

    public NoteSelection lastUpdateBeforeEq(Date value) {
        addLessThanOrEquals(NoteColumns.LAST_UPDATE, value);
        return this;
    }

    public NoteSelection syncStatus(SyncStatus... value) {
        addEquals(NoteColumns.SYNC_STATUS, value);
        return this;
    }

    public NoteSelection syncStatusNot(SyncStatus... value) {
        addNotEquals(NoteColumns.SYNC_STATUS, value);
        return this;
    }

}
