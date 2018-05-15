package kevin801.deliveryassistant.maps;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
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
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBufferResponse;
import com.google.android.gms.location.places.PlacesOptions;
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
import com.google.android.gms.maps.model.PointOfInterest;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import kevin801.deliveryassistant.R;
import kevin801.deliveryassistant.maps.list.DeliveriesListAdapter;
import kevin801.deliveryassistant.maps.list.Delivery;
import kevin801.deliveryassistant.maps.list.OnItemClicked;

public class MapsActivity extends FragmentActivity implements
        OnMapReadyCallback,
        OnItemClicked,
        GoogleMap.OnMarkerClickListener {
    
    private static final String TAG = MapsActivity.class.getSimpleName();
    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private DeliveriesListAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private ArrayList<Marker> markers;
    private Marker selectedMarker;
    
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
                // getting marker information ready
                final LatLng latLngLoc = place.getLatLng();
                MarkerOptions inputMarker = new MarkerOptions()
                        .position(latLngLoc)
                        .title(Objects.requireNonNull(place.getAddress()).toString());
                
                Delivery delivery = new Delivery(place);
                ArrayList<Delivery> dupList = (ArrayList<Delivery>) mAdapter.getDeliveryList();
                
                boolean noDuplicates = true;
                for (int i = 0; i <= dupList.size() - 1; i++) {
                    if (dupList.isEmpty()) {
                        // adding to empty list.
                        break;
                    } else if (dupList.get(i).getLatLng().equals(delivery.getLatLng())) {
                        // delivery is already in the list.
                        noDuplicates = false;
                        Toast.makeText(MapsActivity.this, "The Address is already on the List", Toast.LENGTH_LONG).show();
                        break;
                    }
                }
                
                if (noDuplicates) {
                    // not contained in list
                    Marker marker = mMap.addMarker(inputMarker);
                    
                    markers.add(marker);
                    marker.showInfoWindow();
                    selectedMarker = marker;
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
        mAdapter.addDelivery(delivery);
    }
    
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // permission is granted
            mMap.setMyLocationEnabled(true);
            mMap.setTrafficEnabled(true);
            gotoDeviceLocation();
            
            // initializing selectedMarker with a marker with no title.
            Marker marker = mMap.addMarker( new MarkerOptions().position(new LatLng(999999, 9999999)));
            selectedMarker = marker;
            marker.remove();
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
        List<Delivery> list = mAdapter.getDeliveryList();
        
        Delivery delivery = (Delivery) list.get(position);
        gotoPlaceLocation(delivery.getPlace());
        
        for (Marker ele : markers) {
            if (ele.getPosition().equals(delivery.getLatLng())) {
                // delivery is found.
                ele.showInfoWindow();
                selectedMarker = ele;
                break;
            }
        }
    }
    
    /**
     * Perform this action when Delete button is pressed.
     *
     * @param view The View.
     */
    public void deleteMarkerButton(View view) {
        if (selectedMarker.isInfoWindowShown()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.delete_warning_title);
            builder.setMessage(R.string.delete_warning_message);
            
            builder.setPositiveButton(R.string.delete_warnining_confirm, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (selectedMarker.isInfoWindowShown()) {
                        markers.remove(selectedMarker); // remove from markers List
                        selectedMarker.remove(); // remove from map
                        mAdapter.removeDelivery(mAdapter.findDeliveryByMarker(selectedMarker));
                    }
                }
            });
            
            builder.setNegativeButton(R.string.delete_warnining_deny, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    
                }
            });
            builder.show();
        } else {
            Toast.makeText(this, R.string.no_marker_selected, Toast.LENGTH_LONG).show();
        }
    }
    
    public void calculateButton(View view) {
    
    }
    
    @Override
    public boolean onMarkerClick(Marker marker) {
        this.selectedMarker = marker;
        return false;
    }
}


