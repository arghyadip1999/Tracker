package com.example.anew;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class Main2Activity extends AppCompatActivity {

    private EditText editTextPhone, editTextPassword;
    private Button loginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        editTextPhone = findViewById(R.id.phoneNumberLogIn);
        editTextPassword = findViewById(R.id.passwordLogIn);
        loginButton = findViewById(R.id.logInButton);


    }
    public void openProfileActivity(View view){
        loginButton.setEnabled(false);
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Logging in...");
        progressDialog.show();
        progressDialog.setCanceledOnTouchOutside(false);
        final String phone = editTextPhone.getText().toString().trim();
        final String password = editTextPassword.getText().toString().trim();

        //Creating a string request
        StringRequest stringRequest = new StringRequest(Request.Method.POST, Config.LOGIN_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        //If we are getting success from server
                        if(!response.equalsIgnoreCase(Config.LOGIN_FAILURE)){
                            //Creating a shared preference
                            String res = "";
                            try {
                                JSONObject object = new JSONObject(response);
                                JSONArray array = object.getJSONArray("users");
                                res = Integer.toString(array.getJSONObject(0).getInt("id"));
                                Log.d("Id", res);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            SharedPreferences sharedPreferences = Main2Activity.this.getSharedPreferences(Config.SHARED_PREF_NAME, Context.MODE_PRIVATE);

                            //Creating editor to store values to shared preferences
                            SharedPreferences.Editor editor = sharedPreferences.edit();

                            //Adding values to editor
                            editor.putBoolean(Config.LOGGEDIN_SHARED_PREF, true);
                            editor.putString(Config.ID, res);
                            editor.putString(Config.KEY_PHONE, phone);

                            //Saving values to editor
                            editor.commit();
                            progressDialog.dismiss();



                            //Starting profile activity

                            Intent intent = new Intent(Main2Activity.this, ProfileActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);


                        }else{
                            //If the server response is not success
                            //Displaying an error message on toast
                            //Toast.makeText(LoginActivity.this, "Invalid username or password", Toast.LENGTH_LONG).show();
                            progressDialog.dismiss();
                            AlertDialog.Builder builder = new AlertDialog.Builder(Main2Activity.this);
                            builder.setMessage("Invalid username or password")
                                    .setTitle(R.string.login_error_title)
                                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            loginButton.setEnabled(true);
                                            dialog.dismiss();
                                        }
                                    });
                            AlertDialog dialog = builder.create();
                            dialog.show();
                            dialog.setCanceledOnTouchOutside(false);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //You can handle error here if you want
                        error.printStackTrace();
                        progressDialog.dismiss();
                        AlertDialog.Builder builder = new AlertDialog.Builder(Main2Activity.this);
                        builder.setMessage("Server error")
                                .setTitle(R.string.login_error_title)
                                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        loginButton.setEnabled(true);
                                        dialog.dismiss();
                                    }
                                });
                        AlertDialog dialog = builder.create();
                        dialog.show();
                        dialog.setCanceledOnTouchOutside(false);

                    }
                }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> params = new HashMap<>();
                //Adding parameters to request
                params.put(Config.KEY_PHONE, "+91"+phone);
                params.put(Config.KEY_PASSWORD, password);


                //returning parameter
                return params;
            }
        };

        //Adding the string request to the queue
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);

    }

    @Override
    protected void onResume() {
        super.onResume();
        loginButton.setEnabled(true);
    }
}
