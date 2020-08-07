package tk.munditv.chat;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import org.jxmpp.stringprep.XmppStringprepException;

import tk.munditv.xmpp.Logger;
import tk.munditv.xmpp.MessageCallback;
import tk.munditv.xmpp.XmppAccount;
import tk.munditv.xmpp.XmppServiceBroadcastEventReceiver;

public class LoginActivity extends AppCompatActivity implements MessageCallback {

    private static final String TAG = "LoginActivity";

    private EditText mAccount;
    private EditText mPassword;
    private Button mLogin;
    private Button mRegister;
    private XmppAccount xmppAccount;
    private SharedPreferences preferences;
    private XmppServiceBroadcastEventReceiver receiver;

    private String mLoginFailure;
    private String mRegisterFailure;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mAccount = findViewById(R.id.edit_account);
        mPassword = findViewById(R.id.edit_password);
        mLogin = findViewById(R.id.btn_login);
        mLoginFailure = getString(R.string.err_login_failure);
        mRegisterFailure = getString(R.string.err_register_failure);
        xmppAccount = MainApp.getInstance().getXmppAccount();
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        mLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mLogin.getText() != null && mPassword.getText() != null) {
                    saveLoginData();
                    MainApp.getInstance().connect();
                }
            }
        });
        mRegister = findViewById(R.id.btn_register);
        mRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mLogin.getText() != null && mPassword.getText() != null) {
                    saveLoginData();
                    MainApp.getInstance().register();
                }
            }
        });
        receiver = new XmppServiceBroadcastEventReceiver();
        receiver.register(this);
        receiver.setMessageCallback(this);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void saveLoginData() {
        xmppAccount.setXmppJid(mAccount.getText().toString());
        xmppAccount.setPassword(mPassword.getText().toString());
        preferences.edit().putString("account", mAccount.getText().toString()).apply();
        preferences.edit().putString("password", mPassword.getText().toString()).apply();
        Logger.debug(TAG, "account = " + mAccount.getText().toString());
        Logger.debug(TAG, "password = " + mPassword.getText().toString());
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        receiver.unregister(this);
        receiver = null;
    }

    @Override
    public void onMessageAdded(String remoteAccount, boolean incoming) throws XmppStringprepException {
        Logger.debug(TAG, "onMessageAdded()");
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
        finish();
    }

    @Override
    public void onAuthenticateFailure() {
        Logger.debug(TAG, "onAuthenticateFailure()");
        Toast.makeText(this, mLoginFailure, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onRegistered() {
        Logger.debug(TAG, "onRegistered()");
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.dialog_message)
                .setTitle(R.string.dialog_title);
        builder.setPositiveButton(R.string.label_ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                MainApp.getInstance().connect();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public void onRegisterFailure() {
        Logger.debug(TAG, "onRegisterFailure()");
        Toast.makeText(this, mRegisterFailure, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onRosterChanged(String entries) {
        Logger.debug(TAG, "onRosterChanged()");
    }

    @Override
    public void onContactAdded(String remoteAccount) {
        Logger.debug(TAG, "onContactAdded()");
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
}
