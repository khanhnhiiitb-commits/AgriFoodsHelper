package com.group5.agrifoodshelper.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateFormatter {
    public static String formatTimestamp(long timestamp) {
        if (timestamp == 0) return "Chưa xác định";
        // Chuyển đổi timestamp thành định dạng: 15:30 - 20/10/2026
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm - dd/MM/yyyy", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }
}