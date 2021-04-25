import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.security.*;

public class Utils {
    public static Key generateSessionKey() throws Exception {
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(128);

        return keyGenerator.generateKey();
    }
    public static byte[] decrypt(byte[] block, PrivateKey privateKey)
            throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException,
            BadPaddingException, IllegalBlockSizeException {
        Cipher deCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        deCipher.init(Cipher.DECRYPT_MODE, privateKey);

        return deCipher.doFinal(block);
    }

    public static byte[] decryptAES(byte[] block, Key sessionKey)
            throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException,
            BadPaddingException, IllegalBlockSizeException {
        Cipher deCipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        deCipher.init(Cipher.DECRYPT_MODE, sessionKey);

        return deCipher.doFinal(block);
    }

    public static byte[] encryptAES(byte[] block, Key sessionKey)
            throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException,
            BadPaddingException, IllegalBlockSizeException {
        Cipher deCipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        deCipher.init(Cipher.ENCRYPT_MODE, sessionKey);

        return deCipher.doFinal(block);
    }

    public static byte[] encrypt(byte[] data, PrivateKey key)
            throws NoSuchPaddingException, NoSuchAlgorithmException, BadPaddingException,
            IllegalBlockSizeException, InvalidKeyException {
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, key);

        return cipher.doFinal(data);
    }

    public static byte[] decryptPublicKey(byte[] block, PublicKey publicKey)
            throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException,
            BadPaddingException, IllegalBlockSizeException {
        Cipher deCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        deCipher.init(Cipher.DECRYPT_MODE, publicKey);

        return deCipher.doFinal(block);
    }

    public static byte[] encryptPublicKey(byte[] data, PublicKey key)
            throws NoSuchPaddingException, NoSuchAlgorithmException, BadPaddingException,
            IllegalBlockSizeException, InvalidKeyException {
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, key);

        return cipher.doFinal(data);
    }


    // Generate nonce
    public static byte[] generateNonce() {
        byte[] nonce = new byte[32];
        SecureRandom random = new SecureRandom();
        random.nextBytes(nonce);

        return nonce;
    }
}
