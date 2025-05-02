package tpi.dgrv4.dpaa.util;

import org.apache.commons.lang3.RandomStringUtils;

import java.security.SecureRandom;

public class RandomUtils {

    private static final SecureRandom secureRandom = new SecureRandom();

    public static String randomString(int count, boolean letters, boolean numbers) {
        return RandomStringUtils.random(count, 0, 0 ,letters, numbers, null, secureRandom);
    }
}
