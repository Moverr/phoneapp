package com.example.phoneproject;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;

import androidx.core.app.ActivityCompat;
import androidx.room.Room;

import com.example.phoneproject.databinding.ActivityMainBinding;


import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import android.provider.ContactsContract;

public class MainActivity extends Activity implements SensorEventListener {

    private SensorManager sensorManager;
    //private boolean color = false;
    private View view;
    private long lastUpdate;


    private ActivityMainBinding binding;



    private float acceleration = 0f;
    private float currentAcceleration = 0f;
    private float lastAcceleration = 0f;
    private Sensor accelerometer;

    //private Sensor sensor;


    private static final int REQUEST_CONTACT = 1;
    private Button mContactPick;
    private Button mContactName;
    private FusedLocationProviderClient fusedLocationClient;
    private Location currentLocation;
    String[] perms = {"android.permission.ACCESS_FINE_LOCATION", "android.permission.ACCESS_COARSE_LOCATION","android.permission.INTERNET"};
    int permsRequestCode = 200;
    public static final String SMS_SENT_ACTION = "com.andriodgifts.gift.SMS_SENT_ACTION";
    public static final String SMS_DELIVERED_ACTION = "com.andriodgifts.gift.SMS_DELIVERED_ACTION";
    private AppDatabase appDatabase; // app's local database
    private int contactCounter = 0; // counter to limit number of contacts

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        view = findViewById(R.id.textView);
        view.setBackgroundColor(Color.GREEN);

        this.SensorActivity();
        lastUpdate = System.currentTimeMillis();

        // Initialize the app's local database
        appDatabase = Room.databaseBuilder(getApplicationContext(),
                AppDatabase.class, "my-database").build();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(perms, permsRequestCode);
            return;
        }
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    // Got last known location. In some rare situations this can be null.
                    if (location != null) {
                        // Logic to handle location object
                        currentLocation = location;

                    }
                });

        mContactPick = findViewById(R.id.contact_pick);
        mContactName = findViewById(R.id.contact_name);

        final Intent pickContact = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);

        mContactPick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (contactCounter < 5) {
                    startActivityForResult(pickContact, REQUEST_CONTACT);
                } else {
                    Toast.makeText(MainActivity.this, "You have reached the limit of 5 contacts.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CONTACT && resultCode == Activity.RESULT_OK) {
            Uri contactUri = data.getData();
            String[] projection = {ContactsContract.CommonDataKinds.Phone.NUMBER, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME};

            Cursor cursor = getContentResolver().query(contactUri, projection, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                String number = cursor.getString(0);
                String name = cursor.getString(1);

                // Store the picked contact in the app's local database
                Contact contact = new Contact(name, number);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        appDatabase.contactDao().insert(contact);
                    }
                }).start();

                // Update the UI to show the picked contact's name
                mContactName.setText(name);

                cursor.close();
            }
        }
    }


    @Override
    public void onRequestPermissionsResult(int permsRequestCode, String[] permissions, int[] grantResults){

       switch(permsRequestCode){

           case 200:

                boolean locationAccepted = grantResults[0]==PackageManager.PERMISSION_GRANTED;
               boolean cameraAccepted = grantResults[1]==PackageManager.PERMISSION_GRANTED;

              break;

    }

    }




    public void SensorActivity() {
        sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    }
    int count = 0;
    @Override
    public void onSensorChanged(SensorEvent event) {

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER  ) {

          //  Toast.makeText(this, ""+count, Toast.LENGTH_SHORT).show();

                //count = 0;
            try {
                getAccelerometer(event);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }


        }

    }



    private void getAccelerometer(SensorEvent event) throws InterruptedException {

        listenToSensors(event);

    }

    int vertical_count = 0;
    int horizontal_count = 0;
    boolean isHorinzontal =  false;
    private void listenToSensors(SensorEvent event) throws InterruptedException {

        boolean status = false;

        String message = "";

        if (isPhoneVertical(event)) {
            if(isHorinzontal == false){

                vertical_count = vertical_count + 1;
                view.setBackgroundColor(Color.GREEN);
                message = "vertical_count" + vertical_count;
                isHorinzontal = true;
                Toast.makeText(this, "NIO", Toast.LENGTH_SHORT).show();
            }



        } else {
            if(isHorinzontal == true) {
                horizontal_count = horizontal_count + 1;
                view.setBackgroundColor(Color.RED);
                message = "horizontal_count" + horizontal_count;
                isHorinzontal = false;
                Toast.makeText(this, "DEM", Toast.LENGTH_SHORT).show();
            }

        }
        //if(!message.isEmpty())
        //    Toast.makeText(this, "HOR : "+horizontal_count, Toast.LENGTH_SHORT).show();
        if(vertical_count > 3 ){
            Toast.makeText(this, "Make Alarm", Toast.LENGTH_SHORT).show();
            vertical_count = horizontal_count =  0;
            this.makeAlam();




        }
    }


    private void makeAlam(){
        //todo: send notification
        if(currentLocation != null) {
            Toast.makeText(MainActivity.this, "" + currentLocation.getLatitude(), Toast.LENGTH_SHORT).show();
        }


        String location = getAddress(currentLocation.getLatitude(), currentLocation.getLongitude());
        String message = "I am in trouble. Please reach me urgently at " + location;
        sendSMS("", message);
        sendSMS("", message);
        sendSMS("", message);



    }

    public String getAddress(double lat, double lng) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
            Address obj = addresses.get(0);
            String add = obj.getAddressLine(0);
            add = add + "\n" + obj.getCountryName();
            add = add + "\n" + obj.getCountryCode();
            add = add + "\n" + obj.getAdminArea();
            add = add + "\n" + obj.getPostalCode();
            add = add + "\n" + obj.getSubAdminArea();
            add = add + "\n" + obj.getLocality();
            add = add + "\n" + obj.getSubThoroughfare();

            Log.v("IGA", "Address" + add);
            // Toast.makeText(this, "Address=>" + add,
            // Toast.LENGTH_SHORT).show();
            return  add;

            // TennisAppActivity.showDialog(add);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return  "";
    }




    private double maxVertical = 3.0;
    private boolean isPhoneVertical(SensorEvent event) {
        float[] values = event.values;
        double y = values[1];
        // do not change this value
        double yAxisInitValue = 10.0;
        double verMargin = yAxisInitValue - maxVertical;

        return y >= verMargin;
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }





    @Override
    protected void onResume() {
        super.onResume();
        // register this class as a listener for the orientation and
        // accelerometer sensors
        sensorManager.registerListener(this,
                accelerometer,
                SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        // unregister listener
        super.onPause();
        sensorManager.unregisterListener(this);
    }
    public void sendSMS(String phoneNo, String msg) {


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
            try {
                SmsManager smsManager = SmsManager.getDefault();
                smsManager.sendTextMessage(phoneNo, null, msg, null, null);

                smsManager.sendTextMessage(phoneNo, null, msg, PendingIntent.getBroadcast(
                        this, 0, new Intent(SMS_SENT_ACTION), 0), PendingIntent.getBroadcast(this, 0, new Intent(SMS_DELIVERED_ACTION), 0));

                Toast.makeText(getApplicationContext(), "Message Sent",
                        Toast.LENGTH_LONG).show();
            } catch (Exception ex) {
                Toast.makeText(getApplicationContext(), ex.getMessage().toString(),
                        Toast.LENGTH_LONG).show();
                ex.printStackTrace();
            }
        }
        else
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            {
                requestPermissions(new String[]{Manifest.permission.SEND_SMS}, 10);
            }
        }
    }





}