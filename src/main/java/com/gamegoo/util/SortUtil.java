package com.gamegoo.util;

import java.util.Comparator;

public class SortUtil {

    public static Comparator<String> memberNameComparator = (s1, s2) -> {
        int length1 = s1.length();
        int length2 = s2.length();
        int minLength = Math.min(length1, length2);

        // 각 문자 비교
        for (int i = 0; i < minLength; i++) {
            int result = compareChars(s1.charAt(i), s2.charAt(i));
            if (result != 0) {
                return result;
            }
        }

        // 앞부분이 동일하면, 길이가 짧은 것이 앞으로 오도록 정렬
        return Integer.compare(length1, length2);
    };

    // 문자 비교 메서드: 한글 -> 영문자 -> 숫자 순으로 우선순위 지정
    private static int compareChars(char c1, char c2) {
        if (Character.isDigit(c1) && Character.isDigit(c2)) {
            return Character.compare(c1, c2);
        } else if (Character.isDigit(c1)) {
            return 1; // 숫자는 항상 뒤로
        } else if (Character.isDigit(c2)) {
            return -1; // 숫자는 항상 뒤로
        } else if (Character.isAlphabetic(c1) && Character.isAlphabetic(c2)) {
            return Character.compare(c1, c2);
        } else if (Character.isAlphabetic(c1)) {
            return 1; // 영문자는 한글보다 뒤
        } else if (Character.isAlphabetic(c2)) {
            return -1; // 영문자는 한글보다 뒤
        } else {
            return Character.compare(c1, c2); // 기본적으로 유니코드 값 비교
        }
    }
}
