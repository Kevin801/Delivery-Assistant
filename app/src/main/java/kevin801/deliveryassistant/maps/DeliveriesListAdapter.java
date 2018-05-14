package kevin801.deliveryassistant.maps;


import android.content.Context;
import android.content.res.Resources;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import kevin801.deliveryassistant.R;

public class DeliveriesListAdapter extends RecyclerView.Adapter<ViewHolder> {
    
    private List<Delivery> deliveryList;
    private OnItemClicked mListener;
    private Context mContext;
    
    public DeliveriesListAdapter(Context context, List<Delivery> dataSet) {
        this.mContext = context;
        deliveryList = dataSet;
    }
    
    public void addData(Delivery delivery) {
    
        deliveryList.add(delivery);
    
        ArrayList<Delivery> copy = new ArrayList<>();
        for (Delivery s : deliveryList) {
            copy.add(s);
        }
        
        deliveryList.clear();
        deliveryList.addAll(copy);
        notifyDataSetChanged();
    }
    
    public List<Delivery> getData() {
        return deliveryList;
    }
    
    // Create new views (invoked by the layout manager)
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        
//        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(mContext);
        
        View rowView = inflater.inflate(R.layout.activity_row_item, parent, false);

        ViewHolder vh = new ViewHolder(rowView, mListener);
        return vh;
    }
    
    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        Delivery delivery = deliveryList.get(position);
    
        // Set item views based on your views and data model
        holder.tvAddress.setText(delivery.getAddress());

        String time = mContext.getString(R.string.delevery_time_list).toString();
        String text = String.format("%s %.1f", time, delivery.getTime());
        holder.tvTime.setText(text);
    
        String dist = mContext.getString(R.string.delevery_dist_list);
        text = String.format("%s %.2f", dist, delivery.getDistance());
        holder.tvDistance.setText(text);
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
