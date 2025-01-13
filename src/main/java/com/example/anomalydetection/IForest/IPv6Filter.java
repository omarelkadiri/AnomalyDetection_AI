package com.example.anomalydetection.IForest;

import com.example.anomalydetection.Structure.LogEntry;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class IPv6Filter {
    private static final Pattern IPV6_PATTERN = Pattern.compile(
            "^(?:" +
                    "(?:[0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}|" +
                    "(?:[0-9a-fA-F]{1,4}:){1,7}:|" +
                    "(?:[0-9a-fA-F]{1,4}:){1,6}:[0-9a-fA-F]{1,4}|" +
                    "(?:[0-9a-fA-F]{1,4}:){1,5}(?::[0-9a-fA-F]{1,4}){1,2}|" +
                    "(?:[0-9a-fA-F]{1,4}:){1,4}(?::[0-9a-fA-F]{1,4}){1,3}|" +
                    "(?:[0-9a-fA-F]{1,4}:){1,3}(?::[0-9a-fA-F]{1,4}){1,4}|" +
                    "(?:[0-9a-fA-F]{1,4}:){1,2}(?::[0-9a-fA-F]{1,4}){1,5}|" +
                    "[0-9a-fA-F]{1,4}:(?:(?::[0-9a-fA-F]{1,4}){1,6})|" +
                    ":(?:(?::[0-9a-fA-F]{1,4}){1,7}|:)|" +
                    "fe80:(?::[0-9a-fA-F]{0,4}){0,4}%[0-9a-zA-Z]{1,}|" +
                    "::(?:ffff(?::0{1,4}){0,1}:){0,1}(?:(?:25[0-5]|(?:2[0-4]|1{0,1}[0-9]){0,1}[0-9])\\.){3,3}(?:25[0-5]|(?:2[0-4]|1{0,1}[0-9]){0,1}[0-9])|" +
                    "(?:[0-9a-fA-F]{1,4}:){1,4}:(?:(?:25[0-5]|(?:2[0-4]|1{0,1}[0-9]){0,1}[0-9])\\.){3,3}(?:25[0-5]|(?:2[0-4]|1{0,1}[0-9]){0,1}[0-9])" +
                    ")$"
    );

    public static List<LogEntry> filterOutIPv6Logs(List<LogEntry> logs) {
        List<LogEntry> ipv4Logs = new ArrayList<>();
        int filteredCount = 0;

        for (LogEntry log : logs) {
            if (!isIPv6(log.getSource_ip()) && !isIPv6(log.getDest_ip())) {
                ipv4Logs.add(log);
            } else {
                filteredCount++;
            }
        }

        if (filteredCount > 0) {
            System.out.println("Logs IPv6 filtrés: " + filteredCount);
        }

        return ipv4Logs;
    }

    public static boolean isIPv6(String ip) {
        return IPV6_PATTERN.matcher(ip).matches();
    }
}