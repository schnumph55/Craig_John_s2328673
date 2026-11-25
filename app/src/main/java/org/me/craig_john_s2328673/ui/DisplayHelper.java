package org.me.craig_john_s2328673.ui;

import android.graphics.Color;

import org.me.craig_john_s2328673.R;
import org.me.craig_john_s2328673.model.Situation;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public final class DisplayHelper {

    public static final int COLOUR_SHORT = Color.parseColor("#2E7D32");
    public static final int COLOUR_MEDIUM = Color.parseColor("#F9A825");
    public static final int COLOUR_LONG = Color.parseColor("#C62828");
    public static final int COLOUR_INCIDENT = Color.parseColor("#1565C0");
    public static final int COLOUR_UNKNOWN = Color.parseColor("#757575");

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("d MMM yyyy, HH:mm", Locale.UK);

    private DisplayHelper() { }

    public static String camelToWords(String s) {
        if (s == null || s.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (Character.isUpperCase(c) && i > 0) sb.append(' ');
            sb.append(Character.toLowerCase(c));
        }
        sb.setCharAt(0, Character.toUpperCase(sb.charAt(0)));
        return sb.toString();
    }

    public static String typeLabel(Situation s) {
        if (s.isRoadworks()) return s.isUrgent() ? "Urgent roadworks" : "Roadworks";
        String sub = s.getSubType();
        if (sub != null) {
            switch (sub) {
                case "roadClosed": return "Road closed";
                case "queuingTraffic": return "Queuing traffic";
                case "brokenDownVehicle": return "Broken-down vehicle";
                case "obstructionOnTheRoad": return "Obstruction";
                default: return camelToWords(sub);
            }
        }
        return camelToWords(s.getRecordType());
    }

    public static int iconFor(Situation s) {
        if (s.isRoadworks()) return s.isUrgent() ? R.drawable.ic_urgent_roadworks : R.drawable.ic_roadworks;
        String sub = s.getSubType() == null ? "" : s.getSubType();
        switch (sub) {
            case "roadClosed": return R.drawable.ic_road_closed;
            case "queuingTraffic": return R.drawable.ic_queue;
            case "brokenDownVehicle": return R.drawable.ic_breakdown;
            case "obstructionOnTheRoad": return R.drawable.ic_obstruction;
        }
        String type = s.getRecordType() == null ? "" : s.getRecordType();
        switch (type) {
            case "RoadOrCarriagewayOrLaneManagement": return R.drawable.ic_road_closed;
            case "AbnormalTraffic": return R.drawable.ic_queue;
            case "VehicleObstruction": return R.drawable.ic_breakdown;
            case "GeneralObstruction": return R.drawable.ic_obstruction;
            default: return R.drawable.ic_other;
        }
    }

    public static int colourFor(Situation s) {
        if (!s.isRoadworks()) return COLOUR_INCIDENT;
        long days = s.getValidity().getDurationDays();
        if (days < 0) return COLOUR_UNKNOWN;
        if (days <= 7) return COLOUR_SHORT;
        if (days <= 30) return COLOUR_MEDIUM;
        return COLOUR_LONG;
    }

    public static String durationLabel(Situation s) {
        long days = s.getValidity().getDurationDays();
        if (days < 0) return "No end date";
        if (days == 0) return "Under 1 day";
        if (days == 1) return "1 day";
        if (days > 365) return String.format(Locale.UK, "%d days (%.1f years)", days, days / 365.0);
        return days + " days";
    }

    public static String formatDate(OffsetDateTime t) {
        if (t == null) return "Not specified";
        return t.atZoneSameInstant(ZoneId.of("Europe/London")).format(DATE_FORMAT);
    }

    public static String directionLabel(String dir) {
        if (dir == null) return "";
        switch (dir) {
            case "bothWays": return "Both directions";
            case "northBound": return "Northbound";
            case "southBound": return "Southbound";
            case "eastBound": return "Eastbound";
            case "westBound": return "Westbound";
            default: return camelToWords(dir);
        }
    }

    public static String delaysLabel(String delays) {
        if (delays == null) return null;
        switch (delays) {
            case "delays": return "Delays expected";
            case "longDelays": return "Long delays expected";
            case "veryLongDelays": return "Very long delays expected";
            default: return camelToWords(delays);
        }
    }

    public static String[] splitRoadworksComment(String comment) {
        if (comment == null) return new String[0];
        String[] parts = comment.split(" - ");
        for (int i = 0; i < parts.length; i++) parts[i] = parts[i].trim();
        return parts;
    }

    public static String expandAbbreviations(String text) {
        if (text == null) return "";
        String padded = " " + text + " ";
        padded = padded.replace(" NB ", " Northbound ")
                .replace(" SB ", " Southbound ")
                .replace(" EB ", " Eastbound ")
                .replace(" WB ", " Westbound ")
                .replace(" Jct ", " Junction ")
                .replace(" Rbt ", " Roundabout ");
        return padded.trim();
    }

    public static String metaLine(Situation s) {
        String road = s.getRoadName() == null ? "Unknown road" : s.getRoadName();
        if (s.isRoadworks()) return road + " • " + durationLabel(s);
        return road + " • since " + formatDate(s.getValidity().getStart());
    }
}