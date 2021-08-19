package com.example.final_project.Adapters;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.final_project.AddressItem;
import com.example.final_project.R;

import java.util.ArrayList;


public class SilencerAdapter extends RecyclerView.Adapter<CardViewHolder>{

    private final ArrayList<AddressItem> mItems;
    private boolean wasRestored;

    public SilencerAdapter(ArrayList<AddressItem> mItems, boolean wasRestored) {
        this.mItems = mItems;
        this.wasRestored = wasRestored;
    }

    @NonNull
    @Override
    public CardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.main_include_input_box, parent, false);
        return new CardViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull  CardViewHolder holder, int position) {
        AddressItem current = mItems.get(position);
        holder.tiet_street.setText(current.getStreet());
        holder.tiet_city.setText(current.getCity());
        holder.tiet_state.setText(current.getState());
        holder.silent_button.setTag(Integer.toString(position));
        holder.vibrate_button.setTag(Integer.toString(position));


        holder.tiet_street.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus){
                    mItems.get(position).setStreet(holder.tiet_street.getText().toString());
                }
            }
        });

        holder.tiet_city.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus){
                    mItems.get(position).setCity(holder.tiet_city.getText().toString());
                }
            }
        });

        holder.tiet_state.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus){
                    mItems.get(position).setState(holder.tiet_state.getText().toString());
                }
            }
        });
    }


    @Override
    public int getItemCount() {
        return mItems.size();
    }


}
