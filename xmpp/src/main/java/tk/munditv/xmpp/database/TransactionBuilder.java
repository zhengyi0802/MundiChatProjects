package tk.munditv.xmpp.database;

import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import tk.munditv.xmpp.Logger;

/**
 * @author Aleksandar Gotev
 */
public class TransactionBuilder {

    private static final String LOG_TAG = "TransactionBuilder";
    private List<TransactionStatements> statements;

    private SqLiteDatabase database;

    public TransactionBuilder(final SqLiteDatabase database) {
        statements = new ArrayList<>();
        this.database = database;
    }

    public TransactionBuilder add(TransactionStatements statement) {
        statements.add(statement);
        return this;
    }

    public void execute() {
        Logger.debug(LOG_TAG, "Executing transaction");

        SQLiteDatabase db = database.getWritableDatabase();

        db.beginTransaction();

        try {
            for (TransactionStatements stmts : statements) {
                stmts.transactionStatements(db);
            }

            db.setTransactionSuccessful();
            Logger.debug(LOG_TAG, "Transaction executed successfully");

        } finally {
            db.endTransaction();
        }
    }

}
