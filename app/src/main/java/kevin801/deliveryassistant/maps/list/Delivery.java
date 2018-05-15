package kevin801.deliveryassistant.maps.list;

import com.google.android.gms.location.places.Place;
import com.google.android.gms.maps.model.LatLng;

public class Delivery {
    
    private String address;
    private double distance, time, tip, milage;
    private LatLng latLng;
    private Place place;
    
    public Delivery(Place place) {
        address = place.getAddress().toString();
        distance = -1; // change to get delivery distance and time via google map's search.
        time = -1;
        tip = 0;
        milage = 0;
        latLng = place.getLatLng();
        this.place = place;
    }
    
    public String getAddress() {
        return address;
    }
    
    public double getDistance() {
        return distance;
    }
    
    public double getTime() {
        return time;
    }
    
    public Place getPlace() {
        return place;
    }
    
    public LatLng getLatLng() {
        return latLng;
    }
    
    public double getTip() {
        return tip;
    }
    
    public void setTip(double tip) {
        this.tip = tip;
    }
    
    public double getMilage() {
        return milage;
    }
    
    public void setMilage(double milage) {
        this.milage = milage;
    }
}
