package tk.munditv.xmpp.database.loaders;


import android.content.Context;

import androidx.loader.content.AsyncTaskLoader;

import java.util.ArrayList;
import java.util.List;

import tk.munditv.xmpp.Logger;
import tk.munditv.xmpp.XmppServiceBroadcastEventReceiver;
import tk.munditv.xmpp.XmppServiceCommand;
import tk.munditv.xmpp.database.SqLiteDatabase;
import tk.munditv.xmpp.database.models.Message;
import tk.munditv.xmpp.database.providers.MessagesProvider;

/**
 * @author Aleksandar Gotev
 */
public class ChatMessagesLoader extends AsyncTaskLoader<List<Message>> {

    private String account;
    private String mRemoteAccount;
    private MessagesProvider messagesProvider;
    private List<Message> mLastLoaded;

    private XmppServiceBroadcastEventReceiver mXmppReceiver = new XmppServiceBroadcastEventReceiver() {

        private boolean isCurrentRemoteAccount(String remoteAccount) {
            return mRemoteAccount != null && remoteAccount.startsWith(mRemoteAccount);
        }

        @Override
        public void onMessageAdded(String remoteAccount, boolean incoming) {
            if (isCurrentRemoteAccount(remoteAccount)) {
                onContentChanged();
            }
        }

        @Override
        public void onConversationsCleared(String remoteAccount) {
            if (isCurrentRemoteAccount(remoteAccount)) {
                onContentChanged();
            }
        }

        @Override
        public void onMessageSent(long messageId) {
            if (mLastLoaded != null) {
                for (Message msg : mLastLoaded) {
                    if (msg.getId() == messageId) {
                        onContentChanged();
                        break;
                    }
                }
            }
        }

        @Override
        public void onMessageDeleted(long messageId) {
            if (mLastLoaded != null) {
                for (Message msg : mLastLoaded) {
                    if (msg.getId() == messageId) {
                        onContentChanged();
                        break;
                    }
                }
            }
        }
    };

    public ChatMessagesLoader(Context context, SqLiteDatabase database, String account, String remoteAccount) {
        super(context);
        this.account = account;
        this.mRemoteAccount = remoteAccount;
        messagesProvider = new MessagesProvider(database);
    }

    @Override
    protected void onStartLoading() {
        super.onStartLoading();
        mXmppReceiver.register(getContext());
    }

    @Override
    protected void onReset() {
        super.onReset();
        mXmppReceiver.unregister(getContext());
    }

    @Override
    public List<Message> loadInBackground() {
        mLastLoaded = messagesProvider.getMessagesWithRecipient(account, mRemoteAccount);

        List<Long> readMessageIds = new ArrayList<>();
        for (Message msg : mLastLoaded) {
            readMessageIds.add(msg.getId());
        }

        try {
            messagesProvider.setReadMessages(readMessageIds).execute();
            XmppServiceCommand.refreshContact(getContext(), mRemoteAccount);
        } catch (Exception exc) {
            Logger.error("MessagesLoader", "Error while setting read messages", exc);
        }

        return mLastLoaded;
    }
}
