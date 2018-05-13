package kevin801.deliveryassistant.maps;

import android.content.Context;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.TextView;


import java.util.ArrayList;

import kevin801.deliveryassistant.R;

public class DeliveriesListAdapter extends ArrayAdapter<Delivery> implements View.OnClickListener {
    
    private ArrayList<Delivery> deliveries;
    Context mContext;
    
    // View lookup cache
    private static class ViewHolder {
        TextView txtAddress;
        TextView txtTime;
        TextView txtDistance;
    }
    
    public DeliveriesListAdapter(ArrayList<Delivery> data, Context context) {
        super(context, R.layout.activity_row_item, data);
        this.deliveries = data;
        this.mContext = context;
        
    }
    
    public ArrayList<Delivery> getList() {
        return new ArrayList<>();
    }
    
    @Override
    public void onClick(View v) {
    
        int position = (Integer) v.getTag();
        Object object = getItem(position);
        Delivery delivery = (Delivery) object;
    
        Snackbar.make(v, "Release date " + delivery.getAddress(), Snackbar.LENGTH_LONG)
                .setAction("No action", null).show();
        
    }
    
    private int lastPosition = -1;
    
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        Delivery delivery = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        ViewHolder viewHolder; // view lookup cache stored in tag
        
        final View result;
        
        if (convertView == null) {
            
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.activity_row_item, parent, false);
            viewHolder.txtAddress = (TextView) convertView.findViewById(R.id.address_row_item);
            viewHolder.txtTime = (TextView) convertView.findViewById(R.id.time_row_item);
            viewHolder.txtDistance = (TextView) convertView.findViewById(R.id.distance_row_item);
            
            result = convertView;
            
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
            result = convertView;
        }
        
        lastPosition = position;
        
        viewHolder.txtAddress.setText(delivery.getAddress());
        viewHolder.txtTime.setText("Time : " + delivery.getTime());
        viewHolder.txtDistance.setText("Dist: " + delivery.getDistance());
        
        // Return the completed view to render on screen
        return convertView;
    }
    

}
