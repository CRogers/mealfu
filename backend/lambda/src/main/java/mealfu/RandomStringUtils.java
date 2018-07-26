package mealfu;

import java.util.Random;

public enum RandomStringUtils {
    ;

    private static final Random RANDOM = new Random();

    public static String randomHexString(int numchars){
        StringBuilder sb = new StringBuilder();
        while(sb.length() < numchars){
            sb.append(Integer.toHexString(RANDOM.nextInt()));
        }

        return sb.toString().substring(0, numchars);
    }
}
