package tk.munditv.chat;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import tk.munditv.chat.utils.PInfo;

public class ApplicationAdapter extends RecyclerView.Adapter {
    private static final String TAG = "ApplicationAdapter";

    private int mResource;
    private Context mContext;
    private ArrayList<PInfo> mApplications;
    private String remoteAccount;

    public void setRemoteAccount(String remoteAccount) {
        this.remoteAccount = remoteAccount;
    }

    public ApplicationAdapter(Context mContext, int mResource, ArrayList<PInfo> apps) {
        this.mResource = mResource;
        this.mContext = mContext;
        this.mApplications = apps;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = View.inflate(mContext, mResource, null);
        return new ApplicationAdapter.viewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        String appName = mApplications.get(position).getAppname();
        ((viewHolder)holder).mApplication.setText(appName);
        ((viewHolder) holder).mApplication.setTag(mApplications.get(position));
    }

    @Override
    public int getItemCount() {
        return mApplications.size();
    }

    private class viewHolder extends RecyclerView.ViewHolder {
        private Button mApplication;

        public viewHolder(@NonNull View itemView) {
            super(itemView);
            mApplication = itemView.findViewById(R.id.btn_application);
            mApplication.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    PInfo p = (PInfo) view.getTag();
                    String message = "[execute]=" +p.getAppname();
                    if (remoteAccount != null)
                        MainApp.getInstance().sendMessage(remoteAccount, message);
                }
            });
        }
    }

}
