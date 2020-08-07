package tk.munditv.xmpp;

import com.google.gson.Gson;

/**
 * Xmpp Account configuration.
 * @author gotev (Aleksandar Gotev)
 */
public class XmppAccount {

    // presence mode constants
    public static final int PRESENCE_MODE_CHAT = 0;
    public static final int PRESENCE_MODE_AVAILABLE = 1;
    public static final int PRESENCE_MODE_AWAY = 2;
    public static final int PRESENCE_MODE_XA = 3;
    public static final int PRESENCE_MODE_DND = 4;

    private String xmppJid;
    private String serviceName;
    private String password;
    private String host;
    private int port;
    private int priority;
    private String resourceName;
    private String personalMessage;
    private int presenceMode;
    private boolean isBOSH = true;
    private boolean isHttps = true;
    private String filePath = "/http-bind";

    public String getXmppJid() {
        return xmppJid;
    }

    public void setXmppJid(String xmppJid) {
        this.xmppJid = xmppJid;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public String getResourceName() {
        return resourceName;
    }

    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }

    public String getPersonalMessage() {
        return personalMessage == null ? "" : personalMessage;
    }

    public void setPersonalMessage(String personalMessage) {
        this.personalMessage = personalMessage;
    }

    public int getPresenceMode() {
        return presenceMode;
    }

    /**
     * Sets the presence mode for this account.
     * @param presenceMode integer value indicating the presence mode. Use constants defined in this
     *                     class, for example {@link XmppAccount#PRESENCE_MODE_AVAILABLE}.
     */
    public void setPresenceMode(int presenceMode) {
        this.presenceMode = presenceMode;
    }

    public boolean useBOSH() {
        return isBOSH;
    }

    public boolean useHttps() {
        return isHttps;
    }

    public String getFilePath() {
        return filePath;
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }

    public static XmppAccount fromJson(String json) {
        return new Gson().fromJson(json, XmppAccount.class);
    }
}
