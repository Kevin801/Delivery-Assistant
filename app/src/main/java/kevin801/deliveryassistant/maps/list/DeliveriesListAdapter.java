package kevin801.deliveryassistant.maps.list;


import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import kevin801.deliveryassistant.R;

public class DeliveriesListAdapter extends RecyclerView.Adapter<ViewHolder> {
    
    private static final String TAG = DeliveriesListAdapter.class.getSimpleName();
    private List<Delivery> deliveryList;
    private OnItemClicked mListener;
    private Context mContext;
    private LatLng workLatLng;
    
    /**
     * Constructs the Context and Data set.
     * @param context The context where it's being used.
     * @param dataSet The data being used.
     */
    public DeliveriesListAdapter(Context context, List<Delivery> dataSet) {
        this.mContext = context;
        deliveryList = dataSet;
    }
    
    public void setWorkLatLng(LatLng workLatLng) {
        this.workLatLng = workLatLng;
    }
    
    public void updateList(ArrayList<Delivery> newDeliveryList) {
        deliveryList.clear();
        deliveryList.addAll(newDeliveryList);
        
        notifyDataSetChanged();
    
        Log.i(TAG, "updateList: ");
    }
    
    /**
     * Adds a Delivery to the list. Will not show up on List.
     * @param delivery the Delivery to be added.
     */
    public void addDeliveryToList(Delivery delivery) {
        deliveryList.add(delivery);
        
    }
    
    /**
     * Gets the List of Deliveries.
     * @return List of Deliveries.
     */
    public List<Delivery> getDeliveryList() {
        return deliveryList;
    }
    
    /**
     * Removes the Delivery from the list.
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
     * @param marker The Delivery with the designated marker.
     * @return A Delivery where the marker is located. Null if not found.
     */
    public Delivery findDeliveryByMarker(Marker marker) {
        for (Delivery ele : deliveryList) {
            if (marker.getPosition().equals(ele.getLatLng())){
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
    
        // Set item views based on your views and data model
        holder.tvAddress.setText(delivery.getAddress());

        String time = mContext.getString(R.string.delevery_time_list).toString();
        String text = String.format("%s %.1f", time, secondsToMinutes(delivery.getTime()));
        holder.tvTime.setText(text);
        
        String dist = mContext.getString(R.string.delevery_dist_list);
        text = String.format("%s %.1f", dist, metersToMiles(delivery.getDistance()));
        holder.tvDistance.setText(text);
    }
    
    private double metersToMiles(double distance) {
        return Math.abs(distance) * 0.00062137119;
    }
    
    private double secondsToMinutes(double time) {
        return Math.abs(time) / 60;
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
