package tk.munditv.chat;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;


import tk.munditv.xmpp.Logger;
import tk.munditv.xmpp.XmppAccount;
import tk.munditv.xmpp.XmppServiceBroadcastEventEmitter;
import tk.munditv.xmpp.XmppServiceCommand;

public class MainApp extends Application {

    private static final String TAG = "MainApp";
    private static final String HOSTNAME = "webrtc01.mundi-tv.tk";

    private static MainApp mInstance;
    private XmppAccount xmppAccount;
    private String ottAccount;
    private SharedPreferences preferences;

    public static MainApp getInstance() {
        return mInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        initialize();
    }

    private void initialize() {
        Logger.debug(TAG, "initialize()");
        XmppServiceBroadcastEventEmitter.initialize(this, "tk.munditv.chat");
        xmppAccount = new XmppAccount();
        xmppAccount.setResourceName(getString(R.string.rc_resourcename));
        xmppAccount.setPort(443);
        xmppAccount.setPresenceMode(XmppAccount.PRESENCE_MODE_AVAILABLE);
        xmppAccount.setPersonalMessage("Controller");
        xmppAccount.setPriority(0);
        String account = preferences.getString("account", null);
        String password = preferences.getString("password", null);
        String host = preferences.getString("host", null);
        ottAccount = preferences.getString("ottaccount", null);
        xmppAccount.setXmppJid(account);
        xmppAccount.setPassword(password);
        xmppAccount.setHost(host);
        xmppAccount.setServiceName(host);

        Logger.debug(TAG, "xmppAccount.XmppJid = " + xmppAccount.getXmppJid());
        Logger.debug(TAG, "xmppAccount.Password = " + xmppAccount.getPassword());
        Logger.debug(TAG, "xmppAccount.Host = " + xmppAccount.getHost());
        Logger.debug(TAG, "xmppAccount.port = " + xmppAccount.getPort());
        Logger.debug(TAG, "xmppAccount.ServiceName = " + xmppAccount.getServiceName());
        Logger.debug(TAG, "xmppAccount.ResourceName = " + xmppAccount.getResourceName());
        Logger.debug(TAG, "xmppAccount.FilePath = " + xmppAccount.getFilePath());
        Logger.debug(TAG, "xmppAccount.getPersonalMessage = " + xmppAccount.getPersonalMessage());
    }

    public XmppAccount getXmppAccount() {
        Logger.debug(TAG, "getXmppAccount() = " + xmppAccount.getXmppJid());
        return xmppAccount;
    }

    public String getOttAccount() {
        ottAccount = preferences.getString("ottaccount", null);
        Logger.debug(TAG, "getOttAccount() = " + ottAccount);
        return ottAccount;
    }

    public void connect() {
        Logger.debug(TAG, "connect() account = " + xmppAccount.getXmppJid());
        if(xmppAccount.getHost() == null || xmppAccount.getServiceName() == null) {
            xmppAccount.setHost(HOSTNAME);
            xmppAccount.setServiceName(HOSTNAME);
        }
        XmppServiceCommand.connect(this, xmppAccount);
    }

    public void disconnect() {
        Logger.debug(TAG, "disconnect()");
        XmppServiceCommand.disconnect(this);
    }

    public void login() {
        Logger.debug(TAG, "login()");
        XmppServiceCommand.login(this, xmppAccount);
    }

    public void register() {
        Logger.debug(TAG, "register()");
        XmppServiceCommand.register(this, xmppAccount);
    }

    public void sendMessage(String remoteAccount, String message) {
        Logger.debug(TAG, "sendMessage() remoteAccount = " + remoteAccount);
        Logger.debug(TAG, "sendMessage() message = " + message);
        XmppServiceCommand.sendMessage(this, remoteAccount, message);
    }

    public void deleteMessage(long messageId) {
        Logger.debug(TAG, "deleteMessage()");
        XmppServiceCommand.deleteMessage(this, messageId);
    }

    public void addContact(String remoteAccount, String alias) {
        Logger.debug(TAG, "addContact()");
        XmppServiceCommand.addContactToRoster(this, remoteAccount, alias);
    }

    public void removeContact(String remoteAccount) {
        Logger.debug(TAG, "removeContact()");
        XmppServiceCommand.removeContactFromRoster(this, remoteAccount);
    }

    public void renameContact(String remoteAccount, String newAlias) {
        Logger.debug(TAG, "renameContact()");
        XmppServiceCommand.renameContact(this, remoteAccount, newAlias);
    }

    public void refreshContact(String remoteAccount) {
        Logger.debug(TAG, "refreshContact()");
        XmppServiceCommand.refreshContact(this, remoteAccount);
    }

    public void clearConversation(String remoteAccount) {
        Logger.debug(TAG, "clearConversation()");
        XmppServiceCommand.clearConversations(this, remoteAccount);
    }

    public void sendPendingMessages() {
        Logger.debug(TAG, "sendPendingMessages()");
        XmppServiceCommand.sendPendingMessages(this);
    }

    public void setAvatar(String path) {
        Logger.debug(TAG, "setAvatar()");
        XmppServiceCommand.setAvatar(this, path);
    }

    public void setPresence(int presenceMode, String personalMessage) {
        Logger.debug(TAG, "setPresence()");
        XmppServiceCommand.setPresence(this, presenceMode, personalMessage);
    }

    public void getRosterEntries() {
        Logger.debug(TAG, "getRosterEntries()");
        XmppServiceCommand.getRosterEntries(this);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        Logger.debug(TAG, "onTerminate()");
        disconnect();
        xmppAccount = null;
    }

}
