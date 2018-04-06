package com.example.rishi.bandfinder;


import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.example.rishi.bandfinder.helpers.SessionManager;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }

    public void  goToMaps(View view){
        Intent intent = new Intent(MainActivity.this, MapsActivity.class);
        startActivity(intent);
        finish();
    }

    public void goToSettings(View view){
        SessionManager session = new SessionManager(getApplicationContext());
        Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
        //pass some variables along
        intent.putExtra("email",session.getEmail());
        startActivity(intent);
    }

    public void goToWearables(View view){
        //test
    }

    public void shareOnFacebook(View view) {
        //test
    }

    public void logout(View view) {
        SessionManager session = new SessionManager(getApplicationContext());
        session.setLogin(false,"");
        //remove the login and boot to login screen
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }


}