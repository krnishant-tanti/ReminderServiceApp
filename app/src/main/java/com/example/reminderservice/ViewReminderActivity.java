package com.example.reminderservice;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.example.reminderservice.model.ReminderDB;
import com.example.reminderservice.util.MyDividerItemDecoration;
import com.example.reminderservice.util.RecyclerTouchListener;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

public class ViewReminderActivity extends AppCompatActivity {

    final int GOOGLE_LOGOUT = 1511;
    ListView reminderList;
    private ReminderAdapter mAdapter;
    DatabaseHelper myDB;
    ArrayList<String> reminderArrList ;
    private List<ReminderDB> notesList = new ArrayList<>();
    FloatingActionButton showClock;
    private RecyclerView recyclerView;
    private CoordinatorLayout coordinatorLayout;
    private TextView noNotesView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_reminder);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        showClock =  findViewById(R.id.showClock);
        //set onclick listener.
//        reminderList = (ListView) findViewById(R.id.reminderList);
        myDB = new DatabaseHelper(this);
        /* Old Code


        reminderArrList = new ArrayList<>();
        Cursor data = myDB.getListContents();
        if(data.getCount() == 0){
            Toast.makeText(getApplicationContext(),"No Record Found !!",Toast.LENGTH_SHORT).show();
        }else{
            while(data.moveToNext()){
                reminderArrList.add(data.getString(1)+"\n"+timeStampToDate(Long.parseLong(data.getString(2))));
//                reminderArrList.add();
                ListAdapter reminderListAdapter = new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,reminderArrList);
                reminderList.setAdapter(reminderListAdapter);
            }
        }  */

        //new code
        noNotesView = findViewById(R.id.empty_notes_view);
        notesList.addAll(myDB.getAllNotes());
        showClock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openAlarm();
            }
        });
        recyclerView = findViewById(R.id.recycler_view);

        mAdapter = new ReminderAdapter(this, notesList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new MyDividerItemDecoration(this, LinearLayoutManager.VERTICAL, 16));
        recyclerView.setAdapter(mAdapter);

        toggleEmptyNotes();

        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(this,
                recyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, final int position) {
            }

            @Override
            public void onLongClick(View view, int position) {
                showActionsDialog(position);
            }
        }));

    }

    /**
     * Deleting note from SQLite and removing the
     * item from the list by its position
     */
    private void deleteNote(int position) {
        // deleting the note from db
        myDB.deleteNote(notesList.get(position));

        // removing the note from the list
        notesList.remove(position);
        mAdapter.notifyItemRemoved(position);

        toggleEmptyNotes();
    }

    /**
     * Opens dialog with Edit - Delete options
     * Edit - 0
     * Delete - 0
     */
    private void showActionsDialog(final int position) {
        CharSequence colors[] = new CharSequence[]{"Edit", "Delete"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose option");
        builder.setItems(colors, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    editAlarm(notesList.get(position),position);
                } else {
                    deleteNote(position);
                }
            }
        });
        builder.show();
    }

    /**
     * Toggling list and empty notes view
     */
    private void toggleEmptyNotes() {
        // you can check notesList.size() > 0

        if (myDB.getNotesCount() > 0) {
            noNotesView.setVisibility(View.GONE);
        } else {
            noNotesView.setVisibility(View.VISIBLE);
        }
    }

    public void openAlarm(){
        Intent intent = new Intent(this, AddAlarmActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
        startActivity(intent);
    }

    public static String timeStampToDate(long timeStamp,int onlyOne){
        String date = "" ;
        if(onlyOne == 0){
            date = new java.text.SimpleDateFormat("dd-MM-yyyy hh:mm a").format(new java.util.Date (timeStamp * 1000));
        }else if(onlyOne == 1){
            date = new java.text.SimpleDateFormat("dd-MM-yyyy").format(new java.util.Date (timeStamp * 1000));
        }else if(onlyOne == 2){
            date = new java.text.SimpleDateFormat("hh:mm a").format(new java.util.Date (timeStamp * 1000));
        }
        return date;
    }

    public void editAlarm(ReminderDB remList, int position){
        Intent intent = new Intent(this, AddAlarmActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
        intent.putExtra("oldDate",timeStampToDate( Long.parseLong(remList.getTimestamp()) ,1));
        intent.putExtra("oldTime",timeStampToDate( Long.parseLong(remList.getTimestamp()) ,2));
        intent.putExtra("oldText",remList.getReminderMsg());
        intent.putExtra("position", position);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.logout) {
            FirebaseAuth.getInstance().signOut();
            Intent logoutIntent = new Intent(ViewReminderActivity.this, MainActivity.class);
            startActivity(logoutIntent);
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }
}
