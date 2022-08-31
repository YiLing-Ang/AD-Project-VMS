package iss.workshop.androidvms;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class TimeExtensionActivity extends AppCompatActivity implements View.OnClickListener{

    Button btn1hour;
    Button btn2hour;
    Button btn3hour;
    Button btnSubmit;

    //Initialize variable
    DrawerLayout drawerLayout;
    ImageView btnMenu;
    RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_time_extension);

        btn1hour = findViewById(R.id.btn1hour);
        btn1hour.setOnClickListener(this);
        btn2hour = findViewById(R.id.btn2hour);
        btn2hour.setOnClickListener(this);
        btn3hour = findViewById(R.id.btn3hour);
        btn3hour.setOnClickListener(this);
        btnSubmit = findViewById(R.id.btnSubmit);
        btnSubmit.setOnClickListener(this);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        TextView student_name = findViewById(R.id.student_name);
        student_name.setText(preferences.getString("studentName", ""));
        TextView student_score = findViewById(R.id.student_score);
        student_score.setText(preferences.getString("studentScore", ""));

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

    @Override
    public void onClick(View view) {
        int id = view.getId();

        if (id == R.id.btn1hour) {
            Toast toast = Toast.makeText(this, "1hour", Toast.LENGTH_SHORT);
            toast.show();
        }
        if (id == R.id.btn2hour) {
            Toast toast = Toast.makeText(this, "2hour", Toast.LENGTH_SHORT);
            toast.show();
        }
        if (id == R.id.btn3hour) {
            Toast toast = Toast.makeText(this, "3hour", Toast.LENGTH_SHORT);
            toast.show();
        }
        if (id == R.id.btnSubmit) {
            Toast toast = Toast.makeText(this, "Booking extended", Toast.LENGTH_SHORT);
            toast.show();

            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);

        }

    }
}