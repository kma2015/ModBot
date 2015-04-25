package main;

import bot.Bot;
import bot.storage.Database;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jibble.pircbot.IrcException;

public class Main {

    public static Bot main_bot;
    /**
     * Part of the 4 main details needed to start properly: the channel, password, username, server IP,
     * and server port to connect to. All of these details are loaded from a file in USER_HOME/ModBot/login.txt 
     * (by Database.loadFromFile)
     */
    public static String channel, password, username, server_ip;
    public static int port;

    public static void main(String[] args) {
        //restore saved data
        Database.loadFromFile();
        main_bot = new Bot();
        try {
            main_bot.connect(server_ip, port, password);
            main_bot.joinChannel(channel);
            main_bot.sendMessage(channel, "Greetings, humans! I am here to assist you!");
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IrcException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }

        while(true) {
            main_bot.update();
        }

    }

}
