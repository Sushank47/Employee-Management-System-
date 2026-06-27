package com.sushank.ems.util;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class DateUtils {

    /**
     * Calculates the inclusive duration in days between two dates.
     */
    public static long calculateDurationDays(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            return 0;
        }
        return ChronoUnit.DAYS.between(startDate, endDate) + 1;
    }
}
