package kevin801.deliveryassistant.maps.list;

import android.support.annotation.NonNull;

import com.google.android.gms.location.places.Place;
import com.google.android.gms.maps.model.LatLng;

import java.util.Comparator;
import java.util.Objects;

public class Delivery implements Comparable<Delivery>{
    
    private String address, prevAddress;
    private double distance, duration, tip, delta;
    private LatLng prevLatLng, latLng;
    
    
    public Delivery(String address, LatLng latLng) {
        this.address = address;
        this.latLng = latLng;
        prevAddress = "";
        distance = -1;
        duration = -1;
        tip = 0;
        delta = 0;
        prevLatLng = null;
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

    public double getDuration() {
        return duration;
    }
    
    public void setDuration(double duration) {
        this.duration = duration;
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
                Double.compare(delivery.duration, duration) == 0 &&
                Double.compare(delivery.tip, tip) == 0 &&
                Double.compare(delivery.delta, delta) == 0 &&
                Objects.equals(address, delivery.address) &&
                Objects.equals(prevAddress, delivery.prevAddress) &&
                Objects.equals(prevLatLng, delivery.prevLatLng) &&
                Objects.equals(latLng, delivery.latLng) ;
    }
    
    @Override
    public int hashCode() {
        
        return Objects.hash(address, prevAddress, distance, duration, tip, delta, prevLatLng, latLng);
    }
    
    @Override
    public String toString() {
        return "Delivery{" +
                "address='" + address + '\'' +
                ", prevAddress='" + prevAddress + '\'' +
                ", distance=" + distance +
                ", duration=" + duration +
                ", tip=" + tip +
                ", delta=" + delta +
                ", prevLatLng=" + prevLatLng +
                ", latLng=" + latLng +
                '}';
    }
}
