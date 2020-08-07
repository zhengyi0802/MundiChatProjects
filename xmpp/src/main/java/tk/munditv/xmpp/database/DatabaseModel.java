package tk.munditv.xmpp.database;

import android.content.ContentValues;
import android.database.Cursor;

/**
 * @author Aleksandar Gotev
 */
public interface DatabaseModel {
    void setValuesFromCursor(Cursor cursor);
    ContentValues toContentValues();
    void setId(long id);
}
