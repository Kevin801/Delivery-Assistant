package kevin801.deliveryassistant.maps;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;

import kevin801.deliveryassistant.R;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
    
    private static final String TAG = MapsActivity.class.getSimpleName();
    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private Marker marker;
    private ListView deliveriesListView;
    private DeliveriesListAdapter adapter;
    private ArrayList<Delivery> addressList = new ArrayList<>();
    
    private String DELIVERIES_LIST = "deliveries";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        
        
        
        
        mFusedLocationProviderClient = LocationServices
                .getFusedLocationProviderClient(this);
        
        setUpAutoComplete();
        setUpListView();
        
    }
    
    private void setUpListView() {
        deliveriesListView = findViewById(R.id.deliveries_listview);

        adapter = new DeliveriesListAdapter(addressList, this);
        
        
//        adapter = new ArrayAdapter<String>(this,
//                android.R.layout.simple_list_item_1,
//                addressList);
    
        deliveriesListView.setAdapter(adapter);
    }
    
    private void setUpAutoComplete() {
        PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);
        
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                final LatLng latLngLoc = place.getLatLng();
                MarkerOptions inputMarker = new MarkerOptions().position(latLngLoc).title(place.getAddress().toString());
                
                Delivery delivery = new Delivery(place);
    
                if (!addressList.contains(delivery)) {
                    // not contained in list
                    marker = mMap.addMarker(inputMarker);
                    addAddressToList(delivery);
                    gotoAddressLocation(mMap, place);
                }
                
                Log.i(TAG, "Place: " + place.getName());
            }
            @Override
            public void onError(Status status) {
                Toast.makeText(MapsActivity.this, "Place not found." + status.toString(), Toast.LENGTH_LONG).show();
                Log.i(TAG, "An error occurred: " + status);
            }
        });
    }
    
    /**
     * Moves the camera to the location of the newly added marker.
     * @param mMap The google map with the marker and address.
     * @param place The place Object with the information containing the Latitude and Longitude.
     */
    public void gotoAddressLocation(GoogleMap mMap, Place place) {
        LatLng latLng = place.getLatLng();
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
    }
    
    /**
     * Used to add marker's name (addresses) to a list view.
     *
     * @param delivery The Delivery containing the address to be added to the view to add the marker.
     */
    private void addAddressToList(Delivery delivery) {
        addressList.add(delivery);
        adapter.notifyDataSetChanged();
    }
    
    
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // permission is granted
            mMap.setMyLocationEnabled(true);
            mMap.setTrafficEnabled(true);
            gotoDeviceLocation(mMap);
        }
    }
    
    /**
     * Moves the camera to the device's location.
     * @param googleMap The currently viewed map.
     */
    public void gotoDeviceLocation(GoogleMap googleMap) {
        mMap = googleMap;
        try {
            Task<Location> locationResult = mFusedLocationProviderClient.getLastLocation();
            locationResult.addOnCompleteListener(new OnCompleteListener<Location>() {
                @Override
                public void onComplete(@NonNull Task<Location> task) {
                    if (task.isSuccessful()) {
                        // Set the map's camera position to the current location of the device.
                        Location location = task.getResult();
                        LatLng currentLatLng = new LatLng(location.getLatitude(),
                                location.getLongitude());
                        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(currentLatLng, 15.0F);
                        mMap.moveCamera(update);
                    }
                }
            });
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }
    
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }
    


}


