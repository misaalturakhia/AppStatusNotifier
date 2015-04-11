package com.appstatusnotifier;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/** A helper class that facilitates reading from and writing to the SQLite database which holds the
 * data about apps installing, updating and uninstalling
 * Created by Misaal on 10/04/2015.
 */
public class CustomDbHelper extends SQLiteOpenHelper {

    /**
     * Constructor
     * @param context : Context
     */
    public CustomDbHelper(Context context){
        super(context, DBContract.DB_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DBContract.QUERY_CREATE_LOG_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL(DBContract.QUERY_DROP_TABLE_LOGS);
        onCreate(db);
    }


    /** Gets a cursor object for a query that returns all the data from the table
     *
     * @return : Cursor
     */
    public Cursor getCursorForAll(){
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery(DBContract.QUERY_GET_ALL_DATA, null);
    }


    /** Adds a log entry to the database
     *
     * @param appName : the name of the app
     * @param statusCode : the code that signifies the change in status
     */
    public void addAppData(String packageName, String appName, int statusCode){
        SQLiteDatabase dbW = this.getWritableDatabase();
        ContentValues values = createRowContent(packageName, appName, statusCode);
        dbW.insert(DBContract.LOGS_TABLE_NAME, null, values);
        // now check if there are any rows that dont have an app name that have the same package name
        if(!appName.isEmpty()){
            writeAppNameToPreviousEntries(dbW, packageName, appName);
        }
        dbW.close();

    }

    /** Checks if there are any previous entries in the database with the same package name and with
     * app_name = ''. Updates these rows with the app name.
     *
     * @param db : writable database
     * @param packageName : String
     * @param appName : String
     */
    private void writeAppNameToPreviousEntries(SQLiteDatabase db, String packageName, String appName) {
        // SELECT * FROM TABLE WHERE PACKAGE_NAME = 'com.misaal.android' AND APP_NAME = '' (empty)
        Cursor cursor = db.rawQuery("SELECT * FROM " + DBContract.LOGS_TABLE_NAME
                + " WHERE " + DBContract.COLUMN_PACKAGE_NAME + " = '" + packageName + "' "
                + "AND " + DBContract.COLUMN_APP_NAME + " = ''", null);
        // if there are no such entries without app names
        if(!cursor.moveToFirst()){
            return;
        }

        while(!cursor.isAfterLast()){
            int id = cursor.getInt(cursor.getColumnIndexOrThrow(DBContract.COLUMN_ID));
            int statusCode = cursor.getInt(cursor.getColumnIndexOrThrow(DBContract.COLUMN_STATUS_CODE));
            ContentValues values = new ContentValues();
            values.put(DBContract.COLUMN_APP_NAME, appName);
            values.put(DBContract.COLUMN_PACKAGE_NAME, packageName);
            values.put(DBContract.COLUMN_STATUS_CODE, statusCode);
            db.update(DBContract.LOGS_TABLE_NAME, values, DBContract.COLUMN_ID+" = "+id, null);
        }
    }


    /** Reads the database to check if there is another entry with the same package name and if that
     * entry contains the app name
     *
     * @param packageName : package name of the application
     * @return : the app name if it exists in the database, or "" (empty string) if it isnt present
     */
    public String fetchAppName(String packageName){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = getRowsByPackage(db, packageName);
        if(!cursor.moveToFirst()){ // no result
            return "";
        }
        while(!cursor.isAfterLast()){ // traverse results
            // get value in app_name column
            String appName = cursor.getString(cursor.getColumnIndexOrThrow(DBContract.COLUMN_APP_NAME));
            if(!appName.contains("com.")){ // if package name is also stored in
                return appName;
            }
        }
        return "";

    }


    /** Returns a cursor object for the query
     * SELECT * FROM LOGS WHERE PACKAGE_NAME = 'com.misaal.android ... '
     * @param db : Readable Database
     * @param packageName : package name string
     * @return : Cursor
     */
    private Cursor getRowsByPackage(SQLiteDatabase db, String packageName){
        return db.rawQuery("SELECT * FROM " + DBContract.LOGS_TABLE_NAME
                + " WHERE " + DBContract.COLUMN_PACKAGE_NAME + " = '" + packageName + "'", null);
    }


    /**
     * Puts row data into a ContentValues object
     * @param packageName : the package name of the app
     * @param appName : the name of the app
     * @param statusCode : the status change
     * @return ContentValues
     */
    private ContentValues createRowContent(String packageName, String appName, int statusCode){
        ContentValues contentValues = new ContentValues();
        contentValues.put(DBContract.COLUMN_PACKAGE_NAME, packageName);
        contentValues.put(DBContract.COLUMN_APP_NAME, appName);
        contentValues.put(DBContract.COLUMN_STATUS_CODE, statusCode);
        return contentValues;
    }


    /**
     * A class that specifies the database contract
     */
    public abstract class DBContract {

        public static final String DB_NAME = "LogDb.db";
        // table name
        public static final String LOGS_TABLE_NAME = "logs";
        // column names
        public static final String COLUMN_ID = "_id";
        public static final String COLUMN_PACKAGE_NAME = "package_name";
        public static final String COLUMN_APP_NAME = "app_name";
        public static final String COLUMN_STATUS_CODE = "status_code";

        public static final String QUERY_CREATE_LOG_TABLE =
                "create table " + DBContract.LOGS_TABLE_NAME + " " +
                "(" + DBContract.COLUMN_ID + " integer primary key autoincrement, "
                + DBContract.COLUMN_PACKAGE_NAME + " text, "
                + DBContract.COLUMN_APP_NAME + " text, "
                + DBContract.COLUMN_STATUS_CODE + " integer)";

        public static final String QUERY_GET_ALL_DATA = "SELECT * FROM "+ DBContract.LOGS_TABLE_NAME;

        public static final String QUERY_DROP_TABLE_LOGS = "DROP TABLE IF EXISTS "+ DBContract.LOGS_TABLE_NAME;
    }
}
