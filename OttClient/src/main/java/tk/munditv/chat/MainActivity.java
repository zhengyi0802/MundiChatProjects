package tk.munditv.chat;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.gson.Gson;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import org.jxmpp.stringprep.XmppStringprepException;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import tk.munditv.chat.utils.PInfo;
import tk.munditv.chat.utils.Packages;
import tk.munditv.xmpp.Logger;
import tk.munditv.xmpp.MessageCallback;
import tk.munditv.xmpp.XmppAccount;
import tk.munditv.xmpp.XmppRosterEntry;
import tk.munditv.xmpp.XmppServiceBroadcastEventEmitter;
import tk.munditv.xmpp.XmppServiceBroadcastEventReceiver;
import tk.munditv.xmpp.XmppServiceCommand;
import tk.munditv.xmpp.database.SqLiteDatabase;
import tk.munditv.xmpp.database.models.Message;
import tk.munditv.xmpp.database.providers.MessagesProvider;

import static tk.munditv.xmpp.XmppAccount.PRESENCE_MODE_AVAILABLE;

public  class MainActivity extends AppCompatActivity implements MessageCallback {

    private final static String TAG = "MainActivity";
    public static final String DATABASE_NAME = "messages.db";
    private static final int PERMISSION_REQUEST_CODE = 100;

    private XmppAccount mAccount;
    private XmppServiceBroadcastEventReceiver receiver;
    private MessagesProvider messagesProvider;
    private SqLiteDatabase mDatabase;
    private Context mContext;

