package iss.workshop.androidvms;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class NavAdapter extends RecyclerView.Adapter<NavAdapter.ViewHolder> {

    //Initialize variable
    Activity activity;
    ArrayList<String> arrayList;
    Context context;


    //Create constructor
    public NavAdapter(Activity activity, ArrayList<String> arrayList, Context context){
        this.activity=activity;
        this.arrayList=arrayList;
        this.context=context;

    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //initialize view
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_drawer_main,parent,false);

        //return holder view
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        //set text on text view
        holder.textView.setText(arrayList.get(position));

        holder.textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //get licked item position
                int position = holder.getAdapterPosition();
                //check condition
                switch (position) {
                    case 0:
                        //when position is equal 0 redirect to homepage
                        activity.startActivity(new Intent(activity, MainActivity.class)
                                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                        break;
                    case 1:
                        activity.startActivity(new Intent(activity,CreateBookingActivity.class)
                                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                        break;
                    case 2:
                        activity.startActivity(new Intent(activity,ViewBookingActivity.class)
                                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                        break;
                    case 3:
                        activity.startActivity(new Intent(activity,CheckInActivity.class)
                                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                        break;
                    case 4:
                        activity.startActivity(new Intent(activity,ReportActivity.class)
                                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                        break;
                    case 5:
                        //Log out
                        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                        //Set title
                        builder.setTitle("Logout");
                        builder.setMessage("Are you sure you want to log out?");
                        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Finish all activity
                                activity.finishAffinity();
                                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
                                SharedPreferences.Editor editor = preferences.edit();
                                editor.clear();
                                editor.commit();
                                activity.startActivity(new Intent(activity, LoginActivity.class)
                                        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                            }
                        });
                        //Negative cancel button
                        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        });
                        //Show dialog
                        builder.show();
                        break;


                }
            }
        });

    }

    @Override
    public int getItemCount() {
        //return arraylist size
        return arrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        //Initialize variable
        TextView textView;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            //assign variable
            textView = itemView.findViewById(R.id.text_view);
        }
    }
}
