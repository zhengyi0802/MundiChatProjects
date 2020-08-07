package tk.munditv.xmpp;

import android.content.Context;
import android.content.Intent;

import com.google.gson.Gson;

import java.util.ArrayList;

/**
 * Emits the xmpp service broadcast intents.
 * @author gotev (Aleksandar Gotev)
 */
public class XmppServiceBroadcastEventEmitter {

    private final static String TAG = "XmppServiceBroadcastEventEmitter";

    private final static String DEFAULT_NAMESPACE = "tk.munditv.xmpp";
    public static final String BROADCAST_CONNECTED = ".xmpp.connected";
    public static final String BROADCAST_DISCONNECTED = ".xmpp.disconnected";
    public static final String BROADCAST_AUTHENTICATED = ".xmpp.authentocated";
    public static final String BROADCAST_AUTHENTICATEFAILURE = ".xmpp.authenticatefailure";
    public static final String BROADCAST_REGISTERED = ".xmpp.registered";
    public static final String BROADCAST_REGISTERFAILURE = ".xmpp.registerfailure";
    public static final String BROADCAST_ROSTER_CHANGED = ".xmpp.rosterchanged";
    public static final String BROADCAST_MESSAGE_ADDED = ".xmpp.messageadded";
    public static final String BROADCAST_MESSAGE_DELETED = ".xmpp.messagedeleted";
    public static final String BROADCAST_CONTACT_ADDED = ".xmpp.contactadded";
    public static final String BROADCAST_CONTACT_ADD_ERROR = ".xmpp.contactadderror";
    public static final String BROADCAST_CONTACT_REMOVED = ".xmpp.contactremoved";
    public static final String BROADCAST_CONVERSATIONS_CLEARED = ".xmpp.conversationscleared";
    public static final String BROADCAST_CONVERSATIONS_CLEAR_ERROR = ".xmpp.conversationsclearerror";
    public static final String BROADCAST_CONTACT_RENAMED = ".xmpp.contactrenamed";
    public static final String BROADCAST_MESSAGE_SENT = ".xmpp.messagesent";
    public static final String BROADCAST_GET_ROSTER_ENTRIES = ".xmpp.getrosterentrues{";

    public static final String PARAM_REMOTE_ACCOUNT = "remoteAccount";
    public static final String PARAM_ALIAS = "alias";
    public static final String PARAM_MESSAGE_ID = "messageId";
    public static final String PARAM_INCOMING = "incoming";
    public static final String PARAM_ROSTERS = "rosters";

    private static Context mContext;
    private static String mNamespace;

    /**
     * Private constructor to avoid instantiation.
     */
    private XmppServiceBroadcastEventEmitter() { }

    public static synchronized void initialize(Context context) {
        Logger.debug(TAG, "initialize()");
        mContext = context;
        mNamespace = DEFAULT_NAMESPACE;
    }

    public static synchronized void initialize(Context context, String namespace) {
        Logger.debug(TAG, "initialize()");
        mContext = context;
        mNamespace = namespace;
    }

    public static String getNamespace() {
        return mNamespace;
    }

    public static synchronized void broadcastRosterChanged(ArrayList<XmppRosterEntry> entries) {
        Logger.debug(TAG, "broadcastRosterChanged()");
        Intent broadcast = new Intent();
        broadcast.setAction(mNamespace + BROADCAST_ROSTER_CHANGED);
        String str = new Gson().toJson(entries);
        broadcast.putExtra(PARAM_ROSTERS, str);
        mContext.sendBroadcast(broadcast);
    }

    public static synchronized void broadcastXmppConnected() {
        Logger.debug(TAG, "broadcastXmppConnected()");

        Intent broadcast = new Intent();
        broadcast.setAction(mNamespace + BROADCAST_CONNECTED);
        mContext.sendBroadcast(broadcast);
    }

    public static synchronized void broadcastXmppDisconnected() {
        Logger.debug(TAG, "broadcastXmppDisconnected()");

        Intent broadcast = new Intent();
        broadcast.setAction(mNamespace + BROADCAST_DISCONNECTED);
        mContext.sendBroadcast(broadcast);
    }

    public static synchronized void broadcastAuthenticated() {
        Logger.debug(TAG, "BroadcastAuthenticated()");

        Intent broadcast = new Intent();
        broadcast.setAction(mNamespace + BROADCAST_AUTHENTICATED);
        mContext.sendBroadcast(broadcast);
    }

    public static synchronized void broadcastAuthenticateFailure() {
        Logger.debug(TAG, "broadcastAuthenticateFailure()");

        Intent broadcast = new Intent();
        broadcast.setAction(mNamespace + BROADCAST_AUTHENTICATEFAILURE);
        mContext.sendBroadcast(broadcast);
    }

