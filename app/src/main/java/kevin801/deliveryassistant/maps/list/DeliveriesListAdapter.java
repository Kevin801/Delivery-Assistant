package kevin801.deliveryassistant.maps.list;


import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.model.Marker;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

import kevin801.deliveryassistant.R;

public class DeliveriesListAdapter extends RecyclerView.Adapter<ViewHolder> {
    
    private static final String TAG = DeliveriesListAdapter.class.getSimpleName();
    private List<Delivery> deliveryList;
    private OnItemClicked mListener;
    private Context mContext;
    
    /**
     * Constructs the Context and Data set.
     *
     * @param context The context where it's being used.
     * @param dataSet The data being used.
     */
    public DeliveriesListAdapter(Context context, List<Delivery> dataSet) {
        this.mContext = context;
        deliveryList = dataSet;
    }
    
    public void updateList(ArrayList<Delivery> newDeliveryList) {
        deliveryList.clear();
        deliveryList.addAll(newDeliveryList);
        
        notifyDataSetChanged();
        
        Log.i(TAG, "updateList: ");
    }
    
    /**
     * Adds a Delivery to the list. Will not show up on List.
     *
     * @param delivery the Delivery to be added.
     */
    public void addDeliveryToList(Delivery delivery) {
        deliveryList.add(delivery);
        
    }
    
    /**
     * Gets the List of Deliveries.
     *
     * @return List of Deliveries.
     */
    public List<Delivery> getDeliveryList() {
        return deliveryList;
    }
    
    /**
     * Removes the Delivery from the list.
     *
     * @param delivery the Delivery designated inside the list.
     */
    public void removeDelivery(Delivery delivery) {
        if (!deliveryList.remove(delivery)) {
            // delivery not found
            throw new NoSuchElementException();
        }
        
        // remove reference by copying to new array
        ArrayList<Delivery> copy = new ArrayList<>();
        for (Delivery s : deliveryList) {
            copy.add(s);
        }
        
        // refreshing delivery list
        deliveryList.clear();
        deliveryList.addAll(copy);
        notifyDataSetChanged();
    }
    
    /**
     * Finds the Delivery associated with the given marker.
     *
     * @param marker The Delivery with the designated marker.
     * @return A Delivery where the marker is located. Null if not found.
     */
    public Delivery findDeliveryByMarker(Marker marker) {
        for (Delivery ele : deliveryList) {
            if (marker.getPosition().equals(ele.getLatLng())) {
                return ele;
            }
        }
        return null;
    }
    
    // Create new views (invoked by the layout manager)
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        
        View rowView = inflater.inflate(R.layout.activity_row_item, parent, false);
        
        ViewHolder vh = new ViewHolder(rowView, mListener);
        return vh;
    }
    
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Delivery delivery = deliveryList.get(position);
        // show only address, and city
        List<String> prevAddressSplit = Arrays.asList(delivery.getPrevAddress().split(","));
        List<String> endAddressSplit = Arrays.asList(delivery.getAddress().split(","));
        
        try {
            String strStart = "From: \t\t" + prevAddressSplit.get(0) + "," + prevAddressSplit.get(1);
            String strEnd = "To: \t\t\t\t\t" + endAddressSplit.get(0) + "," + endAddressSplit.get(1);
            
            // Set item views based on your views and data model
            holder.tvStartAddress.setText(strStart);
            holder.tvEndAddress.setText(strEnd);
            
            holder.tvTime.setText(secondsToMinutes(delivery.getDuration()));
            holder.tvDistance.setText(metersToMiles(delivery.getDistance()));
        } catch (Exception e) {
            Log.e(TAG, "onBindViewHolder: ExceptionHit", e);
            // usually throws when first starting up
        }
    }
    
    private String metersToMiles(double distance) {
        double num = Math.abs(distance) * 0.00062137119;
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(1);
        return nf.format(num) + " mi.";
    }
    
    private String secondsToMinutes(double time) {
        double num = Math.abs(time) / 60;
        return Math.round(num) + " min";
    }
    
    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return deliveryList.size();
    }
    
    public void setOnClick(OnItemClicked mListener) {
        this.mListener = mListener;
    }
}
