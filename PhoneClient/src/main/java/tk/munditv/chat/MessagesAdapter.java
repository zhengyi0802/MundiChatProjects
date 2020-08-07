package tk.munditv.chat;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import tk.munditv.xmpp.Logger;
import tk.munditv.xmpp.database.models.Message;

public class MessagesAdapter extends RecyclerView.Adapter {

    private static final String TAG = "MessagesAdapter";

    private int mResource;
    private Context mContext;
    private ArrayList<Message> mMessages;

    public MessagesAdapter(Context mContext, int mResource, ArrayList<Message> mMessages) {
        Logger.debug(TAG, "MessagesAdapter created!");
        this.mResource = mResource;
        this.mContext = mContext;
        this.mMessages = mMessages;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Logger.debug(TAG, "MessagesAdapter - onCreateViewHolder!");
        View itemView = View.inflate(mContext, mResource, null);
        return new viewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Logger.debug(TAG, "MessagesAdapter - onBindViewHolder!");
        Message m = mMessages.get(position);
        ((viewHolder)holder).message.setText(m.getMessage());
        String createtime = getDate(m.getCreationTimestamp());
        ((viewHolder)holder).timestamp.setText(createtime);
    }

    @Override
    public int getItemCount() {
        Logger.debug(TAG, "MessagesAdapter - getItemCount() = ! + MmESSAGES.SIZE()");
        return mMessages.size();
    }
    private class viewHolder extends RecyclerView.ViewHolder {
        private TextView message;
        private TextView timestamp;

        public viewHolder(@NonNull View itemView) {
            super(itemView);
            message = (TextView) itemView.findViewById(R.id.txt_message);
            timestamp = (TextView) itemView.findViewById(R.id.txt_time);
        }
    }

    private String getDate(long milliSeconds) {
        // Create a DateFormatter object for displaying date in specified
        // format.
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss");
        // Create a calendar object that will convert the date and time value in
        // milliseconds to date.
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliSeconds);
        return formatter.format(calendar.getTime());
    }

}
