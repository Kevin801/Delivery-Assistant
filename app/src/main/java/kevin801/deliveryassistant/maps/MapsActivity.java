package kevin801.deliveryassistant.maps;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
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
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.PriorityQueue;

import kevin801.deliveryassistant.R;
import kevin801.deliveryassistant.maps.list.DeliveriesListAdapter;
import kevin801.deliveryassistant.maps.list.Delivery;
import kevin801.deliveryassistant.maps.list.OnItemClicked;

public class MapsActivity extends AppCompatActivity implements
        OnMapReadyCallback,
        OnItemClicked,
        GoogleMap.OnMarkerClickListener {
    
    private static final String TAG = MapsActivity.class.getSimpleName();
    private Context mContext;
    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private DeliveriesListAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    
    private ArrayList<Marker> markerList;
    private Marker selectedMarker;
    private Marker workMarker;
    private Polyline currentPolyline;
    
    private LatLng workLatLng = null;
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
        
        setUpListView(new ArrayList<Delivery>());
        setUpGoogleMapsSearch();
        navigateButton = findViewById(R.id.navigate_button);
        navigateButton.setClickable(false);
    }
    
    private void setUpListView(List<Delivery> dataSet) {
        RecyclerView mRecyclerListView = (RecyclerView) findViewById(R.id.deliveries_listview);
        
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerListView.setLayoutManager(mLayoutManager);
        
        mAdapter = new DeliveriesListAdapter(mContext, dataSet);
        mRecyclerListView.setAdapter(mAdapter);
        
        mAdapter.setOnClick(this);
    }
    
    private void setUpGoogleMapsSearch() {
        PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);
        
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                // getting marker information ready
                LatLng latLngLoc = place.getLatLng();
                MarkerOptions inputMarker = new MarkerOptions()
                        .position(latLngLoc)
                        .title(Objects.requireNonNull(place.getAddress()).toString());
                
                Delivery delivery = new Delivery(place.getAddress().toString(), latLngLoc);
                
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
                    
                    mAdapter.addDeliveryToList(delivery);
                    
                    drawPolylines();
                    navigateButton.setClickable(true);
                }
                gotoLatLngLocation(place.getLatLng());
                Log.i(TAG, "Place: " + place.getName());
            }
            
            @Override
            public void onError(Status status) {
                Toast.makeText(MapsActivity.this, "Place not found." + status.toString(), Toast.LENGTH_LONG).show();
                Log.i(TAG, "An error occurred: " + status);
            }
        });
    }
    
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // permission granted
            googleMap.setOnMarkerClickListener(this);
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setZoomControlsEnabled(true);
            mMap.getUiSettings().setMapToolbarEnabled(false);
            
            gotoDeviceLocation();
            setUpOnMyLocationButtonClickedListener();
    
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
    private void setUpOnMyLocationButtonClickedListener() {
        mMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {
                gotoDeviceLocation();
                return true;
            }
        });
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
    private void gotoDeviceLocation() {
        try {
            Task<Location> locationResult = mFusedLocationProviderClient.getLastLocation();
            locationResult.addOnCompleteListener(new OnCompleteListener<Location>() {
                @Override
                public void onComplete(@NonNull Task<Location> task) {
                    if (task.isSuccessful()) {
                        // Set the map's camera position to the current location of the device.
                        Location location = task.getResult();
                        LatLng currLatLng = new LatLng(location.getLatitude(),
                                location.getLongitude());
                        
                        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(currLatLng, 15.0F);
                        mMap.animateCamera(update);
                        
                        if (workLatLng == null) {
                            setUpWorkInfo(currLatLng);
                        }
                    }
                }
            });
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }
    
    private void setUpWorkInfo(LatLng currLatLng) {
        workLatLng = currLatLng;
        
        // adding work to list
        String workAddress = "";
        try {
            Geocoder geocoder = new Geocoder(mContext, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(workLatLng.latitude, workLatLng.longitude, 1);
            workAddress = addresses.get(0).getAddressLine(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        Delivery workDelivery = new Delivery("Work", workLatLng);
        workDelivery.setPrevAddress(workAddress);
    
        ArrayList<Delivery> initialList = new ArrayList<Delivery>();
        initialList.add(workDelivery);
        mAdapter.updateList(initialList);
        
        // setting up work marker
        workMarker = mMap.addMarker(new MarkerOptions()
                .title("Work").position(workLatLng)
                .icon(bitmapDescriptorFromVector(mContext, R.drawable.ic_work_black_24dp)));
    
        markerList.add(workMarker);
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
    public void gotoLatLngLocation(LatLng latLng) {
        mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
    }
    
    @Override
    public void onItemClick(View view, int position) {
        List<Delivery> list = mAdapter.getDeliveryList();
        
        Delivery deliveryClicked = (Delivery) list.get(position);
        gotoLatLngLocation(deliveryClicked.getLatLng());
        
        for (Marker markerEle : markerList) {
            if (markerEle.getPosition().equals(deliveryClicked.getLatLng())) {
                // delivery found
                markerEle.showInfoWindow();
                selectedMarker = markerEle;
                
                break;
            }
        }
    }
    
    @Override
    public boolean onMarkerClick(Marker marker) {
        this.selectedMarker = marker;
        selectedMarker.showInfoWindow();
        mMap.animateCamera(CameraUpdateFactory.newLatLng(marker.getPosition()));
        return true;
    }
    
    /**
     * Perform this action when Delete button is pressed.
     *
     * @param view The View.
     */
    public void deleteMarkerButton(View view) {
        if (!selectedMarker.equals(workMarker) && selectedMarker.isInfoWindowShown()) {
            // show dialog before deleting
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.delete_warning_title);
            builder.setMessage(R.string.delete_warning_message);
            
            builder.setPositiveButton(R.string.delete_warnining_confirm, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (selectedMarker.isInfoWindowShown()) {
                        markerList.remove(selectedMarker); // remove from markers List
                        selectedMarker.remove();
                        currentPolyline.remove();
                        
                        mAdapter.removeDelivery(mAdapter.findDeliveryByMarker(selectedMarker));
                        
                        if (!mAdapter.getDeliveryList().isEmpty()) {
                            // not empty, draw lines
                            drawPolylines();
                        } else {
                            // empty
                            navigateButton.setClickable(false);
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
            if (selectedMarker.equals(workMarker)) {
                Toast.makeText(this, R.string.work_marker_selected, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, R.string.no_marker_selected, Toast.LENGTH_LONG).show();
            }
        }
    }
    
    public void navigateButton(View view) {
        try {
            String googleNavURI = generateNavURI();
            Uri gmmIntentUri = Uri.parse(googleNavURI);
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");
            startActivity(mapIntent);
        } catch (Exception e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }
    
    private String generateNavURI() {
        
        return "";
    }
    
    /*
     *
     * (most) CODE BELOW COURTESY OF:
     * https://www.journaldev.com/13373/android-google-map-drawing-route-two-points
     *
     */
    
    /**
     * used to draw lines every time a marker is added or deleted.
     * Use sparingly; limited amount of uses per day.
     */
    private void drawPolylines() {
        currentPolyline.remove();
        // Check whether start and end locations are captured
        if (markerList.size() >= 1) {
            // making waypoints as deliveries
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
            ArrayList points = new ArrayList(); // = null
            PolylineOptions lineOptions = new PolylineOptions();
            MarkerOptions markerOptions = new MarkerOptions();
            
            ArrayList<String> distanceList = new ArrayList();
            ArrayList<String> durationList = new ArrayList();
            ArrayList<String> startAddressList = new ArrayList();
            ArrayList<String> endAddressList = new ArrayList();
            
            ArrayList<LatLng> startLatLngList = new ArrayList();
            ArrayList<LatLng> endLatLngList = new ArrayList();
            
            // for every List of List of HashMaps
            for (int i = 0; i < result.size(); i++) {
                
                switch (i) {
                    case 0: // 0 - path
                        List<HashMap<String, String>> path = result.get(0);
                        points = new ArrayList();
                        lineOptions = new PolylineOptions();
                        
                        // for each hashtable, add to points
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
                        
                        Log.i(TAG, "onPostExecute: " + currentPolyline.toString());
                        // Drawing polyline in the Google Map for the i-th route
                        currentPolyline = mMap.addPolyline(lineOptions);
                        
                        break;
                    
                    case 1: // 1 - Travel Data
                        List<HashMap<String, String>> travelDataListOfHM = result.get(1);
                        
                        // for each HashMap, add to it's lists
                        for (int j = 0; j < travelDataListOfHM.size(); j++) {
                            HashMap<String, String> travelData = travelDataListOfHM.get(j);
                            // unpacking data from hashtable to new arrayList of each data type
                            distanceList.add(travelData.get("distance"));
                            durationList.add(travelData.get("duration"));
                            
                            startAddressList.add(travelData.get("startAddress"));
                            endAddressList.add(travelData.get("endAddress"));
                            
                            double lat = Double.parseDouble(travelData.get("startLat"));
                            double lng = Double.parseDouble(travelData.get("startLng"));
                            startLatLngList.add(new LatLng(lat, lng));
                            
                            lat = Double.parseDouble(travelData.get("endLat"));
                            lng = Double.parseDouble(travelData.get("endLng"));
                            endLatLngList.add(new LatLng(lat, lng));
                        }
                        break;
                }// end switch
            } // all data recieved
            
            ArrayList<Delivery> newDeliveryList = new ArrayList<>();
            
            // for every json delivery, add it to a new delivery list, then send to mAdapter
            for (int idxOfLeg = 0; idxOfLeg < endLatLngList.size(); idxOfLeg++) {
                // unpacking data
                String endAddress = endAddressList.get(idxOfLeg);
                String startAddress = startAddressList.get(idxOfLeg);
                
                double distance = Double.parseDouble(distanceList.get(idxOfLeg));
                double duration = Double.parseDouble(durationList.get(idxOfLeg));
                
                double startLat = startLatLngList.get(idxOfLeg).latitude;
                double startLng = startLatLngList.get(idxOfLeg).longitude;
                
                double endLat = endLatLngList.get(idxOfLeg).latitude;
                double endLng = endLatLngList.get(idxOfLeg).longitude;
                
                if ((startLat != endLat) && (startLng != endLng)) {
                    // startLatLng and endLatLng are not the same.
                    PriorityQueue<Delivery> deliveryPQ = new PriorityQueue<>();
                    
                    for (Delivery deliveryCopy : mAdapter.getDeliveryList()) {
                        // for every delivery, find the smallest delta.
                        // delta will be the difference of two Lat Lng numbers.
                        
                        double delta = Math.abs(deliveryCopy.getLatLng().latitude - endLat)
                                + Math.abs(deliveryCopy.getLatLng().longitude - endLng);
                        
                        deliveryCopy.setDelta(delta);
                        deliveryPQ.add(deliveryCopy);
                    }
                    // closest delivery to the current iteration of Legs.
                    Delivery closestDelivery = deliveryPQ.peek();
                    
                    closestDelivery.setPrevAddress(startAddress);
                    closestDelivery.setAddress(endAddress);
                    
                    closestDelivery.setDistance(distance);
                    closestDelivery.setDuration(duration);
                    closestDelivery.setPrevLatLng(new LatLng(startLat, startLng));
                    
                    newDeliveryList.add(closestDelivery);
                }
            }
            mAdapter.updateList(newDeliveryList);
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
        
        // Building the url to the web service; used for drawing paths
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


