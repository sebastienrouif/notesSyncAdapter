package org.rouif.notes.fragment;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.google.api.client.util.DateTime;

import org.rouif.notes.R;
import org.rouif.notes.backend.model.noteApi.model.Note;
import org.rouif.notes.provider.note.NoteColumns;
import org.rouif.notes.provider.note.NoteContentValues;
import org.rouif.notes.provider.note.NoteCursor;
import org.rouif.notes.provider.note.NoteSelection;
import org.rouif.notes.provider.note.SyncStatus;
import org.rouif.notes.utils.LogUtils;

import java.util.Date;

public class NoteDetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    public static final long NEW_NOTE = -10L;

    private static final String TAG = LogUtils.makeLogTag(NoteDetailFragment.class);
    private static final String ARG_ITEM_ID = "item_id";
    private static final int NOTES_DETAIL_LOADER = 101;

    private long mLocalId;
    private Note mNote;
    private EditText mTitleView;
    private TextView mUpdateView;
    private EditText mContentView;
    private boolean mShouldDelete;

    public static NoteDetailFragment newInstance(long localId) {
        Bundle arguments = new Bundle();
        arguments.putLong(NoteDetailFragment.ARG_ITEM_ID, localId);
        NoteDetailFragment fragment = new NoteDetailFragment();
        fragment.setArguments(arguments);
        return fragment;
    }

    public NoteDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        if (getArguments().containsKey(ARG_ITEM_ID)) {
            mLocalId = getArguments().getLong(ARG_ITEM_ID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_note_detail, container, false);
        getViewReferences(rootView);
        return rootView;
    }

    private void getViewReferences(View view) {
        mTitleView = (EditText) view.findViewById(R.id.note_detail_title);
        mUpdateView = (TextView) view.findViewById(R.id.note_detail_update);
        mContentView = (EditText) view.findViewById(R.id.note_detail_content);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.note_detail, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onDetach() {
        if (shouldSaveNote()) {
            saveNote(mTitleView.getText().toString(), mContentView.getText().toString());
        }
        super.onDetach();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                saveNote(mTitleView.getText().toString(), mContentView.getText().toString());
                return true;
            case R.id.action_delete:
                deleteNote();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void deleteNote() {
        NoteSelection noteSelection = new NoteSelection()
                .id(mLocalId);

        NoteContentValues noteContentValues = new NoteContentValues()
                .putSyncStatus(SyncStatus.TO_DELETE)
                .putLastUpdate(new Date());

        noteContentValues.update(getActivity().getContentResolver(), noteSelection);

        mShouldDelete = true;
        getActivity().finish();
    }

    private boolean shouldSaveNote() {
        return didContentChanged() && !mShouldDelete;
    }

    private boolean didContentChanged() {
        boolean titleChanged;
        boolean contentChanged;
        if (mNote == null) {
            titleChanged = !TextUtils.isEmpty(mTitleView.getText().toString());
            contentChanged = !TextUtils.isEmpty(mContentView.getText().toString());
        } else {
            titleChanged = !TextUtils.equals(mNote.getTitle(), mTitleView.getText().toString());
            contentChanged = !TextUtils.equals(mNote.getContent(), mContentView.getText().toString());
        }
        return titleChanged || contentChanged;
    }


    private void saveNote(String title, String content) {
        NoteContentValues noteContentValues = new NoteContentValues()
                .putContent(content)
                .putTitle(title)
                .putLastUpdate(new Date())
                .putSyncStatus(SyncStatus.TO_SYNC);

        if (mLocalId == NEW_NOTE) {
            Uri uri = noteContentValues.insert(getActivity().getContentResolver());
            String lastPathSegment = uri.getLastPathSegment();
            mLocalId = Integer.parseInt(lastPathSegment);
            LogUtils.logd(TAG, "mLocalId " + mLocalId);
        } else {
            NoteSelection noteSelection = new NoteSelection()
                    .id(mLocalId);
            int updateCount = noteContentValues.update(getActivity().getContentResolver(), noteSelection);
            LogUtils.logd(TAG, "updateCount " + updateCount);
        }
    }

    private void setNoteToView(Note note) {
        if (note != null) {
            mTitleView.setText(note.getTitle());
            mUpdateView.setText(String.valueOf(note.getId()));
            mContentView.setText(note.getContent());
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getLoaderManager().initLoader(NOTES_DETAIL_LOADER, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case NOTES_DETAIL_LOADER:
                NoteSelection noteSelection = new NoteSelection()
                        .syncStatus(SyncStatus.SYNCED, SyncStatus.TO_SYNC)
                        .and().id(mLocalId);

                String[] projection = NoteColumns.ALL_COLUMNS;
                return new CursorLoader(getActivity(), noteSelection.uri(), projection, noteSelection.sel(), noteSelection.args(), null);
            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        NoteCursor noteCursor = new NoteCursor(data);
        if (noteCursor.moveToFirst()) {
            Note note = new Note();
            note.setContent(noteCursor.getContent())
                    .setLastUpdate(new DateTime(noteCursor.getLastUpdate()))
                    .setTitle(noteCursor.getTitle())
                    .setId(noteCursor.getId());
            mNote = note;
        }
        setNoteToView(mNote);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

}
