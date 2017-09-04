package stream.flarebot.flarebot.commands.general;

import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import stream.flarebot.flarebot.commands.Command;
import stream.flarebot.flarebot.commands.CommandType;
import stream.flarebot.flarebot.objects.GuildWrapper;
import stream.flarebot.flarebot.objects.Report;
import stream.flarebot.flarebot.objects.ReportStatus;
import stream.flarebot.flarebot.util.GeneralUtils;
import stream.flarebot.flarebot.util.MessageUtils;
import stream.flarebot.flarebot.util.ReportManager;

import java.sql.Timestamp;
import java.util.List;
import java.util.stream.Collectors;

public class ReportCommand implements Command {

    @Override
    public void onCommand(User sender, GuildWrapper guild, TextChannel channel, Message message, String[] args, Member member) {
        if (args.length >= 2) {
            User user = GeneralUtils.getUser(args[0], channel.getGuild().getId());
            if (user == null) {
                MessageUtils.sendErrorMessage("Invalid user: " + args[0], channel);
                return;
            }

            Report report = new Report(channel.getGuild().getId(), ReportManager.getInstance().getLastId(), MessageUtils.getMessage(args, 1), sender.getId(), user.getId(), new Timestamp(System.currentTimeMillis()), ReportStatus.OPEN);

            List<Message> messages = channel.getHistory()
                    .retrievePast(100)
                    .complete()
                    .stream()
                    .filter(m -> m.getAuthor().equals(user))
                    .collect(Collectors.toList());
            report.setMessages(messages.subList(0, Math.min(10, messages.size() - 1)));

            guild.getReportManager().report(report);

            MessageUtils.sendPM(channel, sender, GeneralUtils.getReportEmbed(sender, report).setDescription("Successfully reported the user"));
        } else {
            MessageUtils.getUsage(this, channel, sender).queue();
        }
    }

    @Override
    public String getCommand() {
        return "report";
    }

    @Override
    public String getDescription() {
        return "Allows users to report other members on the server";
    }

    @Override
    public String getUsage() {
        return "`{%}report <user> <reason>` - Reports a user your guild moderators";
    }

    @Override
    public CommandType getType() {
        return CommandType.GENERAL;
    }
}
