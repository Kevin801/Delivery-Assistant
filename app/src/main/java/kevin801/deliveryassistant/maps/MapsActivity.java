package kevin801.deliveryassistant.maps;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.AsyncTask;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.dynamic.ObjectWrapper;
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
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.GroundOverlayOptions;
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

public class MapsActivity extends AppCompatActivity implements
        OnMapReadyCallback,
        OnItemClicked,
        GoogleMap.OnMarkerClickListener {
    
    private static final String TAG = MapsActivity.class.getSimpleName();
    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private DeliveriesListAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    
    private ArrayList<Marker> markerList;
    private Marker selectedMarker;
    /**
     * The marker where the user's work is located
     */
    private Marker workMarker;
    private Polyline currentPolyline;
    private LatLng workLatLng = null;
    private LatLng currLatLng;
    private Context mContext;
    private Button navigateButton;
    
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
        
        markerList = new ArrayList<>();
        mContext = this;
        setUpListView();
        setUpAutoComplete();
        
        navigateButton = findViewById(R.id.navigate_button);
        navigateButton.setClickable(false);
        
    }
    
    private void setUpListView() {
        RecyclerView mRecyclerListView = (RecyclerView) findViewById(R.id.deliveries_listview);
        
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerListView.setLayoutManager(mLayoutManager);
        
        mAdapter = new DeliveriesListAdapter(mContext, new ArrayList<Delivery>());
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
                for (Delivery ele : dupList) {
                    if (ele.getLatLng().equals(delivery.getLatLng())) {
                        // delivery is already in the list.
                        noDuplicates = false;
                        Toast.makeText(MapsActivity.this, "The Address is already on the List", Toast.LENGTH_LONG).show();
                        break;
                    }
                }
                
                if (noDuplicates) {
                    // not contained in list
                    Marker marker = mMap.addMarker(inputMarker);
                    
                    markerList.add(marker);
                    marker.showInfoWindow();
                    selectedMarker = marker;
                    mAdapter.addDelivery(delivery);
                    drawPolylines();
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
     * used to draw lines every time a marker is added or deleted.
     * Use sparingly; limited amount of uses per day.
     */
    private void drawPolylines() {
        currentPolyline.remove();
        // Checks, whether start and end locations are captured
        if (markerList.size() >= 1) {
            List<LatLng> waypointList = new ArrayList<>();
            
            for (int i = 0; i < markerList.size(); i++) {
                waypointList.add(markerList.get(i).getPosition());
            }
            
            // Getting URL to the Google Directions API
            String url = getDirectionsUrlWithWaypoints(workMarker.getPosition(), waypointList);
            
            DownloadTask downloadTask = new DownloadTask();
            
            // Start downloading json data from Google Directions API
            downloadTask.execute(url);
        }
    }
    
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // permission is granted
            googleMap.setOnMarkerClickListener(this);
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setZoomControlsEnabled(true);
            mMap.getUiSettings().setMapToolbarEnabled(false);

//            mMap.setTrafficEnabled(true);     TODO: add toolbar button to enable/disable traffic.
            gotoDeviceLocation();
            
            LatLng nonExistant = new LatLng(9999999, 9999999);
            
            // initializing selectedMarker with a non existant location
            Marker marker = mMap.addMarker(new MarkerOptions().position(nonExistant));
            selectedMarker = marker;
            selectedMarker.remove();
            
            // initializing currentPolyline with a line with non existant location
            Polyline polyline = mMap.addPolyline(new PolylineOptions().add(nonExistant));
            currentPolyline = polyline;
            polyline.remove();
        }
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_maps_toolbar, menu);
        CheckBox checkBox = (CheckBox) menu.findItem(R.id.action_display_traffic).getActionView();
        
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_display_traffic:
                if (item.isChecked()) {
                    item.setChecked(false);
                    mMap.setTrafficEnabled(false);
                } else {
                    item.setChecked(true);
                    mMap.setTrafficEnabled(true);
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
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
                        currLatLng = new LatLng(location.getLatitude(),
                                location.getLongitude());
                        
                        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(currLatLng, 15.0F);
                        mMap.moveCamera(update);
                        
                        if (workLatLng == null) { // default to user location.
                            workLatLng = currLatLng;
                        }
                        workMarker = mMap.addMarker(new MarkerOptions()
                                .title("Work").position(workLatLng)
                                .icon(bitmapDescriptorFromVector(mContext, R.drawable.ic_work_black_24dp)));
                        // TODO: add different icon for Work marker.
                        markerList.add(workMarker);
                        
                    }
                }
            });
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }
    
    private BitmapDescriptor bitmapDescriptorFromVector(Context context, @DrawableRes int vectorDrawableResourceId) {
        Drawable background = ContextCompat.getDrawable(context, vectorDrawableResourceId);
        background.setBounds(0, 0, background.getIntrinsicWidth(), background.getIntrinsicHeight());
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorDrawableResourceId);
        vectorDrawable.setBounds(40, 20, vectorDrawable.getIntrinsicWidth() + 40, vectorDrawable.getIntrinsicHeight() + 20);
        Bitmap bitmap = Bitmap.createBitmap(background.getIntrinsicWidth(), background.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        background.draw(canvas);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
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
        
        for (Marker ele : markerList) {
            if (ele.getPosition().equals(delivery.getLatLng())) {
                // delivery found
                ele.showInfoWindow();
                selectedMarker = ele;
                
                break;
            }
        }
    }
    
    @Override
    public boolean onMarkerClick(Marker marker) {
        if (!marker.equals(workMarker)) {
            this.selectedMarker = marker;
            selectedMarker.showInfoWindow();
            mMap.animateCamera(CameraUpdateFactory.newLatLng(marker.getPosition()));
            return true;
        }
        Log.i(TAG, "onMarkerClick: " + marker.toString());
        return false;
    }
    
    /**
     * Perform this action when Delete button is pressed.
     *
     * @param view The View.
     */
    public void deleteMarkerButton(View view) {
        if (selectedMarker != workMarker && selectedMarker.isInfoWindowShown()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.delete_warning_title);
            builder.setMessage(R.string.delete_warning_message);
            
            builder.setPositiveButton(R.string.delete_warnining_confirm, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (selectedMarker.isInfoWindowShown()) {
                        markerList.remove(selectedMarker); // remove from markers List
                        selectedMarker.remove(); // remove from map
                        currentPolyline.remove();
                        
                        mAdapter.removeDelivery(mAdapter.findDeliveryByMarker(selectedMarker));
                        
                        if (!mAdapter.getDeliveryList().isEmpty()) {
                            // not empty, draw lines
                            drawPolylines();
                        }
                        
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
    
    public void navigateButton(View view) {
        // TODO: set clickable, start navigation
        
        
    }
    
    /*
     *
     *  CODE BELOW COURTESY OF:
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
            PolylineOptions lineOptions = new PolylineOptions().add(new LatLng(999999, 999999));
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
            Log.i(TAG, "onPostExecute: " + currentPolyline.toString());
            // Drawing polyline in the Google Map for the i-th route
            currentPolyline = mMap.addPolyline(lineOptions);
        }
    }
    
    /**
     * Creates roundTrip with deliveries as the waypoints.
     *
     * @param origin       The User's current location or work.
     * @param waypointList The list of deliveries being listed as waypoints.
     * @return
     */
    private String getDirectionsUrlWithWaypoints(LatLng origin, List<LatLng> waypointList) {
        
        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;
        
        // Destination of route
        String str_dest = "destination=" + origin.latitude + "," + origin.longitude;
        
        // startWaypoints, optimize true.
        String waypointStart = "waypoints=optimize:true";
        
        // add Waypoints, separate by  |*waypointLatLng*
        String waypoints = "";
        for (LatLng ele : waypointList) {
            waypoints = waypoints + "|" + ele.latitude + "," + ele.longitude;
        }
        
        // Sensor enabled
        String sensor = "sensor=false";
        String mode = "mode=driving";
        
        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + waypointStart + waypoints + "&" + sensor + "&" + mode;
        
        // Output format
        String output = "json";
        
        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;
        
        Log.d(TAG, "getDirectionsUrlWithWaypoints() returned: " + url);
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


