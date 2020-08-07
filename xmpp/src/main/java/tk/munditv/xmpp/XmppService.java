package tk.munditv.xmpp;

import android.content.Intent;
import android.content.SharedPreferences;

import org.jivesoftware.smack.SmackConfiguration;
import org.jxmpp.stringprep.XmppStringprepException;

import java.util.ArrayList;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;

import tk.munditv.xmpp.database.SqLiteDatabase;
import tk.munditv.xmpp.database.providers.MessagesProvider;

import static tk.munditv.xmpp.XmppServiceCommand.ACTION_ADD_CONTACT;
import static tk.munditv.xmpp.XmppServiceCommand.ACTION_CLEAR_CONVERSATIONS;
import static tk.munditv.xmpp.XmppServiceCommand.ACTION_CONNECT;
import static tk.munditv.xmpp.XmppServiceCommand.ACTION_DELETE_MESSAGE;
import static tk.munditv.xmpp.XmppServiceCommand.ACTION_DISCONNECT;
import static tk.munditv.xmpp.XmppServiceCommand.ACTION_LOGIN;
import static tk.munditv.xmpp.XmppServiceCommand.ACTION_REFRESH_CONTACT;
import static tk.munditv.xmpp.XmppServiceCommand.ACTION_REGISTER;
import static tk.munditv.xmpp.XmppServiceCommand.ACTION_REMOVE_CONTACT;
import static tk.munditv.xmpp.XmppServiceCommand.ACTION_RENAME_CONTACT;
import static tk.munditv.xmpp.XmppServiceCommand.ACTION_SEND;
import static tk.munditv.xmpp.XmppServiceCommand.ACTION_SEND_PENDING_MESSAGES;
import static tk.munditv.xmpp.XmppServiceCommand.ACTION_SET_AVATAR;
import static tk.munditv.xmpp.XmppServiceCommand.ACTION_SET_PRESENCE;
import static tk.munditv.xmpp.XmppServiceCommand.PARAM_ACCOUNT;
import static tk.munditv.xmpp.XmppServiceCommand.PARAM_ALIAS;
import static tk.munditv.xmpp.XmppServiceCommand.PARAM_AVATAR_PATH;
import static tk.munditv.xmpp.XmppServiceCommand.PARAM_MESSAGE;
import static tk.munditv.xmpp.XmppServiceCommand.PARAM_MESSAGE_ID;
import static tk.munditv.xmpp.XmppServiceCommand.PARAM_PERSONAL_MESSAGE;
import static tk.munditv.xmpp.XmppServiceCommand.PARAM_PRESENCE_MODE;
import static tk.munditv.xmpp.XmppServiceCommand.PARAM_REMOTE_ACCOUNT;

/**
 * Main XMPP Service.
 * @author gotev (Aleksandar Gotev)
 */
public class XmppService extends BackgroundService {

    private static final String TAG = XmppService.class.getSimpleName();

    // configurable values
    /**
     * Name of the database used as a persistent storage for the exchanged messages.
     */
    public static final String DATABASE_NAME = "messages.db";

    /**
     * Name of the shared preferences file.
     */
    public static final String PREFS_NAME = "messages-prefs.conf";
    private static final String KEY_CONFIGURED_ACCOUNT = "configuredAccount";

    /**
     * Connection connect timeout in milliseconds.
     */
    public static int CONNECT_TIMEOUT = 5000;

    /**
     * Sets the number of milliseconds to wait for a response from
     * the server.
     */
    public static int PACKET_REPLY_TIMEOUT = 5000;

    /**
     * Default ping interval.
     */
    public static int DEFAULT_PING_INTERVAL = 600; //10 minutes;

    /**
     * Use stream management (XEP-0198).
     */
    public static boolean USE_STREAM_MANAGEMENT = true;

    /**
     * Stream management resumption time in seconds.
     */
    public static int STREAM_MANAGEMENT_RESUMPTION_TIME = 30;

    /**
     * Custom {@link SSLContext} to use for the connection.
     */
    public static SSLContext CUSTOM_SSL_CONTEXT = null;

    /**
     * Custom {@link HostnameVerifier} to use for the connection.
     */
    public static HostnameVerifier CUSTOM_HOSTNAME_VERIFIER = null;
    // end configurable values

    private SharedPreferences mPrefs;
    private XmppServiceConnection mConnection;
    private static volatile SqLiteDatabase mDatabase;
    private static volatile boolean connected = false;
    private static volatile boolean authenticated = false;
    private static volatile boolean registered = false;
    private static volatile ArrayList<XmppRosterEntry> rosterEntries;

    public static ArrayList<XmppRosterEntry> getRosterEntries() {
        synchronized (XmppService.class) {
            return rosterEntries;
        }
    }

    protected static void setRosterEntries(ArrayList<XmppRosterEntry> entries) {
        synchronized (XmppService.class) {
            rosterEntries = entries;
        }
    }

