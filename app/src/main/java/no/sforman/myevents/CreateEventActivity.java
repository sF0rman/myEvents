package no.sforman.myevents;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.firebase.firestore.GeoPoint;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class CreateEventActivity extends AppCompatActivity {

    public static final String TAG = "CreateEventActivity";

    //UI
    MapFragment mapFragment;
    EditText name;
    EditText description;
    EditText startDate;
    EditText startTime;
    EditText endDate;
    EditText endTime;
    EditText location;
    EditText reminderDate;
    EditText reminderTime;
    CheckBox isOnline;
    CheckBox hasReminder;
    TextView nameError;
    TextView descriptionError;
    TextView timeError;
    TextView locationError;
    TextView reminderError;
    TextView isOnlineError;

    // Places
    public static final int AUTOCOMPLETE_REQUEST_CODE = 1;
    String placeName;
    LatLng placeLatLng;

    //Event variables
    String eventName;
    String eventDescription;
    String eventLocation;
    Boolean event_isOnline;
    Calendar reminderCal = Calendar.getInstance();
    Calendar startCal = Calendar.getInstance();
    Calendar endCal = Calendar.getInstance();
    Calendar today = Calendar.getInstance();
    GeoPoint eventGeoPoint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);

        initUI();
        if(isOnline()){
            initFirebase();
            initPlaces();
        } else {
            Log.e(TAG, "onCreate: No network");
        }

    }

    public boolean isOnline() {
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        return (networkInfo != null && networkInfo.isConnected());
    }

    private void initFirebase(){

    }

    private void initUI(){
        name = findViewById(R.id.create_event_name_text);
        startDate = findViewById(R.id.create_event_startdate_text);
        startTime = findViewById(R.id.create_event_starttime_text);
        endDate = findViewById(R.id.create_event_enddate_text);
        endTime = findViewById(R.id.create_event_endtime_text);
        reminderDate = findViewById(R.id.create_event_reminder_date);
        reminderTime = findViewById(R.id.create_event_reminder_time);
        location = findViewById(R.id.create_event_location_text);
        description = findViewById(R.id.create_event_description_text);

        isOnline = findViewById(R.id.create_event_is_online);
        hasReminder = findViewById(R.id.create_event_add_reminder);

        nameError = findViewById(R.id.create_event_name_error);
        timeError = findViewById(R.id.create_event_time_error);
        reminderError = findViewById(R.id.create_event_reminder_time_error);
        isOnlineError = findViewById(R.id.create_event_is_online_error);
        locationError = findViewById(R.id.create_event_location_error);
        descriptionError = findViewById(R.id.create_event_description_error);

        hasReminder.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                    reminderDate.setVisibility(View.VISIBLE);
                    reminderTime.setVisibility(View.VISIBLE);
                    reminderError.setVisibility(View.VISIBLE);
                } else {
                    reminderDate.setVisibility(View.GONE);
                    reminderTime.setVisibility(View.GONE);
                    reminderError.setVisibility(View.GONE);
                }
            }
        });

        isOnline.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                    location.setVisibility(View.GONE);
                    locationError.setVisibility(View.GONE);
                } else {
                    location.setVisibility(View.VISIBLE);
                    locationError.setVisibility(View.VISIBLE);
                }
            }
        });


    }

    private void initPlaces(){
        String mapKey = getString(R.string.events_maps_key);
        mapFragment = (MapFragment) getSupportFragmentManager().findFragmentById(R.id.create_event_map_fragment);
        Places.initialize(getApplicationContext(), mapKey);
        PlacesClient pClient = Places.createClient(this);
    }

    public void findLocation(View v){
        List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG);

        Intent i = new Autocomplete.IntentBuilder(
                AutocompleteActivityMode.OVERLAY, fields)
                .build(this);
        startActivityForResult(i, AUTOCOMPLETE_REQUEST_CODE);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            if(resultCode == RESULT_OK) {
                Place place = Autocomplete.getPlaceFromIntent(data);
                Log.i(TAG, "onActivityResult: Place" + place.getName() + ", " + place.getLatLng());
                placeLatLng = place.getLatLng();
                eventGeoPoint = new GeoPoint(placeLatLng.latitude, placeLatLng.longitude);
                eventLocation = place.getName();
                location.setText(eventLocation);

                if(mapFragment != null){
                    mapFragment.setLocationMarker(placeLatLng);
                }
            }
        }
    }

    public void onSubmitEvent(View v){
        Log.d(TAG, "onSubmitEvent: Submit clicked");

        if(validInput()){

        }
    }

    public boolean validInput(){
        clearErrors();
        eventName = name.getText().toString();
        eventDescription = description.getText().toString();
        Boolean isOk = true;

        if(eventName.length() < 2){
            nameError.setText(getString(R.string.error_invalid_input));
            isOk = false;
            Log.e(TAG, "validInput: Name not long enough");
        }
        if(startCal.after(endCal)){
            timeError.setText(R.string.error_event_end_early);
            isOk = false;
            Log.e(TAG, "validInput: Event ends before it starts");
        }

        if(startCal.before(today)){
            timeError.setText(R.string.error_event_early);
            isOk = false;
            Log.e(TAG, "validInput: Event starts before today");
        }

        if(!isOnline.isChecked()){
            if(eventGeoPoint == null){
                locationError.setText(getString(R.string.error_no_location));
                isOk = false;
                Log.e(TAG, "validInput: No location set");
            }
        }

        if(eventDescription.length() < 5){
            descriptionError.setText(R.string.error_invalid_input);
            isOk = false;
            Log.e(TAG, "validInput: Description too short");
        }

        if(hasReminder.isChecked()){
            if(reminderDate.getText().toString().matches("")) {
                reminderError.setText(R.string.error_no_reminder);
                isOk = false;
                Log.e(TAG, "validInput: No reminder set");
            } else if (reminderCal.before(today)){
                reminderError.setText(R.string.error_reminder_early);
                isOk = false;
                Log.e(TAG, "validInput: Reminder too early " + reminderCal.toString());
            }  else if(reminderCal.after(startCal)){
                reminderError.setText(R.string.error_reminder_late);
                isOk = false;
                Log.e(TAG, "validInput: Reminder too late");
            }
        }

        return isOk;
    }

    public void clearErrors(){
        nameError.setText("");
        descriptionError.setText("");
        timeError.setText("");
        locationError.setText("");
        reminderError.setText("");
        isOnlineError.setText("");
    }

    public void setDateTime(View v){
        switch (v.getId()){
            case R.id.create_event_reminder_date:
                showDatePickerDialog(reminderDate, reminderCal);
                break;

            case R.id.create_event_reminder_time:
                showTimePickerDialog(reminderTime, reminderCal);
                break;

            case R.id.create_event_startdate_text:
                showDatePickerDialog(startDate, startCal);
                break;

            case R.id.create_event_enddate_text:
                showDatePickerDialog(endDate, endCal);
                break;

            case R.id.create_event_starttime_text:
                showTimePickerDialog(startTime, endCal);
                break;

            case R.id.create_event_endtime_text:
                showTimePickerDialog(endTime, endCal);
                break;
        }
    }

    public void showDatePickerDialog(final EditText text, final Calendar cal){
        Log.d(TAG, "showDatePickerDialog: Open");
        DialogFragment datePicker = new DatePickerFragment();
        ((DatePickerFragment) datePicker).setOnDateChosenListener(new DatePickerFragment.OnDateChosenListener() {
            @Override
            public void onDateChosen(int year, int month, int day) {
                text.setText(String.format("%02d/%02d/%04d", day, month+1, year));
                cal.set(year, month, day);

                if(text == startDate){
                    showTimePickerDialog(startTime, startCal);
                } else if (text == endDate){
                    showTimePickerDialog(endTime, endCal);
                } else if (text == reminderDate){
                    showTimePickerDialog(reminderTime, reminderCal);
                }
            }
        });
        datePicker.show(getSupportFragmentManager(), "DatePicker");
    }

    public void showTimePickerDialog(final EditText text, final Calendar cal){
        Log.d(TAG, "showTimePickerDialog: Open");
        DialogFragment timePicker = new TimePickerFragment();
        ((TimePickerFragment) timePicker).setOnTimeChosenListener(new TimePickerFragment.OnTimeChosenListener() {
            @Override
            public void onTimeChosen(int hour, int min) {
                text.setText(String.format("%02d:%02d", hour, min));
                cal.set(Calendar.HOUR_OF_DAY, hour);
                cal.set(Calendar.MINUTE, min);

                if(text == startTime){
                    endCal = startCal;
                    endCal.add(Calendar.HOUR_OF_DAY, 2);
                    Log.d(TAG, "onTimeChosen: " + startCal.toString());
                    Log.d(TAG, "onTimeChosen: "+ endCal.toString());
                    endDate.setText(String.format("%02d/%02d/%04d", endCal.get(Calendar.DATE), endCal.get(Calendar.MONTH+1), endCal.get(Calendar.YEAR)));
                    endTime.setText(String.format("%02d:%02d", endCal.get(Calendar.HOUR_OF_DAY), endCal.get(Calendar.MINUTE)));
                }
            }
        });
        timePicker.show(getSupportFragmentManager(), "TimePicker");
    }

}
