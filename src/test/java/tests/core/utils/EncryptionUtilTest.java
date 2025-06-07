package tests.core.utils;

import core.utils.EncryptionUtil;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class EncryptionUtilTest {

    @Test
    public void testHashPassword_NotNull() {
        String password = "securePassword123";
        String hash = EncryptionUtil.hashPassword(password);
        assertNotNull(hash, "Hash should not be null");
    }

    @Test
    public void testHashPassword_Consistency() {
        String password = "samePassword";
        String hash1 = EncryptionUtil.hashPassword(password);
        String hash2 = EncryptionUtil.hashPassword(password);
        assertEquals(hash1, hash2, "Hashes for same input should be consistent");
    }

    @Test
    public void testHashPassword_DifferentInputProducesDifferentHash() {
        String hash1 = EncryptionUtil.hashPassword("passwordOne");
        String hash2 = EncryptionUtil.hashPassword("passwordTwo");
        assertNotEquals(hash1, hash2, "Different passwords should result in different hashes");
    }

    @Test
    public void testHashPassword_CorrectLengthForSHA256() {
        String hash = EncryptionUtil.hashPassword("anyPassword");
        assertEquals(64, hash.length(), "SHA-256 hash length should be 64 characters in hex");
    }

    @Test
    public void testHashPassword_EmptyString() {
        String hash = EncryptionUtil.hashPassword("");
        assertNotNull(hash, "Hash for empty string should not be null");
        assertEquals(64, hash.length(), "SHA-256 hash for empty string should be 64 characters");
    }
}
