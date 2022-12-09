package net.twistedmc.shield.mab.permissions;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;

public class Permissions {
    public static boolean checkLevel(Member member, PermissionLevel level) {
        switch (level) {
            case CREATOR:
                return member.getUser().getId().equals("478410064919527437") || member.getUser().getId().equals("208757906428919808");
            case OWNER:
                return member.isOwner();
            case ADMIN:
                return member.hasPermission(Permission.ADMINISTRATOR);
            case MODERATOR:
                return member.hasPermission(Permission.MODERATE_MEMBERS);
            case EVERYONE:
                return true;
            default:
                return false;
        }
    }

    public static String getDescription(PermissionLevel level) {
        switch (level) {
            case CREATOR:
                return "Bot Creator";
            case OWNER:
                return "Server Owner";
            case ADMIN:
                return "Administrator";
            case MODERATOR:
                return "Moderator (Able to timeout people)";
            case EVERYONE:
                return "Everyone";
        }

        return "";
    }
}