    public static synchronized void broadcastRegistered() {
        Logger.debug(TAG, "broadcastRegistered()");

        Intent broadcast = new Intent();
        broadcast.setAction(mNamespace + BROADCAST_REGISTERED);
        mContext.sendBroadcast(broadcast);
    }

    public static synchronized void broadcastRegisterFailure() {
        Logger.debug(TAG, "broadcastRegisterFailure()");

        Intent broadcast = new Intent();
        broadcast.setAction(mNamespace + BROADCAST_REGISTERFAILURE);
        mContext.sendBroadcast(broadcast);
    }

    public static synchronized void broadcastMessageAdded(String remoteAccount, boolean incoming) {
        Logger.debug(TAG, "broadcastMessageAdded()");

        Intent broadcast = new Intent();

        broadcast.setAction(mNamespace + BROADCAST_MESSAGE_ADDED);
        broadcast.putExtra(PARAM_REMOTE_ACCOUNT, remoteAccount);
        broadcast.putExtra(PARAM_INCOMING, incoming);

        mContext.sendBroadcast(broadcast);
    }

    public static synchronized void broadcastMessageDeleted(long messageId) {
        Logger.debug(TAG, "broadcastMessageDeleted()");

        Intent broadcast = new Intent();

        broadcast.setAction(mNamespace + BROADCAST_MESSAGE_DELETED);
        broadcast.putExtra(PARAM_MESSAGE_ID, messageId);

        mContext.sendBroadcast(broadcast);
    }

    public static synchronized void broadcastContactAdded(String remoteAccount) {
        Logger.debug(TAG, "broadcastContactAdded()");

        Intent broadcast = new Intent();
        broadcast.setAction(mNamespace + BROADCAST_CONTACT_ADDED);
        broadcast.putExtra(PARAM_REMOTE_ACCOUNT, remoteAccount);
        mContext.sendBroadcast(broadcast);
    }

    public static synchronized void broadcastContactAddError(String remoteAccount) {
        Logger.debug(TAG, "broadcastContactAddError()");
        Intent broadcast = new Intent();
        broadcast.setAction(mNamespace + BROADCAST_CONTACT_ADD_ERROR);
        broadcast.putExtra(PARAM_REMOTE_ACCOUNT, remoteAccount);
        mContext.sendBroadcast(broadcast);
    }

    public static synchronized void broadcastContactRemoved(String remoteAccount) {
        Logger.debug(TAG, "broadcastContactRemoved()");
        Intent broadcast = new Intent();
        broadcast.setAction(mNamespace + BROADCAST_CONTACT_REMOVED);
        broadcast.putExtra(PARAM_REMOTE_ACCOUNT, remoteAccount);
        mContext.sendBroadcast(broadcast);
    }

    public static synchronized void broadcastConversationsCleared(String remoteAccount) {
        Logger.debug(TAG, "broadcastConversationsCleared()");
        Intent broadcast = new Intent();
        broadcast.setAction(mNamespace + BROADCAST_CONVERSATIONS_CLEARED);
        broadcast.putExtra(PARAM_REMOTE_ACCOUNT, remoteAccount);
        mContext.sendBroadcast(broadcast);
    }

    public static synchronized void broadcastConversationsClearError(String remoteAccount) {
        Logger.debug(TAG, "broadcastConversationsClearError()");
        Intent broadcast = new Intent();
        broadcast.setAction(mNamespace + BROADCAST_CONVERSATIONS_CLEAR_ERROR);
        broadcast.putExtra(PARAM_REMOTE_ACCOUNT, remoteAccount);
        mContext.sendBroadcast(broadcast);
    }

    public static synchronized void broadcastContactRenamed(String remoteAccount, String newAlias) {
        Logger.debug(TAG, "broadcastContactRenamed()");
        Intent broadcast = new Intent();
        broadcast.setAction(mNamespace + BROADCAST_CONTACT_RENAMED);
        broadcast.putExtra(PARAM_REMOTE_ACCOUNT, remoteAccount);
        broadcast.putExtra(PARAM_ALIAS, newAlias);
        mContext.sendBroadcast(broadcast);
    }

    public static synchronized void broadcastMessageSent(long messageId) {
        Logger.debug(TAG, "broadcastMessageSent()");
        Intent broadcast = new Intent();
        broadcast.setAction(mNamespace + BROADCAST_MESSAGE_SENT);
        broadcast.putExtra(PARAM_MESSAGE_ID, messageId);
        mContext.sendBroadcast(broadcast);
    }

    public static synchronized void broadcastGetRosterEntries() {
        Logger.debug(TAG, "broadcastGetRosterEntries()");
        Intent broadcast = new Intent();
        broadcast.setAction(mNamespace + BROADCAST_GET_ROSTER_ENTRIES);
        mContext.sendBroadcast(broadcast);
    }
}
