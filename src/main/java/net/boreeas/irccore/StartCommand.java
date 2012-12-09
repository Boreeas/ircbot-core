/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.boreeas.irccore;

import java.io.IOException;
import net.boreeas.irc.BotAccessLevel;
import net.boreeas.irc.IrcBot;
import net.boreeas.irc.User;
import net.boreeas.irc.Command;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.plist.PropertyListConfiguration;

/**
 *
 * @author Boreeas
 */
public class StartCommand extends Command {

    public StartCommand(IrcBot bot) {
        super(bot);
    }

    @Override
    public String getTrigger() {
        return "start";
    }

    @Override
    public void execute(User sender, String target, String[] args) throws
            IOException {

        String sendTo = getReplyTarget(sender.nick(), target);

        if (require(sender.nick(), BotAccessLevel.OWNER)) {

            if (args.length == 0) {
                bot.sendNotice(sendTo,
                               "Not enough arguments: start <botfile> [botfile...]");
            } else {

                for (int i = 0; i < args.length; i++) {

                    try {
                        PropertyListConfiguration conf =
                                                  new PropertyListConfiguration(args[i]);
                        IrcBot bot = new IrcBot(conf);
                        bot.connect();
                        bot.start();
                    } catch (ConfigurationException ex) {
                        bot.sendNotice(sendTo, "Unable to load bot for file "
                                               + args[i] + ": "
                                               + ex.getMessage());
                    }
                }
            }
        }
    }

    @Override
    public String help() {
        return "Starts the bot from the specified file - " + getTrigger() + " <botfile> [botfile...]";
    }
}
