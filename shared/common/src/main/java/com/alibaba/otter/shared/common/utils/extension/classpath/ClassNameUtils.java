package com.alibaba.otter.shared.common.utils.extension.classpath;

/**
 * class名字转换处理类
 * 
 * @author jianghang 2012-10-23 下午04:37:48
 * @version 4.1.0
 */
public class ClassNameUtils {

    /**
     * Convert a class name with underscores to the corresponding column name using "_". A name like "CustomerNumber"
     * class name would match a "CUSTOMER_NUMBER".
     * 
     * @param name the class name to be converted
     * @return the name using "_"
     */
    public static String convertClassNameToUnderscoreName(String name) {
        StringBuilder result = new StringBuilder();

        if (name != null) {
            int len = name.length();

            if (len > 0) {
                result.append(name.charAt(0));

                for (int i = 1; i < len; i++) {
                    if (true == Character.isUpperCase(name.charAt(i))) {
                        result.append('_');
                    }

                    result.append(name.charAt(i));
                }
            }
        }

        return result.toString().toUpperCase();
    }

    /**
     * Convert a column name with underscores to the corresponding class name using "camel case". A name like
     * "customer_number" would match a "CustomerNumber" class name.
     * 
     * @param name the column name to be converted
     * @return the name using "camel case"
     */
    public static String convertUnderscoreNameToClassName(String name) {
        StringBuffer result = new StringBuffer();
        boolean nextIsUpper = false;

        if (name != null) {
            int len = name.length();

            if (len > 0) {
                String s = String.valueOf(name.charAt(0));

                result.append(s.toUpperCase());

                for (int i = 1; i < len; i++) {
                    s = String.valueOf(name.charAt(i));

                    if ("_".equals(s)) {
                        nextIsUpper = true;
                    } else {
                        if (nextIsUpper) {
                            result.append(s.toUpperCase());
                            nextIsUpper = false;
                        } else {
                            result.append(s.toLowerCase());
                        }
                    }
                }
            }
        }

        return result.toString();
    }

}
