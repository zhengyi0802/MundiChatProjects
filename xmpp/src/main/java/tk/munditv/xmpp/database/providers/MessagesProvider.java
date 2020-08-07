package tk.munditv.xmpp.database.providers;

import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;

import java.util.Date;
import java.util.List;

import tk.munditv.xmpp.database.Provider;
import tk.munditv.xmpp.database.SqLiteDatabase;
import tk.munditv.xmpp.database.TransactionBuilder;
import tk.munditv.xmpp.database.TransactionStatements;
import tk.munditv.xmpp.database.models.Message;
import tk.munditv.xmpp.database.tables.MessagesTable;

/**
 * Hi-level methods to interact with messages database table.
 *
 * @author Aleksandar Gotev
 */
public class MessagesProvider extends Provider<Message> {

    public MessagesProvider(final SqLiteDatabase database) {
        super(Message.class, MessagesTable.NAME, database);
    }

    public TransactionBuilder addOutgoingMessage(String account, String recipient, String message) {
        Message msg = new Message();

        msg.setAccount(account);
        msg.setRemoteAccount(recipient);
        msg.setMessage(message);
        msg.setDirection(MessagesTable.Direction.OUTGOING);
        msg.setStatus(MessagesTable.Status.WAITING_FOR_SEND);

        return add(msg);
    }

    public TransactionBuilder addIncomingMessage(String account, String recipient, String message) {
        Message msg = new Message();

        msg.setAccount(account);
        msg.setRemoteAccount(recipient);
        msg.setMessage(message);
        msg.setDirection(MessagesTable.Direction.INCOMING);
        msg.setStatus(MessagesTable.Status.DELIVERED);

        return add(msg);
    }

    public List<Message> getMessagesWithRecipient(String account, String recipient) {
        final StringBuilder query = new StringBuilder();

        String escapedAccount = DatabaseUtils.sqlEscapeString(account);
        String escapedRecipient = DatabaseUtils.sqlEscapeString(recipient + "%");

        query.append(MessagesTable.COL_ACCOUNT).append(" = ")
             .append(escapedAccount).append(" AND ")
             .append(MessagesTable.COL_REMOTE_ACCOUNT).append(" LIKE ")
             .append(escapedRecipient).append(" ORDER BY ")
             .append(MessagesTable.COL_CREATION_TIMESTAMP);

        return queryList(query.toString());
    }

    public TransactionBuilder setReadMessages(List<Long> messageIds) {
        final long readTimestamp = new Date().getTime();

        final String query = "UPDATE " + MessagesTable.NAME + " SET "
                     + MessagesTable.COL_STATUS + " = " + MessagesTable.Status.READ.ordinal()
                     + ", " + MessagesTable.COL_READ_TIMESTAMP + " = " + readTimestamp
                     + " WHERE " + MessagesTable.COL_DIRECTION + " = "
                     + MessagesTable.Direction.INCOMING.ordinal() + " AND "
                     + MessagesTable.COL_ID + " IN ("
                     + getLongListAsCsv(messageIds) + ")";

        TransactionBuilder transactionBuilder = getNewTransactionBuilder();
        transactionBuilder.add(new TransactionStatements() {
            @Override
            public void transactionStatements(SQLiteDatabase database) {
                database.execSQL(query);
            }
        });

        return transactionBuilder;
    }

    public TransactionBuilder deleteMessage(long id) {
        return delete(MessagesTable.COL_ID + " = " + id, null);
    }

    public List<Message> getPendingMessages(String account) {
        final StringBuilder query = new StringBuilder();

        String escapedAccount = DatabaseUtils.sqlEscapeString(account);

        query.append(MessagesTable.COL_ACCOUNT).append(" = ")
             .append(escapedAccount).append(" AND ")
             .append(MessagesTable.COL_DIRECTION).append(" = ")
             .append(MessagesTable.Direction.OUTGOING.ordinal())
             .append(" AND ").append(MessagesTable.COL_STATUS).append(" = ")
             .append(MessagesTable.Status.WAITING_FOR_SEND.ordinal()).append(" ORDER BY ")
             .append(MessagesTable.COL_ID);

        return queryList(query.toString());
    }

    public TransactionBuilder updateMessageStatus(long id, MessagesTable.Status newStatus) {
        String query = "UPDATE " + MessagesTable.NAME + " SET "
                     + MessagesTable.COL_STATUS + " = " + newStatus.ordinal();

        if (newStatus == MessagesTable.Status.SENT) {
            query += ", " + MessagesTable.COL_SENT_TIMESTAMP + " = " + new Date().getTime();
        }

        query += " WHERE " + MessagesTable.COL_ID + " = " + id;

        final String finalQuery = query;

        TransactionBuilder transactionBuilder = getNewTransactionBuilder();
        transactionBuilder.add(new TransactionStatements() {
            @Override
            public void transactionStatements(SQLiteDatabase database) {
                database.execSQL(finalQuery);
            }
        });

        return transactionBuilder;
    }

    public TransactionBuilder deleteConversation(String account, String recipient) {
        final StringBuilder query = new StringBuilder();

        String escapedAccount = DatabaseUtils.sqlEscapeString(account);
        String escapedRecipient = DatabaseUtils.sqlEscapeString(recipient + "%");

        query.append(MessagesTable.COL_ACCOUNT).append(" = ")
                .append(escapedAccount).append(" AND ")
                .append(MessagesTable.COL_REMOTE_ACCOUNT).append(" LIKE ")
                .append(escapedRecipient);

        return delete(query.toString(), null);
    }

    public long countUnreadMessages(String account, String recipient) {

        String escapedAccount = DatabaseUtils.sqlEscapeString(account);
        String escapedRecipient = DatabaseUtils.sqlEscapeString(recipient + "%");

        String where = MessagesTable.COL_ACCOUNT + " = " + escapedAccount
                     + " AND " + MessagesTable.COL_REMOTE_ACCOUNT + " LIKE "
                     + escapedRecipient + " AND " + MessagesTable.COL_DIRECTION + " = "
                     + MessagesTable.Direction.INCOMING.ordinal() + " AND "
                     + MessagesTable.COL_STATUS + " <> " + MessagesTable.Status.READ.ordinal();

        return executeCountQuery(where);
    }
}
