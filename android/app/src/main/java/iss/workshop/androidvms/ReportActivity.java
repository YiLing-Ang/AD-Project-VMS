package iss.workshop.androidvms;


import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Base64;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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

import java.io.ByteArrayOutputStream;

import Classes.MySingleton;

public class ReportActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemSelectedListener {

    private static final int PERMISSION_CODE = 1000;
    private static final int IMAGE_CAPTURE_CODE = 1001;
    Button btnSubmit;
    Button btnCamera;
    EditText details;
    ImageView mImageView;

    Uri image_uri;
    String spinnerSelection;

    //Initialize variable
    DrawerLayout drawerLayout;
    ImageView btnMenu;
    RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String currentbooking = preferences.getString("currentbooking", "");
        if (currentbooking.equalsIgnoreCase("true")){
            setContentView(R.layout.activity_report);

        TextView student_name = findViewById(R.id.student_name);
        student_name.setText(preferences.getString("studentName", ""));
        TextView student_score = findViewById(R.id.student_score);
        student_score.setText(preferences.getString("studentScore", ""));


        btnCamera = findViewById(R.id.btnCamera);
        btnCamera.setOnClickListener(this);

        btnSubmit = findViewById(R.id.btnSubmit);
        btnSubmit.setOnClickListener(this);

        mImageView = findViewById(R.id.imgReport);
        details = findViewById(R.id.reportDetails);

        RequestQueue mQueue = Volley.newRequestQueue(this);

        //Spinner for misuse type
        Spinner spinnerMisuse = findViewById(R.id.spinnerMisuse);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.misuseType, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMisuse.setAdapter(adapter);
        spinnerMisuse.setOnItemSelectedListener(this);

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
        else {
            Toast.makeText(ReportActivity.this, "No current booking", Toast.LENGTH_LONG).show();
            finish();
            Intent intent = new Intent(ReportActivity.this, MainActivity.class);
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

        if (id == R.id.btnSubmit) {

            String detailsString = details.getText().toString();

            //bitmap from image URI
            Bitmap bitmap = null;
            ContentResolver contentResolver = getContentResolver();
            try {
                if (Build.VERSION.SDK_INT < 28) {
                    bitmap = MediaStore.Images.Media.getBitmap(contentResolver, image_uri);
                } else {
                    ImageDecoder.Source source = ImageDecoder.createSource(contentResolver, image_uri);
                    bitmap = ImageDecoder.decodeBitmap(source);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (bitmap == null || TextUtils.isEmpty(details.getText())) {
                Toast.makeText(ReportActivity.this, "Please include picture and details", Toast.LENGTH_LONG).show();
            } else {
                //converting bitmap to byte[] for json transport
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                byte[] imgByteArray = stream.toByteArray();
                String encodedImage = Base64.encodeToString(imgByteArray, Base64.DEFAULT);
                bitmap.recycle();

                JSONObject jsonObject = new JSONObject();
                JSONArray jsonArray = new JSONArray();
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
                try {
                    jsonObject.put("studentId", preferences.getString("studentId", ""));
                    jsonObject.put("details", detailsString);
                    jsonObject.put("category", spinnerSelection.toString());
                    jsonObject.put("image", encodedImage);
                    jsonArray.put(jsonObject);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                JsonArrayRequest request = new JsonArrayRequest(Request.Method.POST, "http://10.0.2.2:8080/api/student/report/save/" + preferences.getString("token", ""), jsonArray, new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        try {
                            String responseString = response.getJSONObject(0).getString("response");
                            Toast.makeText(ReportActivity.this, responseString, Toast.LENGTH_LONG).show();
                            finish();
                            Intent intent = new Intent(ReportActivity.this, MainActivity.class);
                            startActivity(intent);
                            overridePendingTransition(0, 0);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        Toast.makeText(ReportActivity.this, error.getLocalizedMessage(), Toast.LENGTH_LONG).show();

                    }
                });
                MySingleton.getInstance(ReportActivity.this).addToRequestQueue(request);
            }
        }

        if (id == R.id.btnCamera) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED ||
                        checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                    String[] permission = {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
                    requestPermissions(permission, PERMISSION_CODE); //SHOW POPUP PERMISSION REQUEST
                } else {
                    //permission granted, proceed camera
                    openCamera();

                }
            }
        }

    }

    private void openCamera() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "Report image");
        values.put(MediaStore.Images.Media.DESCRIPTION, "From Camera");
        image_uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        //Camera intent
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
        startActivityForResult(cameraIntent, IMAGE_CAPTURE_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    ;
                    openCamera();
                } else {
                    Toast.makeText(this, "permission denied.", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            //set image captured to imageView
            mImageView.setImageURI(image_uri);
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
        spinnerSelection = adapterView.getItemAtPosition(position).toString();

    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
    }
}