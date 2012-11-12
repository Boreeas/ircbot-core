/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.boreeas.irccore;

import java.io.IOException;
import net.boreeas.irc.BotAccessLevel;
import net.boreeas.irc.Command;
import net.boreeas.irc.IRCBot;
import net.boreeas.irc.User;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author Boreeas
 */
public class RawCommand extends Command {

    public RawCommand(IRCBot bot) {
        super(bot);
    }

    @Override
    public String getTrigger() {
        return "raw";
    }

    @Override
    public void execute(User sender, String target, String[] args)
            throws IOException {

        if (require(sender.nick(), BotAccessLevel.OWNER)) {

            bot.sendRaw(StringUtils.join(args, " "));
        } else {

            bot.sendNotice(getReplyTarget(sender.nick(), target), "Go away");
        }
    }

    @Override
    public String help() {
        return "Send raw text to the IRC server - " + getTrigger() + " <text>";
    }
}
