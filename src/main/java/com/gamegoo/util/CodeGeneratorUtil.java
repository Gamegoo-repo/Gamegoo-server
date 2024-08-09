package com.gamegoo.util;

import java.security.SecureRandom;

public class CodeGeneratorUtil {

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final SecureRandom random = new SecureRandom();
    private static final int CODE_LENGTH = 5;

    /**
     * 이메일 인증에 사용하는 메소드 : 랜덤 코드 만들기
     *
     * @return
     */
    public static String generateRandomCode() {
        StringBuilder code = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            int index = random.nextInt(CHARACTERS.length());
            code.append(CHARACTERS.charAt(index));
        }
        System.out.println(code);
        return code.toString();
    }


}
