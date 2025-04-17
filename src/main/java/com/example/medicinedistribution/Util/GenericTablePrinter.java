package com.example.medicinedistribution.Util;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap; // Giữ thứ tự chèn
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Lớp tiện ích để in danh sách các đối tượng dưới dạng bảng được định dạng ra console.
 * Sử dụng Java Reflection để tự động xác định các cột từ các phương thức getter công khai,
 * tự động điều chỉnh độ rộng cột và đẩy các cột boolean về cuối.
 */
public final class GenericTablePrinter {

    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_RED = "\u001B[31m";
    private static final String ANSI_GREEN = "\u001B[32m";
    // Độ rộng tối đa của giá trị boolean ('false' là 5 ký tự)
    private static final int MAX_BOOLEAN_VALUE_WIDTH = 5; // "false"

    /**
     * Ngăn chặn việc tạo instance của lớp tiện ích này.
     */
    private GenericTablePrinter() {
        throw new UnsupportedOperationException("Đây là một lớp tiện ích và không thể được khởi tạo.");
    }

    /**
     * In ra bảng biểu diễn dữ liệu từ một List các đối tượng bất kỳ.
     * Cột boolean sẽ được đẩy về cuối. Độ rộng cột được tối ưu hóa.
     *
     * @param list Danh sách các đối tượng cần in. Nếu null hoặc rỗng, một thông báo sẽ được in.
     * @param <T> Kiểu dữ liệu của các đối tượng trong danh sách.
     */
    public static <T> void printTable(List<T> list) {
        if (list == null || list.isEmpty()) {
            System.out.println("Danh sách rỗng hoặc null, không có gì để in.");
            return;
        }

        Class<?> objClass = list.get(0).getClass();
        Map<String, Method> allGetters = findPublicGetters(objClass);

        if (allGetters.isEmpty()) {
            System.out.println("Không tìm thấy phương thức getter công khai nào trong lớp " + objClass.getSimpleName() + " để tạo cột.");
            return;
        }

        // --- Sắp xếp lại các cột: boolean ra sau cùng ---
        List<String> nonBooleanHeaders = new ArrayList<>();
        List<String> booleanHeaders = new ArrayList<>();
        int i = 0;
        for (Map.Entry<String, Method> entry : allGetters.entrySet()) {
            Class<?> returnType = entry.getValue().getReturnType();
            if (returnType == boolean.class || returnType == Boolean.class) {
                booleanHeaders.add(entry.getKey());
            } else {
                nonBooleanHeaders.add(entry.getKey());
            }
        }
        // Kết hợp lại danh sách header theo thứ tự mong muốn
        List<String> orderedHeaders = new ArrayList<>(nonBooleanHeaders);
        orderedHeaders.addAll(booleanHeaders);

        // --- Tính toán độ rộng tối ưu cho mỗi cột ---
        Map<String, Integer> columnWidths = calculateOptimizedColumnWidths(list, orderedHeaders, allGetters,i);

        // --- Tạo chuỗi định dạng và phân cách ---
        String formatString = buildFormatString(orderedHeaders, columnWidths);
        String separatorString = buildSeparatorString(orderedHeaders, columnWidths);

        // --- In bảng ---
        StringBuilder sb = new StringBuilder();
        sb.append(String.format(formatString, orderedHeaders.toArray())); // In header
        sb.append(separatorString); // In dòng phân cách
        String trueValueWithColor = ANSI_GREEN + "true" + ANSI_RESET;
        String falseValueWithColor = ANSI_RED + "false" + ANSI_RESET;

        int ansiTrueLength = ANSI_GREEN.length() + ANSI_RESET.length() + 1;
        int ansiFalseLength = ANSI_RED.length() + ANSI_RESET.length();

        String spaceTruePadding = " ".repeat(ansiTrueLength);
        String spaceFalsePadding = " ".repeat(ansiFalseLength);

        String trueValueWithPadding = trueValueWithColor + spaceTruePadding;
        String falseValueWithPadding = falseValueWithColor + spaceFalsePadding;




        // In các dòng dữ liệu theo thứ tự cột đã sắp xếp
        for (T item : list) {
            List<Object> rowValuesForFormatting = new ArrayList<>();
            for (String header : orderedHeaders) { // Duyệt theo thứ tự mới
                Method getter = allGetters.get(header); // Lấy getter tương ứng
                try {
                    Object value = getter.invoke(item);
                    if (value instanceof Boolean) {
                        rowValuesForFormatting.add(((Boolean) value) ? trueValueWithPadding : falseValueWithPadding);
                    } else {
                        rowValuesForFormatting.add(value == null ? "null" : value);
                    }
                } catch (Exception e) {
                    rowValuesForFormatting.add("[LỖI]");
                    System.err.println("Lỗi khi gọi getter " + getter.getName() + " cho đối tượng " + item + ": " + e.getMessage());
                }
            }
            sb.append(String.format(formatString, rowValuesForFormatting.toArray()));
        }

        System.out.println(sb.toString());
    }

    // --- Các phương thức private hỗ trợ ---

    /**
     * Tìm các phương thức getter công khai (public getX() hoặc isX()).
     * (Không thay đổi so với phiên bản trước)
     */
    private static Map<String, Method> findPublicGetters(Class<?> clazz) {
        Map<String, Method> getters = new LinkedHashMap<>();
        for (Method method : clazz.getMethods()) {
            if (isGetter(method)) {
                String propertyName = getPropertyNameFromGetter(method);
                getters.putIfAbsent(propertyName, method);
            }
        }
        return getters;
    }

