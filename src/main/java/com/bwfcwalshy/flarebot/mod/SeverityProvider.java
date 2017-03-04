package com.bwfcwalshy.flarebot.mod;

import com.bwfcwalshy.flarebot.FlareBot;
import com.bwfcwalshy.flarebot.scheduler.FlarebotTask;
import com.google.gson.reflect.TypeToken;
import net.dv8tion.jda.core.entities.Guild;

import java.io.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SeverityProvider {
    private static final Map<String, Map<Automod, SeverityLevel>> LEVELS = new ConcurrentHashMap<>();

    private static final File FILE = new File("severities.json");

    static {
        if (FILE.exists())
            try {
                LEVELS.putAll(FlareBot.GSON.fromJson(new FileReader(FILE), new TypeToken<Map<String, String>>() {
                }.getType()));
            } catch (Exception e) {
                FlareBot.LOGGER.info("Could not load severities!", e);
            }
        new FlarebotTask("Save per guild severity configs") {
            @Override
            public void run() {
                try {
                    FlareBot.GSON.toJson(LEVELS, new FileWriter(FILE));
                } catch (IOException e) {
                    FlareBot.LOGGER.info("Could not save severities!", e);
                }
            }
        }.repeat(300000, 300000);
    }

    public static SeverityLevel getSeverityFor(Guild guild, Automod automod) {
        return LEVELS.computeIfAbsent(guild.getId(), m -> new ConcurrentHashMap<>())
                .getOrDefault(automod, automod.getSeverityLevel());
    }

    public static void setSeverityFor(Guild guild, Automod automod, SeverityLevel severity) {
        LEVELS.computeIfAbsent(guild.getId(), m -> new ConcurrentHashMap<>()).put(automod, severity);
    }
}
