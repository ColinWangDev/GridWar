package com.gridwar.time;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAdjusters;

public final class SeasonSchedule {

    public static final ZoneId SYDNEY = ZoneId.of("Australia/Sydney");

    private SeasonSchedule() {}

    /**
     * Next weekly boundary: Monday 00:00 in Sydney strictly after {@code from}
     * (the instant when the following season week begins / current week ends).
     */
    public static Instant nextMondayMidnightAfter(Instant from) {
        ZonedDateTime z = from.atZone(SYDNEY);
        LocalDate d = z.toLocalDate();
        LocalDate monday = d.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        ZonedDateTime thisMondayStart = monday.atStartOfDay(SYDNEY);
        if (z.isBefore(thisMondayStart)) {
            return thisMondayStart.toInstant();
        }
        return thisMondayStart.plusWeeks(1).toInstant();
    }

    /** Start of the season week that ends at {@code weekEndMondayMidnight} (exclusive end, 7-day window). */
    public static Instant weekStartsAt(Instant weekEndMondayMidnight) {
        return weekEndMondayMidnight.atZone(SYDNEY).minusWeeks(1).toInstant();
    }
}
