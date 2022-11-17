/*
 * Decompiled with CFR 0.150.
 */
package hcsmod.clans.server;

import hcsmod.clans.server.ClansConfig;
import hcsmod.clans.server.ClansServer;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PlayerBaseCaptureOptions {
    public final List<Option> options = new ArrayList<Option>();
    public boolean multiplyProtectionPoints = false;

    public void calculateForCurrentTime() {
        this.options.clear();
        this.multiplyProtectionPoints = false;
        ClansConfig config = ClansServer.config;
        if (config.protectionTimeFrames != null && config.protectionTimeFrames.length == 7) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM HH:mm:ss");
            LocalDate day = LocalDate.now();
            for (int i = 0; i < config.protectionOptions.length; ++i) {
                day = day.plusDays(1L);
                int dayOfWeek = day.getDayOfWeek().ordinal();
                ClansConfig.ProtectionTimeFrame timeFrame = config.protectionTimeFrames[dayOfWeek];
                LocalTime startAt = LocalTime.parse(timeFrame.start);
                LocalTime endAt = LocalTime.parse(timeFrame.end);
                long start = startAt.atDate(day).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() / 1000L;
                long end = endAt.atDate(day).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() / 1000L;
                ClansConfig.ProtectionOption option = config.protectionOptions[i];
                for (long anal = start; anal <= end; anal += (long)config.protectionTimeStepSeconds) {
                    this.options.add(new Option(anal, option.item, option.quantity, option.name + ", " + sdf.format(new Date(anal * 1000L)) + " \u043f\u043e \u043c\u0441\u043a"));
                }
            }
        } else {
            long anal = System.currentTimeMillis() / 1000L;
            for (int i = 0; i < config.protectionOptions.length; ++i) {
                ClansConfig.ProtectionOption option = config.protectionOptions[i];
                this.options.add(new Option(anal += (long)config.protectionTimeStepSeconds, option.item, option.quantity, option.name));
            }
            this.multiplyProtectionPoints = true;
        }
    }

    public static class Option {
        public final long unixTime;
        public final int item;
        public final int cost;
        public final String description;

        public Option(long unixTime, int item, int cost, String description) {
            this.unixTime = unixTime;
            this.item = item;
            this.cost = cost;
            this.description = description;
        }
    }
}

