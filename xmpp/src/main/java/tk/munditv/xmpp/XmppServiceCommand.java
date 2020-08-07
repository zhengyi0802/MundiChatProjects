package tk.munditv.xmpp;

import android.content.Context;
import android.content.Intent;

/**
 * Triggers xmpp service commands.
 * @author gotev (Aleksandar Gotev)
 */
public class XmppServiceCommand {

    private final static String TAG = "XmppServiceCommand";

    protected static final String ACTION_CONNECT = ".xmpp.connect";
    protected static final String ACTION_DISCONNECT = ".xmpp.disconnect";
    protected static final String ACTION_LOGIN = ".xmpp.login";
    protected static final String ACTION_REGISTER = ".xmpp.register";
    protected static final String ACTION_SEND = ".xmpp.send";
    protected static final String ACTION_ADD_CONTACT = ".xmpp.addcontact";
    protected static final String ACTION_REMOVE_CONTACT = ".xmpp.removecontact";
    protected static final String ACTION_CLEAR_CONVERSATIONS = ".xmpp.clearconversations";
    protected static final String ACTION_RENAME_CONTACT = ".xmpp.renamecontact";
    protected static final String ACTION_REFRESH_CONTACT = ".xmpp.refreshcontact";
    protected static final String ACTION_SEND_PENDING_MESSAGES = ".xmpp.sendpending";
    protected static final String ACTION_SET_AVATAR = ".xmpp.setavatar";
    protected static final String ACTION_DELETE_MESSAGE = ".xmpp.deletemessage";
    protected static final String ACTION_SET_PRESENCE = ".xmpp.setpresence";
    protected static final String ACTION_GET_ROSTERENTRIES = ".xmpp.getrosterentries";

    protected static final String PARAM_ACCOUNT = "account";
    protected static final String PARAM_REMOTE_ACCOUNT = "remoteAccount";
    protected static final String PARAM_MESSAGE = "message";
    protected static final String PARAM_MESSAGE_ID = "message_id";
    protected static final String PARAM_ALIAS = "alias";
    protected static final String PARAM_PRESENCE_MODE = "presenceMode";
    protected static final String PARAM_PERSONAL_MESSAGE = "personalMessage";
    protected static final String PARAM_AVATAR_PATH = "avatarPath";

    /**
     * Triggers service connection.
     * @param context application context
     */
    public static void connect(Context context, XmppAccount account) {
        Logger.debug(TAG, "connect()");
        Intent intent = new Intent(context, XmppService.class);
        intent.setAction(ACTION_CONNECT);
        intent.putExtra(PARAM_ACCOUNT, account.toString());
        context.startService(intent);
    }

    /**
     * Triggers disconnection.
     * @param context application context
     */
    public static void disconnect(Context context) {
        Logger.debug(TAG, "disconnect()");
        Intent intent = new Intent(context, XmppService.class);
        intent.setAction(ACTION_DISCONNECT);
        context.startService(intent);
    }

    /**
     * Triggers login
     * @param context application context
     */
    public static void login(Context context, XmppAccount account) {
        Logger.debug(TAG, "login()");
        Intent intent = new Intent(context, XmppService.class);
        intent.setAction(ACTION_LOGIN);
        intent.putExtra(PARAM_ACCOUNT, account.toString());
        context.startService(intent);
    }

    /**
     * Triggers register
     * @param context application context
     */
    public static void register(Context context, XmppAccount account) {
        Logger.debug(TAG, "register()");
        Intent intent = new Intent(context, XmppService.class);
        intent.setAction(ACTION_REGISTER);
        intent.putExtra(PARAM_ACCOUNT, account.toString());
        context.startService(intent);
    }

    /**
     * Sends a message to a remote account.
     * @param context application context
     * @param remoteAccount xmpp JID
     * @param message message
     */
    public static void sendMessage(Context context, String remoteAccount, String message) {
        Logger.debug(TAG, "sendMessage()");

        Intent intent = new Intent(context, XmppService.class);
        intent.setAction(ACTION_SEND);
        intent.putExtra(PARAM_REMOTE_ACCOUNT, remoteAccount);
        intent.putExtra(PARAM_MESSAGE, message);
        context.startService(intent);
    }

    /**
     * Deletes a message from the XMPP service's database.
     * @param context application context
     * @param messageId unique id of the message to delete
     */
    public static void deleteMessage(Context context, long messageId) {
        Logger.debug(TAG, "deleteMessage()");

        Intent intent = new Intent(context, XmppService.class);
        intent.setAction(ACTION_DELETE_MESSAGE);
        intent.putExtra(PARAM_MESSAGE_ID, messageId);
        context.startService(intent);
    }

