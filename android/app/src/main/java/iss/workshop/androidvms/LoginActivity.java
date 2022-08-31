package iss.workshop.androidvms;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.button.MaterialButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import Classes.MySingleton;

public class LoginActivity extends AppCompatActivity {

    Button btnLogin;
    RequestQueue mQueue;
    EditText username;
    EditText password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String token = preferences.getString("token", "");
        if (token.equals("")) {
            setContentView(R.layout.activity_login);

            username = findViewById(R.id.username);
            password = findViewById(R.id.password);

            mQueue = Volley.newRequestQueue(this);
            btnLogin = findViewById(R.id.btnLogin);
            btnLogin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    {
                        postData();
                    }

                }
            });
        } else {
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
        }
    }

    private void postData() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("username", username.getText().toString());
            jsonObject.put("password", password.getText().toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, "http://10.0.2.2:8080/api/student/login", jsonObject, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                String responseString = "";
                String studentIdString = "";
                String studentNameString = "";
                String studentScore = "";
                String groupSize = "";
                try {
                    responseString = response.getString("response");
                    studentIdString = response.getString("studentId");
                    studentNameString = response.getString("studentName");
                    studentScore = response.getString("studentScore");
                    groupSize = response.getString("groupSize");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (responseString.equalsIgnoreCase("invalid login")) {
                    Toast.makeText(LoginActivity.this, "Invalid Login", Toast.LENGTH_LONG).show();
                } else {
                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(LoginActivity.this);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("token", responseString);
                    editor.putString("studentId", studentIdString);
                    editor.putString("studentName", studentNameString);
                    editor.putString("studentScore", studentScore);
                    editor.putString("groupSize", groupSize);
                    editor.apply();
                    Toast.makeText(LoginActivity.this, "Log in successful", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                Toast.makeText(LoginActivity.this, error.getLocalizedMessage(), Toast.LENGTH_LONG).show();

            }
        });
        MySingleton.getInstance(LoginActivity.this).addToRequestQueue(request);
    }
}