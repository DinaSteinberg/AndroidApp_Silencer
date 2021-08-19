package com.example.final_project.Adapters;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.final_project.R;
import com.google.android.material.textfield.TextInputEditText;

public class CardViewHolder extends RecyclerView.ViewHolder {

    public final CardView cv_input_card;
    public final TextView tv_title;
    public final TextInputEditText tiet_street, tiet_city, tiet_state;
    public final Button silent_button, vibrate_button;

    public CardViewHolder(View view) {
        super(view);

        cv_input_card = (CardView) view.findViewById(R.id.inner_input_card);
        tv_title = (TextView) view.findViewById(R.id.title_text_view);
        tiet_street = (TextInputEditText) view.findViewById(R.id.street);
        tiet_city = (TextInputEditText) view.findViewById(R.id.city);
        tiet_state = (TextInputEditText) view.findViewById(R.id.state);
        silent_button = (Button) view.findViewById(R.id.silent_button);
        vibrate_button = (Button) view.findViewById(R.id.vibrate_button);


    }
}

