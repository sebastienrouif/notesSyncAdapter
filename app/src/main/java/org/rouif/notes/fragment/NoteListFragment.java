package org.rouif.notes.fragment;

import android.app.Activity;
import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import org.rouif.notes.R;
import org.rouif.notes.adapter.NoteAdapter;
import org.rouif.notes.provider.note.NoteColumns;
import org.rouif.notes.provider.note.NoteSelection;
import org.rouif.notes.provider.note.SyncStatus;
import org.rouif.notes.sync.SyncUtils;
import org.rouif.notes.utils.LogUtils;


public class NoteListFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = LogUtils.makeLogTag(NoteListFragment.class);
    private static final String STATE_ACTIVATED_POSITION = "activated_position";
    private static final int NOTES_LOADER = 100;
    private Callbacks mCallbacks = sDummyCallbacks;
    private int mActivatedPosition = ListView.INVALID_POSITION;
    private NoteAdapter mNoteAdapter;

    public interface Callbacks {
        public void onItemSelected(Long severId, Long localId);
    }


    private static Callbacks sDummyCallbacks = new Callbacks() {
        @Override
        public void onItemSelected(Long severId, Long localId) {
        }
    };

    public NoteListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Restore the previously serialized activated item position.
        if (savedInstanceState != null
                && savedInstanceState.containsKey(STATE_ACTIVATED_POSITION)) {
            setActivatedPosition(savedInstanceState.getInt(STATE_ACTIVATED_POSITION));
        }

        setEmptyText(getString(R.string.notes_list_empty_place_holder));
        mNoteAdapter = new NoteAdapter(getActivity(), null, true);
        setListAdapter(mNoteAdapter);
        getLoaderManager().restartLoader(NOTES_LOADER, null, this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.note_list, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_new:
                mCallbacks.onItemSelected(0L, NoteDetailFragment.NEW_NOTE);
                return true;
            case R.id.action_sync:
                SyncUtils.requestManualSync(getActivity());
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (!(activity instanceof Callbacks)) {
            throw new IllegalStateException("Activity must implement fragment's callbacks.");
        }
        mCallbacks = (Callbacks) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = sDummyCallbacks;
    }

    @Override
    public void onListItemClick(ListView listView, View view, int position, long localId) {
        super.onListItemClick(listView, view, position, localId);
        // Notify the active callbacks interface (the activity, if the
        // fragment is attached to one) that an item has been selected.
        mCallbacks.onItemSelected(0L,localId);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mActivatedPosition != ListView.INVALID_POSITION) {
            // Serialize and persist the activated item position.
            outState.putInt(STATE_ACTIVATED_POSITION, mActivatedPosition);
        }
    }

    /**
     * Turns on activate-on-click mode. When this mode is on, list items will be
     * given the 'activated' state when touched.
     */
    public void setActivateOnItemClick(boolean activateOnItemClick) {
        // When setting CHOICE_MODE_SINGLE, ListView will automatically
        // give items the 'activated' state when touched.
        getListView().setChoiceMode(activateOnItemClick
                ? ListView.CHOICE_MODE_SINGLE
                : ListView.CHOICE_MODE_NONE);
    }

    private void setActivatedPosition(int position) {
        if (position == ListView.INVALID_POSITION) {
            getListView().setItemChecked(mActivatedPosition, false);
        } else {
            getListView().setItemChecked(position, true);
        }

        mActivatedPosition = position;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case NOTES_LOADER:
                NoteSelection noteSelection = new NoteSelection();
                noteSelection.syncStatus(SyncStatus.SYNCED, SyncStatus.TO_SYNC);
                String[] projection = NoteColumns.ALL_COLUMNS;
                return new CursorLoader(getActivity(), noteSelection.uri(), projection, noteSelection.sel(), noteSelection.args(), null);
            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mNoteAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mNoteAdapter.swapCursor(null);
    }
}
