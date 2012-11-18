/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.boreeas.irccore;

import java.io.IOException;
import net.boreeas.irc.ConfigKey;
import net.boreeas.irc.IRCBot;
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
            bot.sendRaw("PONG :" + evt.code());
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

            bot.handleCommand(evt.user(), evt.target(), prefix, command, actualArgs);
        } catch (IOException ex) {
            logger.error("Error while replying to message");
        }
    }

    private boolean checkForCommandPrefix(String chan, String s) {
        return s.startsWith(bot.commandPrefix(chan));
    }

    private boolean checkForBotNamePrefix(String s) {
        return s.toLowerCase().startsWith(bot.nick().toLowerCase());
    }
}
