package kevin801.deliveryassistant.maps.list;


import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import kevin801.deliveryassistant.R;

public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
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