package com.example.rishi.bandfinder;


import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.audiofx.BassBoost;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.example.rishi.bandfinder.helpers.SessionManager;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Rishi on 12/12/2017.
 */

public class textEditFragment extends DialogFragment {

    //variable to indicated what part of the profile we are editing
    boolean editingBio;

    String bioString;
    String roleString;
    String email;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(),R.style.AppCompatAlertDialogStyle);
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View content =  inflater.inflate(R.layout.texteditfragment, null);

        builder.setView(content)
                // Add action buttons
                .setPositiveButton("Update", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        EditText editText = (EditText) content.findViewById(R.id.textInput);
                        if (editingBio){
                            updateServerWithProfile(email,roleString,editText.getText().toString());
                        }
                        if (!editingBio){
                            updateServerWithProfile(email,editText.getText().toString(),bioString);
                        }


                        Intent intent = new Intent(getActivity(), MainActivity.class);
                        startActivity(intent);

                    }
                });

        //change the variables here
        EditText editText = (EditText) content.findViewById(R.id.textInput);
        Bundle bundle = this.getArguments();


        //reading values from the bundle
        int editing = bundle.getInt("editing");
        bioString = bundle.getString("bio");
        roleString = bundle.getString("role");
        email = bundle.getString("email");


        editingBio = editing != 1;

        String profileText;


        if (!editingBio){
            //editing role
            profileText = bundle.getString("role");
            editText.setText(profileText);
        }


        if (editingBio){
            //editing bio
            profileText = bundle.getString("bio");
            editText.setText(profileText);
        }

        // Create the AlertDialog object and return
        return builder.create();

    }

    private void updateServerWithProfile(final String email, final String role, final String bio) {
        StringRequest postRequest = new StringRequest(Request.Method.POST, AppConfig.URL_UPDATE_PROFILE,
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
                params.put("email", email );
                params.put("role", role );
                params.put("bio",bio);
                return params;
            }
        };
        AppController.getInstance().addToRequestQueue(postRequest,"update_profile");
    }


}