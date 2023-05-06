package com.example.phoneproject;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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

import com.example.phoneproject.dtos.Contact;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.example.phoneproject.databinding.ActivityMainBinding;


import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import android.provider.ContactsContract;

public class MainActivity extends Activity implements SensorEventListener {

    private SensorManager sensorManager;
    //private boolean color = false;
    private View view;
    private long lastUpdate;



    private ContactDatabaseHelper contactDbHelper;

    private float acceleration = 0f;
    private float currentAcceleration = 0f;
    private float lastAcceleration = 0f;
    private Sensor accelerometer;

    //private Sensor sensor;


    private static final int REQUEST_CONTACT = 1;

    private Button manageContactsButton;
    private Button mContactName;

    private FusedLocationProviderClient fusedLocationClient;
    private Location currentLocation;
    String[] perms = {"android.permission.ACCESS_FINE_LOCATION", "android.permission.ACCESS_COARSE_LOCATION", "android.permission.INTERNET"};

    int permsRequestCode = 200;
    private String selectedContactName;
    private String selectedContactNumber;
    public static final String SMS_SENT_ACTION = "com.andriodgifts.gift.SMS_SENT_ACTION";
    public static final String SMS_DELIVERED_ACTION = "com.andriodgifts.gift.SMS_DELIVERED_ACTION";

private   List<String> items;

    ArrayAdapter<String> itemsAdapter ;
    private ListView recyclerView;
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

        contactDbHelper = new ContactDatabaseHelper(this);
        manageContactsButton = findViewById(R.id.addContact);
        mContactName = findViewById(R.id.deleteContact);
        Intent pickcontact;

