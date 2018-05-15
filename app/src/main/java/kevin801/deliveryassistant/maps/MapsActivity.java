package kevin801.deliveryassistant.maps;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
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
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
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
    private Polyline currentPolyline;
    
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
            Marker marker = mMap.addMarker(new MarkerOptions().position(new LatLng(999999, 9999999)));
            selectedMarker = marker;
            marker.remove();
            
            // initializing currentPolyline with a line with no title.
            Polyline polyline = mMap.addPolyline(new PolylineOptions().add(new LatLng(999999, 99999999)));
            currentPolyline = polyline;
            polyline.remove();
            
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
                        LatLng currentLatLng =
                                new LatLng(location.getLatitude(),
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
        
        currentPolyline.remove();
        // Checks, whether start and end locations are captured
        if (markers.size() >= 2) {
            LatLng origin = (LatLng) markers.get(0).getPosition();
            LatLng dest = (LatLng) markers.get(1).getPosition();
            
            // Getting URL to the Google Directions API
            String url = getDirectionsUrl(origin, dest);
            
            DownloadTask downloadTask = new DownloadTask();
            
            // Start downloading json data from Google Directions API
            downloadTask.execute(url);
        }
    }
    
    @Override
    public boolean onMarkerClick(Marker marker) {
        this.selectedMarker = marker;
        return true;
    }
    
    /*
     *
     *  BELOW CODE COURTESY OF:
     * https://www.journaldev.com/13373/android-google-map-drawing-route-two-points
     *
     */
    
    private class DownloadTask extends AsyncTask {
        
        @Override
        protected void onPostExecute(Object result) {
            super.onPostExecute(result);
            
            ParserTask parserTask = new ParserTask();
            parserTask.execute(result.toString());
        }
        
        
        @Override
        protected Object doInBackground(Object[] url) {
            
            String data = "";
            try {
                data = downloadUrl(url[0].toString());
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }
    }
    
    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {
        
        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {
            
            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;
            
            try {
                jObject = new JSONObject(jsonData[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser();
                
                routes = parser.parse(jObject);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return routes;
        }
        
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList points = null;
            PolylineOptions lineOptions = null;
            MarkerOptions markerOptions = new MarkerOptions();
            
            for (int i = 0; i < result.size(); i++) {
                points = new ArrayList();
                lineOptions = new PolylineOptions();
                
                List<HashMap<String, String>> path = result.get(i);
                
                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);
                    
                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);
                    
                    points.add(position);
                }
                
                lineOptions.addAll(points);
                lineOptions.width(15);
                lineOptions.color(Color.BLUE);
                lineOptions.geodesic(true);
                
            }
            
            // Drawing polyline in the Google Map for the i-th route
            currentPolyline = mMap.addPolyline(lineOptions);
        }
    }
    
    private String getDirectionsUrl(LatLng origin, LatLng dest) {
        
        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;
        
        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;
        
        // Sensor enabled
        String sensor = "sensor=false";
        String mode = "mode=driving";
        
        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + sensor + "&" + mode;
        
        // Output format
        String output = "json";
        
        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;
        
        return url;
    }
    
    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);
            
            urlConnection = (HttpURLConnection) url.openConnection();
            
            urlConnection.connect();
            
            iStream = urlConnection.getInputStream();
            
            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));
            
            StringBuffer sb = new StringBuffer();
            
            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            
            data = sb.toString();
            
            br.close();
            
        } catch (Exception e) {
            Log.d("Exception", e.toString());
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }
    
}


