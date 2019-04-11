package com.example.reminderservice;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ContactListActivity extends AppCompatActivity {
    //    String contactLists[];
    ArrayList<String> contactLists = new ArrayList<String>();
    ArrayAdapter<String> adapter;
    ListView listView;
    Button addContactBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_list);

        loadContacts();

        listView = (ListView) findViewById(R.id.contactList);
        Collections.sort(contactLists);
        adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_multiple_choice, contactLists);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        listView.setAdapter(adapter);
        addContactBtn = (Button) findViewById(R.id.submitContactBtn);

        addContactBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                SparseBooleanArray checked = listView.getCheckedItemPositions();
                ArrayList<String> selectedItems = new ArrayList<String>();
                for (int i = 0; i < checked.size(); i++) {
                    // Item position in adapter
                    int position = checked.keyAt(i);
                    // Add sport if it is checked i.e.) == TRUE!
                    if (checked.valueAt(i))
                        selectedItems.add(adapter.getItem(position));
                }

                String[] outputStrArr = new String[selectedItems.size()];

                for (int i = 0; i < selectedItems.size(); i++) {
                    outputStrArr[i] = selectedItems.get(i);
                }
                if(outputStrArr.length > 0) {
                    Intent intent = new Intent(ContactListActivity.this, AddAlarmActivity.class);
                    intent.putExtra("selectedItems", outputStrArr);
                    intent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
                    startActivity(intent);
                    finish();
                }else{
                    Toast.makeText(getApplicationContext(), "activity error", Toast.LENGTH_SHORT).show();
                }
            }
        });


    }

    public void loadContacts(){

        StringBuilder stringBuilder = new StringBuilder();
        ContentResolver contentResolver = getContentResolver();
        Cursor contactCusor = contentResolver.query(ContactsContract.Contacts.CONTENT_URI, null , null,null);

        if(contactCusor.getCount()>0){
            while(contactCusor.moveToNext()) {
                String id = contactCusor.getString(contactCusor.getColumnIndex(ContactsContract.Contacts._ID));
                String displayName = contactCusor.getString(contactCusor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                int hasPhoneNumber = Integer.parseInt(contactCusor.getString(contactCusor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)));

                if (hasPhoneNumber > 0) {
                    Cursor cursor1 = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ? ", new String[]{id}, null);

                    while (cursor1.moveToNext()) {
                        String phoneNo = cursor1.getString(cursor1.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        if(!contactLists.contains(displayName + "\n" + phoneNo) && phoneNo.length() > 1) {
                            contactLists.add(displayName + "\n" + phoneNo);
                        }
                    }
                    cursor1.close();
                }
            }

        }
    }
}
