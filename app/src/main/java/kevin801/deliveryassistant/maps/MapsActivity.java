package kevin801.deliveryassistant.maps;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.nfc.Tag;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
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

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

import kevin801.deliveryassistant.R;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, LoaderManager.LoaderCallbacks {
    
    private static final String TAG = MapsActivity.class.getSimpleName();
    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private Marker marker;
    private ListView addressListView;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> addressList = new ArrayList<>();
    
    LoaderManager loaderManager;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        loaderManager.initLoader(0, null, this);
        
        getSupportLoaderManager().initLoader(0, null, this);
        
        
        mFusedLocationProviderClient = LocationServices
                .getFusedLocationProviderClient(this);
        
        setUpAutoComplete();
        setUpListView();

    }
    
    private void setUpListView() {
        addressListView = findViewById(R.id.marker_listview);

        adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1,
                addressList);

        addressListView.setAdapter(adapter);
    }
    
    private void setUpAutoComplete() {
        PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);
        
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                final LatLng latLngLoc = place.getLatLng();
                MarkerOptions inputMarker = new MarkerOptions().position(latLngLoc)
                        .title(place.getName().toString());
                
                if (!addressList.contains( place.getAddress() )) {
                    // not contained in list
                    marker = mMap.addMarker(inputMarker);
                    getDeviceLocation(mMap);
                    addAddressToList(place);
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
     * Used to add marker's name (addresses) to a list view.
     *
     * @param place the Place containing the address to be added to the view to add the marker.
     */
    private void addAddressToList(Place place) {
        
        addressList.add(place.getAddress().toString());
        adapter.notifyDataSetChanged();
    }
    
    
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // permission is granted
            mMap.setMyLocationEnabled(true);
            getDeviceLocation(mMap);
        }
    }
    
    /**
     * Moves the camera to the device's location.
     * @param googleMap
     */
    private void getDeviceLocation(GoogleMap googleMap) {
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
    
    @NonNull
    @Override
    public Loader onCreateLoader(int id, @Nullable Bundle args) {
        return null;
    }
    
    @Override
    public void onLoadFinished(@NonNull Loader loader, Object data) {
    
    }
    
    @Override
    public void onLoaderReset(@NonNull Loader loader) {
    
    }
    
    public class Delivery{
        String address;
        int distance;
        LatLng latLng;
        
        public Delivery(Place place) {
            address = place.getAddress().toString();
            distance = -1;
            latLng = place.getLatLng();
        }
    }
}


