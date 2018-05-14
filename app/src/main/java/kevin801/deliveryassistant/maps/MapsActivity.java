package kevin801.deliveryassistant.maps;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
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
import java.util.List;
import java.util.Objects;

import kevin801.deliveryassistant.R;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, OnItemClicked {
    
    private static final String TAG = MapsActivity.class.getSimpleName();
    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private DeliveriesListAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private ArrayList<Marker> markers;
    
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
        
        markers = new ArrayList<>();
        
        setUpListView();
        setUpAutoComplete();
    }
    
    private void setUpListView() {
        RecyclerView mRecyclerListView = (RecyclerView) findViewById(R.id.deliveries_listview);
        
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerListView.setLayoutManager(mLayoutManager);
        
        mAdapter = new DeliveriesListAdapter(this, new ArrayList<Delivery>());
        mRecyclerListView.setAdapter(mAdapter);
        
        mAdapter.setOnClick(this);
    }
    
    private void setUpAutoComplete() {
        PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);
        
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                
                final LatLng latLngLoc = place.getLatLng();
                MarkerOptions inputMarker = new MarkerOptions()
                        .position(latLngLoc)
                        .title(Objects.requireNonNull(place.getAddress()).toString());
                
                Delivery delivery = new Delivery(place);
                ArrayList<Delivery> dupList = (ArrayList<Delivery>) mAdapter.getData();
                
                boolean noDuplicates = true;
                for (int i = 0; i <= dupList.size() - 1; i++) {
                    if (dupList.isEmpty()) {
                        break;
                    } else if (dupList.get(i).getLatLng().equals(delivery.getLatLng())) {
                        noDuplicates = false;
                        Toast.makeText(MapsActivity.this,"The Address is alread on the List", Toast.LENGTH_LONG).show();
                        break;
                    }
                }
    
                if (noDuplicates) {
                    // not contained in list
                    Marker marker = mMap.addMarker(inputMarker);
                    
                    markers.add(marker);
                    marker.showInfoWindow();
    
                    addDeliveryToList(delivery);
                }
                gotoPlaceLocation(place);
    
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
     * Used to add Delivery to a list view.
     *
     * @param delivery The Delivery containing the address to be added to the view to add the marker.
     */
    private void addDeliveryToList(Delivery delivery) {
        mAdapter.addData(delivery);
    }
    
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // permission is granted
            mMap.setMyLocationEnabled(true);
            mMap.setTrafficEnabled(true);
            gotoDeviceLocation();
        }
    }
    
    /**
     * Moves the camera to the device's location.
     */
    public void gotoDeviceLocation() {
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
    
    /**
     * Moves the camera to the location of the newly added marker.
     *
     * @param place The place Object with the information containing the Latitude and Longitude.
     */
    public void gotoPlaceLocation(Place place) {
        LatLng latLng = place.getLatLng();
        mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
    }
    
    @Override
    public void onItemClick(View view, int position) {
        List<Delivery> list = mAdapter.getData();
        
        Delivery delivery = (Delivery) list.get(position);
        gotoPlaceLocation(delivery.getPlace());
        
        for (Marker ele : markers) {
            if (ele.getPosition().equals(delivery.getLatLng())) {
                ele.showInfoWindow();
                break;
            }
        }
    }
}


