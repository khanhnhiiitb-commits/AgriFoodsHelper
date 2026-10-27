package com.group5.agrifoodshelper.utils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

public class VNPayPaymentHelper {
    private static final String VNP_URL = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html";
    private static final String VNP_TMN_CODE = "A0DS4U03";
    private static final String VNP_HASH_SECRET = "XXBLJLSJXULSYEKMFNHXATBVJOWFIFAA";

    public static String createPaymentUrl(long amount, String orderInfo) {
        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", "2.1.0");
        vnp_Params.put("vnp_Command", "pay");
        vnp_Params.put("vnp_TmnCode", VNP_TMN_CODE);

        // VNPay yêu cầu số tiền phải nhân lên 100 lần
        vnp_Params.put("vnp_Amount", String.valueOf(amount * 100));
        vnp_Params.put("vnp_CurrCode", "VND");
        vnp_Params.put("vnp_TxnRef", String.valueOf(System.currentTimeMillis()));
        vnp_Params.put("vnp_OrderInfo", orderInfo);
        vnp_Params.put("vnp_OrderType", "other");
        vnp_Params.put("vnp_Locale", "vn");
        vnp_Params.put("vnp_ReturnUrl", "agrifoodshelper://vnpay_return");
        vnp_Params.put("vnp_IpAddr", "127.0.0.1");

        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss", java.util.Locale.getDefault());
        vnp_Params.put("vnp_CreateDate", formatter.format(cld.getTime()));

        cld.add(Calendar.MINUTE, 15);
        vnp_Params.put("vnp_ExpireDate", formatter.format(cld.getTime()));

        // Sắp xếp các key theo thứ tự Alphabet
        List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
        Collections.sort(fieldNames);

        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();

        boolean first = true;
        try {
            for (String fieldName : fieldNames) {
                String fieldValue = vnp_Params.get(fieldName);
                if ((fieldValue != null) && !fieldValue.isEmpty()) {
                    if (!first) {
                        hashData.append('&');
                        query.append('&');
                    }

                    // 1. Xây dựng chuỗi hashData: fieldName=fieldValue (Giá trị ở hashData chuẩn VNPay thường giữ nguyên hoặc encode UTF-8)
                    hashData.append(fieldName).append('=').append(URLEncoder.encode(fieldValue, StandardCharsets.UTF_8.toString()));

                    // 2. Xây dựng chuỗi queryUrl trên thanh địa chỉ trình duyệt
                    query.append(URLEncoder.encode(fieldName, StandardCharsets.UTF_8.toString()))
                            .append('=')
                            .append(URLEncoder.encode(fieldValue, StandardCharsets.UTF_8.toString()));

                    first = false;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        String queryUrl = query.toString();
        String vnp_SecureHash = VNPaySecurityUtil.hmacSHA512(VNP_HASH_SECRET, hashData.toString());

        return VNP_URL + "?" + queryUrl + "&vnp_SecureHash=" + vnp_SecureHash;
    }
}