    public static synchronized boolean isConnected() {
        return connected;
    }

    public static synchronized void setConnected(boolean newConnectedStatus) {
        connected = newConnectedStatus;

        if (newConnectedStatus) {
            XmppServiceBroadcastEventEmitter.broadcastXmppConnected();
        } else {
            XmppServiceBroadcastEventEmitter.broadcastXmppDisconnected();
        }
    }

    public static synchronized void setAuthenticated(boolean newAuthenticatedStatus) {
        Logger.debug(TAG, "setAuthenticated(" + newAuthenticatedStatus + ")");
        authenticated = newAuthenticatedStatus;
        if (newAuthenticatedStatus) {
            XmppServiceBroadcastEventEmitter.broadcastAuthenticated();
        } else {
            XmppServiceBroadcastEventEmitter.broadcastAuthenticateFailure();
        }

        return;
    }

    public static synchronized SqLiteDatabase getDatabase() {
        return mDatabase;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        enqueueJob(new Runnable() {
            @Override
            public void run() {
                mPrefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                SmackConfiguration.setDefaultReplyTimeout(PACKET_REPLY_TIMEOUT);
                Logger.info(TAG, "initializing database");
                mDatabase = new SqLiteDatabase(XmppService.this, DATABASE_NAME);
            }
        });
    }

    @Override
    public void onDestroy() {

        Logger.info(TAG, "Destroying service");

        if (mDatabase != null) {
            mDatabase.close();
            mDatabase = null;
        }

        if (mConnection != null) {
            mConnection.disconnect();
            mConnection = null;
        }

        connected = false;
        rosterEntries = null;

        super.onDestroy();
    }

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {

        enqueueJob(new Runnable() {
            @Override
            public void run() {
                if (intent == null) {
                    // handle the case in which the service has been interrupted by the system.
                    // In this case the service will be automatically relaunched, but with an empty
                    // intent. Reconnect automatically the last configured account
                    if (!connected || mConnection == null) {
                        String activeAccount = mPrefs.getString(KEY_CONFIGURED_ACCOUNT, "");

                        if (!activeAccount.isEmpty()) {
                            handleConnect(XmppAccount.fromJson(activeAccount));
                        }
                    }

                    return;
                }

                String action = intent.getAction();

                if (ACTION_CONNECT.equals(action)) {
                    handleConnect(XmppAccount.fromJson(intent.getStringExtra(PARAM_ACCOUNT)));

                } else if (ACTION_DISCONNECT.equals(action)) {
                    handleDisconnect();

                } else if (ACTION_LOGIN.equals(action)) {
                    Logger.debug(TAG, "handleLogin");
                    handleLogin(XmppAccount.fromJson(intent.getStringExtra(PARAM_ACCOUNT)));

                } else if (ACTION_REGISTER.equals(action)) {
                    Logger.debug(TAG, "handleRegister");
                    handleRegister(XmppAccount.fromJson(intent.getStringExtra(PARAM_ACCOUNT)));

                } else if (ACTION_SEND.equals(action)) {
                    handleSendMessage(intent);

                } else if (ACTION_DELETE_MESSAGE.equals(action)) {
                    handleDeleteMessage(intent);

                } else if (ACTION_SEND_PENDING_MESSAGES.equals(action)) {
                    handleSendPendingMessages();

                } else if (ACTION_ADD_CONTACT.equals(action)) {
                    handleAddContact(intent);

                } else if (ACTION_REMOVE_CONTACT.equals(action)) {
                    handleRemoveContact(intent);

                } else if (ACTION_RENAME_CONTACT.equals(action)) {
                    handleRenameContact(intent);

                } else if (ACTION_REFRESH_CONTACT.equals(action)) {
                    try {
                        handleRefreshContact(intent);
                    } catch (XmppStringprepException e) {
                        e.printStackTrace();
                    }

                } else if (ACTION_CLEAR_CONVERSATIONS.equals(action)) {
                    handleClearConversations(intent);

                } else if (ACTION_SET_AVATAR.equals(action)) {
                    handleSetAvatar(intent);

                } else if (ACTION_SET_PRESENCE.equals(action)) {
                    handleSetPresence(intent);

                }
            }
        });

        return START_STICKY;
    }

    private void handleConnect(XmppAccount account) {
        try {
            if (mConnection != null) {
                mConnection.disconnect();
            }

            mConnection = new XmppServiceConnection(account, mDatabase);
            if( account.getXmppJid() == null)
                mConnection.connect(false);
            else
                mConnection.connect(true);
            mConnection.sendPendingMessages();
            mPrefs.edit().putString(KEY_CONFIGURED_ACCOUNT, account.toString()).apply();

        } catch (Exception exc) {
            Logger.error(TAG, "Error while connecting", exc);
            XmppServiceBroadcastEventEmitter.broadcastXmppDisconnected();
        }
    }

