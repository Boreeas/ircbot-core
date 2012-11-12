/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.boreeas.irccore;

import java.io.IOException;
import net.boreeas.irc.BotAccessLevel;
import net.boreeas.irc.IRCBot;
import net.boreeas.irc.User;
import net.boreeas.irc.Command;
import org.apache.commons.configuration.FileConfiguration;

/**
 *
 * @author Boreeas
 */
public class JumpCommand extends Command {

    public JumpCommand(IRCBot bot) {
        super(bot);
    }

    @Override
    public String getTrigger() {
        return "jump";
    }

    @Override
    public void execute(User sender, String target, String[] args) throws
            IOException {

        String sendTo = getReplyTarget(sender.nick(), target);

        if (!require(sender.nick(), BotAccessLevel.ADMIN)) {

            bot.sendNotice(sendTo, "No, go away");
            return;
        }

        if (args.length < 1) {

            bot.sendNotice(sendTo, "Missing parameter: jump <server> [port]");
            return;
        }

        String host = args[0];
        int port = 6667;

        if (args.length > 1) {
            try {
                port = Integer.parseInt(args[1]);
            } catch (NumberFormatException ex) {
                bot.sendNotice(sendTo, "Invalid parameter 'port': Not an int: "
                                        + args[1]);
                return;
            }
        }

        FileConfiguration config = bot.config();
        String oldHost = config.getString("host");
        int oldPort = config.getInt("port");

        config.setProperty("host", host);
        config.setProperty("port", port);

        IRCBot newBot = new IRCBot(config);
        log.info("[JUMP] Trying to jump to " + host + ":" + port);

        try {
            bot.getPluginManager().disableAllPlugins();
            newBot.connect();

            log.info("[JUMP] Jump successful");
            newBot.start();
            bot.disconnect("JUMP invoked by " + sender + " (" + host + ":"
                           + port + ")");

        } catch (IOException ex) {
            bot.sendNotice(target, "Unable to jump to " + host + ":" + port
                                   + " - IOException (" + ex + ") - Check "
                                   + "the log for more details.");

            log.fatal("[JUMP] Jump failed", ex);

            config.setProperty("host", oldHost);
            config.setProperty("port", oldPort);
            bot.loadPlugins();
        }
    }

    @Override
    public String help() {
        return "Jump to the target network - jump <server> [port]";
    }
}
