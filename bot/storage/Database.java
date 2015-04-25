package bot.storage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import main.Main;
import misc.UserProfile;

public class Database {

    static ArrayList<UserProfile> user_profiles = new ArrayList<UserProfile>();
    static ArrayList<String> messages = new ArrayList<String>(), reports = new ArrayList<String>();

    /**
     * Adds a user profile to the database.
     * @param nick The nickname of the user.
     * @param hostname The hostname of the user.
     * @see UserProfile
     */
    public static void addUser(String nick, String hostname) {
        boolean add = true;
        for (int i = 0; i != user_profiles.size(); i++) {
            UserProfile p = user_profiles.get(i);
            if (p != null) {
                if (p.getNickname().equals(nick)) {
                    add = false;
                }
            }
        }
        if (add) {
            user_profiles.add(new UserProfile(nick, hostname));
            System.out.println("Creating profile for "+nick+"...");
        }
    }

    /**
     * Loads from the database. The database is a text file stores at USER_HOME/ModBot/database.txt.
     * @see Database#saveToFile() 
     */
    public static void loadFromFile() {
        String url = System.getProperty("user.home");
        File home_dir = new File(url+"/ModBot/");
        if (home_dir.exists() == false) {
            home_dir.mkdir();
        }
        
        Properties prop = new Properties();
        System.out.println("Loading from "+url+"/ModBot/...");
        
        try {
            //load the properties file
            prop.load(new FileInputStream(url+"/ModBot/database.txt"));
            int user_count = Integer.parseInt(prop.getProperty("userCount"));
            int report_count = Integer.parseInt(prop.getProperty("reportCount"));
            int message_count = Integer.parseInt(prop.getProperty("messageCount"));

            //restore user profiles
            for (int i = 0; i != user_count; i++) {
                addUser(prop.getProperty("user"+i+"Nick"), prop.getProperty("user"+i+"Hostname"));
                getUserProfile(prop.getProperty("user"+i+"Nick"), prop.getProperty("user"+i+"Hostname"))
                        .setTimeLastActive(Double.parseDouble(prop.getProperty("user"+i+"LastActiveTime")));
            }
            System.out.println("Loaded "+user_profiles.size()+" user profiles!");

            //restore reports
            for (int i = 0; i != report_count; i++) {
                reports.add(prop.getProperty("report"+i));
            }
            System.out.println("Loaded "+reports.size()+" reports!");

            //restore message log
            for (int i = 0; i != message_count; i++) {
                messages.add(prop.getProperty("message"+i));
            }
            System.out.println("Loaded chat log ("+messages.size()+" messages)!");
            System.out.println("Loading login credentials...");

        } catch (IOException ex) {System.err.print("No database found! Creating new file.\n");}
        
        prop = new Properties();
        try {
            prop.load(new FileInputStream(url+"/ModBot/login.txt"));
            Main.username = prop.getProperty("username");
            Main.channel = prop.getProperty("channel");
            Main.password = prop.getProperty("password");
            Main.server_ip = prop.getProperty("server_ip");
            Main.port = Integer.parseInt(prop.getProperty("port"));
        } catch (IOException ex) {
            System.err.println(System.getProperty("user.home")+"/ModBot/login.txt must exist"
                    + " and must contain the properties: \nusername, channel, password, server_ip and port");
            Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }

    /**
     * Saves the message log, reports, and user profiles to the database.
     * @see Database#loadFromFile() 
     */
    public static void saveToFile() {
        String url = System.getProperty("user.home")+"/ModBot/database.txt";
        //save to file and close the stream
        try {
            Properties prop = new Properties();

            //store basic user profile data
            prop.setProperty("userCount", user_profiles.size()+"");
            for (int i = 0; i != user_profiles.size(); i++) {
                UserProfile p = user_profiles.get(i);
                prop.setProperty("user"+i+"Nick", p.getNickname());
                prop.setProperty("user"+i+"Hostname", p.getHostname());
                prop.setProperty("user"+i+"LastActiveTime", (p.getTimeLastActive())+"");
            }

            prop.setProperty("reportCount", reports.size()+"");
            for (int i = 0; i != reports.size(); i++) {
                prop.setProperty("report"+i, reports.get(i));
            }

            prop.setProperty("messageCount", messages.size()+"");
            for (int i = 0; i != messages.size(); i++) {
                prop.setProperty("message"+i, messages.get(i));
            }

            FileOutputStream f = new FileOutputStream(url);
            prop.store(f, null);
            f.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**@return The number of user profiles stored in the database.*/
    public static int getUserCount() {
        return user_profiles.size();
    }

    /**
     * Gets the user profile associated with the specified nickname and hostname.
     * @param nick The nickname of the user you want to get.
     * @param hostname The hostname of the user you want to get.
     * @return The associated UserProfile.
     * @see UserProfile
     */
    public static UserProfile getUserProfile(String nick, String hostname) {
        for (int i = 0; i != user_profiles.size(); i++) {
            UserProfile p = user_profiles.get(i);
            if (p.getNickname().equals(nick) && p.getHostname().equals(hostname)) {
                return p;
            }
        }
        return null;
    }

    /**
     * Gets the user profile associated with the specified nickname. The returned UserProfile only matches the specified
     * nickname, so the actual identity of the user in question may not always be guaranteed.
     * @param nick The nickname of the user you want to get.
     * @return The associated UserProfile.
     * @see UserProfile
     */
    public static UserProfile getUserProfileFromNickname(String nick) {
        for (int i = 0; i != user_profiles.size(); i++) {
            UserProfile p = user_profiles.get(i);
            if (p.getNickname().equals(nick)) {
                return p;
            }
        }
        return null;
    }

    /**
     * Gets the user profile associated with the specified hostname. The returned UserProfile only matches the specified
     * hostname, so the actual identity of the user in question may not always be guaranteed. However, it is more certain
     * than finding the UserProfile with only the nickname.
     * @param hostgname The nickname of the user you want to get.
     * @return The associated UserProfile.
     * @see UserProfile
     */
    public static UserProfile getUserProfileFromHostname(String hostname) {
        for (int i = 0; i != user_profiles.size(); i++) {
            UserProfile p = user_profiles.get(i);
            if (p.getHostname().equals(hostname)) {
                return p;
            }
        }
        return null;
    }
    
    /**
     * Get all of the user messages in the database.
     * @return An ArrayList<String> containing all messages.
     */
    public static ArrayList<String> getMessages() {
        return messages;
    }
    
    /**
     * Get all of the user reports in the database. 
     * Reports are sent to /u/Computerology for review when he logs in.
     * @return An ArrayList<String> containing all user reports.
     */
    public static ArrayList<String> getReports() {
        return reports;
    }
}