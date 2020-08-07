package tk.munditv.xmpp;

import org.jxmpp.stringprep.XmppStringprepException;

public interface MessageCallback {
    public void onMessageAdded(String remoteAccount, boolean incoming) throws XmppStringprepException;
    public void onConnected();
    public void onDisconnected();
    public void onAuthenticated();
    public void onAuthenticateFailure();
    public void onRegistered();
    public void onRegisterFailure();
    public void onRosterChanged(String entries);
    public void onContactAdded(String remoteAccount);
    public void onContactRemoved(String remoteAccount);
    public void onContactRenamed(String remoteAccount, String newAlias);
    public void onContactAddError(String remoteAccount);
    public void onConversationsCleared(String remoteAccount);
    public void onConversationsClearError(String remoteAccount);
    public void onMessageSent(long messageId);
    public void onMessageDeleted(long messageId);
    public void onGetRosterEntries();
}
