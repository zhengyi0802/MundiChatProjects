package tk.munditv.chat;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import tk.munditv.chat.utils.PInfo;
import tk.munditv.xmpp.Logger;
import tk.munditv.xmpp.MessageCallback;
import tk.munditv.xmpp.XmppAccount;
import tk.munditv.xmpp.XmppRosterEntry;
import tk.munditv.xmpp.XmppServiceBroadcastEventReceiver;
import tk.munditv.xmpp.XmppServiceCommand;
import tk.munditv.xmpp.database.SqLiteDatabase;
import tk.munditv.xmpp.database.models.Message;
import tk.munditv.xmpp.database.providers.MessagesProvider;

public class MainActivity extends AppCompatActivity implements MessageCallback {

    private static final String TAG = "MainActivity";
    private static final int ACTION_LOGIN = 0;
    private static final int PERMISSION_REQUEST_CODE = 100;

    private XmppServiceBroadcastEventReceiver receiver;
    private TextView mAccount;
    private TextView mOttAccount;
    private RecyclerView mMessagesView;
    private RecyclerView mApplicationsView;
    private MessagesAdapter mMessagesAdapter;
    private ApplicationAdapter mApplicationAdapter;
    private ArrayList<Message> mMessagesList;
    private ArrayList<PInfo> mPInfoList;
    private EditText mMessage;
    private Button mSendButton;
    private SharedPreferences preferences;
    private XmppAccount xmppAccount;
    private String ottAccount;
    private String host;
    private String mConnectedMessages;
    private String mAvailable;

    private SqLiteDatabase mDatabase;
    private MessagesProvider messagesProvider;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Logger.debug(TAG, "onCreate()");
        setContentView(R.layout.activity_main);
        checkStorage();

