package com.example.final_project.Activities;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.view.View;

import androidx.core.app.ActivityCompat;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.final_project.Adapters.SilencerAdapter;
import com.example.final_project.Models.AddressItem;
import com.example.final_project.R;
import com.example.final_project.Utils;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static androidx.preference.PreferenceManager.getDefaultSharedPreferences;

public class MainActivity extends AppCompatActivity {

    //for location
    Double latitude = 0.0;
    Double longitude = 0.0;
    private FusedLocationProviderClient fusedLocationClient;
    AddressItem currentLocation;

    //For permissions
    private final int REQUEST_CODE = 5;

    //Keys
    private final String mkey_address = "ADDRESS_LIST";
    private final String mkey_silent = "SILENT";
    private final String mkey_vibrate = "VIBRATE";

    //For the audio
    AudioManager am;

    private SilencerAdapter mAdapter;
    private ArrayList<AddressItem> mItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        toStart();
        Toolbar toolbar = setupViews();
        setSupportActionBar(toolbar);

        mItems = savedInstanceState == null ? new ArrayList<>() :
                AddressItem.getListFromGSONString(savedInstanceState.getString(mkey_address));

        restoreFromPreferences();
        createFirstBox();
        setupAdapter(savedInstanceState);
        setupFAB();

    }

    private void toStart() {
        PreferenceManager.setDefaultValues(this, R.xml.root_preferences, false);
        Utils.setNightModeOnOffFromPreferenceValue(getApplicationContext(), getString(R.string.night_mode_key));
    }

    private void createFirstBox() {
        if (mItems.size() == 0) {
            mItems.add(new AddressItem());
        }
    }


    private void setupFAB() {
        findViewById(R.id.fab).setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View view) {
                addNewItem();
                getPermissions();
            }
        });
    }

    private void setupAdapter(Bundle savedInstanceState) {
        mAdapter = new SilencerAdapter(mItems, savedInstanceState != null);
        RecyclerView rv = findViewById(R.id.rv_addresses);
        int span = getResources().getInteger(R.integer.recycler_view_span);
        rv.setLayoutManager(new GridLayoutManager(getApplicationContext(), span));
        rv.setAdapter(mAdapter);
    }

    private Toolbar setupViews() {
        setContentView(R.layout.activity_main);
        return findViewById(R.id.toolbar);
    }

    private void addNewItem() {
        mItems.add(new AddressItem());
        mAdapter.notifyItemInserted(mItems.size() - 1);

    }

    @Override
    protected void onStop() {
        super.onStop();
        saveListToPreferences();
    }

    private void saveListToPreferences() {
        SharedPreferences.Editor editor = createPrefsEditor();
        editor.putString(mkey_address, AddressItem.getGSONStringFromList(mItems));
        editor.apply();
    }

    private void restoreFromPreferences() {
        SharedPreferences defaultSharedPreferences = getDefaultSharedPreferences(this);
        String addressString = defaultSharedPreferences.getString(mkey_address, null);
        if (addressString != null) {
            mItems = AddressItem.getListFromGSONString(addressString);
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(mkey_address, AddressItem.getGSONStringFromList(mItems));
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mItems = AddressItem.getListFromGSONString(savedInstanceState.getString(mkey_address));
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    private void getPermissions() {
        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_NETWORK_STATE) == PackageManager.PERMISSION_GRANTED) {
            getLocation();
        } else if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION) &&
                shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) &&
                shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_NETWORK_STATE)) {

            requestPermissionRationale();

        } else {
            //requestPermissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION);
            requestPermissions(new String[]{Manifest.permission.ACCESS_NETWORK_STATE,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
            if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_NETWORK_STATE) != PackageManager.PERMISSION_GRANTED) {
                getLocation();
            }
        }
    }

    private void requestPermissionRationale() {
        Utils.showInfoDialog(MainActivity.this, "Request Permissions",
                "We need to access your location to run this app. Please enable access.");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE) {// If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLocation();
            } else {
                Utils.showInfoDialog(MainActivity.this, "App Unable to run",
                        "This app is unable to run without the permissions enabled.");
            }
        }

    }

    private void getLocation() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_NETWORK_STATE) != PackageManager.PERMISSION_GRANTED) {

            Toast.makeText(MainActivity.this, "Access Denied", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(MainActivity.this, "Access Granted", Toast.LENGTH_SHORT).show();
            fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
                if (location != null) {
                    latitude = location.getLatitude();
                    longitude = location.getLongitude();
                    try {
                        Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
                        List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
                        Toast.makeText(MainActivity.this, "Geocoder was made", Toast.LENGTH_SHORT).show();
                        if (addresses != null) {
                            String address = addresses.get(0).getAddressLine(0);
                            String city = addresses.get(0).getLocality();
                            String state = addresses.get(0).getAdminArea();
                            String zip_code = addresses.get(0).getPostalCode();
                            String country = addresses.get(0).getCountryCode();
                            String name = addresses.get(0).getFeatureName();

                            currentLocation = new AddressItem(address, city, state);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private void onPressed() {

        am = (AudioManager) getBaseContext().getSystemService(Context.AUDIO_SERVICE);

        //For Normal mode
        am.setRingerMode(AudioManager.RINGER_MODE_NORMAL);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            showSettings();
            return true;
        } else if (id == R.id.action_about) {
            Utils.showInfoDialog(this, "About this app", "This app can turn your phone on silent or " +
                    "vibrate when you reach a certain destination. It's very helpful for meetings, study sessions, or" +
                    "a special distraction free place. No more unexpected ringing when your phone was supposed to be on silent.");
        } else if (id == R.id.action_saved_items) {
            showSavedItems();
        }

        return super.onOptionsItemSelected(item);
    }

    private void showSettings() {
        Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
        startActivityForResult(intent, 1);
    }

    private void showSavedItems() {
        Intent intent = new Intent(getApplicationContext(), SavedAddressesActivity.class);
        intent.putExtra(mkey_address, AddressItem.getGSONStringFromList(mItems));
        startActivity(intent);
    }


    private SharedPreferences.Editor createPrefsEditor() {
        SharedPreferences defaultSharedPreferences = getDefaultSharedPreferences(this);
        return defaultSharedPreferences.edit();
    }

    public void makeSilent(View view) {
        //update preferences
        SharedPreferences.Editor editor = createPrefsEditor();
        editor.putBoolean(mkey_silent, true);
        editor.apply();

        am = (AudioManager) getBaseContext().getSystemService(Context.AUDIO_SERVICE);
        int position = Integer.parseInt((String) view.getTag());
        AddressItem currentItem = mItems.get(position);
        while (currentItem.equals(currentLocation)) am.setRingerMode(AudioManager.RINGER_MODE_SILENT);
    }

    public void makeVibrate(View view) {
        //update preferences
        SharedPreferences.Editor editor = createPrefsEditor();
        editor.putBoolean(mkey_vibrate, true);
        editor.apply();

        am = (AudioManager) getBaseContext().getSystemService(Context.AUDIO_SERVICE);
        int position = Integer.parseInt((String) view.getTag());
        AddressItem currentItem = mItems.get(position);
        while (currentItem.equals(currentLocation)) am.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
    }

}