package com.example.reminderservice;

import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.reminderservice.model.ReminderDB;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static android.content.ContentValues.TAG;

public class AddAlarmActivity extends Activity {
    private int notificationId = 1;
    String[] resultArr;
    private static final int MY_PERMISSIONS_REQUEST_SEND_SMS =0 ;
    private boolean updateReminder = false;
    private List<ReminderDB> reminderList = new ArrayList<>();
    String oldDate = "",date,curTime,oldTime = "",oldText="",message;
    DatabaseHelper myDB = new DatabaseHelper(this);
    LinearLayout btnDatePicker, btnTimePicker;
    EditText editText,phoneNoText;
    FloatingActionButton setBtn;
    TextView txtDate, txtTime;
    String phoneNo,mMessage="";
    private int mYear, mMonth, mDay, mHour, mMinute,reminderPositon;
    long selectedTimeStamp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_alarm);

        oldDate = getIntent().getStringExtra("oldDate");
        oldTime = getIntent().getStringExtra("oldTime");
        oldText = getIntent().getStringExtra("oldText");
        reminderPositon = (int) getIntent().getIntExtra("position",0);

        Bundle bundle = this.getIntent().getExtras();
        if (bundle != null){
            ListView lv = (ListView) findViewById(R.id.selectedContactList);
            resultArr = bundle.getStringArray("selectedItems");
//            Log.d("FFFFFFFFFFGGGGGGGGGGGGGGG","FGGGG"+resultArr[0]);
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(AddAlarmActivity.this,
                    android.R.layout.simple_list_item_1, resultArr);
            lv.setAdapter(adapter);
        }

        if(!checkPermission()){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS,Manifest.permission.READ_PHONE_STATE,Manifest.permission.READ_CONTACTS}, MY_PERMISSIONS_REQUEST_SEND_SMS);
        }

        if(oldDate != null && oldTime != null && oldText != null ){
            date = oldDate;
            curTime = oldTime;
            updateReminder = true;
        }else {
            date = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
            curTime = new SimpleDateFormat("hh:mm a").format(Calendar.getInstance().getTime());
            Log.i(TAG, curTime);
        }

        //get current date time
        final Calendar c = Calendar.getInstance();
        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);
        mHour = c.get(Calendar.HOUR_OF_DAY);
        mMinute = c.get(Calendar.MINUTE);


        setBtn = (FloatingActionButton) findViewById(R.id.setBtn);
        btnDatePicker = (LinearLayout) findViewById(R.id.dateLinear);
        btnTimePicker = (LinearLayout) findViewById(R.id.timeLinear);
        editText = findViewById(R.id.editTask);
//        phoneNoText = findViewById(R.id.phoneTxt);
        editText.setText(oldText);
        txtDate = (TextView) findViewById(R.id.in_date);
        txtDate.setText(date);
        txtTime = (TextView) findViewById(R.id.in_time);
        txtTime.setText(curTime);
        FloatingActionButton conBtn= (FloatingActionButton) findViewById(R.id.contactBtn);

        conBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
//                startActivityForResult(new Intent(AddAlarmActivity.this,ContactListActivity.class), REQUEST_CODE);
                Intent viewIntent = new Intent(AddAlarmActivity.this, ContactListActivity.class);
                viewIntent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
                startActivity(viewIntent);
                finish();
            }

        });

        btnDatePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                DatePickerDialog datePickerDialog = new DatePickerDialog(AddAlarmActivity.this,
                        new DatePickerDialog.OnDateSetListener() {

                            @Override
                            public void onDateSet(DatePicker view, int year,
                                                  int monthOfYear, int dayOfMonth) {

                                mYear = year;
                                mMonth = monthOfYear;
                                mDay = dayOfMonth;

                                txtDate.setText(dateFormat(dayOfMonth + "-" + (monthOfYear + 1) + "-" + year));

                            }
                        }, mYear, mMonth, mDay);
                datePickerDialog.show();
            }
        });
        btnTimePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Launch Time Picker Dialog
                TimePickerDialog timePickerDialog = new TimePickerDialog(AddAlarmActivity.this,
                        new TimePickerDialog.OnTimeSetListener() {

                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay,
                                                  int minute) {
                                String am_pm = "";

                                Calendar datetime = Calendar.getInstance();
                                datetime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                datetime.set(Calendar.MINUTE, minute);

                                if (datetime.get(Calendar.AM_PM) == Calendar.AM)
                                    am_pm = "AM";
                                else if (datetime.get(Calendar.AM_PM) == Calendar.PM)
                                    am_pm = "PM";

                                String strHrsToShow = (datetime.get(Calendar.HOUR) == 0) ? "12" : Integer.toString(datetime.get(Calendar.HOUR));

                                mHour = hourOfDay;
                                mMinute = minute;

                                txtTime.setText(timeFormat(strHrsToShow + ":" + datetime.get(Calendar.MINUTE) + " " + am_pm));
                            }
                        }, mHour, mMinute, false);
                timePickerDialog.show();
            }
        });

        //set add onclick listener
        setBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                message = editText.getText().toString();
                String inputdate = dateFormat(txtDate.getText().toString());
                String inputTime = timeFormat(txtTime.getText().toString());
