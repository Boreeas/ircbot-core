/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.boreeas.irccore;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.boreeas.irc.ConfigKey;
import net.boreeas.irc.IRCBot;
import net.boreeas.irc.ModeChangeBuilder;
import net.boreeas.irc.Preferences;
import net.boreeas.irc.events.*;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author Boreeas
 */
public class CoreEventListener extends DefaultEventListener {

    private final Log logger = LogFactory.getLog("CoreEventListener");
    private IRCBot bot;

    public CoreEventListener(IRCBot bot) {
        this.bot = bot;
    }

    @Override
    public void onPingReceived(PingEvent evt) {

        try {
            bot.send("PONG :" + evt.code());
        } catch (IOException ex) {
            logger.fatal("Unable to send pong", ex);
            logger.info("Reconnecting to prevent ping timeout");
            bot.reconnect();
        }
    }

    @Override
    public void onWelcomeReceived(WelcomeReceivedEvent evt) {

        for (String channel:
             bot.config().getStringArray(ConfigKey.CHANNELS.key())) {
            try {
                bot.joinChannel(channel);
            } catch (IOException ex) {
                logger.fatal("Unable to send join", ex);
                logger.info("Reconnecting to prevent ping timeout");
                bot.reconnect();
            }
        }

        try {
            bot.changeModes(new ModeChangeBuilder().addMode('B'));
        } catch (IOException ex) {
            logger.error("Could not set mode +B (Bot)", ex);
        }
    }

    @Override
    public void onSupportListReceived(SupportListReceivedEvent evt) {

        if (ArrayUtils.contains(evt.supports(), "whox")
            || ArrayUtils.contains(evt.supports(), "WHOX")) {
            bot.getPreferences().setBoolean(Preferences.GLOBAL_WHOX, true);
        }
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent evt) {

        try {
            checkCommand(evt);
        } catch (IOException ex) {
            logger.error("Error while replying to message");
        }
    }

    private void checkCommand(MessageReceivedEvent evt) throws IOException {

        logger.debug("Checking if is command: " + evt.message());
        String prefix = null;
        String command = null;
        String[] actualArgs = null;

        if (checkForCommandPrefix(evt.target(), evt.message())) {
            // Message starts with command prefix

            String[] args = evt.message().split(" ");

            if (args.length < 2) { // <prefix> <command>
                bot.sendNotice(bot.getReplyTarget(evt.target(), evt.user().nick()), "Missing command");
                return;
            }

            // Strip ! from !core help
            prefix = args[0].substring(bot.commandPrefix(evt.target()).length());
            command = args[1];
            actualArgs = (String[]) ArrayUtils.subarray(args, 2, args.length);

        } else if (bot.getPreferences().getBoolean(evt.target(), Core.CMD_LISTEN_TO_NAME)
                   && checkForBotNamePrefix(evt.message())) {
            // Bot listens to messages addressed to itself, and message
            // was addressed to it.

            String[] args = evt.message().split(" ");

            if (args.length < 3) { // <name> <prefix> <command>
                bot.sendNotice(bot.getReplyTarget(evt.target(), evt.user().nick()), "Missing command");
                return;
            }

            prefix = args[1];
            command = args[2];
            actualArgs = (String[]) ArrayUtils.subarray(args, 3, args.length);
        }

        if (command != null) { // if command is assigned, it's a command
            bot.handleCommand(evt.user(), evt.target(), prefix, command, actualArgs);
        }
    }

    private boolean checkForCommandPrefix(String target, String s) {
        logger.debug("Command prefix for " + target  + " is " + bot.commandPrefix(target));
        return s.startsWith(bot.commandPrefix(target));
    }

    private boolean checkForBotNamePrefix(String s) {
        return s.toLowerCase().startsWith(bot.nick().toLowerCase());
    }
}
