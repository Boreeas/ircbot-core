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

/**
 *
 * @author Boreeas
 */
public class NickCommand extends Command {

    public NickCommand(IrcBot bot) {
        super(bot);
    }

    @Override
    public String getTrigger() {
        return "nick";
    }

    @Override
    public void execute(User sender, String target, String[] args) throws
            IOException {

        if (args.length == 0) {
            bot.sendNotice(getReplyTarget(sender.nick(), target),
                           "Not enough arguments. Format: <nick>");
            return;
        }


        if (require(sender.nick(), BotAccessLevel.MOD)) {
            bot.changeNick(args[0]);
        } else {
            nope(sender.nick(), getReplyTarget(sender.nick(), target),
                 BotAccessLevel.MOD);
        }
    }

    @Override
    public String help() {
        return "Changes nick of bot - nick <newnick>";
    }
}
