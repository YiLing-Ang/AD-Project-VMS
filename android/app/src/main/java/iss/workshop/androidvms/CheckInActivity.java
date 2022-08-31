package iss.workshop.androidvms;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;
import com.journeyapps.barcodescanner.camera.CameraSettings;

import org.json.JSONException;
import org.json.JSONObject;

import Classes.MySingleton;

public class CheckInActivity extends AppCompatActivity implements View.OnClickListener {
    Button btnCheckIn;
    RequestQueue mQueue;

    //Initialize variable
    DrawerLayout drawerLayout;
    ImageView btnMenu;
    RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String currentbooking = preferences.getString("currentbooking", "");
        if (currentbooking.equalsIgnoreCase("true")) {
            setContentView(R.layout.activity_check_in);

            btnCheckIn = findViewById(R.id.btnCheckIn);
            btnCheckIn.setOnClickListener(this);

            //assign variable
            drawerLayout = findViewById(R.id.drawer_layout);
            btnMenu = findViewById(R.id.btnMenu);
            recyclerView = findViewById(R.id.recycler_View);

            //Set layout manager
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            //Set adapter
            recyclerView.setAdapter(new NavAdapter(this, MainActivity.arrayList, this));
            btnMenu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //Open drawer
                    drawerLayout.openDrawer(GravityCompat.START);
                }
            });

            TextView student_name = findViewById(R.id.student_name);
            student_name.setText(preferences.getString("studentName", ""));
            TextView student_score = findViewById(R.id.student_score);
            student_score.setText(preferences.getString("studentScore", ""));
            mQueue = Volley.newRequestQueue(this);
            new AlertDialog.Builder(CheckInActivity.this)
                    .setTitle("Check-in Acknowledgement")
                    .setMessage("I acknowledge that the room is in an acceptable condition before checking in.\nIf it is not, I must make a report before checking in.")
                    .setPositiveButton("Continue", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            scanCode();
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Intent intent = new Intent(CheckInActivity.this, MainActivity.class);
                            finish();
                            startActivity(intent);
                            overridePendingTransition(0, 0);
                        }
                    }).show();
        } else {
            Toast.makeText(CheckInActivity.this, "No current booking", Toast.LENGTH_LONG).show();
            finish();
            Intent intent = new Intent(CheckInActivity.this, MainActivity.class);
            startActivity(intent);
            overridePendingTransition(0, 0);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        //Close drawer
        MainActivity.closeDrawer(drawerLayout);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();

        if (id == R.id.btnCheckIn) {
            new AlertDialog.Builder(CheckInActivity.this)
                    .setTitle("Check-in Acknowledgement")
                    .setMessage("I acknowledge that the room is in an acceptable condition before checking in.\nIf it is not, I must make a report before checking in.")
                    .setPositiveButton("Continue", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            scanCode();
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Intent intent = new Intent(CheckInActivity.this, MainActivity.class);
                            finish();
                            startActivity(intent);
                            overridePendingTransition(0, 0);
                        }
                    }).show();
        }

    }

    private void scanCode() {
        ScanOptions options = new ScanOptions();
        options.setPrompt("Volume up to flash on");
        options.setBeepEnabled(true);
        options.setOrientationLocked(true);
        options.setCaptureActivity(QRActivity.class);
        barLauncher.launch(options);
    }

    ActivityResultLauncher<ScanOptions> barLauncher = registerForActivityResult(new ScanContract(), result -> {
        if (result.getContents() != null) {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("studentId", preferences.getString("studentId", ""));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, Uri.parse(result.getContents()) + "/" + preferences.getString("token", ""), jsonObject, new Response.Listener<JSONObject>() {

                @Override
                public void onResponse(JSONObject response) {
                    String responseString = "";
                    try {
                        responseString = response.getString("response");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    if (responseString.equalsIgnoreCase("Checked in successfully")) {
                        Toast.makeText(CheckInActivity.this, "Checked in successfully", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(CheckInActivity.this, responseString, Toast.LENGTH_LONG).show();
                    }
                    Intent intent = new Intent(CheckInActivity.this, MainActivity.class);
                    finish();
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(CheckInActivity.this, error.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                }
            });
            MySingleton.getInstance(CheckInActivity.this).addToRequestQueue(request);
        }
    });
}