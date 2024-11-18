package com.appsonair.appremark.utils;

public class RemarkTypeMapper {
    public static String getType(String type) {
        return switch (type) {
            case "Improvement suggestion" -> "SUGGESTION";
            case "Bug report" -> "BUG";
            default -> "SUGGESTION";
        };
    }
}
