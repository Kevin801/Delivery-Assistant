package kevin801.deliveryassistant.maps.list;

import android.support.annotation.NonNull;

import com.google.android.gms.location.places.Place;
import com.google.android.gms.maps.model.LatLng;

import java.util.Comparator;
import java.util.Objects;

public class Delivery implements Comparable<Delivery>{
    
    private String address, prevAddress;
    private double distance, time, tip, delta;
    private LatLng prevLatLng, latLng;
    private Place place;
    
    
    public Delivery(Place place) {
        address = place.getAddress().toString();
        prevAddress = "";
        distance = -1; // change to get delivery distance and time via google map's search.
        time = -1;
        tip = 0;
        delta = 0;
        prevLatLng = null;
        latLng = place.getLatLng();
        this.place = place;
    }
    
    public String getAddress() {
        return address;
    }
    
    public void setAddress(String address) {
        this.address = address;
    }
    
    public double getDistance() {
        return distance;
    }
    
    public void setDistance(double distance) {
        this.distance = distance;
    }

    public double getTime() {
        return time;
    }
    
    public void setTime(double time) {
        this.time = time;
    }
    
    public Place getPlace() {
        return place;
    }
    
    public LatLng getPrevLatLng() {
        return prevLatLng;
    }
    
    public void setPrevLatLng(LatLng prevLatLng) {
        this.prevLatLng = prevLatLng;
    }
    
    public String getPrevAddress() {
        return this.prevAddress;
    }
    
    public void setPrevAddress(String prevAddress) {
        this.prevAddress = prevAddress;
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
    
    public double getDelta() {
        return delta;
    }
    
    /**
     * The distance from JSON's LatLng is from this LatLng
     * @param delta
     */
    public void setDelta(double delta) {
        this.delta = delta;
    }
    
    @Override
    public int compareTo(@NonNull Delivery that) {
        if (this.getDelta() < that.getDelta()) return -1;
        if (this.getDelta() > that.getDelta()) return 1;
        return 0;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Delivery delivery = (Delivery) o;
        return Double.compare(delivery.distance, distance) == 0 &&
                Double.compare(delivery.time, time) == 0 &&
                Double.compare(delivery.tip, tip) == 0 &&
                Double.compare(delivery.delta, delta) == 0 &&
                Objects.equals(address, delivery.address) &&
                Objects.equals(prevAddress, delivery.prevAddress) &&
                Objects.equals(prevLatLng, delivery.prevLatLng) &&
                Objects.equals(latLng, delivery.latLng) &&
                Objects.equals(place, delivery.place);
    }
    
    @Override
    public int hashCode() {
        
        return Objects.hash(address, prevAddress, distance, time, tip, delta, prevLatLng, latLng, place);
    }
    
    @Override
    public String toString() {
        return "Delivery{" +
                "address='" + address + '\'' +
                ", prevAddress='" + prevAddress + '\'' +
                ", distance=" + distance +
                ", time=" + time +
                ", tip=" + tip +
                ", delta=" + delta +
                ", prevLatLng=" + prevLatLng +
                ", latLng=" + latLng +
                '}';
    }
}