    /**
     * Adds a new contact to currently connected user's roster.
     * @param context application context
     * @param remoteAccount xmpp JID to add
     * @param alias alis to give to the xmpp JID
     */
    public static void addContactToRoster(Context context, final String remoteAccount,
                                          final String alias) {
        Logger.debug(TAG, "addContactToRoster()");
        final Intent addContact = new Intent(context, XmppService.class);
        addContact.setAction(ACTION_ADD_CONTACT);
        addContact.putExtra(PARAM_REMOTE_ACCOUNT, remoteAccount);
        addContact.putExtra(PARAM_ALIAS, alias);
        context.startService(addContact);
    }

    /**
     * Removes a contact from the currently connected user's roster.
     * @param context application context
     * @param remoteAccount xmpp JID to remove
     */
    public static void removeContactFromRoster(Context context, String remoteAccount) {
        Logger.debug(TAG, "removeContactFromRoster()");
        final Intent intent = new Intent(context, XmppService.class);
        intent.setAction(ACTION_REMOVE_CONTACT);
        intent.putExtra(PARAM_REMOTE_ACCOUNT, remoteAccount);
        context.startService(intent);
    }

    /**
     * Gives a new alias to an existing xmpp JID which is conained in the current user's roster.
     * @param context application context
     * @param remoteAccount xmpp JID
     * @param newAlias new alias to give to the xmpp JID
     */
    public static void renameContact(Context context, String remoteAccount, String newAlias) {
        Logger.debug(TAG, "renameContact()");
        final Intent intent = new Intent(context, XmppService.class);
        intent.setAction(ACTION_RENAME_CONTACT);
        intent.putExtra(PARAM_REMOTE_ACCOUNT, remoteAccount);
        intent.putExtra(PARAM_ALIAS, newAlias);
        context.startService(intent);
    }

    /**
     * Refreshes a contact in the roster.
     * @param context application context
     * @param remoteAccount xmpp JID to refresh
     */
    public static void refreshContact(Context context, String remoteAccount) {
        Logger.debug(TAG, "refreshContact()");
        final Intent intent = new Intent(context, XmppService.class);
        intent.setAction(ACTION_REFRESH_CONTACT);
        intent.putExtra(PARAM_REMOTE_ACCOUNT, remoteAccount);
        context.startService(intent);
    }

    /**
     * Deletes all the messages exchanged with a given xmpp JID.
     * @param context application context
     * @param remoteAccount xmpp JID
     */
    public static void clearConversations(Context context, String remoteAccount) {
        Logger.debug(TAG, "clearConversations()");
        final Intent intent = new Intent(context, XmppService.class);
        intent.setAction(ACTION_CLEAR_CONVERSATIONS);
        intent.putExtra(PARAM_REMOTE_ACCOUNT, remoteAccount);
        context.startService(intent);
    }

    /**
     * Triggers the sending of the pending messages.
     * @param context application context
     */
    public static void sendPendingMessages(Context context) {
        Logger.debug(TAG, "sendPendingMessages()");
        final Intent intent = new Intent(context, XmppService.class);
        intent.setAction(ACTION_SEND_PENDING_MESSAGES);
        context.startService(intent);
    }

    /**
     * Sets the avatar for the currently logged in user.
     * @param context application context
     * @param avatarPath absolute path to the file which contains the new avatar
     */
    public static void setAvatar(Context context, String avatarPath) {
        Logger.debug(TAG, "setAvatar()");
        final Intent intent = new Intent(context, XmppService.class);
        intent.setAction(ACTION_SET_AVATAR);
        intent.putExtra(PARAM_AVATAR_PATH, avatarPath);
        context.startService(intent);
    }

    /**
     * Sets the presence for the currently logged in user.
     * @param context application context
     * @param presenceMode presence mode to assign
     * @param personalMsg personal message
     */
    public static void setPresence(Context context, int presenceMode, String personalMsg) {
        Logger.debug(TAG, "setPresence()");
        final Intent intent = new Intent(context, XmppService.class);
        intent.setAction(ACTION_SET_PRESENCE);
        intent.putExtra(PARAM_PRESENCE_MODE, presenceMode);
        intent.putExtra(PARAM_PERSONAL_MESSAGE, personalMsg);
        context.startService(intent);
    }

    public static void getRosterEntries(Context context) {
        Logger.debug(TAG, "getRosterEntries()");
        final Intent intent = new Intent(context, XmppService.class);
        intent.setAction(ACTION_GET_ROSTERENTRIES);
        context.startService(intent);
    }

}
