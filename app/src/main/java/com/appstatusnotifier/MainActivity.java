package com.appstatusnotifier;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.widget.ListView;
import android.widget.TextView;

/**
 * Displays a list of logs of changes in app statuses (Installed, Updated, Uninstalled);
 */
public class MainActivity extends ActionBarActivity {

    private CustomCursorAdapter mAdapter;
    private CustomDbHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // create the database helper
        dbHelper = new CustomDbHelper(this);

        // initialize the list view, set the empty view to it and also set the cursor adapter to it
        ListView listView = (ListView)findViewById(R.id.app_data_list_view);
        TextView emptyView = (TextView)findViewById(R.id.list_empty_view);
        listView.setEmptyView(emptyView);

        mAdapter = new CustomCursorAdapter(this, dbHelper.getCursorForAll(), true);
        listView.setAdapter(mAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Cursor newCursor = dbHelper.getCursorForAll();
        mAdapter.changeCursor(newCursor);
        mAdapter.notifyDataSetChanged();
    }
}
