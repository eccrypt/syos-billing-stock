package core.utils; //Its crucial to define this inorder to call classes in other packages

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class PasswordHashUtil {
    public static String hashPassword(String password){
        try{
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash){
                hexString.append(String.format("%02x", b));
            }
            return hexString.toString();
        }catch (NoSuchAlgorithmException exception){
            throw new RuntimeException("Encryption Error", exception);
        }

    }
}
