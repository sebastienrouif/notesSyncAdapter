package org.rouif.notes.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import org.rouif.notes.R;
import org.rouif.notes.fragment.NoteDetailFragment;

public class NoteDetailActivity extends Activity {
    private static final String ARG_ITEM_ID = "item_id";

    public static void startActivity(long localId, Context context) {
        Intent detailIntent = new Intent(context, NoteDetailActivity.class);
        detailIntent.putExtra(ARG_ITEM_ID, localId);
        context.startActivity(detailIntent);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_detail);

        getActionBar().setDisplayHomeAsUpEnabled(true);

        if (savedInstanceState == null) {
            // Create the detail fragment and add it to the activity
            // using a fragment transaction.
            Bundle arguments = new Bundle();
            long localId = getIntent().getLongExtra(ARG_ITEM_ID, 0);
            NoteDetailFragment fragment = NoteDetailFragment.newInstance(localId);
            getFragmentManager().beginTransaction()
                    .add(R.id.note_detail_container, fragment)
                    .commit();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            navigateUpTo(new Intent(this, NoteListActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
