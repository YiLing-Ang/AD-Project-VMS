package iss.workshop.androidvms;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
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

public class ViewBookingActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    RequestQueue mQueue;
    private List<String> roomName = new ArrayList<String>();
    private List<String> timeslot = new ArrayList<String>();
    private List<String> status = new ArrayList<String>();
    private List<String> roomId = new ArrayList<String>();
    private List<String> bookingId = new ArrayList<String>();
    private List<String> date = new ArrayList<String>();
    private List<String> duration = new ArrayList<String>();
    private List<String> checkedIn = new ArrayList<String>();
    private List<String> bookingInProgress = new ArrayList<String>();
    JSONArray jsonArray = new JSONArray();

    //Initialize variable
    DrawerLayout drawerLayout;
    ImageView btnMenu;
    RecyclerView recyclerView;
    ImageView btnGuide;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mQueue = Volley.newRequestQueue(this);
        postDataList();

        setContentView(R.layout.activity_view_booking);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        TextView student_name = findViewById(R.id.student_name);
        student_name.setText(preferences.getString("studentName", ""));
        TextView student_score = findViewById(R.id.student_score);
        student_score.setText(preferences.getString("studentScore", ""));

        //assign variable
        drawerLayout = findViewById(R.id.drawer_layout);
        btnMenu = findViewById(R.id.btnMenu);
        recyclerView = findViewById(R.id.recycler_View);
        btnGuide = findViewById(R.id.btnGuide);

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
        btnGuide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog dialog = new AlertDialog.Builder(ViewBookingActivity.this)
                        .setIcon(android.R.drawable.btn_star)// to be replaced
                        .setTitle("Page Actions")
                        .setMessage("Tap booking to view more details.\nTap and hold to cancel the booking.")
                        .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                            }
                        }).create();
                dialog.show();
            }
        });
    }


    @Override
    protected void onPause() {
        super.onPause();
        //Close drawer
        MainActivity.closeDrawer(drawerLayout);
    }


    @Override
    public void onItemClick(AdapterView<?> av,
                            View v, int pos, long id) {


        AlertDialog dialog = new AlertDialog.Builder(ViewBookingActivity.this)
                .setIcon(android.R.drawable.btn_star)// to be replaced
                .setTitle("Booking Details")
                .setMessage("Booking ID:       " + bookingId.get(pos) + "\nRoom ID:           " + roomId.get(pos) + "\nRoom Name:    " + roomName.get(pos)
                        + "\nDate/Time:       " + date.get(pos) + " / " + timeslot.get(pos) + "\nDuration:           " + duration.get(pos)
                        + "\nStatus:               " + status.get(pos) + "\nChecked In:       " + checkedIn.get(pos)
                )
                .setPositiveButton("Close", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                })
                .create();
        dialog.show();

        int width = getWindowManager().getDefaultDisplay().getWidth();
        WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
        params.width = width;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        params.gravity = Gravity.CENTER;

    }

    private void postDataCancel(int pos) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("bookingId", bookingId.get(pos));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, "http://10.0.2.2:8080/api/student/booking/cancel/" + preferences.getString("token", ""), jsonObject, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                String responseString = "";
                try {
                    responseString = response.getString("response");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Toast.makeText(ViewBookingActivity.this, responseString, Toast.LENGTH_LONG).show();
                if (responseString.equalsIgnoreCase("COMPLETED")) {
                    Intent intent = new Intent(ViewBookingActivity.this, ViewBookingActivity.class);
                    finish();
                    startActivity(intent);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(ViewBookingActivity.this, error.getLocalizedMessage(), Toast.LENGTH_LONG).show();
            }
        });
        MySingleton.getInstance(ViewBookingActivity.this).addToRequestQueue(request);
    }

    private void postDataList() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("studentId", preferences.getString("studentId", ""));
            jsonArray.put(jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.POST, "http://10.0.2.2:8080/api/student/booking/history/" + preferences.getString("token", ""), jsonArray, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {

                try {
                    for (int i = 0; i < response.length(); i++) {
                        roomName.add(response.getJSONObject(i).getJSONObject("room").getString("roomName"));
                        timeslot.add(response.getJSONObject(i).getString("time"));
                        status.add(response.getJSONObject(i).getString("status"));
                        roomId.add(response.getJSONObject(i).getJSONObject("room").getString("id"));
                        bookingId.add(response.getJSONObject(i).getString("id"));
                        date.add(response.getJSONObject(i).getString("date"));
                        duration.add(response.getJSONObject(i).getString("duration"));
                        checkedIn.add(response.getJSONObject(i).getString("checkedIn"));
                        bookingInProgress.add(response.getJSONObject(i).getString("bookingInProgress"));

                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (roomName.size() != 0) {
                    BookingAdapter adapter = new BookingAdapter(ViewBookingActivity.this, 0);
                    adapter.setData(roomName, timeslot, status, roomId, date);
                    ListView listView = findViewById(R.id.listView);


                    if (listView != null) {
                        listView.setAdapter(adapter);
                        listView.setOnItemClickListener(ViewBookingActivity.this);
                    }

                    listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                        @Override
                        public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long l) {
                            new AlertDialog.Builder(ViewBookingActivity.this)
                                    .setIcon(android.R.drawable.ic_delete)
                                    .setTitle("Cancel Booking")
                                    .setMessage("Please confirm you would like to cancel this booking")
                                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            postDataCancel(position);
                                        }
                                    })
                                    .setNegativeButton("No", null).show();

                            return true;
                        }
                    });
                }
                else {
                    TextView blankViewText = findViewById(R.id.blankViewText);
                    blankViewText.setVisibility(View.VISIBLE);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                Toast.makeText(ViewBookingActivity.this, error.getLocalizedMessage(), Toast.LENGTH_LONG).show();

            }
        });
        MySingleton.getInstance(ViewBookingActivity.this).addToRequestQueue(request);
    }

}