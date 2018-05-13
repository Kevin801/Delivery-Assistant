package kevin801.deliveryassistant.maps;

import com.google.android.gms.location.places.Place;
import com.google.android.gms.maps.model.LatLng;

public class Delivery {
    
    private String address;
    private double distance, time;
    private LatLng latLng;
    private Place place;
    
    public Delivery(Place place) {
        address = place.getAddress().toString();
        distance = -1; // change to get delivery distance and time via google map's search.
        time = -1;
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
    
}
