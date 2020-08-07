package tk.munditv.xmpp.database;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import tk.munditv.xmpp.Logger;

/**
 * @author Aleksandar Gotev
 */
public class Provider<T extends DatabaseModel> {

    private static final String LOG_TAG = "Provider";

    protected String tableName;
    protected Class<T> clazz;
    protected SqLiteDatabase database;

    public Provider(Class<T> clazz, final String tableName, final SqLiteDatabase database) {
        this.clazz = clazz;
        this.tableName = tableName;
        this.database = database;
    }

    public TransactionBuilder getNewTransactionBuilder() {
        return new TransactionBuilder(database);
    }

    protected List<T> queryList(String where) {
        String query = "SELECT * FROM " + tableName + " WHERE " + where;

        return getListFromCursor(database.query(query));
    }

    protected List<T> getListFromCursor(final Cursor result) {
        List<T> list;

        try {
            if (result != null && result.getCount() > 0) {
                list = new ArrayList<>(result.getCount());

                while (result.moveToNext()) {
                    try {
                        T newModel = clazz.cast(clazz.newInstance());
                        newModel.setValuesFromCursor(result);
                        list.add(newModel);
                    } catch (Exception exc) {
                        Logger.error(LOG_TAG, "Error while creating new instance", exc);
                    }
                }

            } else {
                list = new ArrayList<>(1);
            }
        } finally {
            result.close();
        }

        return list;
    }

    protected long executeCountQuery(String where) {
        String query = "SELECT COUNT(*) FROM " + tableName + " WHERE " + where;

        if (database == null) return 0;

        Cursor result = database.query(query);

        long total;

        try {
            if (result != null && result.getCount() > 0) {
                if (result.moveToFirst()) {
                    total = result.getLong(0);
                } else {
                    total = 0;
                }

            } else {
                total = 0;
            }
        } finally {
            result.close();
        }

        return total;
    }

    protected void deleteAll() {
        database.deleteAll(tableName);
    }

    private void insertRecord(SQLiteDatabase database, T record) {
        long id = database.insert(tableName, null, record.toContentValues());

        if (id > 0) {
            record.setId(id);
        }
    }

    private TransactionBuilder ensureTransactionBuilder(TransactionBuilder builder) {
        if (builder == null) {
            return getNewTransactionBuilder();
        }

        return builder;
    }

    protected TransactionBuilder add(final T record,
                                     TransactionBuilder transactionBuilderToAppendTo) {
        if (record == null) return null;

        TransactionBuilder transaction = ensureTransactionBuilder(transactionBuilderToAppendTo);

        transaction.add(new TransactionStatements() {
            @Override
            public void transactionStatements(SQLiteDatabase database) {
                insertRecord(database, record);
            }
        });

        return transaction;
    }

    protected TransactionBuilder add(final T record) {
        return add(record, null);
    }

    protected TransactionBuilder add(final List<T> records,
                                     TransactionBuilder transactionBuilderToAppendTo) {
        if (records == null || records.isEmpty()) return null;

        TransactionBuilder transaction = ensureTransactionBuilder(transactionBuilderToAppendTo);

        transaction.add(new TransactionStatements() {
            @Override
            public void transactionStatements(SQLiteDatabase database) {
                for (T record : records) {
                    insertRecord(database, record);
                }
            }
        });

        return transaction;
    }

    protected TransactionBuilder add(final List<T> records) {
        return add(records, null);
    }

    protected TransactionBuilder delete(final String whereClause,
                                        final String[] whereArgs,
                                        TransactionBuilder transactionBuilderToAppendTo) {

        TransactionBuilder transaction = ensureTransactionBuilder(transactionBuilderToAppendTo);

        transaction.add(new TransactionStatements() {
            @Override
            public void transactionStatements(SQLiteDatabase database) {
                database.delete(tableName, whereClause, whereArgs);
            }
        });

        return transaction;
    }

    protected TransactionBuilder delete(final String whereClause, final String[] whereArgs) {
        return delete(whereClause, whereArgs, null);
    }

    protected final String getLongListAsCsv(List<Long> list) {
        if (list == null || list.isEmpty()) return "";

        StringBuilder str = new StringBuilder();

        for (Long id : list) {
            str.append(id).append(",");
        }

        str.deleteCharAt(str.length() - 1);

        return str.toString();
    }
}
