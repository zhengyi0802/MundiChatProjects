package tk.munditv.xmpp.database;

/**
 * @author Aleksandar Gotev
 */
public interface DatabaseTable {
    String getCreateSql();
    String getDropSql();
}
