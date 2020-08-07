package tk.munditv.xmpp.database.migrations;

import android.database.sqlite.SQLiteDatabase;

import tk.munditv.xmpp.database.DatabaseMigration;
import tk.munditv.xmpp.database.tables.MessagesTable;

/**
 * @author Aleksandar Gotev
 */
public class M1_CreateMessagesTable implements DatabaseMigration {

    private MessagesTable messagesTable;

    public M1_CreateMessagesTable() {
        messagesTable = new MessagesTable();
    }

    @Override
    public void up(SQLiteDatabase db) {
        db.execSQL(messagesTable.getCreateSql());
    }

    @Override
    public void down(SQLiteDatabase db) {
        db.execSQL(messagesTable.getDropSql());
    }
}