        receiver = new XmppServiceBroadcastEventReceiver();
        receiver.register(this);
        receiver.setMessageCallback(this);
        mAccount = findViewById(R.id.txt_account);
        mOttAccount = findViewById(R.id.txt_ottaccount);
        mMessage = findViewById(R.id.edit_message);
        mSendButton = findViewById(R.id.btn_send);
        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String message = mMessage.getText().toString();
                if (message.length() > 0) {
                    MainApp.getInstance().sendMessage(ottAccount, message);
                }
                mMessage.setText(null);
            }
        });

        mMessagesView = findViewById(R.id.view_messages);
        mMessagesList = new ArrayList<Message>();
        mMessagesAdapter
                = new MessagesAdapter(this, R.layout.item_messages,mMessagesList);
        mMessagesView .setLayoutManager(new LinearLayoutManager(this));
        mMessagesView.setAdapter(mMessagesAdapter);
        mMessagesView.addItemDecoration(new DividerItemDecoration(this,
                DividerItemDecoration.VERTICAL));

        mApplicationsView = findViewById(R.id.view_apps);
        mPInfoList = new ArrayList<PInfo>();
        mApplicationAdapter
                = new ApplicationAdapter(this, R.layout.item_applications, mPInfoList);
        mApplicationsView .setLayoutManager(new LinearLayoutManager(this));
        mApplicationsView.setAdapter(mApplicationAdapter);
        mApplicationsView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.callOnClick();
            }
        });

        mConnectedMessages = getString(R.string.message_connected);
        mAvailable = getString(R.string.message_available);

        xmppAccount = MainApp.getInstance().getXmppAccount();
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        ottAccount = MainApp.getInstance().getOttAccount();
        host = xmppAccount.getHost();
        if (ottAccount == null) {
            initScanner();
        } else {
            setOttClientName();
        }
        mDatabase = new SqLiteDatabase(this, "messages.db");
        messagesProvider = new MessagesProvider(mDatabase);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Logger.debug(TAG, "onStart()");
        MainApp.getInstance().connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Logger.debug(TAG, "onStop()");
        //MainApp.getInstance().disconnect();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Logger.debug(TAG, "onDestroy()");
        MainApp.getInstance().disconnect();
        receiver.unregister(this);
        receiver = null;
        xmppAccount = null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        mPInfoList = null;
        mPInfoList = new ArrayList<PInfo>();
        mMessagesList = null;
        mMessagesList = new ArrayList<Message>();
        mMessagesAdapter.notifyDataSetChanged();
        mApplicationAdapter.notifyDataSetChanged();
        XmppServiceCommand.disconnect(this);
        switch(item.getItemId())//得到被點選的item的itemId
        {
            case R.id.action_scan:
                initScanner();
                break;
            case R.id.action_login:
                startLogin();
                break;
            case R.id.action_settings:
                break;
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Logger.debug(TAG, "onActivityResult() requestCode = " + requestCode);
        if (requestCode == ACTION_LOGIN) {
            setOttClientName();
        } else {
            Logger.debug(TAG, "Scanner stop");
            QrData qrdata = null;
            IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
            if (result != null) {
                if (result.getContents() == null) {
                    Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show();
                    qrdata = new QrData();
                    qrdata.host = "webrtc01.mundi-tv.tk";
                    qrdata.serialno = "01234567";
                    qrdata.username = "soundbarx-0123456789abcdef";
                    ottAccount = qrdata.username + "@" + qrdata.host;
                    preferences.edit().putString("ottaccount", ottAccount).commit();
                    preferences.edit().putString("host", qrdata.host).commit();
                    host = qrdata.host;
                } else {
                    Toast.makeText(this, "Scanned: " + result.getContents(), Toast.LENGTH_LONG).show();
                    qrdata = new Gson().fromJson(result.getContents(), QrData.class);
                    ottAccount = qrdata.username + "@" + qrdata.host;
                    preferences.edit().putString("ottaccount", ottAccount).commit();
                    preferences.edit().putString("host", qrdata.host).commit();
                    host = qrdata.host;
                    Toast.makeText(this, "ottAccount = " + ottAccount, Toast.LENGTH_LONG).show();
                }
            } else {
                super.onActivityResult(requestCode, resultCode, data);
            }
            setOttClientName();
            if (xmppAccount.getXmppJid() == null) {
                startLogin();
            }
        }
    }

    private void startLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivityForResult(intent, ACTION_LOGIN);
    }

    private void initScanner() {
        new IntentIntegrator(this)
                // 自定义Activity，重点是这行----------------------------
                .setCaptureActivity(CustomCaptureActivity.class)
                .setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES)// 扫码的类型,可选：一维码，二维码，一/二维码
                .setPrompt("请对准二维码")// 设置提示语
                .setCameraId(0)// 选择摄像头,可使用前置或者后置
                .setBeepEnabled(true)// 是否开启声音,扫完码之后会"哔"的一声
                .setBarcodeImageEnabled(true)// 扫完码之后生成二维码的图片
                .initiateScan();// 初始化扫码
    }

    public void setOttClientName() {
        mAccount.setText(xmppAccount.getXmppJid());
        mOttAccount.setText(ottAccount.split("@")[0]);
        xmppAccount.setHost(host);
        xmppAccount.setServiceName(host);
        Logger.debug(TAG, "setOttClientName host = " + host);
    }

    @Override
    public void onMessageAdded(String remoteAccount, boolean incoming) {
        Logger.debug(TAG, "onMessageAdded()");
        List<Message> arrayList = new ArrayList<Message>();
        arrayList = messagesProvider.getMessagesWithRecipient(xmppAccount.getXmppJid(), remoteAccount);
        boolean appflag = false;
        if(incoming) {
            for (Message m : arrayList) {
                String message = m.getMessage();
                Logger.debug(TAG, "message = " + message);
                String tag = "[package] ";
                String tag2 = "[error] ";
                if (message.contains(tag)) {
                    appflag = true;
                    String msg = message.substring(tag.length());
                    try {
                        PInfo p = new Gson().fromJson(msg, PInfo.class);
                        Logger.debug(TAG, "get package name = " + p.getAppname());
                        mPInfoList.add(p);
                        mApplicationAdapter.setRemoteAccount(remoteAccount);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else if(message.contains(tag2)) {
                    Logger.debug(TAG, "OTT error Occurred!");
                    mMessagesList.add(m);
                } else {
                    Logger.debug(TAG, "message = " + m.getMessage());
                    mMessagesList.add(m);
                }
            }

            if (appflag) {
                Logger.debug(TAG, "notify Application Adapter");
                mApplicationAdapter.notifyDataSetChanged();
            } else {
                Logger.debug(TAG, "notify incoming Messages Adapter message");
                mMessagesAdapter.notifyDataSetChanged();
            }
        } else {
            for (Message m : arrayList) {
                mMessagesList.add(m);
            }
            Logger.debug(TAG, "notify outgoing Messages Adapter");
            mMessagesAdapter.notifyDataSetChanged();
        }
        MainApp.getInstance().clearConversation(remoteAccount);
    }

    @Override
    public void onConnected() {
        Logger.debug(TAG, "onConnected()");
    }

    @Override
    public void onDisconnected() {
        Logger.debug(TAG, "onDisconnected()");
    }

    @Override
    public void onAuthenticated() {
        Logger.debug(TAG, "onAuthenticated()");
        mAccount.setText(xmppAccount.getXmppJid() + mConnectedMessages);
        MainApp.getInstance().addContact(ottAccount, "Ottbox");
    }

    @Override
    public void onAuthenticateFailure() {
        Logger.debug(TAG, "onAuthenticateFailure()");
        Intent intent = new Intent(this, LoginActivity.class);
        startActivityForResult(intent, ACTION_LOGIN);
    }

    @Override
    public void onRegistered() {
        Logger.debug(TAG, "onRegistered()");
    }

    @Override
    public void onRegisterFailure() {
        Logger.debug(TAG, "onRegisterFailure()");
    }

    @Override
    public void onRosterChanged(String entries) {
        Logger.debug(TAG, "onRosterChanged() entries = " + entries);
        XmppRosterEntry[] arrayList = new Gson().fromJson(entries, XmppRosterEntry[].class);
        for (int i = 0; i < arrayList.length; i++){
            if (arrayList[i].getXmppJID().contains(ottAccount)) {
                Logger.info(TAG, "ott account = " + arrayList[i].getXmppJID());
                Logger.info(TAG, "ott alias = " + arrayList[i].getAlias());
                Logger.info(TAG, "ott available = " + arrayList[i].isAvailable());
                if (arrayList[i].isAvailable()) {
                    mOttAccount.setText(ottAccount.split("@")[0] + mAvailable);
                }
            }
        }
    }

    @Override
    public void onContactAdded(String remoteAccount) {
        Logger.debug(TAG, "onContactAdded() remoteAccount = " + remoteAccount);
        MainApp.getInstance().sendMessage(remoteAccount, "[Lists]");
    }

    @Override
    public void onContactRemoved(String remoteAccount) {
        Logger.debug(TAG, "onContactRemoved()");
    }

    @Override
    public void onContactRenamed(String remoteAccount, String newAlias) {
        Logger.debug(TAG, "onContactRenamed()");
    }

    @Override
    public void onContactAddError(String remoteAccount) {
        Logger.debug(TAG, "onContactAddError()");
    }

    @Override
    public void onConversationsCleared(String remoteAccount) {
        Logger.debug(TAG, "onConversationsCleared()");
    }

    @Override
    public void onConversationsClearError(String remoteAccount) {
        Logger.debug(TAG, "onConversationsClearError()");
    }

    @Override
    public void onMessageSent(long messageId) {
        Logger.debug(TAG, "onMessageSent()");
    }

    @Override
    public void onMessageDeleted(long messageId) {
        Logger.debug(TAG, "onMessageDeleted()");
    }

    @Override
    public void onGetRosterEntries() {
        Logger.debug(TAG, "onGetRosterEntries()");
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

    private void checkStorage() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            if (Build.VERSION.SDK_INT >= 23) {
                if(!checkPermission()) {
                    requestPermission();
                }
            }
        }
    }

    private boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(MainActivity.this,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (result == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

    private void requestPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            Toast.makeText(MainActivity.this, "Write External Storage permission allows us to save files. Please allow this permission in App Settings.", Toast.LENGTH_LONG).show();
        } else {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[],
                                           int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0]
                        == PackageManager.PERMISSION_GRANTED) {
                    Log.e("value", "Permission Granted, Now you can use local drive .");
                } else {
                    Log.e("value", "Permission Denied, You cannot use local drive .");
                }
                break;
        }
    }
}
