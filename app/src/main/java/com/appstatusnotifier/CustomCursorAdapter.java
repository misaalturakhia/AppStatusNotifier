package com.appstatusnotifier;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

/**
 * Created by Misaal on 10/04/2015.
 */
public class CustomCursorAdapter extends CursorAdapter {


    /**
     * Constructor
     * @param context : Context
     * @param cursor : Cursor
     * @param autoRequery : Boolean
     */
    public CustomCursorAdapter(Context context, Cursor cursor, boolean autoRequery) {
        super(context, cursor, autoRequery);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // textview that shows the serial no of the log/app
        TextView srNoTv = (TextView)view.findViewById(R.id.serial_no_tv);
        // textview that shows the name of the app in question
        TextView appNameTv = (TextView)view.findViewById(R.id.app_name_tv);
        // textview that displays the status of the app in the log
        TextView statusTv = (TextView)view.findViewById(R.id.status_tv);

        // get id of the log entry
        int id = cursor.getInt(cursor.getColumnIndexOrThrow(CustomDbHelper.DBContract.COLUMN_ID));
        srNoTv.setText(""+id);
        // get the app name and set it to the textview
        String appName = cursor.getString(cursor.getColumnIndexOrThrow(CustomDbHelper.DBContract.COLUMN_APP_NAME));
        if(appName.isEmpty()){
            // if there is no stored app name, display the package name
            String packageName = cursor.getString(cursor.getColumnIndexOrThrow(CustomDbHelper.DBContract.COLUMN_PACKAGE_NAME));
            // sets the package name if the app name was not retrievable
            appNameTv.setText(packageName);
        }else{
            appNameTv.setText(appName);
        }

        // get the status code from the database, convert it to a string form and set it to the textview
        int statusCode = cursor.getInt(cursor.getColumnIndexOrThrow(CustomDbHelper.DBContract.COLUMN_STATUS_CODE));
        String status = getStatusFromCode(statusCode);
        statusTv.setText(status);
    }


    /** Converts the status code retrieved from the database and converts it to the status string
     * required to be displayed
     *
     * @param statusCode : integer status code
     * @return : status String
     */
    private String getStatusFromCode(int statusCode) {
        switch(statusCode){
            case 0:
                return "Installed";
            case 1:
                return "Updated";
            case 2:
                return "Uninstalled";
        }
        return null;
    }
}
