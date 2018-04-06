package com.example.rishi.bandfinder;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //unpack the variables
        Bundle extras = getIntent().getExtras();
        final String email = extras.getString("email");

        //update profile from the DB
        getAndSetName(email);
        getAndSetUserInfo(email);

        //update
        TextView usernameString = (TextView) findViewById(R.id.username);
        usernameString.setText(email);

        //the Floating action button that allows users to communicate
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("plain/text");
                intent.putExtra(Intent.EXTRA_EMAIL, new String[] { email});
                intent.putExtra(Intent.EXTRA_SUBJECT, "Hey!");
                intent.putExtra(Intent.EXTRA_TEXT, "Write your message here");
                startActivity(Intent.createChooser(intent, ""));
            }
        });
    }

    private void getAndSetUserInfo(final String email) {
        StringRequest postRequest = new StringRequest(Request.Method.POST, AppConfig.URL_RETRIEVE_PROFILE,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response) {
                        //response
                        Log.d("Response", response);


                        try {
                            JSONObject obj = new JSONObject(response);
                            //finding the elements to change
                            TextView roleString = (TextView) findViewById(R.id.roleText);
                            TextView bioString = (TextView) findViewById(R.id.bioText);
                            ImageView profileImage = (ImageView) findViewById(R.id.profileImage);

                            String encodedString = obj.getString("profilePic");
                            //if a custom image is set, change it here
                            if (encodedString != "null") {
                                byte[] encodeByte = Base64.decode(encodedString, Base64.DEFAULT);
                                Bitmap bitmap = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
                                profileImage.setImageBitmap(bitmap);
                            }
                            //if a custom role is set here
                            if(obj.getString("role") == "null") {
                                roleString.setText("Musician");
                            }
                            else {
                                roleString.setText(obj.getString("role"));
                            }
                            //custo mbio
                            bioString.setText(obj.getString("bio"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // error
                        Log.d("Error.Response: POPCORN", error.toString());
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams()
            {
                //request assembler
                Map<String, String>  params = new HashMap<>();
                params.put("email",email);
                return params;
            }
        };
        AppController.getInstance().addToRequestQueue(postRequest,"req_gps");
    }


//retrieves name from Db and sets it
    private void getAndSetName(final String email) {
        StringRequest postRequest = new StringRequest(Request.Method.POST, AppConfig.URL_GET_NAME,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response) {                        // response
                        Log.d("Response:", response);
                        try {
                            JSONObject obj = new JSONObject(response);

                            TextView usernameString = (TextView) findViewById(R.id.username);
                            usernameString.setText(obj.getString("name"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // error
                        Log.d("Error.Response:", error.toString());
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String>  params = new HashMap<>();
                params.put("email",email);
                return params;
            }
        };
        AppController.getInstance().addToRequestQueue(postRequest,"req_gps");
    }
}
