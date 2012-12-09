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
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author Boreeas
 */
public class QuitCommand extends Command {

    public QuitCommand(IrcBot bot) {
        super(bot);
    }


    @Override
    public String getTrigger() {
        return "quit";
    }

    @Override
    public void execute(User sender, String target, String[] args) throws IOException {

        if (require(sender.nick(), BotAccessLevel.OWNER)) {
            String quitReason = "QUIT invoked by " + sender;

            if (args.length > 0) {
                quitReason += " (" + StringUtils.join(args, ' ') + ")";
            }

            bot.disconnect(quitReason);
        } else {
            bot.sendNotice(getReplyTarget(sender.nick(), target), "No, go away");
        }
    }

    @Override
    public String help() {
        return "Stops the bot - " + getTrigger() + " [reason]";
    }

}
