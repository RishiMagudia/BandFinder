package com.example.rishi.bandfinder;


import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.provider.MediaStore;

import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

public class SettingsActivity extends AppCompatActivity {

    String bioStringG;
    String roleStringG;
    String emailG;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Bundle extras = getIntent().getExtras();
        final String email = extras.getString("email");

        emailG = email;

        getAndSetName(email);
        getAndSetUserInfo(email);

        TextView usernameString = (TextView) findViewById(R.id.username);
        usernameString.setText(email);

        Toast.makeText(getApplicationContext(), "Click on the component to edit it!.",
                Toast.LENGTH_LONG).show();

    }

    private void getAndSetUserInfo(final String email) {
        StringRequest postRequest = new StringRequest(Request.Method.POST, AppConfig.URL_RETRIEVE_PROFILE,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response) {
                        //response
                        Log.d("Response:POPCORN", response);


                        try {
                            JSONObject obj = new JSONObject(response);

                            TextView roleString = (TextView) findViewById(R.id.roleText);
                            TextView bioString = (TextView) findViewById(R.id.bioText);
                            ImageView profileImage = (ImageView) findViewById(R.id.profileImage);

                            String encodedString = obj.getString("profilePic");

                            if (encodedString != "null") {
                                byte[] encodeByte = Base64.decode(encodedString, Base64.DEFAULT);
                                Bitmap bitmap = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
                                profileImage.setImageBitmap(bitmap);
                            }
                            if(obj.getString("role") == "null") {
                                roleString.setText("Musician");
                                roleStringG = "Musician";
                            }
                            else {
                                roleString.setText(obj.getString("role"));
                                roleStringG = "Musician";

                            }
                            bioString.setText(obj.getString("bio"));
                            bioStringG = obj.getString("bio");
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
                Map<String, String>  params = new HashMap<>();
                params.put("email",email);
                return params;
            }
        };
        AppController.getInstance().addToRequestQueue(postRequest,"req_gps");
    }



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

    static final int REQUEST_IMAGE_CAPTURE = 1;

    public void editImage(View view) {
        Log.d("popcorn","Image clicked");
        //asking for permission
        ActivityCompat.requestPermissions(SettingsActivity.this,new String[]{Manifest.permission.CAMERA},1);
        ActivityCompat.requestPermissions(SettingsActivity.this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }



    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Log.d("popcorn","image recieved!!!");
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            ImageView profileImage = (ImageView) findViewById(R.id.profileImage);
            profileImage.setImageBitmap(imageBitmap);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            imageBitmap.compress(Bitmap.CompressFormat.PNG,100,bos);
            byte [] b=bos.toByteArray();
            String encodedImage=Base64.encodeToString(b, Base64.DEFAULT);
            sendImageToServer(emailG,encodedImage);

        }
    }

    private void sendImageToServer(final String email, final String image) {
        StringRequest postRequest = new StringRequest(Request.Method.POST, AppConfig.URL_UPDATE_IMAGE,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response) {
                        // response
                        Log.d("Response popcorn: ", response);
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // error
                        Log.d("Error.popcorn:", error.toString());
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String>  params = new HashMap<>();
                params.put("email",email );
                params.put("image",image);
                return params;
            }
        };
        AppController.getInstance().addToRequestQueue(postRequest,"update_image");
    }



    public void editBio(View view) {
        Log.d("popcorn","bio clicked");
        Bundle bundle = new Bundle();
        bundle.putInt("editing",0);
        bundle.putString("bio",bioStringG);
        bundle.putString("role",roleStringG);
        DialogFragment newFragment = new textEditFragment();
        newFragment.setArguments(bundle);
        newFragment.show(getSupportFragmentManager(),"");

    }


    public void editRole(View view) {
        Log.d("popcorn","role clicked");
        Bundle bundle = new Bundle();
        bundle.putInt("editing",1);
        bundle.putString("email",emailG);
        bundle.putString("bio",bioStringG);
        bundle.putString("role",roleStringG);
        DialogFragment newFragment = new textEditFragment();
        newFragment.setArguments(bundle);
        newFragment.show(getSupportFragmentManager(),"");


    }


}
