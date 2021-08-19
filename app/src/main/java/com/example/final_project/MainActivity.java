package com.example.final_project;

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
import androidx.navigation.ui.AppBarConfiguration;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.final_project.Adapters.SilencerAdapter;
import com.example.final_project.databinding.ActivityMainBinding;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static androidx.preference.PreferenceManager.getDefaultSharedPreferences;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;

    //for location
    TextView location_txt;
    Double latitude = 0.0;
    Double longitude = 0.0;
    private FusedLocationProviderClient fusedLocationClient;
    AddressItem currentLocation;
    AddressItem enteredLocation;

    //For permissions
    private final int REQUEST_CODE = 5;
    private final String ADDRESS_KEY = "Key_List";

    //For the audio
    AudioManager am;

    private SilencerAdapter mAdapter;
    private ArrayList<AddressItem> mItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Toolbar toolbar = setupViews();
        setSupportActionBar(toolbar);

        mItems = savedInstanceState == null ? new ArrayList<>():
        AddressItem.getListFromGSONString(savedInstanceState.getString(ADDRESS_KEY));

        restoreFromPreferences();
        createFirstBox();
        setupAdapter(savedInstanceState);
        setupFAB();

    }

    private void createFirstBox() {
        if(mItems.size() == 0){
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
        Toolbar toolbar = findViewById(R.id.toolbar);
        return toolbar;
    }

    private void addNewItem() {
        mItems.add(new AddressItem());
        mAdapter.notifyItemInserted (mItems.size()-1);

    }

    @Override
    protected void onStop() {
        super.onStop();
        saveListToPreferences();
    }

    private void saveListToPreferences() {
        SharedPreferences defaultSharedPreferences = getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = defaultSharedPreferences.edit();
        editor.putString(ADDRESS_KEY, AddressItem.getGSONStringFromList(mItems));
    }

    private void restoreFromPreferences() {
        SharedPreferences defaultSharedPreferences = getDefaultSharedPreferences(this);
        if (defaultSharedPreferences.getBoolean(ADDRESS_KEY,true)) {
            String addressString = defaultSharedPreferences.getString(ADDRESS_KEY, null);
            if (addressString!=null) {
                mItems = AddressItem.getListFromGSONString(addressString);
            }
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull  Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(ADDRESS_KEY, AddressItem.getGSONStringFromList(mItems));
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mItems = AddressItem.getListFromGSONString(savedInstanceState.getString(ADDRESS_KEY));
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
        showInfoDialog(MainActivity.this, "Request Permissions",
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
                showInfoDialog(MainActivity.this, "App Unable to run",
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
                        Toast.makeText(getApplicationContext(), e.toString(),Toast.LENGTH_SHORT).show();
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
        }else if(id == R.id.action_about){
            showInfoDialog(this,"About this app","This app can turn your phone on silent or " +
                    "vibrate when you reach a certain destination. It's very helpful for meetings, study sessions, or" +
                    "a special distraction free place. No more unexpected ringing when your phone was supposed to be on silent.");
        }

        return super.onOptionsItemSelected(item);
    }

    private void showSettings() {
        Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
        startActivityForResult(intent, 1);
    }


    /**
     * Shows an Android (nicer) equivalent to JOptionPane
     *
     * @param strTitle Title of the Dialog box
     * @param strMsg   Message (body) of the Dialog box
     */
    public static void showInfoDialog(Context context, String strTitle, String strMsg) {
        // create the listener for the dialog
        final DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        };

        // Create the AlertDialog.Builder object
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);

        // Use the AlertDialog's Builder Class methods to set the title, icon, message, et al.
        // These could all be chained as one long statement, if desired
        alertDialogBuilder.setTitle(strTitle);
        alertDialogBuilder.setIcon(R.mipmap.ic_launcher);
        alertDialogBuilder.setMessage(strMsg);
        alertDialogBuilder.setCancelable(true);
        alertDialogBuilder.setNeutralButton(context.getString(android.R.string.ok), listener);

        // Create and Show the Dialog
        alertDialogBuilder.show();
    }

    public void makeSilent(View view) {
        am = (AudioManager) getBaseContext().getSystemService(Context.AUDIO_SERVICE);
        int position = Integer.parseInt((String) view.getTag());
        AddressItem currentItem = mItems.get(position);
        while (currentItem.equals(currentItem))am.setRingerMode(AudioManager.RINGER_MODE_SILENT);
    }

    public void makeVibrate(View view) {
        am = (AudioManager) getBaseContext().getSystemService(Context.AUDIO_SERVICE);
        int position = Integer.parseInt((String) view.getTag());
        AddressItem currentItem = mItems.get(position);
        while (currentItem.equals(currentItem))am.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
    }



}