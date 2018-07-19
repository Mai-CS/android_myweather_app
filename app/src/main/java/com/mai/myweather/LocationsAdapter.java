package com.mai.myweather;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Mai on 7/19/2018.
 */

@SuppressWarnings({"DefaultFileTemplate", "WeakerAccess"})
public class LocationsAdapter extends RecyclerView.Adapter<LocationsAdapter.ViewHolder>
{
    private ArrayList<CustomLocation> mLocationsList;


    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    @SuppressWarnings("WeakerAccess")
    public static class ViewHolder extends RecyclerView.ViewHolder
    {
        public TextView tempTextView, nameTextView, statusTextView;

        public ViewHolder(View view)
        {
            super(view);
            tempTextView = view.findViewById(R.id.temp_text_view);
            nameTextView = view.findViewById(R.id.name_text_view);
            statusTextView = view.findViewById(R.id.status_text_view);
        }
    }

    public LocationsAdapter(ArrayList<CustomLocation> mLocationsList)
    {
        this.mLocationsList = mLocationsList;
    }

    // Create new views (invoked by the layout manager)
    @NonNull
    @Override
    public LocationsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                                          int viewType)
    {
        // create a new view
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_location, parent, false);
        return new ViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position)
    {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        CustomLocation customLocation = mLocationsList.get(position);
        holder.tempTextView.setText(customLocation.getTemp());
        holder.nameTextView.setText(customLocation.getName());
        holder.statusTextView.setText(customLocation.getStatus());

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount()
    {
        return mLocationsList.size();
    }
}
