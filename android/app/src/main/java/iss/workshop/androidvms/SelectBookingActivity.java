package iss.workshop.androidvms;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
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
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import Classes.MySingleton;

public class SelectBookingActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    RequestQueue mQueue;
    private List<String> roomName = new ArrayList<String>();
    private List<String> date = new ArrayList<String>();
    private List<String> time = new ArrayList<String>();
    private List<String> duration = new ArrayList<String>();
    private List<String> roomId = new ArrayList<String>();
    JSONArray jsonArray = new JSONArray();

    //Initialize variable
    DrawerLayout drawerLayout;
    ImageView btnMenu;
    RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mQueue = Volley.newRequestQueue(this);
        postData("booking/options", 0);

        setContentView(R.layout.activity_select_booking);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        TextView student_name = findViewById(R.id.student_name);
        student_name.setText(preferences.getString("studentName", ""));
        TextView student_score = findViewById(R.id.student_score);
        student_score.setText(preferences.getString("studentScore", ""));

        Toast.makeText(this, "In view booking select activity", Toast.LENGTH_SHORT).show();

        //assign variable
        drawerLayout = findViewById(R.id.drawer_layout);
        btnMenu = findViewById(R.id.btnMenu);
        recyclerView = findViewById(R.id.recycler_View);

        btnMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Open drawer
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        //Set layout manager
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        //Set adapter
        recyclerView.setAdapter(new NavAdapter(this, MainActivity.arrayList, this));

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

        TextView roomView = v.findViewById(R.id.tvRoom);
        TextView dateView = v.findViewById(R.id.tvDate);
        TextView timeView = v.findViewById(R.id.tvTimeslot);
        TextView durationView = v.findViewById(R.id.tvStatus);
        String roomIdSelected = roomView.getTag().toString();
        String dateSelected = dateView.getTag().toString();
        String timeSelected = timeView.getTag().toString();
        String durationSelected = durationView.getText().toString();

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        new AlertDialog.Builder(SelectBookingActivity.this)
                .setIcon(android.R.drawable.ic_input_add) //change icon?
                .setMessage("Please confirm booking selection")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        try {
                            jsonArray.remove(0);
                            JSONObject jsonObject = new JSONObject();
                            jsonObject.put("studentId", preferences.getString("studentId", ""));
                            jsonObject.put("roomId", roomIdSelected);
                            jsonObject.put("date", dateSelected);
                            jsonObject.put("time", timeSelected);
                            jsonObject.put("duration", durationSelected);
                            jsonArray.put(jsonObject);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        postData("booking/save", 1);
                        finish();
                        Intent intent = new Intent(SelectBookingActivity.this, ViewBookingActivity.class);
                        startActivity(intent);
                        overridePendingTransition(0, 0);
                    }
                })
                .setNegativeButton("No", null).show();
    }
    public void postData(String urlSuffix, int selectMethod){

        if (selectMethod==0) {
            if (getIntent().hasExtra("json")) {
                try {
                    JSONObject jsonObject = new JSONObject(getIntent().getStringExtra("json"));
                    jsonArray.put(jsonObject);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.POST,"http://10.0.2.2:8080/api/student/"+urlSuffix+"/"+preferences.getString("token", ""),jsonArray, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {

                try {
                    for(int i = 0; i < response.length(); i++){
                        roomName.add(response.getJSONObject(i).getJSONObject("room").getString("roomName"));
                        roomId.add(response.getJSONObject(i).getJSONObject("room").getString("id"));
                        date.add(response.getJSONObject(i).getString("date"));
                        time.add(response.getJSONObject(i).getString("time"));
                        duration.add(response.getJSONObject(i).getString("duration"));
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if(roomName.size()!=0){
                BookingAdapter adapter = new BookingAdapter(SelectBookingActivity.this, 0);
                adapter.setData(roomName, time, duration, roomId, date);

                ListView listView = findViewById(R.id.listViewSelectBooking);
                if (listView != null) {
                    listView.setAdapter(adapter);
                    listView.setOnItemClickListener(SelectBookingActivity.this);
                }}
                else
                {
                    TextView blankViewText = findViewById(R.id.blankViewText);
                    blankViewText.setVisibility(View.VISIBLE);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                Toast.makeText(SelectBookingActivity.this,error.getLocalizedMessage(),Toast.LENGTH_LONG).show();

            }
        });
        MySingleton.getInstance(SelectBookingActivity.this).addToRequestQueue(request);

    }
    
}