package kevin801.deliveryassistant.maps;


import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

import kevin801.deliveryassistant.R;

public class DeliveriesListAdapter extends RecyclerView.Adapter<DeliveriesListAdapter.ViewHolder> {
    
    private List<Delivery> deliveryList = new ArrayList<>();
    private OnItemClicked mListener;
    
    public interface OnItemClicked {
        void onItemClick(View view, int position);
    }
    
    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        TextView tvAddress;
        TextView tvTime;
        TextView tvDistance;
        private OnItemClicked mListener;
        
        public ViewHolder(View view, OnItemClicked listener) {
            super(view);
            tvAddress = (TextView) itemView.findViewById(R.id.address_row_item);
            tvTime = (TextView) itemView.findViewById(R.id.time_row_item);
            tvDistance = (TextView) itemView.findViewById(R.id.distance_row_item);
            mListener = listener;
            view.setOnClickListener(this);
        }
    
        @Override
        public void onClick(View view) {
            mListener.onItemClick(view, getAdapterPosition());
        }
    }
    
    public DeliveriesListAdapter(/*ArrayList<Delivery> deliveries*/) {
//        this.deliveryList = deliveries;
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
    public DeliveriesListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        
        View rowView = inflater.inflate(R.layout.activity_row_item, parent, false);

        ViewHolder vh = new ViewHolder(rowView, mListener);
        return vh;
    }
    
    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        Delivery delivery = deliveryList.get(position);
    
        // Set item views based on your views and data model
        TextView textView = holder.tvAddress;
        textView.setText(delivery.getAddress());
    
        textView = holder.tvTime;
        textView.setText("Time: " + delivery.getTime());
    
        textView = holder.tvDistance;
        textView.setText("Dist: " + delivery.getDistance());
    }
    
    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return deliveryList.size();
    }
    
    public void setOnClick(OnItemClicked mListener) {
        this.mListener = mListener;
    }
    
//    public ArrayList<Delivery> getList() {
//        // TODO: needs to return the data of this list.
//        return new ArrayList<>();
//    }
//
//
//    public Delivery getItem(int position){
//        return deliveries.get(position);
//    }
//
//    @Override
//    public View getView(int position, View convertView, ViewGroup parent) {
//        // Get the data item for this position
//        Delivery delivery = getItem(position);
//        // Check if an existing view is being reused, otherwise inflate the view
//        ViewHolder viewHolder; // view lookup cache stored in tag
//
//        final View result;
//
//        if (convertView == null) {
//
//            viewHolder = new ViewHolder();
//            LayoutInflater inflater = LayoutInflater.from(getContext());
//            convertView = inflater.inflate(R.layout.activity_row_item, parent, false);
//            viewHolder.txtAddress = (TextView) convertView.findViewById(R.id.address_row_item);
//            viewHolder.txtTime = (TextView) convertView.findViewById(R.id.time_row_item);
//            viewHolder.txtDistance = (TextView) convertView.findViewById(R.id.distance_row_item);
//
//            result = convertView;
//
//            convertView.setTag(viewHolder);
//        } else {
//            viewHolder = (ViewHolder) convertView.getTag();
//            result = convertView;
//        }
//
//        lastPosition = position;
//
//        viewHolder.txtAddress.setText(delivery.getAddress());
//        viewHolder.txtTime.setText("Time : " + delivery.getTime());
//        viewHolder.txtDistance.setText("Dist: " + delivery.getDistance());
//
//        // Return the completed view to render on screen
//        return convertView;
//    }
}
