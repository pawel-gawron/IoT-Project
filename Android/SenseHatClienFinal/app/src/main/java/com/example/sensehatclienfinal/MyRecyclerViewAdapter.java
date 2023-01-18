package com.example.sensehatclienfinal;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MyRecyclerViewAdapter extends RecyclerView.Adapter<MyRecyclerViewAdapter.ViewHolder> {

    private List<String> mDataValue;
    private List<String> mDataName;
    private List<String> mDataUnit;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;
    private int mpositionName;
    private int mpositionValue;
    private int mpositionUnit;



    // data is passed into the constructor
    MyRecyclerViewAdapter(List<String> dataName, List<String> dataValue, List<String> dataUnit, int positionName, int positionValue, int positionUnit) {
        this.mDataValue = dataValue;
        this.mDataUnit = dataUnit;
        this.mDataName = dataName;
        this.mpositionName = positionName;
        this.mpositionValue = positionValue;
        this.mpositionUnit = positionUnit;
    }

    // inflates the row layout from xml when needed
    @Override
    public MyRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View contactView = inflater.inflate(R.layout.activity_dynamic_list_measurements, parent, false);
        ViewHolder viewHolder = new ViewHolder(contactView);

        return viewHolder;
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(MyRecyclerViewAdapter.ViewHolder holder, int position) {
        try {
            String dataName = mDataName.get(position);
            String dataValue = String.valueOf(mDataValue.get(position));
            String dataUnit = mDataUnit.get(position);

            TextView textViewName = holder.myTextViewName;
            TextView textViewValue = holder.myTextViewValue;
            TextView textViewUnit = holder.myTextViewUnit;

            textViewName.setText(dataName);
            textViewValue.setText(dataValue);
            textViewUnit.setText(dataUnit);
//            holder.myTextView.setText(dataName);

        } catch (Exception e) {
            // This will catch any exception, because they are all descended from Exception
            System.out.println("Error " + e.getMessage());
//            System.out.println("Error message " + mData.get(positionName));
        }

    }

    // total number of rows
    @Override
    public int getItemCount() {
        return mDataName.size();
    }


    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView myTextViewName;
        TextView myTextViewValue;
        TextView myTextViewUnit;

        ViewHolder(View itemView) {
            super(itemView);
            myTextViewName = itemView.findViewById(mpositionName);
            myTextViewValue = itemView.findViewById(mpositionValue);
            myTextViewUnit = itemView.findViewById(mpositionUnit);
//            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }
    }

    // convenience method for getting data at click position
    String getItem(int id) {
        return mDataName.get(id);
    }

    // allows clicks events to be caught
    void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }
}