    private void handleDisconnect() {
        if (mConnection != null) {
            mConnection.disconnect();
        }

        mPrefs.edit().remove(KEY_CONFIGURED_ACCOUNT).apply();
        stopSelf();
    }

    private void handleLogin(XmppAccount account) {
        try {
            if( mConnection == null) {
                mConnection = new XmppServiceConnection(account, mDatabase);
                mConnection.connect(true);
            } else {
                mConnection.dologin(account);
            }
            mConnection.sendPendingMessages();
            mPrefs.edit().putString(KEY_CONFIGURED_ACCOUNT, account.toString()).apply();

        } catch (Exception exc) {
            Logger.error(TAG, "Error while connecting", exc);
            XmppServiceBroadcastEventEmitter.broadcastAuthenticateFailure();
        }
    }

    private void handleRegister(XmppAccount account) {
        try {
            if( mConnection == null) {
                mConnection = new XmppServiceConnection(account, mDatabase);
                mConnection.connect(false);
            }
            registered = mConnection.register(account);
            if (registered) {
                XmppServiceBroadcastEventEmitter.broadcastRegistered();
            } else {
                XmppServiceBroadcastEventEmitter.broadcastRegisterFailure();
            }
        } catch (Exception exc) {
            Logger.error(TAG, "Error while connecting", exc);
            XmppServiceBroadcastEventEmitter.broadcastRegisterFailure();
        }
    }

    private void handleSendMessage(Intent intent) {
        String remoteAccount = intent.getStringExtra(PARAM_REMOTE_ACCOUNT);
        String message = intent.getStringExtra(PARAM_MESSAGE);

        try {
            mConnection.addMessageAndProcessPending(remoteAccount, message);

        } catch (Exception exc) {
            Logger.error(TAG, "Error while sending message to " + remoteAccount, exc);
        }
    }

    private void handleDeleteMessage(Intent intent) {
        long messageID = intent.getLongExtra(PARAM_MESSAGE_ID, -1);

        try {
            new MessagesProvider(mDatabase).deleteMessage(messageID).execute();
            XmppServiceBroadcastEventEmitter.broadcastMessageDeleted(messageID);

        } catch (Exception exc) {
            Logger.error(TAG, "Error while deleting message with ID=" + messageID, exc);
        }
    }

    private void handleSendPendingMessages() {
        try {
            mConnection.sendPendingMessages();
        } catch (Exception exc) {
            Logger.error(TAG, "Error while sending pending messages", exc);
        }
    }

    private void handleAddContact(Intent intent) {
        String remoteAccount = intent.getStringExtra(PARAM_REMOTE_ACCOUNT);
        String alias = intent.getStringExtra(PARAM_ALIAS);

        try {
            mConnection.addContact(remoteAccount, alias);

        } catch (Exception exc) {
            Logger.error(TAG, "Error while adding contact: " + remoteAccount, exc);
        }
    }

    private void handleRemoveContact(Intent intent) {
        String remoteAccount = intent.getStringExtra(PARAM_REMOTE_ACCOUNT);

        try {
            mConnection.removeContact(remoteAccount);

        } catch (Exception exc) {
            Logger.error(TAG, "Error while removing contact: " + remoteAccount, exc);
        }
    }

    private void handleRenameContact(Intent intent) {
        String remoteAccount = intent.getStringExtra(PARAM_REMOTE_ACCOUNT);
        String alias = intent.getStringExtra(PARAM_ALIAS);

        try {
            mConnection.renameContact(remoteAccount, alias);

        } catch (Exception exc) {
            Logger.error(TAG, "Error while renaming contact: " + remoteAccount, exc);
        }
    }

    private void handleRefreshContact(Intent intent) throws XmppStringprepException {
        String remoteAccount = intent.getStringExtra(PARAM_REMOTE_ACCOUNT);

        if (mConnection != null)
            mConnection.singleEntryUpdated(remoteAccount);
    }

    private void handleClearConversations(Intent intent) {
        String remoteAccount = intent.getStringExtra(PARAM_REMOTE_ACCOUNT);

        if (mConnection != null)
            mConnection.clearConversationsWith(remoteAccount);
    }

    private void handleSetAvatar(Intent intent) {
        String avatarPath = intent.getStringExtra(PARAM_AVATAR_PATH);

        try {
            mConnection.setOwnAvatar(avatarPath);
        } catch (Exception exc) {
            Logger.error(TAG, "Error while setting own avatar", exc);
        }
    }

    private void handleSetPresence(Intent intent) {
        int presenceMode = intent.getIntExtra(PARAM_PRESENCE_MODE, -1);
        String personalMessage = intent.getStringExtra(PARAM_PERSONAL_MESSAGE);

        try {
            mConnection.setPresence(presenceMode, personalMessage);
        } catch (Exception exc) {
            Logger.error(TAG, "Error while setting presence", exc);
        }
    }
}
