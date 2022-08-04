package net.twistedmc.shield.Util;

public enum ModerationCommandAction {
    WARN("warning","Warning","Warning issued by Moderator."),
    KICK("kick","Kick","Kick Issued by Moderator"),
    BAN("ban","Ban","Ban Issued by Moderator"),
    UNDERAGE("underage","Underage Ban","Underage User. (< 13)"),
    SCAMBAN("scamban","Compromised Account","Compromised Account"),
    VIRTUALBAN("virtban","Virtual Ban","Virtually Banned by Moderator"),
    TIMEOUT("timeout","Timeout","Timed out by Moderator");




    private String label;
    private String actionLabel;
    private String defaultReason;

    ModerationCommandAction(String label,String actionLabel,String defaultReason) {
        this.label = label;
        this.actionLabel = actionLabel;
        this.defaultReason = defaultReason;
    }

    public String getlabel() {
        return label;
    }

    public String getActionLabel() {
        return actionLabel;
    }

    public String getDefaultReason() {
        return defaultReason;
    }
}