        Intent contacts = new Intent(this, ManageContacts.class);
        final Intent pickContact = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);


        manageContactsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(pickContact, REQUEST_CONTACT);
            }
        });
        mContactName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Show the selected contact name and number
                if (selectedContactName != null && selectedContactNumber != null) {
                    String message = "Selected Contact: " + selectedContactName + " " + selectedContactNumber;
                    Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
                }
            }
        });

        items = new ArrayList<>();


      itemsAdapter =
                new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, items);

        recyclerView = (ListView)   findViewById(R.id.listview);

       // MyListAdapter adapter = new MyListAdapter(myListData);
       // recyclerView.setHasFixedSize(true);
       // recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(itemsAdapter);

    }

    private static final int REQUEST_READ_CONTACTS_PERMISSION = 0;

    private boolean hasContactsPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) ==
                PackageManager.PERMISSION_GRANTED;
    }


    private void requestContactsPermission() {
        if (!hasContactsPermission()) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_CONTACTS}, REQUEST_READ_CONTACTS_PERMISSION);
        }
    }


    private static final int MAX_CONTACTS = 5; // Maximum number of contacts user can select
    private int numSelectedContacts = 0; // Counter to keep track of number of selected contacts


   /* @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Toast.makeText(this, ""+resultCode, Toast.LENGTH_SHORT).show();

        if (resultCode != Activity.RESULT_OK) {
            return;
        }

        if (requestCode == REQUEST_CONTACT && data != null) {
            Uri contactUri = data.getData();

            // Query the content provider to get the contact name and number
            String[] projection = new String[] {
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                    ContactsContract.CommonDataKinds.Phone.NUMBER
            };

            Cursor cursor = getContentResolver().query(contactUri, projection, null, null, null);


            if (cursor != null && cursor.moveToFirst()) {
                int nameIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
                int numberIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);

                String selectedContactName = cursor.getString(nameIndex);
                String selectedContactNumber = cursor.getString(numberIndex);

                if (numSelectedContacts >= MAX_CONTACTS) {
                    // Show an error message to the user
                    Toast.makeText(MainActivity.this, "You can select only " + MAX_CONTACTS + " contacts.", Toast.LENGTH_SHORT).show();
                } else {
                    // Create a new contact object with the selected contact name and number
                    Contact newContact = new Contact(selectedContactName, selectedContactNumber);
                    // Insert the new contact object into the database
                    ContactDbHelper contactDbHelper = new ContactDbHelper(this);
                    SQLiteDatabase db = contactDbHelper.getWritableDatabase();
                    ContentValues values = new ContentValues();
                    values.put(ContactContract.ContactEntry.COLUMN_NAME_NAME, newContact.getName());
                    values.put(ContactContract.ContactEntry.COLUMN_NAME_NUMBER, newContact.getNumber());
                    long newRowId = db.insert(ContactContract.ContactEntry.TABLE_NAME, null, values);
                    db.close();

                    // Increment the counter and show the selected contact name and number
                    numSelectedContacts++;
                    String message = "Selected Contact: " + selectedContactName + " " + selectedContactNumber;
                    Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
                }
            }


            if (cursor != null) {
                cursor.close();
            }
        }
    }

*/

    List<Contact> contacts =new ArrayList<>();

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
     //   super.onActivityResult(requestCode, resultCode, data);

        //if (resultCode != Activity.RESULT_OK) return;

        if (requestCode == REQUEST_CONTACT && data != null) {
            Uri contactUri = data.getData();

            // Specify which fields you want
            // your query to return values for
            String[] queryFields = new String[]{
                    ContactsContract.Contacts.DISPLAY_NAME
            };

            // Perform your query - the contactUri
            // is like a "where" clause here
            Cursor cursor = this.getContentResolver()
                    .query(contactUri, queryFields, null, null, null);
            try {
                // Double-check that you
                // actually got results
                if (cursor.getCount() == 0) return;

                // Pull out the first column of
                // the first row of data
                // that is your contact's name
                cursor.moveToFirst();




                String name = cursor.getString(0);

                Toast.makeText(this, name, Toast.LENGTH_SHORT).show();
                String phone =  this.getPhoneNumber(name);
               // mContactName.setText(name);
                Contact cc= new Contact(name,phone);
                contacts.add(cc);
                items.add(phone);


                itemsAdapter.notifyDataSetChanged();

                Toast.makeText(this, ""+items.size(), Toast.LENGTH_SHORT).show();
             //   itemsAdapter.notifyAll();
               // recyclerView.setAdapter(itemsAdapter);
            } finally {
                cursor.close();
            }

        }
    }


    public void remove(String phone){
       contacts =  contacts.stream().filter(x->x.getNumber() != phone).collect(Collectors.toList());
      //  saveData();
    }

    public void loadData(){

    }

    public void saveData(){
        ContactDbHelper contactDbHelper = new ContactDbHelper(this);

        if(this.contacts !=null &&this.contacts.size() > 0){
            SQLiteDatabase db = contactDbHelper.getWritableDatabase();
         //todo: delelte    db.delete();
            for(Contact cc:this.contacts){
                //todo:insert
                ContentValues values = new ContentValues();
                values.put(ContactContract.ContactEntry.COLUMN_NAME_NAME, cc.getName());
                values.put(ContactContract.ContactEntry.COLUMN_NAME_NUMBER, cc.getNumber());
                long newRowId = db.insert(ContactContract.ContactEntry.TABLE_NAME, null, values);

            }

            db.close();
        }



    }

    private String getPhoneNumber(String  NAME){
        String number = "";
        ContentResolver cr = this.getContentResolver();
        Cursor cursor = cr.query(ContactsContract.Contacts.CONTENT_URI, null,
                "DISPLAY_NAME = '" + NAME + "'", null, null);
        if (cursor.moveToFirst()) {
            String contactId =   cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
            //
            //  Get all phone numbers.
            //
            Cursor phones = cr.query(Phone.CONTENT_URI, null,
                    Phone.CONTACT_ID + " = " + contactId, null, null);
            while (phones.moveToNext()) {
                  number = phones.getString(phones.getColumnIndex(Phone.NUMBER));
                Toast.makeText(this, number, Toast.LENGTH_SHORT).show();
                break;
            }
            phones.close();
        }
        cursor.close();

        return number;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_READ_CONTACTS_PERMISSION && grantResults.length > 0) {
            //   updateButton(grantResults[0] == PackageManager.PERMISSION_GRANTED);
        }

    }


    public void SensorActivity() {
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    }

    int count = 0;

    @Override
    public void onSensorChanged(SensorEvent event) {

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {

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
    boolean isHorinzontal = false;

    private void listenToSensors(SensorEvent event) throws InterruptedException {

        boolean status = false;

        String message = "";

        if (isPhoneVertical(event)) {
            if (isHorinzontal == false) {

                vertical_count = vertical_count + 1;
                view.setBackgroundColor(Color.GREEN);
                message = "vertical_count" + vertical_count;
                isHorinzontal = true;
                Toast.makeText(this, "NIO", Toast.LENGTH_SHORT).show();
            }


        } else {
            if (isHorinzontal == true) {
                horizontal_count = horizontal_count + 1;
                view.setBackgroundColor(Color.RED);
                message = "horizontal_count" + horizontal_count;
                isHorinzontal = false;
                Toast.makeText(this, "DEM", Toast.LENGTH_SHORT).show();
            }

        }
        //if(!message.isEmpty())
        //    Toast.makeText(this, "HOR : "+horizontal_count, Toast.LENGTH_SHORT).show();
        if (vertical_count > 3) {
            Toast.makeText(this, "Make Alarm", Toast.LENGTH_SHORT).show();
            vertical_count = horizontal_count = 0;
            this.makeAlam();


        }
    }


    private void makeAlam() {
        //todo: send notification
        if (currentLocation != null) {
            Toast.makeText(MainActivity.this, "" + currentLocation.getLatitude(), Toast.LENGTH_SHORT).show();
        }


        String location = getAddress(currentLocation.getLatitude(), currentLocation.getLongitude());
        String message = "I am in trouble. Please reach me urgently at " + location;
        if(contacts!= null && contacts.size()>0){
            contacts.stream().forEach(x->sendSMS(x.getNumber(),message));
        }
        /*
        sendSMS("", message);
        sendSMS("", message);
        sendSMS("", message);
*/

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
            return add;

            // TennisAppActivity.showDialog(add);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return "";
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

              //  smsManager.sendTextMessage(phoneNo, null, msg, PendingIntent.getBroadcast(
               //         this, 0, new Intent(SMS_SENT_ACTION), 0), PendingIntent.getBroadcast(this, 0, new Intent(SMS_DELIVERED_ACTION), 0));

                Toast.makeText(getApplicationContext(), "Message Sent",
                        Toast.LENGTH_LONG).show();
            } catch (Exception ex) {
                Toast.makeText(getApplicationContext(), ex.getMessage().toString(),
                        Toast.LENGTH_LONG).show();
                ex.printStackTrace();
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.SEND_SMS}, 10);
            }
        }
    }


}

