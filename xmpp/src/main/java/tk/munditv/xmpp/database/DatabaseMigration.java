package tk.munditv.xmpp.database;

import android.database.sqlite.SQLiteDatabase;

/**
 * Interface for a database migration
 * @author Aleksandar Gotev
 */
public interface DatabaseMigration {
    /**
     * SQL to execute to apply the migration.
     * @param db
     */
    void up(SQLiteDatabase db);

    /**
     * SQL to execute to apply to revert the migration.
     * @param db
     */
    void down(SQLiteDatabase db);
}
