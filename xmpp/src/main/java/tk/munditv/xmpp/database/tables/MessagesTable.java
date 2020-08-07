package tk.munditv.xmpp.database.tables;

import android.provider.BaseColumns;

import tk.munditv.xmpp.database.DatabaseTable;

/**
 * @author Aleksandar Gotev
 */
public class MessagesTable implements DatabaseTable {
    public static final String NAME = "messages";

    public static final String COL_ID = BaseColumns._ID;
    public static final String COL_CREATION_TIMESTAMP = "creation_timestamp";
    public static final String COL_ACCOUNT = "account";
    public static final String COL_REMOTE_ACCOUNT = "remote_account";
    public static final String COL_DIRECTION = "direction";
    public static final String COL_MESSAGE = "message";
    public static final String COL_STATUS = "status";
    public static final String COL_SENT_TIMESTAMP = "sent_timestamp";
    public static final String COL_DELIVERED_TIMESTAMP = "delivered_timestamp";
    public static final String COL_READ_TIMESTAMP = "read_timestamp";

    public enum Direction {
        INCOMING,
        OUTGOING
    }

    public enum Status {
        WAITING_FOR_SEND,
        ERROR,
        SENT,
        DELIVERED,
        READ
    }

    @Override
    public String getCreateSql() {
        StringBuilder sql = new StringBuilder();

        sql.append("CREATE TABLE ").append(NAME).append(" (")
           .append(COL_ID).append(" INTEGER PRIMARY KEY AUTOINCREMENT,")
           .append(COL_CREATION_TIMESTAMP).append(" INT NOT NULL,")
           .append(COL_ACCOUNT).append(" TEXT NOT NULL,")
           .append(COL_REMOTE_ACCOUNT).append(" TEXT NOT NULL,")
           .append(COL_DIRECTION).append(" INT NOT NULL,")
           .append(COL_MESSAGE).append(" TEXT NOT NULL,")
           .append(COL_STATUS).append(" INT NOT NULL,")
           .append(COL_SENT_TIMESTAMP).append(" INT NOT NULL,")
           .append(COL_DELIVERED_TIMESTAMP).append(" INT NOT NULL,")
           .append(COL_READ_TIMESTAMP).append(" INT NOT NULL")
           .append(");");

        return sql.toString();
    }

    @Override
    public String getDropSql() {
        return "DROP TABLE IF EXISTS " + NAME + ";";
    }
}
