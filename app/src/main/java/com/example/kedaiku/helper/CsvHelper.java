package com.example.kedaiku.helper;

import java.util.List;

public class CsvHelper {

    public static String convertToCsv(String[] headers, List<String[]> rows,String judul) {
        StringBuilder sb = new StringBuilder();
        sb.append(judul);
        sb.append("\n");

        // Tambahkan header CSV
        if (headers != null && headers.length > 0) {
            for (int i = 0; i < headers.length; i++) {
                sb.append(escapeCsv(headers[i]));
                if (i != headers.length - 1) {
                    sb.append(",");
                }
            }
            sb.append("\n");
        }

        // Tambahkan setiap baris data
        if (rows != null) {
            for (String[] row : rows) {
                if (row != null) {
                    for (int i = 0; i < row.length; i++) {
                        sb.append(escapeCsv(row[i]));
                        if (i != row.length - 1) {
                            sb.append(",");
                        }
                    }
                    sb.append("\n");
                }
            }
        }

        return sb.toString();
    }

    private static String escapeCsv(String field) {
        if (field == null) {
            return "";
        }
        String result = field.replace("\"", "\"\""); // Escape kutipan ganda
        if (result.contains(",") || result.contains("\"") || result.contains("\n")) {
            result = "\"" + result + "\"";
        }
        return result;
    }
}
