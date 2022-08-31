package iss.workshop.androidvms;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RoomAvailabilityAdapter extends ArrayAdapter {
    private final Context context;
    protected List<String> roomName;
    protected List<List<String>> facilities;

    public RoomAvailabilityAdapter(Context context, int resId) {
        super(context, resId);
        this.context = context;
    }

    public void setData(List<String> roomName, List<List<String>> facilities) {
        this.roomName = roomName;
        this.facilities = facilities;
        for (int i = 0; i < roomName.size(); i++) {
            add(null);
        }
    }


    @NonNull
    public View getView(int pos, View view, @NonNull ViewGroup parent) {
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(
                    Activity.LAYOUT_INFLATER_SERVICE);

            // if we are not responsible for adding the view to the parent,
            // then attachToRoot should be 'false' (which is in our case)
            view = inflater.inflate(R.layout.room_availability_view, parent, false);
        }

        TextView tvRoom = view.findViewById(R.id.tvRoom);
        tvRoom.setText(roomName.get(pos));

        // set the text for TextView
        TextView tvFacility1 = view.findViewById(R.id.tvFacility1);
        if (facilities.get(pos).contains("Projector")){
        tvFacility1.setCompoundDrawablesWithIntrinsicBounds(
                R.drawable.ic_projector_svgrepo_com, 0, 0, 0);}

        TextView tvFacility2 = view.findViewById(R.id.tvFacility2);
        if (facilities.get(pos).contains("White Board")){
        tvFacility2.setCompoundDrawablesWithIntrinsicBounds(
                R.drawable.ic_class_whiteboard_svgrepo_com, 0, 0, 0);}

        TextView tvFacility3 = view.findViewById(R.id.tvFacility3);
        if (facilities.get(pos).contains("Computer")){
        tvFacility3.setCompoundDrawablesWithIntrinsicBounds(
                R.drawable.ic_computer_svgrepo_com, 0, 0, 0);}

        return view;
    }
}
