package org.rouif.notes.activity;

import android.app.Activity;
import android.os.Bundle;

import org.rouif.notes.R;
import org.rouif.notes.fragment.NoteDetailFragment;
import org.rouif.notes.fragment.NoteListFragment;
import org.rouif.notes.sync.SyncUtils;


public class NoteListActivity extends Activity
        implements NoteListFragment.Callbacks {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mIsTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_list);
        SyncUtils.createSyncAccount(this);

        if (findViewById(R.id.note_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-large and
            // res/values-sw600dp). If this view is present, then the
            // activity should be in two-pane mode.
            mIsTwoPane = true;

            // In two-pane mode, list items should be given the
            // 'activated' state when touched.
            ((NoteListFragment) getFragmentManager()
                    .findFragmentById(R.id.note_list))
                    .setActivateOnItemClick(true);
        }

        // TODO: If exposing deep links into your app, handle intents here.
    }

    /**
     * Callback method from {@link NoteListFragment.Callbacks}
     * indicating that the item with the given ID was selected.
     */
    @Override
    public void onItemSelected(Long severId, Long localId) {
        if (mIsTwoPane) {
            NoteDetailFragment fragment = NoteDetailFragment.newInstance(localId);
            getFragmentManager().beginTransaction()
                    .replace(R.id.note_detail_container, fragment)
                    .commit();
        } else {
            NoteDetailActivity.startActivity(localId, this);
        }
    }
}
