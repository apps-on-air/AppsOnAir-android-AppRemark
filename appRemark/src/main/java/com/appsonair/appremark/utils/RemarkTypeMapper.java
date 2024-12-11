package com.appsonair.appremark.utils;

public class RemarkTypeMapper {
//    public static String getType(String type) {
//        return switch (type) {
//            case "Improvement suggestion" -> "SUGGESTION";
//            case "Bug report" -> "BUG";
//            default -> "SUGGESTION";
//        };
//    }
public static String getType(String type) {
    switch (type) {
        case "Improvement suggestion":
            return "SUGGESTION";
        case "Bug report":
            return "BUG";
        default:
            return "SUGGESTION";
    }
}
}
