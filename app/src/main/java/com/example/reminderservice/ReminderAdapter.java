package com.example.reminderservice;
import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.reminderservice.model.ReminderDB;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static com.example.reminderservice.ViewReminderActivity.timeStampToDate;

public class ReminderAdapter extends RecyclerView.Adapter<ReminderAdapter.MyViewHolder> {

    private Context context;
    private List<ReminderDB> notesList;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView note;
        public TextView dot;
        public TextView timestamp;

        public MyViewHolder(View view) {
            super(view);
            note = view.findViewById(R.id.note);
            dot = view.findViewById(R.id.dot);
            timestamp = view.findViewById(R.id.timestamp);
        }
    }


    public ReminderAdapter(Context context, List<ReminderDB> notesList) {
        this.context = context;
        this.notesList = notesList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.viewreminder_list, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        ReminderDB note = notesList.get(position);
        long msgTimeStamp = Long.parseLong(note.getTimestamp());
        long currentTimeStamp = System.currentTimeMillis()/1000;

        if(msgTimeStamp < currentTimeStamp) {
            holder.dot.setTextColor(Color.parseColor("#FF4500"));
            holder.note.setTextColor(Color.parseColor("#FF4500"));
            holder.timestamp.setTextColor(Color.parseColor("#FF4500"));
        }
        holder.note.setText(note.getReminderMsg());

        // Displaying dot from HTML character code
        holder.dot.setText(Html.fromHtml("&#8226;",Html.FROM_HTML_MODE_LEGACY));

        // Formatting and displaying timestamp
        holder.timestamp.setText(formatDate(timeStampToDate(msgTimeStamp,0)));
    }

    @Override
    public int getItemCount() {
        return notesList.size();
    }

    /**
     * Formatting timestamp to `MMM d` format
     * Input: 2018-02-21 00:15:42
     * Output: Feb 21
     */
    private String formatDate(String dateStr) {
        try {
            SimpleDateFormat fmt = new SimpleDateFormat("dd-MM-yyyy hh:mm a");
            Date date = fmt.parse(dateStr);
            SimpleDateFormat fmtOut = new SimpleDateFormat("EEE, MMM d, yyyy 'at' hh:mm a");
            return fmtOut.format(date);
        } catch (ParseException e) {

        }
        return "";
    }
}