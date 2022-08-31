package iss.workshop.androidvms;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;

import java.util.ArrayList;
import java.util.List;

public class BookingAdapter extends ArrayAdapter {
    private final Context context;
    protected List<String> roomName, timeslot, status, roomId, date;

    public BookingAdapter(Context context, int resId) {
        super(context, resId);
        this.context = context;
    }

    public void setData(List<String> roomName, List<String> timeslot, List<String> status) {
        this.roomName = roomName;
        this.timeslot = timeslot;
        this.status = status;
        roomId = new ArrayList<>();
        date = new ArrayList<>();

        for (int i = 0; i < roomName.size(); i++) {
            add(null);
        }
    }

    public void setData(List<String> roomName, List<String> timeslot, List<String> status, List<String> date) {
        this.roomName = roomName;
        this.timeslot = timeslot;
        this.status = status;
        this.date = date;
        roomId = new ArrayList<>();

        for (int i = 0; i < roomName.size(); i++) {
            add(null);
        }
    }

    public void setData(List<String> roomName, List<String> timeslot, List<String> status, List<String> roomId, List<String> date) {
        this.roomName = roomName;
        this.timeslot = timeslot;
        this.status = status;
        this.roomId = roomId;
        this.date = date;

        for (int i = 0; i < roomName.size(); i++) {
            add(null);
        }
    }

    @androidx.annotation.NonNull
    public View getView(int pos, View view, @NonNull ViewGroup parent) {
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(
                    Activity.LAYOUT_INFLATER_SERVICE);

            // if we are not responsible for adding the view to the parent,
            // then attachToRoot should be 'false' (which is in our case)
            view = inflater.inflate(R.layout.booking_view, parent, false);
        }

        // set the text for TextView
        TextView ivStatus = view.findViewById(R.id.tvStatusIcon);
        String output = status.get(pos);
        if (output.equalsIgnoreCase("SUCCESSFUL")||output.equalsIgnoreCase("REJECTED")||output.equalsIgnoreCase("WAITINGLIST")||output.equalsIgnoreCase("CANCELLED")) {
            ivStatus.setVisibility(View.VISIBLE);
            switch (output) {
                case ("SUCCESSFUL"):
                    ivStatus.setCompoundDrawablesWithIntrinsicBounds(
                            R.drawable.ic_baseline_check_circle_outline_24, 0, 0, 0);
                    break;
                case ("REJECTED"):
                    ivStatus.setCompoundDrawablesWithIntrinsicBounds(
                            R.drawable.ic_baseline_cancel_24, 0, 0, 0);
                    break;
                case ("WAITINGLIST"):
                    ivStatus.setCompoundDrawablesWithIntrinsicBounds(
                            R.drawable.ic_baseline_watch_later_24, 0, 0, 0);
                    break;
                case ("CANCELLED"):
                    ivStatus.setCompoundDrawablesWithIntrinsicBounds(
                            R.drawable.ic_baseline_cancelled, 0, 0, 0);
                    break;
            }
        }
        else{
            TextView tvStatus = view.findViewById(R.id.tvStatus);
            tvStatus.setText(output);
            tvStatus.setVisibility(View.VISIBLE);
        }


        // set the text for TextView
        TextView tvRoom = view.findViewById(R.id.tvRoom);
        tvRoom.setText(roomName.get(pos));
        if(roomId.size()!=0)
        tvRoom.setTag(roomId.get(pos));

        TextView tvDate = view.findViewById(R.id.tvDate);
        tvDate.setText(date.get(pos)+" "+timeslot.get(pos));
        tvDate.setTag(date.get(pos));


        // set the text for TextView
        TextView textView = view.findViewById(R.id.tvTimeslot);
        textView.setTag(timeslot.get(pos));

        //if(date.size()!=0)
        //textView.setTag(date.get(pos));

        return view;
    }
}
