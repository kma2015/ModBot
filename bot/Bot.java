package bot;

import bot.storage.Database;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import main.Main;
import misc.UserProfile;
import org.jibble.pircbot.*;

/**
 * The class that holds the bot's main logic. An instance of this class is created on startup.
 * @author Computerology
 */
public class Bot extends PircBot {

    //messages
    ArrayList<String> voters = new ArrayList<String>();    //voting
    int mute_votes = 0, unmute_votes = 0;
    double vote_timer = 60000; //60 seconds
    String accused = "";

    boolean playing_number_game = false;
    int number_chosen = 0;
    String closest_user = "";
    int closest_value = 1000000;
    double game_timer = 30000;

    /**
     * Creates a new instance of ModBot. Called in Main. Calling it more than once will probably screw things up.
     * @param channel The name of the channel ModBot calls home :) ("#channel" format)
     */
    public Bot() {
        this.setName(Main.username);
    }

    /**
     * Updates ModBot. 
     * This includes finding online users that are not in ModBot's database and adding them, and reconnecting if need be.
     * This method does not handle incoming messages or actions.
     */
    public void update() {
        //every 500 mills
        try {
            Thread.sleep(500);
        } catch (InterruptedException ex) {
            Logger.getLogger(Bot.class.getName()).log(Level.SEVERE, null, ex);
        }

        //every 500 milliseconds, check for users with no profile and add a basic profile for them
        //this does not include hostname but that should not matter in most cases
        this.addAllOnlineUsersToDatabase();

        //if you lose connection, reconnect
        if (this.isConnected() == false) {

            //wait 60 seconds before reconnecting
            try {
                Thread.sleep(60000);
            } catch (InterruptedException ex) {
                Logger.getLogger(Bot.class.getName()).log(Level.SEVERE, null, ex);
            }

            System.out.println("Lost connection to server: reconnecting now...");
            try {
                this.reconnect();
                this.joinChannel(Main.channel);
            } catch (IOException ex) {
                Logger.getLogger(Bot.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IrcException ex) {
                Logger.getLogger(Bot.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        //if vote is happening, countdown timer
        if (accused.length() > 0) {
            vote_timer-=500;//subtract 500 mills
            if (vote_timer <= 0) {
                voters.clear();
                mute_votes = 0;
                accused = "";
                this.sendMessage(Main.channel, "The active vote has expired.");
                vote_timer=60000;
            }
        }
    }

    public void onMessage(String channel, String sender, String login, String hostname, String message) {
        this.handleMessage(channel, sender, login, hostname, message);
    }

    public void onPrivateMessage(String sender, String login, String hostname, String message) {
        if (message.toLowerCase().contains("say something")) {
            //find something to say that is within bounds and is not about ModBot
            while (true) {
                int random_index = Math.abs(new Random().nextInt() % Database.getMessages().size());
                if (random_index < Database.getMessages().size() && random_index > -1) {
                    String response = Database.getMessages().get(random_index);
                    if (response.contains("ModBot") == false) {
                        this.sendMessage(sender, Database.getMessages().get(random_index));
                        break;
                    }
                }
            }
        } else if (message.toLowerCase().contains("clear reports")) {
            if (sender.equals("Computerology") && (Database.getUser(sender).isOp())) {
                Database.getReports().clear();
                this.sendMessage("Computerology", "All user reports cleared. Have a nice day, "+sender);
            } else {
                this.sendMessage("Computerology", "My programmer has restricted the usage of that command. Sorry!");
            }
        } else if (message.toLowerCase().contains("help")) {
            this.sendMessage(sender, "As well as a lot of the usual IRC commands, I also have some of my own built-in"
                    + " commands. Here is the full list. And remember, you can always contact a moderator for assistance if my limitations as a bot"
                    + " begin to show.");
            this.sendMessage(sender, "You can say, \""+Main.username+", <command>\". <command> being any of the following: mute <user>, seen <user>, report <user> for <reason>, Ping, Marco, do <action>, "
                    + "think of a number, I'm leaving <reason>.");
        } else if (message.contains("sendMessage ")) {
            if (sender.equals("Computerology") && (Database.getUser(sender).isOp())) {
                String message_to_send = message.substring(12);
                this.sendMessage(Main.channel, message_to_send);
            }
        }
    }

    /**
     * Handles incoming messages (used as a global handling method for all message types). PircBot's onMessage and onPrivateMessage call this.
     * @see PircBot#onMessage(String, String, String, String, String) PircBot.onMessage
     */
    public void handleMessage(String channel, String sender, String login, String hostname, String message) {
        //store the message for reference :)
        Database.getMessages().add(message);
        //create user profile if not already created
        Database.addUser(sender, hostname);
        //get list of users
        User[] user_list = this.getUsers(Main.channel);
        UserProfile message_creator = Database.getUserProfileFromNickname(sender);
        if (message_creator != null) {
            message_creator.updateTimeLastActive();
        }

        //mark as not away
        if (Database.getUserProfileFromNickname(sender) != null) {
            Database.getUserProfileFromNickname(sender).setAway(false);
        }

        //save to file
        Database.saveToFile();

        //only activate commands if contains "ModBot" in the first part of the string
        if (message.indexOf(Main.username) == 0) {
            //help
            if (message.contains(Main.username+", help")) {
                this.sendMessage(channel, sender+", I have sent you a private message containing some useful information that may help you.");
                this.sendMessage(sender, "Greetings, "+sender+". As well as a lot of the usual IRC commands, I also have some of my own built-in"
                        + " commands. Here is the full list. And remember, you can always contact a moderator for assistance if my limitations as a bot"
                        + " begin to show.");
                this.sendMessage(sender, "You can say, \""+Main.username+", <command>\". <command> being any of the following: mute <user>, seen <user>, report <user> for <reason>, Ping, Marco, do <action>, "
                        + "think of a number, I'm leaving <reason>.");
            }
            
            else if (message.contains(Main.username+", elevator")) {
                this.sendMessage(channel, "http://tholman.com/elevator.js/");
            }
            
            else if (message.contains(Main.username) && message.contains("statistics")) {
                int chatlog_size = Database.getMessages().size();
                int userbase = Database.getUserCount();
                this.sendMessage(Main.channel, "I have "+chatlog_size+" messages and "+userbase+" user profiles stored in my database."
                        + " I do not keep track of anything else.");
            }

            //leave with reason
            else if (message.contains(Main.username+", I'm leaving")) {
                int index = message.indexOf(Main.username+", I'm leaving")+((Main.username+", I'm leaving ").length());
                String reason;
                if (message.contains(Main.username+", I'm leaving ")) {
                    reason = message.substring(index);
                } else {
                    reason = "";
                }
                //store reason in user profile
                if (Database.getUserProfileFromNickname(sender) != null) {
                    Database.getUserProfileFromNickname(sender).setAwayReason(reason);
                    Database.getUserProfileFromNickname(sender).setAway(true);
                    this.sendMessage(channel, "See you soon, "+sender+"!");
                }
            }

            //get last seen time
            else if (message.contains(Main.username+", seen ")) {
                //remove question marks
                message = message.replace("?", "");
                System.out.println("Seen command");
                int index = message.indexOf(Main.username+", seen ")+((Main.username+", seen ").length());
                String username = message.substring(index);
                if (username.equals("ModBot") == false) {
                    //post time last active
                    UserProfile user = Database.getUserProfileFromNickname(username);
                    String response = "";
                    if (user != null) {
                        //if online
                        if (user.isOffline() == false) {
                            if (user.isAway()) {
                                if (user.getAwayReason().length() > 1) {
                                    response+=username+" has left \""+user.getAwayReason()+"\"";
                                } else {
                                    response+=username+" has left. No reason was given.";
                                }
                            } else {
                                response+="According to my records, "+user.getLastActiveStatus()+". ";
                            }

                        } else { //if offline
                            response+=username+" is offline. "+user.getLastActiveStatus()+". ";
                            if (user.getAwayReason().length() > 1) {
                                response+="They left \""+user.getAwayReason()+"\"";
                            }
                        }
                    } else {
                        response+="I did not find "+username+" in my records. Check your spelling?";
                    }
                    this.sendMessage(channel, response);
                }
            }

            //handle mutes
            else if (message.contains(Main.username+", mute ")) {
                if (unmute_votes <= 0) {
                    vote_timer=60000;
                    int index = message.indexOf(Main.username+", mute ")+((Main.username+", mute ").length());
                    String username = message.substring(index);
                    //make sure no one votes against other people who are not the accused
                    if (username.equals(accused) || accused.equals("")) {
                        //if username is a valid user
                        for (int i = 0; i != user_list.length; i++) {
                            if (user_list[i].getNick().equals(username)) {
                                if (accused.length() <= 0) {
                                    accused = user_list[i].getNick();
                                }
                            }
                        }
                        //if the person_in_question is assigned
                        if (accused.length() > 0) {
                            //if voter has not voted before
                            if (voters.contains(sender) == false) {
                                voters.add(sender);
                                //count the vote
                                mute_votes++;
                                //inform server_ip of kick vote
                                if ((5-mute_votes) > 1) {
                                    this.sendMessage(channel, sender+" voted to mute "+username+". "+(5-mute_votes)+" more votes needed.");
                                } else if ((5-mute_votes) == 1) {
                                    this.sendMessage(channel, sender+" voted to mute "+username+". 1 more vote is needed.");
                                }
                                if (mute_votes >= 5) {
                                    this.sendMessage(channel, username+" was silenced! To unmute them in the future, say \""+Main.username+", unmute "+username+"\".");
                                    this.sendMessage(channel, "!quiet "+username);
                                    mute_votes = 0;
                                    unmute_votes = 0;
                                    voters.clear();
                                    accused = "";
                                }
                            } else {
                                this.sendMessage(channel, "You have already voted, "+sender+"!");
                            }
                        } else {
                            this.sendMessage(channel, "Could not find user! Check your spelling.");
                        }
                    } else {
                        this.sendMessage(channel, "A vote is already taking place against "+accused+"!");
                    }
                } else {
                    this.sendMessage(channel, "There is already a vote taking place, "+sender+"!");
                }
            }

            //handle unmutes
            else if (message.contains(Main.username+", unmute ")) {
                if (mute_votes <= 0) {
                    vote_timer=60000;
                    int index = message.indexOf(Main.username+", unmute ")+((Main.username+", unmute ").length());
                    String username = message.substring(index);
                    //make sure no one votes against others who are not the accused
                    if (username.equals(accused) || accused.equals("")) {
                        //if username is a valid user
                        for (int i = 0; i != user_list.length; i++) {
                            if (user_list[i].getNick().equals(username)) {
                                if (accused.length() <= 0) {
                                    accused = user_list[i].getNick();
                                }
                            }
                        }
                        //if the person_in_question is assigned
                        if (accused.length() > 0) {
                            //if voter has not voted before
                            if (voters.contains(sender) == false) {
                                voters.add(sender);
                                //count the vote
                                unmute_votes++;
                                //inform server_ip of kick vote
                                if ((5-unmute_votes) > 1) {
                                    this.sendMessage(channel, sender+" voted to unmute "+username+". "+(5-unmute_votes)+" more votes needed.");
                                } else if ((5-unmute_votes) == 1) {
                                    this.sendMessage(channel, sender+" voted to unmute "+username+". 1 more vote is needed.");
                                }
                                if (unmute_votes >= 5) {
                                    this.sendMessage(channel, username+" was unmuted!");
                                    this.sendMessage(channel, "!unquiet "+username);
                                    mute_votes = 0;
                                    unmute_votes = 0;
                                    voters.clear();
                                    accused = "";
                                }
                            } else {
                                this.sendMessage(channel, "You have already voted, "+sender+"!");
                            }
                        } else {
                            this.sendMessage(channel, "Could not find user! Check your spelling.");
                        }
                    } else {
                        this.sendMessage(channel, "A vote is already taking place against "+accused+"!");
                    }
                } else {
                    this.sendMessage(channel, "There is already a vote taking place!");
                }
            }

            //prompt ModBot to say something random
            else if (message.contains(Main.username+", say something")) {
                //find something to say that is within bounds and is not about ModBot
                while (true) {
                    int random_index = Math.abs(new Random().nextInt() % Database.getMessages().size());
                    if (random_index < Database.getMessages().size() && random_index > -1) {
                        String response = Database.getMessages().get(random_index);
                        if (response.contains("ModBot") == false) {
                            this.sendMessage(channel, Database.getMessages().get(random_index));
                            break;
                        }
                    }
                }
            }

            //handle kicks
            else if (message.contains(Main.username+", report ")) {
                int index = message.indexOf(Main.username+", report ")+((Main.username+", report ").length());
                String username = message.substring(index, message.indexOf(" for "));
                String reason = message.substring(message.indexOf(" for ")+(" for ".length()));
                //if username is a valid user
                boolean valid = false;
                for (int i = 0; i != user_list.length; i++) {
                    if (user_list[i].getNick().equals(username)) {
                        valid = true;
                    }
                }
                //if the person being reported is online
                if (valid) {
                    this.sendMessage(channel, sender+" reported "+username+" for "+reason);
                    Database.getReports().add(sender+" reported "+username+" for "+reason);
                    //save report
                } else {
                    this.sendMessage(channel, username+" is not online! Check your spelling.");
                }
            }

            //cancel voting
            else if (message.contains(Main.username+", cancel vote")) {
                if (sender.equals("Computerology") && (Database.getUser(sender).isOp())) {
                    if (accused.length() > 0) {
                        voters.clear();
                        mute_votes = 0;
                        unmute_votes = 0;
                        accused = "";
                        this.sendMessage(channel, "Of course, "+sender+". The vote has been cancelled.");
                    } else {
                        this.sendMessage(channel, "There are no active votes, "+sender+".");
                    }
                } else {
                    this.sendMessage(channel, "My programmer has restricted the usage of that command. Sorry!");
                }
            }

            //kick all
            else if (message.contains(Main.username+", kick all")) {
                if (sender.equals("Computerology") && (Database.getUser(sender).isOp())) {
                    kickAll();
                } else {
                    this.sendMessage(channel, "My programmer has restricted the usage of that command. Sorry!");
                }
            }
        }
        //print message details, just cause
        System.out.println(sender+": "+message+" ("+hostname+" to "+channel+")");
    }

    public void onNickChange(String oldNick, String login, String hostname, String newNick) {
        //print this event to the console
        System.out.println(oldNick+" has changed their nickname to "+newNick);
    }

    /**
     * Finds all online users and creates user profiles for the ones without.
     */
    public void addAllOnlineUsersToDatabase() {
        User[] user_list = this.getUsers(Main.channel);
        for (int i = 0; i != user_list.length; i++) {
            Database.addUser(user_list[i].getNick(), "");
        }
    }

    public void onQuit(String nick, String login, String hostname, String reason) {
        System.out.println(nick+" ("+hostname+") has quit: "+reason);
    }

    public void onJoin(String channel, String sender, String login, String hostname) {

        System.out.println(sender+" ("+hostname+") has joined "+channel);

        //create profile
        if (Database.getUserProfileFromNickname(sender) == null) {
            Database.addUser(sender, hostname);
        }
        if (sender.equals("Computerology") && (Database.getUser(sender).isOp())) {
            if (Database.getReports().isEmpty() == false) {
                this.sendMessage("Computerology", "Greetings, Computerology! In your absence, some reports have been made against other users:");
                for (int i = 0; i != Database.getReports().size(); i++) {
                    this.sendMessage("Computerology", Database.getReports().get(i));
                }
                this.sendMessage("Computerology", "Say \""+Main.username+", clear reports\" to mark all reports as read.");
            }
        }

        Database.saveToFile();
    }

    /**
     * Kicks all users from the server. Calling this will probably make everyone mad :)
     */
    public void kickAll() {
        User[] user_list = this.getUsers(Main.channel);
        for (int i = 0; i != user_list.length; i++) {
            //if not modbot or chanserv
            if (user_list[i].getNick().equals("ChanServ") == false && user_list[i].getNick().equals("ModBot") == false) {
                this.kick(Main.channel, user_list[i].getNick(), "You have been kicked!");
            }
        }
    }

}
