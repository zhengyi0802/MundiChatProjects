package tk.munditv.xmpp.database;

/**
 * Interface containing methods to implement for doing database transactions.
 *
 * @author Aleksandar Gotev
 */

import android.database.sqlite.SQLiteDatabase;

public interface TransactionStatements {

    /**
     * Contains all the statements to be executed in the transaction.
     *
     * @param database database instance
     */
    void transactionStatements(SQLiteDatabase database);
}
