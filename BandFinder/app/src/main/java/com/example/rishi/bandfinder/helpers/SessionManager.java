package com.example.rishi.bandfinder.helpers;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.Map;

/**
 * Created by Rishi on 22/11/2017.
 */

//used to store shared global variables across sessions and activities
public class SessionManager {
    // Shared Preferences
    private SharedPreferences pref;

    // LogCat tag
    private static String TAG = SessionManager.class.getSimpleName();

    private SharedPreferences.Editor editor;
    private Context _context;

    // Shared pref mode
    private int PRIVATE_MODE = 0;

    // Shared preferences file name
    private static final String PREF_NAME = "AndroidLogin";

    private static final String KEY_IS_LOGGEDIN = "isLoggedIn";

    private static final String EMAIL = "";

    public SessionManager(Context context) {
        this._context = context;
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }

    //set the login and email
    public void setLogin(boolean isLoggedIn, String email) {

        editor.putBoolean(KEY_IS_LOGGEDIN, isLoggedIn);
        editor.putString(EMAIL,email);

        // commit changes
        editor.commit();

        Log.d(TAG, "User login session modified!");
    }

    public String getEmail() {
        return pref.getString(EMAIL,"no email found");
    }

    //returns if user is already logged in
    public boolean isLoggedIn(){
        return pref.getBoolean(KEY_IS_LOGGEDIN, false);
    }
}