//                phoneNo = phoneNoText.getText().toString();

                if (message.length() != 0 && inputTime.length() != 0) {

                    String timeStamp = inputdate + " " + inputTime;
                    selectedTimeStamp = timeStampFormat(timeStamp);
                    long currentTimeStamp = System.currentTimeMillis()/1000;

                    if(selectedTimeStamp < currentTimeStamp){
                        Toast.makeText(getApplicationContext(), "Incorrect Date Time Selected ", Toast.LENGTH_SHORT).show();
                    }else {
                        if (updateReminder) {
                            updateReminder(message, Long.toString(selectedTimeStamp), reminderPositon);
                            setAlarmNotification();
                        } else {
                            AddData(message, Long.toString(selectedTimeStamp));
                            setAlarmNotification();
                        }
                        if (resultArr.length > 0 && checkPermission()) {

                            mMessage = "You have a reminder on " + inputdate + " at " + inputTime + " with a text message '" + message + "'";
                            for (int i= 0 ;i<resultArr.length;i++ ){
                                boolean validNumber = false;
                                String conDetails[] = resultArr[i].split(System.getProperty("line.separator"));
                                if(conDetails[1].length() > 10){
                                    phoneNo = "+91"+conDetails[1].substring(conDetails[1].length() - 10);
                                    validNumber = true;
                                }else if (conDetails[1].length() == 10){
                                    phoneNo = "+91"+ conDetails[1];
                                    validNumber = true;
                                }

                                if(validNumber){
                                    SmsManager smsManager = SmsManager.getDefault();
                                    smsManager.sendTextMessage(phoneNo, null, mMessage, null, null);
                                }
                                Log.i("FFFFFFFFF",phoneNo);
                            }

                        }
                        editText.setText("");
                    }

                } else {
                    Toast.makeText(getApplicationContext(), "No Date Time Added !! ", Toast.LENGTH_SHORT).show();
                }

            }
        });

    }

    private void setAlarmNotification(){

        //set notification & text
        Intent alarmReceiverIntent = new Intent(AddAlarmActivity.this, AlarmReceiver.class);
        alarmReceiverIntent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
        alarmReceiverIntent.putExtra("notificationId", notificationId);
        alarmReceiverIntent.putExtra("todo", message);
        final int _id = (int) System.currentTimeMillis();

        PendingIntent alarmIntent = PendingIntent.getBroadcast(AddAlarmActivity.this, _id, alarmReceiverIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        AlarmManager alarm = (AlarmManager) getSystemService(ALARM_SERVICE);

        //create time
        Calendar startTime = Calendar.getInstance();
//                Log.i(TAG, mYear + "----" + mMonth + "---" + mDay + "----" + mHour + "---" + mMinute);
        startTime.set(mYear,mMonth,mDay,mHour,mMinute,00);
        long alarmStartTime = startTime.getTimeInMillis();

        //set alarm.
        alarm.setExact(AlarmManager.RTC_WAKEUP, alarmStartTime, alarmIntent);
        Toast.makeText(getApplicationContext(), "Done!", Toast.LENGTH_SHORT).show();
        Intent viewIntent = new Intent(AddAlarmActivity.this, MainActivity.class);
        viewIntent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
        startActivity(viewIntent);

    }

    public void AddData(String msg, String time) {
        boolean inseartData = myDB.addData(msg, time);
        if (inseartData == true) {
            Toast.makeText(getApplicationContext(), "Reminder added Successfully", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getApplicationContext(), "Something went wrong", Toast.LENGTH_SHORT).show();
        }
    }

    public String dateFormat(String date) {

        SimpleDateFormat spf = new SimpleDateFormat("dd-MM-yyyy");
        Date newDate = null;
        try {
            newDate = spf.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        spf = new SimpleDateFormat("dd-MM-yyyy");
        return spf.format(newDate);

    }

    public String timeFormat(String time) {
        SimpleDateFormat spf = new SimpleDateFormat("hh:mm a");
        Date newTime = null;
        try {
            newTime = spf.parse(time);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        spf = new SimpleDateFormat("hh:mm a");
        return spf.format(newTime);
    }

    public static long timeStampFormat(String timeStamp) {
        long newTimeStamp = 0;
        try {
            newTimeStamp = new SimpleDateFormat("dd-MM-yyyy hh:mm a").parse(timeStamp).getTime() / 1000;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return newTimeStamp;
    }


    /**
     * Updating note in db and updating
     * item in the list by its position
     */
    private void updateReminder(String msg,String timeStamp, int position) {
        reminderList.addAll(myDB.getAllNotes());
        ReminderDB n = reminderList.get(position);
        // updating note text
        n.setReminderMsg(msg);
        n.setTimestamp(timeStamp);

        // updating note in db
        myDB.updateNote(n);

        // refreshing the list
        reminderList.set(position, n);
    }

    public boolean checkPermission() {
        int smsPermission = ContextCompat.checkSelfPermission(this,Manifest.permission.SEND_SMS);
        int phonePermission = ContextCompat.checkSelfPermission(this,Manifest.permission.READ_PHONE_STATE);
        int contactPermission = ContextCompat.checkSelfPermission(this,Manifest.permission.READ_CONTACTS);
        return (smsPermission == PackageManager.PERMISSION_GRANTED ) && (phonePermission == PackageManager.PERMISSION_GRANTED) && (contactPermission == PackageManager.PERMISSION_GRANTED);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,String permissions[], int[] grantResults) {

        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_SEND_SMS:
                if (grantResults.length > 0 && (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED && grantResults[2] == PackageManager.PERMISSION_GRANTED)) {
                    Toast.makeText(getApplicationContext(), "Permission Granted",Toast.LENGTH_LONG).show();

                } else {
                    Toast.makeText(getApplicationContext(),
                            "SMS faild, please try again.", Toast.LENGTH_LONG).show();
                }
                break;
        }

    }

    @Override
    public Intent getIntent() {
        return super.getIntent();
    }
}
