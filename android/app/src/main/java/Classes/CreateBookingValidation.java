package Classes;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.Toast;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoField;

public class CreateBookingValidation {
    public static boolean isWeekend(final LocalDate ld) {
        DayOfWeek day = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            day = DayOfWeek.of(ld.get(ChronoField.DAY_OF_WEEK));
            return day == DayOfWeek.SUNDAY || day == DayOfWeek.SATURDAY;
        }
        return false;
    }

    public static String DateTimePax(LocalDate date, LocalTime time, EditText duration, EditText pax, Context context)
    {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        Integer groupSize = Integer.valueOf(preferences.getString("groupSize",""));
        if(date.isEqual(LocalDate.now()) && time.isBefore(LocalTime.now().plusHours(8).minusMinutes(1)))
            return "Please select a time in the future for a booking today";
        if(isWeekend(date))
            return "Please select a date from Monday to Friday";
        if(time.isBefore(LocalTime.of(9,0))||time.plusMinutes(Integer.valueOf(duration.getText().toString())).isAfter(LocalTime.of(18, 0)))
            return "Booking must start at 9am or end by 6pm";
        if(LocalDate.now().plusWeeks(2).isBefore(date))
            return "Please select date within 2 weeks";
        if(Integer.valueOf(pax.getText().toString())>groupSize)
            return "Pax size must be less than or equal to your group size ("+groupSize+")";
        else return null;
    }
}