    private ImageView   mQRCodeImage;
    private TextView    mSerialno;
    private TextView    mMessage;
    private TextView    mLoginStatus;
    private String      mSerialNumber;
    private String      mHostName;
    private String      mServiceName;
    private String      mResourceName;
    private String      mUsername;
    private String      mPassword;
    private String      mProductModel;
    private String      mQRString;
    private ArrayList<PInfo> apps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.activity_main);
        Logger.info(TAG, "onCreate() start!");
        mMessage = findViewById(R.id.message_box);
        mSerialno = findViewById(R.id.label_serialno);
        mLoginStatus = findViewById(R.id.login_status);
        mQRCodeImage = findViewById(R.id.qrcode_image);
        mHostName = getString(R.string.string_hostname);
        mServiceName = getString(R.string.string_servicename);
        mResourceName = getString(R.string.string_resourcename);
        checkStorage();
        initialize();
    }

    private void initialize() {
        getSerialNumber();
        if(mProductModel.equals("Emulator")) {
            mUsername = "sdk-" + mSerialNumber.toLowerCase();
        } else {
            mUsername = mProductModel  + "-" + mSerialNumber;
        }
        mUsername = mUsername.toLowerCase();
        mAccount = new XmppAccount();
        mQRString = "{\"host\":\"" + mHostName + "\",";
        mQRString = mQRString + "\"serialno\":\"" + mSerialNumber + "\",";
        mQRString = mQRString + "\"username\":\"" + mUsername + "\"}";
        if(mProductModel.equals("Emulator")) {
            mPassword = mSerialNumber.toLowerCase().substring(8);
        } else {
            mPassword = mSerialNumber.toLowerCase().substring(4);
        }
        Logger.debug(TAG, "QR String = " + mQRString);
        mAccount.setHost(mHostName);
        mAccount.setPort(443);
        mAccount.setXmppJid(mUsername);
        mAccount.setPassword(mPassword);
        mAccount.setServiceName(mServiceName);
        mAccount.setResourceName(mResourceName);
        mAccount.setPresenceMode(PRESENCE_MODE_AVAILABLE);
        Logger.debug(TAG, mAccount.toString());
        XmppServiceBroadcastEventEmitter.initialize(this, "tk.munditv");
        receiver = new XmppServiceBroadcastEventReceiver();
        receiver.register(this);
        receiver.setMessageCallback(this);
        XmppServiceCommand.connect(this, mAccount);
        mDatabase = new SqLiteDatabase(this, DATABASE_NAME);
        messagesProvider = new MessagesProvider(mDatabase);
        QRCodeGenerator();
        Packages pkg = new Packages(this);
        apps = pkg.getPackages();

    }

    private void sendPackages(String remoteAccount) {
        final int max = apps.size();
        Logger.debug(TAG, "packages count = " + max);
        for (int i = 0; i < max; i++) {
            PInfo p = apps.get(i);
            String message = new Gson().toJson(p);
            message = "[package] " + message;
            XmppServiceCommand.sendMessage(this, remoteAccount ,message);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Logger.info(TAG, "onStart() start!");
    }

    @Override
    protected void onStop() {
        Logger.info(TAG, "onStop() start!");
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Logger.info(TAG, "onDestroy() start!");
        super.onDestroy();
        XmppServiceCommand.disconnect(this);
        receiver.unregister(this);
        receiver = null;
    }

    @Override
    public void onMessageAdded(String remoteAccount, boolean incoming) throws XmppStringprepException {
        List<Message> arrayList = new ArrayList<Message>();
        arrayList = messagesProvider.getMessagesWithRecipient(mAccount.getXmppJid().toString()
                , remoteAccount);
        String message = "";
        for (Message m : arrayList) {
            message = m.getMessage();
            long timestamp = m.getCreationTimestamp();
            checkMessage(remoteAccount, m);
        }
        if (incoming) mMessage.setText(message);
        XmppServiceCommand.clearConversations(this, remoteAccount);
    }
    private static long listSent = 0;
    private void checkMessage(String remoteAccount, Message message) {
        if (message.getMessage().contains("Lists")) {
            Logger.debug(TAG, "message = " + message.getMessage()
                    + ", time = " + message.getCreationTimestamp());
            if (listSent != message.getCreationTimestamp()) sendPackages(remoteAccount);
            listSent = message.getCreationTimestamp();
            XmppServiceCommand.deleteMessage(this, message.getId());
        } else if (message.getMessage().contains("[execute]")) {
            checkPackage(message.getMessage());
        } else if (message.getMessage().contains("[youtube]")) {
            String url = message.getMessage().substring(9);
            try {
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_VIEW);
                //sendIntent.setPackage("");
                sendIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                sendIntent.setData(Uri.parse(url));
                startActivity(sendIntent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void checkPackage(String msg) {
        final int max = apps.size();
        for (int i = 0; i < max; i++) {
            PInfo p = apps.get(i);
            Log.d(TAG, "compare package name = " + p.getAppname() + " message = " + msg);
            if(msg.contains(p.getAppname())) {
                execute(p);
            }
        }
        return;
    }

    private void execute(PInfo p) {
        Log.d(TAG, "execute =" + p.getAppname());

        String packagename = p.getPName();
        Intent intent = getPackageManager().getLaunchIntentForPackage(packagename);
        if (intent != null) {
            // We found the activity now start the activity
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
        return;
    }

    @Override
    public void onConnected() {
        Logger.debug(TAG, "Connected!");
        mLoginStatus.setText(getString(R.string.str_connected));
    }

    @Override
    public void onDisconnected() {
        Logger.debug(TAG, "DisConnected!");
        mLoginStatus.setText(getString(R.string.str_disconnected));
        Handler mHandler = new Handler();
        //mHandler.postDelayed(doLogin, 2000);
    }

    @Override
    public void onAuthenticated() {
        Logger.debug(TAG, "Authenticated!");
        mLoginStatus.setText(getString(R.string.str_login));
        XmppServiceCommand.setPresence(this, PRESENCE_MODE_AVAILABLE, "Ready to Chat");
    }

    @Override
    public void onAuthenticateFailure() {
        Logger.debug(TAG, "AuthenticateFailure!");
        mLoginStatus.setText(getString(R.string.str_tryregister));
        XmppServiceCommand.register(this, mAccount);
    }

    @Override
    public void onRegistered() {
        Logger.debug(TAG, "onRegistered!");
        mLoginStatus.setText(getString(R.string.str_registered));
        Handler mHandler = new Handler();
        mHandler.postDelayed(dodisconnect, 3000);
        mHandler.postDelayed(doLogin, 5000);
    }

    private Runnable dodisconnect = new Runnable() {
        @Override
        public void run() {
            XmppServiceCommand.disconnect(mContext);
        }
    };

    private Runnable doLogin = new Runnable() {
        @Override
        public void run() {
            XmppServiceCommand.login(mContext, mAccount);
        }
    };

    @Override
    public void onRegisterFailure() {
        Logger.debug(TAG, "onRegisterFailure!");
        mLoginStatus.setText(getString(R.string.str_registerfailure));
        Handler mHandler = new Handler();
        //mHandler.postDelayed(dodisconnect, 2000);
    }

    @Override
    public void onRosterChanged(String entries) {
        Logger.debug(TAG, "onRosterChanged!");
        XmppRosterEntry[] arrayList = new Gson().fromJson(entries, XmppRosterEntry[].class);
        for (int i = 0; i < arrayList.length; i++){
            Logger.info(TAG, "remote account = " + arrayList[i].getXmppJID());
            Logger.info(TAG, "remote alias = " + arrayList[i].getAlias());
            Logger.info(TAG, "remote available = " + arrayList[i].isAvailable());
        }
    }

    @Override
    public void onContactAdded(String remoteAccount) {
        Logger.debug(TAG, "onContactAdded!");
    }

    @Override
    public void onContactRemoved(String remoteAccount) {
        Logger.debug(TAG, "onContactRemoved!");
    }

    @Override
    public void onContactRenamed(String remoteAccount, String newAlias) {
        Logger.debug(TAG, "onContactRenamed!");
    }

    @Override
    public void onContactAddError(String remoteAccount) {
        Logger.debug(TAG, "onContactAddError!");
    }

    @Override
    public void onConversationsCleared(String remoteAccount) {
        Logger.debug(TAG, "onConversationsCleared!");
    }

    @Override
    public void onConversationsClearError(String remoteAccount) {
        Logger.debug(TAG, "onConversationsClearError!");
    }

    @Override
    public void onMessageSent(long messageId) {
        Logger.debug(TAG, "onMessageSent!");
    }

    @Override
    public void onMessageDeleted(long messageId) {
        Logger.debug(TAG, "onMessageDeleted!");
    }

    @Override
    public void onGetRosterEntries() {
        Logger.debug(TAG, "onGetRosterEntries!");
    }

    private void getSerialNumber() {
        try {
            Class<?> c = Class.forName("android.os.SystemProperties");
            Method get = c.getMethod("get", String.class);
            mSerialNumber = (String) get.invoke(c, "ro.serialno");
            mProductModel = (String) get.invoke(c, "ro.product.model");
            if( mProductModel.contains("Android SDK")) {
                mProductModel = "Emulator";
            }
            String str = getString(R.string.label_productmodel) + mProductModel + ", ";
            str += getString(R.string.label_serialno) + mSerialNumber;
            mSerialno.setText(str);
            Logger.debug(TAG , "serialnumber = " + mSerialNumber);
            Logger.debug(TAG , "product model = " + mProductModel);
        } catch (Exception ignored) {
            ignored.printStackTrace();
        }
        return;
    }

    private void QRCodeGenerator() {
        mHostName = getString(R.string.string_hostname);
        BarcodeEncoder encoder = new BarcodeEncoder();
        try{
            Bitmap bit = encoder.encodeBitmap(mQRString,
                    BarcodeFormat.QR_CODE,500,500);
            mQRCodeImage.setImageBitmap(bit);
        }catch (WriterException e){
            e.printStackTrace();
        }
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