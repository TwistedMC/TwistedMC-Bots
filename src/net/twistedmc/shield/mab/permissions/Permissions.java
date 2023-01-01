package net.twistedmc.shield.mab.permissions;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;

public class Permissions {
    public static boolean checkLevel(User user, Member member, PermissionLevel level) {
        switch (level) {
            case CREATOR:
                if (user.getId().equals("478410064919527437")) {
                    return true;
                } else if (user.getId().equals("208757906428919808")) {
                    return true;
                } else {
                    return false;
                }
            case TRUSTED:
                if (user.getId().equals("898439795490033714")) {
                    return true;
                } else {
                    return false;
                }
            case OWNER:
                if (member.isOwner()) {
                    return true;
                } else {
                    return false;
                }
            case ADMIN:
                if (member.hasPermission(Permission.ADMINISTRATOR)) {
                    return true;
                } else {
                    return false;
                }
            case MODERATOR:
                if (member.hasPermission(Permission.MODERATE_MEMBERS)) {
                    return true;
                } else {
                    return false;
                }
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