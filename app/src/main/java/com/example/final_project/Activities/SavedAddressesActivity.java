package com.example.final_project.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.example.final_project.Models.AddressItem;
import com.example.final_project.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

import static androidx.preference.PreferenceManager.getDefaultSharedPreferences;

public class SavedAddressesActivity extends AppCompatActivity {

    private TextView tv_address_info;

    private ArrayList<AddressItem> mItems;
    private final String mkey_address = "ADDRESS_LIST";
    private final String mkey_silent = "SILENT";
    private final String mkey_vibrate = "VIBRATE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_addresses);
        setupToolbar();
        setupFAB();
        getIncomingData();
        tv_address_info = findViewById(R.id.address_info_box);
        fillTextView();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() !=null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void setupFAB() {
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    private void getIncomingData() {
        Intent intent = getIntent();
        String gameGSON = intent.getStringExtra(mkey_address);
        mItems = AddressItem.getListFromGSONString(gameGSON);
    }

    private void fillTextView() {
        SharedPreferences defaultSharedPreferences = getDefaultSharedPreferences(this);

        boolean silentMode = defaultSharedPreferences.getBoolean(mkey_silent, false);
        boolean vibrateMode = defaultSharedPreferences.getBoolean(mkey_vibrate, false);

        StringBuilder text = new StringBuilder();
        for(AddressItem m:mItems){
            text.append(m.getAddress() + "\nMode:");
            if(silentMode)text.append("Silent Mode\n\n");
            else if(vibrateMode)text.append("Vibrate Mode\n\n");
            else text.append("No mode selected. Please choose a mode next to this address.\n\n");
        }
        tv_address_info.setText(text.toString());
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        } else
            return super.onOptionsItemSelected(item);
    }
}