package iss.workshop.androidvms;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

//import com.basgeekball.awesomevalidation.AwesomeValidation;
//import com.basgeekball.awesomevalidation.ValidationStyle;
//import com.basgeekball.awesomevalidation.utility.RegexTemplate;

import org.json.JSONException;
import org.json.JSONObject;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Calendar;

import Classes.CreateBookingValidation;

public class CreateBookingActivity extends AppCompatActivity implements View.OnClickListener {

    private DatePickerDialog.OnDateSetListener mDateSetListener;
    private TimePickerDialog.OnTimeSetListener mTimeSetListener;
    private int hour, minute;
    LocalDate inputDate;
    LocalTime inputTime;

    final CharSequence[] items = {"Projector", "White Board", "Computer"};
    final ArrayList selectedItems = new ArrayList();
    final boolean[] checkedItems = {false, false, false};

    Button btnSubmit;
    Button btnFacilities;
    //    Button btnTime;
    EditText paxInput;
    EditText durationInput;

    //Initialize variable
    DrawerLayout drawerLayout;
    ImageView btnMenu;
    RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_booking);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        TextView student_name = findViewById(R.id.student_name);
        student_name.setText(preferences.getString("studentName", ""));
        TextView student_score = findViewById(R.id.student_score);
        student_score.setText(preferences.getString("studentScore", ""));

        Button btnDate = findViewById(R.id.dateSelection); //changed from textview
        btnDate.setOnClickListener(this);
        paxInput = (EditText) findViewById(R.id.paxInput);
        durationInput = (EditText) findViewById(R.id.durationInput);
        btnFacilities = findViewById(R.id.btnFacilities);
        btnFacilities.setOnClickListener(this);
        Button btnTime = findViewById(R.id.btnTime);
        btnSubmit = findViewById(R.id.btnSubmit);
        btnSubmit.setOnClickListener(this);

        //OnClick configuration for date dialog for dates
        findViewById(R.id.dateSelection).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar cal = Calendar.getInstance();
                int year = cal.get(Calendar.YEAR);
                int month = cal.get(Calendar.MONTH);
                int day = cal.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog dialog = new DatePickerDialog(
                        CreateBookingActivity.this,
                        android.R.style.Theme_Holo_Dialog,//Theme_Holo_Dialog, Theme_Holo_Light_Dialog_MinWidth
                        mDateSetListener,
                        year, month, day);
                DatePicker dp = dialog.getDatePicker();
                dp.setMinDate(System.currentTimeMillis() - 1000);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();
            }
        });

        //initialize the datepickerdialog
        mDateSetListener = new DatePickerDialog.OnDateSetListener() {
            @SuppressLint("NewApi")
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                month = month + 1;
                String date = day + "/" + month + "/" + year;
                btnDate.setText(date);
                inputDate = LocalDate.of(year, month, day);

            }
        };

        btnTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LocalTime time = null;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    time = LocalTime.now().plusHours(8);
                    System.out.println(time);
                    int hours = time.getHour();
                    int minutes = time.getMinute();
                    TimePickerDialog dialog = new TimePickerDialog(
                            CreateBookingActivity.this,
                            android.R.style.Theme_Holo_Dialog,//Theme_Holo_Dialog, Theme_Holo_Light_Dialog_MinWidth
                            mTimeSetListener,
                            hours, minutes, true);
                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    dialog.show();
                }
            }
        });

        //initialize the datepickerdialog
        mTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
            @SuppressLint("NewApi")
            @Override
            public void onTimeSet(TimePicker timePicker, int hours, int minutes) {
                LocalTime time = LocalTime.of(hours, minutes);
                System.out.println(time);
                System.out.println(LocalTime.now().plusHours(8));
                System.out.println(inputDate);
                System.out.println(LocalDate.now());
                if (inputDate == null) {
                    Toast.makeText(CreateBookingActivity.this, "Please select a date first", Toast.LENGTH_SHORT).show();
                } else if (inputDate.isEqual(LocalDate.now()) && time.isBefore(LocalTime.now().plusHours(8).minusMinutes(1))) {
                    Toast.makeText(CreateBookingActivity.this, "Please select a time in the future for a booking today", Toast.LENGTH_SHORT).show();
                } else {
                    btnTime.setText(time.toString());
                    inputTime = time;
                }
            }

        };

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

    }

    @Override
    protected void onPause() {
        super.onPause();
        //Close drawer
        MainActivity.closeDrawer(drawerLayout);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onClick(View view) {
        int id = view.getId();

        if (id == R.id.btnSubmit) {
            if (inputDate == null || inputTime == null || TextUtils.isEmpty(paxInput.getText()) || TextUtils.isEmpty(durationInput.getText())) {
                Toast.makeText(this, "Missing mandatory fields", Toast.LENGTH_SHORT).show();
                if (TextUtils.isEmpty(durationInput.getText()))
                    durationInput.setError("Duration in minutes is mandatory");
                if (TextUtils.isEmpty(paxInput.getText()))
                    paxInput.setError("Number of pax is mandatory");
            } else if (CreateBookingValidation.DateTimePax(inputDate, inputTime, durationInput, paxInput,this )!=null)
                Toast.makeText(this, CreateBookingValidation.DateTimePax(inputDate, inputTime, durationInput, paxInput, this), Toast.LENGTH_SHORT).show();
            else if (Integer.valueOf(paxInput.getText().toString()) > 10)
                paxInput.setError("Max limit 10");
            else {
                JSONObject jsonObject = new JSONObject();
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
                try {
                    jsonObject.put("studentId", preferences.getString("studentId", ""));
                    jsonObject.put("date", inputDate.toString());
                    jsonObject.put("time", inputTime.toString());
                    jsonObject.put("capacity", paxInput.getText().toString());
                    jsonObject.put("duration", durationInput.getText().toString());
                    jsonObject.put("facilities", selectedItems);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                Intent intent = new Intent(this, SelectBookingActivity.class);
                intent.putExtra("json", jsonObject.toString());
                startActivity(intent);
            }
        }
        if (id == R.id.btnFacilities) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Facilities list");

            builder.setMultiChoiceItems(items, checkedItems, new DialogInterface.OnMultiChoiceClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                    //Here you add or remove the items from the list selectedItems. That list will be the result of the user selection.
                    if (isChecked) {
                        selectedItems.add(which);
                        checkedItems[which] = true;
                    } else if (selectedItems.contains(which)) {
                        selectedItems.remove(Integer.valueOf(which));
                        checkedItems[which] = false;
                    }
                }
            });

            builder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                }
            });

            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });

            builder.create();
            builder.show();
        }
    }
}