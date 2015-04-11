package com.appstatusnotifier;

import android.app.IntentService;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

/** An Intent Service that receives the intent from AppDataReceiver and stores the log entry in the
 * sqlite database
 * Created by Misaal on 11/04/2015.
 */
public class AppDataService extends IntentService {

    private static final String SERVICE_NAME = AppDataService.class.getSimpleName();
    private static final String ACTION_INSTALLED = "android.intent.action.PACKAGE_ADDED";
    private static final String ACTION_UPDATED = "android.intent.action.PACKAGE_CHANGED";
    private static final String ACTION_ALT_UPDATED = "android.intent.action.PACKAGE_REPLACED";
    private static final String ACTION_UNINSTALLED = "android.intent.action.PACKAGE_REMOVED";


    public AppDataService(){
        super(SERVICE_NAME);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        CustomDbHelper helper = new CustomDbHelper(getApplicationContext());
        String action = intent.getAction();
        // gets package name of the form "package:com.misaal.android"
        String packageName = intent.getData().toString();
        // strip the "package:"
        packageName = packageName.replace("package:", "");
        // get app name from the packagemanager using the packagename
        String appName = getAppName(packageName);
        // in the case of app uninstalling, cannot retrieve the app name from the package
        if(appName.isEmpty()){
            // try to fetch an app name that matches to the package name from the database
            // should work if it contains an entry for the app installing or updating
            appName = helper.fetchAppName(packageName);
        }
        // from the action string, a status code is generated (0-install, 1-update, 2-uninstall)
        int statusCode = getStatusCode(action);
        // store the entry in the sqlite database
        helper.addAppData(packageName, appName, statusCode);
    }


    /**
     * Creates a status code based on the broadcast action.
     * Status codes are stored instead of status strings to reduce database space consumption.
     *
     * @param action : String eg:- android.intent.action.PACKAGE_ADDED
     * @return : 0 or 1 or 2 or -1 in case of invalid input string
     */
    private int getStatusCode(String action) {
        if(action.equals(ACTION_INSTALLED)){
            return 0;
        }else if(action.equals(ACTION_UPDATED)){
            return 1;
        }else if(action.equals(ACTION_ALT_UPDATED)){
            return 1;
        } else if(action.equals(ACTION_UNINSTALLED)){
            return 2;
        }
        return -1;
    }


    /**
     * Tries to get the app name from the package manager. This is not possible in the case of an
     * uninstalled app and so it will return an empty string
     * @param packageName : the package name of the application
     * @return : app name if possible, empty string otherwise
     */
    private String getAppName(String packageName) {
        final PackageManager pm = getPackageManager();
        ApplicationInfo ai;
        try {
            ai = pm.getApplicationInfo( packageName, 0);
        } catch (final PackageManager.NameNotFoundException e) {
            // ai is null when the package name does not match the list held in the package manager
            ai = null;
        }
        return  (String) (ai != null ? pm.getApplicationLabel(ai) : "");
    }
}
