package com.example.phoneproject;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;

import com.example.phoneproject.dtos.Contact;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;

import android.provider.ContactsContract;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.phoneproject.databinding.ActivityManageContactsBinding;

public class ManageContacts extends Activity {

    private AppBarConfiguration appBarConfiguration;
    private ActivityManageContactsBinding binding;



    private Button addContactsbutton;
    private Button mContactName;
    private static final int REQUEST_CONTACT = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityManageContactsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        addContactsbutton = findViewById(R.id.addContact);

        addContactsbutton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view)
            {
                final Intent pickContact = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);

               startActivityForResult(pickContact, REQUEST_CONTACT);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


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
                Toast.makeText(this, "Number"+selectedContactName, Toast.LENGTH_SHORT).show();

                /*
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
                */
            }

            if (cursor != null) {
                cursor.close();
            }
        }
    }




}