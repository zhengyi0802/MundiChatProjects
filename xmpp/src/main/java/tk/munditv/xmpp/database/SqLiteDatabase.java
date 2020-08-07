package tk.munditv.xmpp.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import tk.munditv.xmpp.Logger;
import tk.munditv.xmpp.database.migrations.M1_CreateMessagesTable;

/**
 * @author Aleksandar Gotev
 */
public class SqLiteDatabase extends SQLiteOpenHelper {

    private static final String TAG = "Database";

    private Context context;

    // register database migrations here
    private static final DatabaseMigration[] migrations = new DatabaseMigration[] {
            new M1_CreateMessagesTable()
    };

    public SqLiteDatabase(final Context context, final String databaseName) {
        super(new DatabaseContext(context), databaseName, null, migrations.length);
        Logger.debug(TAG, "Create SqLiteDatabase");
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Logger.info(TAG, "Creating database");

        for (DatabaseMigration migration : migrations) {
            migration.up(db);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Logger.info(TAG, "Upgrading schema from " + oldVersion + " to " + newVersion);

        for (int i = oldVersion; i < newVersion; i++) {
            Logger.info(TAG, "Upgrading from " + i + " to " + (i + 1));
            migrations[i].up(db);
        }
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Logger.info(TAG, "Downgrading schema from " + oldVersion + " to " + newVersion);

        for (int i = oldVersion; i > newVersion; i++) {
            Logger.info(TAG, "Downgrading from " + i + " to " + (i - 1));
            migrations[i - 1].down(db);
        }
    }

    /**
     * Performs a query for all the records in a table.
     *
     * @param tableName
     * @param orderedByColumnName
     * @param ascendant
     * @return A Cursor object, which is positioned before the first entry.
     * Note that Cursors are not synchronized, see the documentation for more details.
     */
    public Cursor query(String tableName, String orderedByColumnName, boolean ascendant) {
        StringBuilder query = new StringBuilder();

        query.append("SELECT * FROM ").append(tableName)
                .append(" ORDER BY ").append(orderedByColumnName);

        if (!ascendant) {
            query.append(" DESC");
        }

        Logger.debug(TAG, "Query: " + query.toString());

        try {
            return getReadableDatabase().rawQuery(query.toString(), null);
        } catch (Exception exc) {
            return null;
        }
    }

    /**
     * Performs an SQL query.
     *
     * @param sqlQuery sql statement
     * @return A Cursor object, which is positioned before the first entry.
     * Note that Cursors are not synchronized, see the documentation for more details.
     */
    public Cursor query(String sqlQuery) {
        Logger.debug(TAG, "Query: " + sqlQuery);

        try {
            return getReadableDatabase().rawQuery(sqlQuery, null);
        } catch (Exception exc) {
            return null;
        }
    }

    /**
     * Performs a query by ID on a table.
     *
     * @param tableName table on which to perform the query
     * @param id id of the record in the table
     * @return A Cursor object, which is positioned before the first entry.
     * Note that Cursors are not synchronized, see the documentation for more details.
     */
    public Cursor queryById(String tableName, long id) {
        return query("SELECT * FROM " + tableName + " WHERE " + BaseColumns._ID + " = " + id);
    }

    /**
     * Insert a record in a table.
     *
     * @param tableName table name
     * @param values record data
     * @return new record id or -1 if an error occurs
     */
    public long insert(String tableName, ContentValues values) {
        Logger.debug(TAG, "Inserting record in " + tableName);

        return getWritableDatabase().insert(tableName, null, values);
    }

    /**
     * Update a record in a table.
     *
     * @param tableName table name
     * @param id record ID
     * @param values values to update
     * @return number of affected rows
     */
    public int update(String tableName, long id, ContentValues values) {
        Logger.debug(TAG, "Updating record with ID = " + id + " in table " + tableName);

        String selection = BaseColumns._ID + " = ?";
        String[] selectionArgs = {String.valueOf(id)};

        return getWritableDatabase().update(tableName, values, selection, selectionArgs);
    }

    /**
     * Delete a record from a table.
     *
     * @param tableName table name
     * @param id record ID
     * @return number of affected rows
     */
    public int delete(String tableName, long id) {
        Logger.debug(TAG, "Deleting record with ID = " + id + " from table " + tableName);

        String selection = BaseColumns._ID + " = ?";
        String[] selectionArgs = {String.valueOf(id)};

        return getWritableDatabase().delete(tableName, selection, selectionArgs);
    }

    /**
     * Delete all the records from a table.
     *
     * @param tableName table from which to delete all the records
     * @return number of affected rows
     */
    public int deleteAll(String tableName) {
        Logger.debug(TAG, "Deleting all records from table " + tableName);

        return getWritableDatabase().delete(tableName, "1", null);
    }

    public Context getContext() {
        return context;
    }

    /**
     * Performs a database transaction.
     * All the statements contained in the interface implementation will be executed.
     *
     * @param db database on which to perform the transaction
     * @param transaction statements to execute in the transaction
     */
    public static void executeTransaction(SQLiteDatabase db, TransactionStatements transaction) {
        Logger.debug(TAG, "Executing transaction");

        db.beginTransaction();
        try {
            transaction.transactionStatements(db);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    /**
     * Utility method to get an integer value from a cursor, given the column name.
     * @param cursor cursor which contains data
     * @param columnName column name
     * @return column's value
     */
    public static int getIntFromCursor(Cursor cursor, String columnName) {
        return cursor.getInt(cursor.getColumnIndex(columnName));
    }

    /**
     * Utility method to get a long value from a cursor, given the column name.
     * @param cursor cursor which contains data
     * @param columnName column name
     * @return column's value
     */
    public static long getLongFromCursor(Cursor cursor, String columnName) {
        return cursor.getLong(cursor.getColumnIndex(columnName));
    }

    /**
     * Utility method to get a string value from a cursor, given the column name.
     * @param cursor cursor which contains data
     * @param columnName column name
     * @return column's value
     */
    public static String getStringFromCursor(Cursor cursor, String columnName) {
        return cursor.getString(cursor.getColumnIndex(columnName));
    }
}
