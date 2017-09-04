package com.eaglesofttech.contacttuts.activity;

import android.Manifest;
import android.app.Dialog;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.eaglesofttech.contacttuts.R;
import com.eaglesofttech.contacttuts.adapter.Contact;
import com.eaglesofttech.contacttuts.adapter.ContactAdapter;
import com.eaglesofttech.contacttuts.utility.CustomDialog;

import java.util.ArrayList;
import java.util.List;

public class ContactList extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    RecyclerView rvContacts;
    // Request code for READ_CONTACTS. It can be any number > 0.
    private static final int PERMISSIONS_REQUEST_READ_CONTACTS = 100;
    CustomDialog customDialog;
    FloatingActionButton floatingActionButton;
    SwipeRefreshLayout swipeRefreshLayout;
    CoordinatorLayout coordinatorLayout;
    

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_list);
        customDialog = new CustomDialog(ContactList.this);

        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);
        rvContacts = (RecyclerView) findViewById(R.id.rvContacts);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        floatingActionButton = (FloatingActionButton) findViewById(R.id.floatingActionButton);
        swipeRefreshLayout.setOnRefreshListener(this);
        showContacts();
    }

    public void addContact(View view) {
        showPopup(0, "", "");
    }

    public void showPopup(final int mode, final String name, final String mobile_number) {

        final Dialog dialog = new Dialog(ContactList.this, R.style.AppCompatAlertDialogStyle);
        dialog.setContentView(R.layout.custom_dialog);
        final EditText edt_name, edt_mobile_number;
        AppCompatButton btn_delete, btn_update, btn_cancel;

        edt_name = (EditText) dialog.findViewById(R.id.edt_name);
        edt_mobile_number = (EditText) dialog.findViewById(R.id.edt_mobile_number);
        btn_cancel = (AppCompatButton) dialog.findViewById(R.id.btn_cancel);
        btn_delete = (AppCompatButton) dialog.findViewById(R.id.btn_delete);
        btn_update = (AppCompatButton) dialog.findViewById(R.id.btn_update);
        if (mode == 0) {
            btn_cancel.setText(getString(R.string.ok));
            btn_delete.setVisibility(View.GONE);
            btn_update.setVisibility(View.GONE);
        }

        edt_name.setText(name);
        edt_mobile_number.setText(mobile_number);

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mode == 0) {
                    if (addContactList(ContactList.this, edt_name.getText().toString(), edt_mobile_number.getText().toString())) {
                        Snackbar.make(coordinatorLayout, getString(R.string.addContact), Snackbar.LENGTH_SHORT).show();
                        rvContacts.invalidate();
                        dialog.dismiss();
                    }
                } else dialog.dismiss();
            }
        });

        btn_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (deleteContactList(ContactList.this, name)) {
                    Snackbar.make(coordinatorLayout, getString(R.string.deleteContact), Snackbar.LENGTH_SHORT).show();
                    rvContacts.invalidate();
                    dialog.dismiss();
                }
            }
        });

        btn_update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (updateContactList(ContactList.this, name, mobile_number)) {
                    Snackbar.make(coordinatorLayout, getString(R.string.updateContact), Snackbar.LENGTH_SHORT).show();
                    rvContacts.invalidate();
                    dialog.dismiss();
                }
            }
        });

        dialog.show();
    }

    public class getContacts extends AsyncTask<Void, Integer, Void> {
        List<Contact> contactList = new ArrayList();
        Contact contact;


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            swipeRefreshLayout.setRefreshing(true);

        }

        @Override
        protected Void doInBackground(Void... voids) {
            ContentResolver contentResolver = getContentResolver();
            Cursor cursor = contentResolver.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC");
            if (cursor.getCount() > 0) {
                while (cursor.moveToNext()) {

                    int hasPhoneNumber = Integer.parseInt(cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)));
                    if (hasPhoneNumber > 0) {
                        String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                        String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                        Uri image = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, Long.parseLong(id));

                        contact = new Contact();
                        contact.setContactName(name);
                        contact.setContactImage(image);

                        Cursor phoneCursor = contentResolver.query(
                                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                null,
                                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                                new String[]{id},
                                null);
                        if (phoneCursor.moveToNext()) {
                            String phoneNumber = phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                            contact.setContactNumber(phoneNumber);
                        }

                        phoneCursor.close();

                        Cursor emailCursor = contentResolver.query(
                                ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                                null,
                                ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?",
                                new String[]{id}, null);
                        while (emailCursor.moveToNext()) {
                            String emailId = emailCursor.getString(emailCursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
                        }
                        contactList.add(contact);
                    }
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            swipeRefreshLayout.setRefreshing(false);
            ContactAdapter contactAdapter = new ContactAdapter(getApplicationContext(), contactList);
            rvContacts.setLayoutManager(new LinearLayoutManager(ContactList.this));
            rvContacts.setAdapter(contactAdapter);
        }

    }

    public static boolean addContactList(Context context, /*int id, */String contactName, String contactNumber) {

        ArrayList<ContentProviderOperation> ops = new ArrayList<>();

        ContentProviderOperation.Builder builder = ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI);
        builder.withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null);
        builder.withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null);
        ops.add(builder.build());

        // Name
        builder = ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI);
        builder.withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0);
        builder.withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE);
        builder.withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, contactName);
        ops.add(builder.build());

        // Number
        builder = ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI);
        builder.withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0);
        builder.withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);
        builder.withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, contactNumber);
        builder.withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_HOME);
        ops.add(builder.build());
        try {
            context.getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }

    public static boolean updateContactList(Context context, String name, String newPhoneNumber) {
        ArrayList<ContentProviderOperation> ops = new ArrayList<>();

        String where = ContactsContract.Data.DISPLAY_NAME + " = ? AND " +
                ContactsContract.Data.MIMETYPE + " = ? AND " +
                String.valueOf(ContactsContract.CommonDataKinds.Phone.TYPE) + " = ? ";

        String[] params = new String[]{name,
                ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE,
                String.valueOf(ContactsContract.CommonDataKinds.Phone.TYPE_HOME)};
        ops.add(ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI)
                .withSelection(where, params)
                .withValue(ContactsContract.CommonDataKinds.Phone.DATA, newPhoneNumber)
                .build());
        try {
            context.getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean deleteContactList(Context context, String name) {
        ContentResolver cr = context.getContentResolver();
        String where = ContactsContract.Data.DISPLAY_NAME + " = ? ";
        String[] params = new String[]{name};

        ArrayList<ContentProviderOperation> ops = new ArrayList<>();
        ops.add(ContentProviderOperation.newDelete(ContactsContract.RawContacts.CONTENT_URI)
                .withSelection(where, params)
                .build());
        try {
            cr.applyBatch(ContactsContract.AUTHORITY, ops);

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Show the contacts in the ListView.
     */
    private void showContacts() {
        // Check the SDK version and whether the permission is already granted or not.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, PERMISSIONS_REQUEST_READ_CONTACTS);
            //After this point you wait for callback in onRequestPermissionsResult(int, String[], int[]) overriden method
        } else {
            new getContacts().execute();
        }
    }

    /**
     * Called when a swipe gesture triggers a refresh.
     */
    @Override
    public void onRefresh() {
        new getContacts().execute();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_READ_CONTACTS) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission is granted
                new getContacts().execute();
            } else {
                Toast.makeText(this, getString(R.string.permissionError), Toast.LENGTH_SHORT).show();
            }
        }
    }


}