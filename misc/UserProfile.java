package misc;

import org.jibble.pircbot.User;

/**
 * A UserProfile stores basic information about the user in question: nickname,
 * hostname, last seen time, away status, and if applicable, the reason for being away.
 * 
 * @author Computerology
 */
public class UserProfile {

    String nick, hostname;

    String away_reason = "";
    boolean away = false;
    double last_active_time_since_epoch = 0;

    /**
     * Creates a new UserProfile instance.
     * @param nickname The connected client's nickname.
     * @param hostname The connected client's hostname.
     */
    public UserProfile(String nickname, String hostname) {
        this.nick = nickname;
        this.hostname = hostname;
    }

    /**@return The nickname of this UserProfile.*/
    public String getNickname() {
        return nick;
    }

    /**@return The hostname of this UserProfile.*/
    public String getHostname() {
        return hostname;
    }

    /**@return The reason this user is away.*/
    public String getAwayReason() {
        return away_reason;
    }
    
    /**Sets the away reason for this user.
     * @param r The reason.
     */
    public void setAwayReason(String r) {
        away_reason = r;
    }

    /**
     * Sets the away status of this user.
     * @param a A boolean indicating if this user is to be away.
     */
    public void setAway(boolean a) {
        away = a;
    }

    /**
     * Gets the away status of this user.
     * @return A boolean indicating if this user is away.
     */
    public boolean isAway() {
        return away;
    }

    /**
     * If the user associated with this profile is online, this method will return false.
     * @return A boolean indicating whether the user is offline.
     */
    public boolean isOffline() {
        boolean offline = true;
        User[] user_list = main.Main.main_bot.getUsers("#aspergers");
        for (int i = 0; i != user_list.length; i++) {
            if (user_list[i].getNick().equals(nick)) {
                offline = false;
            }
        }
        return offline;
    }

    /**
     * A description of when this user was last active.
     * @return A String which describes when the user has been active last. 
     * For example, "User was last active 5 minutes ago".
     */
    public String getLastActiveStatus() {

        if (last_active_time_since_epoch == 0) {
            return nick+" has never been active on this channel";
        }

        long milliseconds_ago = (int)(System.currentTimeMillis()-last_active_time_since_epoch);
        long days_ago = milliseconds_ago/86400000;
        long hours_ago = milliseconds_ago/3600000;
        long minutes_ago = milliseconds_ago/60000;

        if (hours_ago == 0) {
            if (minutes_ago > 0) {
                return nick+" was last active "+minutes_ago+" minute(s) ago";
            } else {
                if (isOffline() == false) {
                    return nick+" is active right now";
                } else {
                    return "";
                }
            }
        } else if (hours_ago <= 24) {
            if ((minutes_ago % (60*hours_ago)) == 0) {
                return nick+" was last active "+hours_ago+" hour(s) ago";
            } else {
                return nick+" was last active "+hours_ago+" hour(s) and "+(minutes_ago % (60*hours_ago))+" minute(s) ago";
            }
        } else if (hours_ago > 24) {
            return nick+" was last active "+days_ago+" day(s) and "+(hours_ago % (24*days_ago))+" hour(s) ago";
        } else {
            return "";
        }

    }

    /**
     * The time this user was last active at, formatted as the number of milliseconds since epoch.
     * @return The time (in milliseconds since epoch) that this user was last active at.
     */
    public double getTimeLastActive() {
        return last_active_time_since_epoch;
    }

    /**Sets the last active time to the current time.*/
    public void updateTimeLastActive() {
        last_active_time_since_epoch = System.currentTimeMillis();
    }

    /**Sets the last active time to a custom time.
     * @param t The time (in milliseconds since epoch) that this user was last active at.
     */
    public void setTimeLastActive(double t) {
        last_active_time_since_epoch = t;
    }

}