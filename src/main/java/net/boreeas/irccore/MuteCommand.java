/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.boreeas.irccore;

import java.io.IOException;
import net.boreeas.irc.*;

/**
 *
 * @author Boreeas
 */
public class MuteCommand extends Command {

    public MuteCommand(IrcBot bot) {
        super(bot);
    }

    @Override
    public String getTrigger() {
        return "mute";
    }

    @Override
    public void execute(User sender, String target, String[] args) throws
            IOException {

        if (!getReplyTarget(sender.nick(), target).equals(target)) {
            bot.sendNotice(sender.nick(), "Mute command must be invoked in context of channel");
            return;
        }

        if (!(require(sender.nick(), BotAccessLevel.MOD)
              || require(sender.nick(), target, ChannelAccessLevel.VOICE))) {

            bot.sendNotice(target, "Bot access level MOD or channel access level VOICE is required");
            return;
        }

        boolean muted = bot.isMuted(target);

        if (!muted) {
            // Wasn't muted before, now muted
            bot.sendNotice(target, "Now muted");
        }

        bot.toggleMute(target);

        if (muted) {
            // Was muted before, now unmuted
            bot.sendNotice(target, "Now unmuted");
        }
    }

    @Override
    public String help() {
        return "Mutes bot in channel. Cannot be invoked from PM";
    }
}
