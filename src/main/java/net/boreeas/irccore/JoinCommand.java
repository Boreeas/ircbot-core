/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.boreeas.irccore;

import java.io.IOException;
import net.boreeas.irc.IRCBot;
import net.boreeas.irc.User;
import net.boreeas.irc.Command;

/**
 *
 * @author Boreeas
 */
public class JoinCommand extends Command {

    public JoinCommand(IRCBot bot) {
        super(bot);
    }

    @Override
    public String getTrigger() {
        return "join";
    }

    @Override
    public void execute(User sender, String target, String[] args)
            throws IOException {

        for (String channel : args) {
            bot.joinChannel(channel);
        }
    }

    @Override
    public String help() {
        return "Join the target channels - join [channel] [channel...]";
    }
}