    /**
     * Kiểm tra xem một phương thức có phải là getter hợp lệ hay không.
     * (Không thay đổi so với phiên bản trước)
     */
    private static boolean isGetter(Method method) {
        if (!Modifier.isPublic(method.getModifiers()) || Modifier.isStatic(method.getModifiers())) return false;
        if (method.getParameterCount() != 0) return false;
        if (method.getReturnType() == void.class) return false;
        if (method.getName().equals("getClass")) return false;
        String name = method.getName();
        Class<?> returnType = method.getReturnType();
        if (name.startsWith("get") && name.length() > 3) return true;
        if (name.startsWith("is") && name.length() > 2 && (returnType == boolean.class || returnType == Boolean.class)) return true;
        return false;
    }

    /**
     * Lấy tên thuộc tính từ tên phương thức getter.
     * (Không thay đổi so với phiên bản trước)
     */
    private static String getPropertyNameFromGetter(Method getter) {
        String name = getter.getName();
        String propertyName;
        if (name.startsWith("get")) propertyName = name.substring(3);
        else if (name.startsWith("is")) propertyName = name.substring(2);
        else propertyName = name;
        if (propertyName.isEmpty()) return "";
        if (propertyName.length() == 1 || !Character.isUpperCase(propertyName.charAt(1))) {
            return Character.toLowerCase(propertyName.charAt(0)) + propertyName.substring(1);
        } else {
            return propertyName;
        }
    }

    /**
     * Tính toán độ rộng cột được tối ưu hóa.
     * Cột boolean sẽ có độ rộng tối thiểu để chứa header hoặc giá trị boolean dài nhất ("false").
     * Các cột khác sẽ có độ rộng tối thiểu để chứa header hoặc giá trị dữ liệu dài nhất.
     *
     * @param list Danh sách đối tượng.
     * @param orderedHeaders Danh sách tên header đã được sắp xếp.
     * @param allGetters Map chứa tất cả getter tìm được.
     * @return Map từ header đến độ rộng cột đã được tối ưu hóa.
     */
    private static <T> Map<String, Integer> calculateOptimizedColumnWidths(List<T> list, List<String> orderedHeaders, Map<String, Method> allGetters,int i) {
        Map<String, Integer> columnWidths = new LinkedHashMap<>();

        for (String header : orderedHeaders) {
            Method getter = allGetters.get(header);
            Class<?> returnType = getter.getReturnType();
            int headerLength = header.length();
            int maxWidth;

            // Xử lý riêng cho cột boolean
            if (returnType == boolean.class || returnType == Boolean.class) {
                // Tính độ dài của "true" và "false" bao gồm cả mã màu ANSI
                String trueValue = ANSI_GREEN + "true" + ANSI_RESET;  // Màu xanh cho true
                String falseValue = ANSI_RED + "false" + ANSI_RESET; // Màu đỏ cho false

                // Đảm bảo chiều rộng cột boolean sẽ phù hợp với chiều rộng của header
                int booleanWidth = Math.max(trueValue.length(), falseValue.length());  // Độ rộng của cột boolean với mã màu

                // Đảm bảo chiều rộng cột boolean không nhỏ hơn chiều dài của header
                maxWidth = Math.max(headerLength, booleanWidth);
//                if (i==0){
//                    maxWidth = Math.max(headerLength, 5);
//                } else {
//                    maxWidth = Math.max(headerLength, booleanWidth);
//                }
            } else {
                // Xử lý cho các cột khác: tìm độ dài dữ liệu lớn nhất
                int maxDataLength = 0;
                for (T item : list) {
                    try {
                        Object value = getter.invoke(item);
                        // Loại bỏ mã ANSI khi tính độ dài
                        String stringValue = removeAnsiCodes(value == null ? "null" : value.toString());
                        if (stringValue.length() > maxDataLength) {
                            maxDataLength = stringValue.length();
                        }
                    } catch (Exception e) {
                        // Bỏ qua lỗi khi tính độ rộng
                    }
                }
                // Độ rộng là max của header và dữ liệu dài nhất
                maxWidth = Math.max(headerLength, maxDataLength);
            }
            columnWidths.put(header, maxWidth);
        }
        return columnWidths;
    }



    /**
     * Loại bỏ mã màu ANSI khỏi chuỗi.
     * (Không thay đổi so với phiên bản trước)
     */
    private static String removeAnsiCodes(String text) {
        if (text == null) return "";
        return text.replaceAll("\u001B\\[[;\\d]*[ -/]*[@-~]", "");
    }


    /**
     * Xây dựng chuỗi định dạng cho System.out.printf.
     * (Không thay đổi so với phiên bản trước)
     */
    private static String buildFormatString(List<String> headers, Map<String, Integer> columnWidths) {
        StringBuilder format = new StringBuilder("|");
        for (String header : headers) {
            format.append(" %-").append(columnWidths.get(header)).append("s |");
        }
        format.append("%n");
        return format.toString();
    }

    /**
     * Xây dựng chuỗi phân cách (dòng gạch ngang).
     * (Không thay đổi so với phiên bản trước)
     */
    private static String buildSeparatorString(List<String> headers, Map<String, Integer> columnWidths) {
        StringBuilder separator = new StringBuilder("|");
        for (String header : headers) {
            int totalWidth = columnWidths.get(header) + 2; // Thêm khoảng trắng đệm
            char[] line = new char[totalWidth];
            Arrays.fill(line, '-');
            separator.append(new String(line)).append("|");
        }
        separator.append("%n");
        return String.format(separator.toString());
    }
}