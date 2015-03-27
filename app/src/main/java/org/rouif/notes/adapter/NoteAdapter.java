package org.rouif.notes.adapter;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import org.rouif.notes.provider.note.NoteCursor;

public class NoteAdapter extends CursorAdapter {

    public NoteAdapter(Context context, Cursor c) {
        super(context, c);
    }

    public NoteAdapter(Context context, Cursor c, boolean autoRequery) {
        super(context, c, autoRequery);
    }

    public NoteAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View newView = inflater.inflate(android.R.layout.simple_list_item_1, parent, false);
        return newView;

    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView content = (TextView) view.findViewById(android.R.id.text1);
        NoteCursor noteCursor = new NoteCursor(cursor);
        content.setText(noteCursor.getTitle());
    }
}
