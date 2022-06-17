package net.twistedmc.shield.Util;

public enum ModerationCommandAction {
    WARN("warning","WARNING","Warning issued by Moderator."),
    KICK("kick","KICK","Kick Issued by Moderator"),
    BAN("ban","BAN","Ban Issued by Moderator"),
    UNDERAGE("underage","UNDERAGE-BAN","Underage User. (< 13)"),
    SCAMBAN("scamban","CA-BAN","Compromised Account"),
    VIRTUALBAN("virtban","VIRTUAL-BAN","Virtually Banned by Moderator"),
    TIMEOUT("timeout","TIMEOUT","Timed out by Moderator");




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
