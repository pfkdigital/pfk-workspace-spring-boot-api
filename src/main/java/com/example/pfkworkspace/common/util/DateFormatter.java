package com.example.pfkworkspace.common.util;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class DateFormatter {
    public static String formatDate(Instant date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        return date.atZone(ZoneId.systemDefault()).format(formatter);
    }
}
