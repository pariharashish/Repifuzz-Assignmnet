package com.repifuzz.jwtUtil;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    @Test
    void generateAndValidateToken_andExtractUsername() throws Exception {
        JwtUtil util = new JwtUtil();

        // Set private fields via reflection
        Field secretField = JwtUtil.class.getDeclaredField("secret");
        secretField.setAccessible(true);
        secretField.set(util, "01234567890123456789012345678901"); // 32 chars

        Field expField = JwtUtil.class.getDeclaredField("expiration");
        expField.setAccessible(true);
        expField.setLong(util, 60_000L);

        String token = util.generateToken("alice@example.com");
        assertNotNull(token);
        assertTrue(util.validateToken(token));
        String username = util.getUsernameFromToken(token);
        assertEquals("alice@example.com", username);
    }

    @Test
    void validateToken_invalidSignature_returnsFalse() throws Exception {
        JwtUtil util = new JwtUtil();
        Field secretField = JwtUtil.class.getDeclaredField("secret");
        secretField.setAccessible(true);
        secretField.set(util, "01234567890123456789012345678901");
        Field expField = JwtUtil.class.getDeclaredField("expiration");
        expField.setAccessible(true);
        expField.setLong(util, 60_000L);

        String token = util.generateToken("bob@example.com");

        // Another util with different secret
        JwtUtil other = new JwtUtil();
        secretField.set(other, "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        expField.setLong(other, 60_000L);

        assertFalse(other.validateToken(token));
    }
}
