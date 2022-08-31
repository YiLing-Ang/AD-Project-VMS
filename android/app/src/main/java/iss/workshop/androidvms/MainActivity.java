package iss.workshop.androidvms;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import Classes.MySingleton;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    Button btnCreateBooking;
    Button btnViewBooking;
    Button btnCheckIn;
    Button btnTimeExtension;
    TextView bookingHeader;
    TextView roomValue;
    TextView dateTimeValue;
    TextView durationValue;
    TextView checkedInValue;
    RequestQueue mQueue;
    JSONArray jsonArray = new JSONArray();

    //Initialize variable
    DrawerLayout drawerLayout;
    ImageView btnMenu;
    RecyclerView recyclerView;
    static ArrayList<String> arrayList = new ArrayList<>();
    NavAdapter adapter;

    List<String> roomName = new ArrayList<String>();
    List<List<String>> facilities = new ArrayList<List<String>>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //initialise buttons
        btnCreateBooking = findViewById(R.id.btnCreateBooking);
        btnCreateBooking.setOnClickListener(this);
        btnViewBooking = findViewById(R.id.btnViewBooking);
        btnViewBooking.setOnClickListener(this);
        btnCheckIn = findViewById(R.id.btnCheckIn);
        btnCheckIn.setOnClickListener(this);
        btnTimeExtension = findViewById(R.id.btnTimeExtension);
        btnTimeExtension.setOnClickListener(this);
        bookingHeader = findViewById(R.id.bookingHeader);
        roomValue = findViewById(R.id.bookingRoomValue);
        dateTimeValue = findViewById(R.id.bookingDateTimeValue);
        durationValue = findViewById(R.id.bookingDurationValue);
        checkedInValue = findViewById(R.id.bookingCheckInImage);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        TextView student_name = findViewById(R.id.student_name);
        student_name.setText(preferences.getString("studentName", ""));
        TextView student_score = findViewById(R.id.student_score);
        student_score.setText(preferences.getString("studentScore", ""));

        mQueue = Volley.newRequestQueue(this);

        //assign variable
        drawerLayout = findViewById(R.id.drawer_layout);
        btnMenu = findViewById(R.id.btnMenu);
        recyclerView = findViewById(R.id.recycler_View);

        //clear array list
        arrayList.clear();

        //add menu item in array list
        arrayList.add("Home");
        arrayList.add("Create Booking");
        arrayList.add("Booking History");
        arrayList.add("Check In");
        arrayList.add("File a Report");
        arrayList.add("Log Out");

        //initialize adapter
        adapter = new NavAdapter(this, arrayList, this);
        //Set layout manager
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        //set adapter
        recyclerView.setAdapter(adapter);

        btnMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Open drawer
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        postData("nearest");
        postDataRooms();
    }

    public static void closeDrawer(DrawerLayout drawerLayout) {
        //check condition
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            //When drawer is open, close drawer
            drawerLayout.closeDrawer(GravityCompat.START);
        }
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();

        if (id == R.id.btnCreateBooking) {
            Intent intent = new Intent(this, CreateBookingActivity.class);
            startActivity(intent);
        }

        if (id == R.id.btnViewBooking) {
            Intent intent = new Intent(this, ViewBookingActivity.class);
            startActivity(intent);
        }

        if (id == R.id.btnCheckIn) {
            Intent intent = new Intent(this, CheckInActivity.class);
            startActivity(intent);
        }
        if (id == R.id.btnTimeExtension) {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
            if (preferences.getString("currentbooking", "").equalsIgnoreCase("true")) {
                new AlertDialog.Builder(MainActivity.this)
                        .setIcon(R.drawable.ic_time_24_dark)
                        .setTitle("Extension application?")
                        .setMessage("Extend current booking by 1 hour?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                postData("extend");
                            }
                        })
                        .setNegativeButton("No", null).show();
            } else
                Toast.makeText(MainActivity.this, "No current booking", Toast.LENGTH_LONG).show();
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        //close drawer
        closeDrawer(drawerLayout);
    }


    private void postData(String urlSuffix) {
        JSONObject jsonObject = new JSONObject();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        try {
            jsonObject.put("studentId", preferences.getString("studentId", ""));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, "http://10.0.2.2:8080/api/student/booking/" + urlSuffix + "/" + preferences.getString("token", ""), jsonObject, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                String responseString = "";
                try {
                    responseString = response.getString("response");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (!responseString.equalsIgnoreCase("NULL") || responseString.equalsIgnoreCase("FOUND")) {
                    if (!responseString.equalsIgnoreCase("FOUND")) {
                        Toast.makeText(MainActivity.this, responseString, Toast.LENGTH_LONG).show();
                        finish();
                        startActivity(getIntent());
                        overridePendingTransition(0, 0);
                    } else
                    {
                        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                        SharedPreferences.Editor editor = preferences.edit();
                        checkedInValue.setText("");
                        editor.putString("currentbooking", "");
                        editor.apply();
                        try {
                            if (response.getString("inprogress").equalsIgnoreCase("true")) {
                                bookingHeader.setText("Booking In Progress");
                                editor.putString("currentbooking", "true");
                                editor.apply();
                            } else {
                                bookingHeader.setText("Upcoming Booking");
                            }
                            roomValue.setText(response.getString("roomName"));
                            dateTimeValue.setText(response.getString("date") + " / " + response.getString("time"));
                            durationValue.setText(response.getString("duration") + " minutes");
                            if (response.getString("checkin").equalsIgnoreCase("true")) {
                                checkedInValue.setCompoundDrawablesWithIntrinsicBounds(
                                        R.drawable.ic_baseline_radio_button_checked_24, 0, 0, 0);
                            } else {
                                checkedInValue.setCompoundDrawablesWithIntrinsicBounds(
                                        R.drawable.ic_baseline_radio_button_unchecked_24, 0, 0, 0);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MainActivity.this, error.getLocalizedMessage(), Toast.LENGTH_LONG).show();
            }
        });
        MySingleton.getInstance(MainActivity.this).addToRequestQueue(request);
    }

    private void postDataRooms() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("studentId", preferences.getString("studentId", ""));
            jsonArray.put(jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.POST, "http://10.0.2.2:8080/api/student/rooms/" + preferences.getString("token", ""), jsonArray, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                try {
                    for (int i = 0; i < response.length(); i++) {
                        roomName.add(response.getJSONObject(i).getString("roomName"));
                        List<String> facilities1 = new ArrayList<String>();
                        JSONArray jsonArray = response.getJSONObject(i).getJSONArray("facilities");
                        for (int k = 0; k < jsonArray.length(); k++) {
                            facilities1.add(jsonArray.getJSONObject(k).getString("name"));
                        }
                        facilities.add(facilities1);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (roomName.size() != 0) {
                    RoomAvailabilityAdapter adapter = new RoomAvailabilityAdapter(MainActivity.this, 0);
                    adapter.setData(roomName, facilities);

                    ListView listView = findViewById(R.id.listViewMain);
                    if (listView != null) {
                        listView.setAdapter(adapter);
                    }

                    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                        }
                    });
                } else {
                    TextView blankViewText = findViewById(R.id.blankViewText);
                    blankViewText.setVisibility(View.VISIBLE);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                Toast.makeText(MainActivity.this, error.getLocalizedMessage(), Toast.LENGTH_LONG).show();

            }
        });
        MySingleton.getInstance(MainActivity.this).addToRequestQueue(request);
    }
}