package no.sforman.myevents;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.firestore.GeoPoint;

import java.util.Calendar;

public class Event implements Parcelable {

    private String id;
    private String owner;
    private String name;
    private String description;
    private long reminderKey;
    private Calendar start;
    private Calendar end;
    private Double latitude;
    private Double longitude;
    private String location;
    private String address;
    private boolean isOnline;


    public Event(String name,
                 String owner,
                 String description,
                 Calendar startDate,
                 Calendar endDate,
                 double latitude,
                 double longitude,
                 String location,
                 String address,
                 boolean isOnline,
                 long reminderKey){

        this.name = name;
        this.owner = owner;
        this.description = description;
        this.start = startDate;
        this.end = endDate;
        this.latitude = latitude;
        this.longitude = longitude;
        this.location = location;
        this.address = address;
        this.isOnline = isOnline;
        this.reminderKey = reminderKey;
    }

    public void addID(String id){
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public String getOwner() {
        return owner;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Calendar getStart() {
        return start;
    }

    public Calendar getEnd() {
        return end;
    }

    public GeoPoint getGeoPoint() {
        return new GeoPoint(this.latitude, this.longitude);
    }

    public String getAddress() {
        return address;
    }

    public String getLocation() {
        return location;
    }

    public long getReminderKey() {
        return reminderKey;
    }

    public boolean isOnline() {
        return isOnline;
    }

    protected Event(Parcel in) {
        id = in.readString();
        name = in.readString();
        owner = in.readString();
        description = in.readString();
        reminderKey = in.readLong();
        start = (Calendar) in.readValue(Calendar.class.getClassLoader());
        end = (Calendar) in.readValue(Calendar.class.getClassLoader());
        latitude = in.readDouble();
        longitude = in.readDouble();
        location = in.readString();
        address = in.readString();
        isOnline = in.readByte() != 0x00;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(owner);
        dest.writeString(description);
        dest.writeLong(reminderKey);
        dest.writeValue(start);
        dest.writeValue(end);
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
        dest.writeString(location);
        dest.writeString(address);
        dest.writeByte((byte) (isOnline ? 0x01 : 0x00));
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Event> CREATOR = new Parcelable.Creator<Event>() {
        @Override
        public Event createFromParcel(Parcel in) {
            return new Event(in);
        }

        @Override
        public Event[] newArray(int size) {
            return new Event[size];
        }
    };
}