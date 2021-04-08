import javax.crypto.Cipher;
import java.io.BufferedReader;
import java.io.FileReader;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.util.Base64;

public class DigitalSignatureSolution {

    public static void main(String[] args) throws Exception {
        //Read the text file and save to String data
        String fileName = "shorttext.txt";
        String data = "";
        String line;
        BufferedReader bufferedReader = new BufferedReader(new FileReader(fileName));
        while ((line = bufferedReader.readLine()) != null) {
            data = data + "\n" + line;
        }
        System.out.println("Original content: " + data);

        //TODO: generate a RSA keypair, initialize as 1024 bits, get public key and private key from this keypair.
        KeyPairGenerator keyGenerator = KeyPairGenerator.getInstance("RSA");
        keyGenerator.initialize(1024);
        KeyPair keyPair = keyGenerator.generateKeyPair();
        Key privateKey = keyPair.getPrivate();
        Key publicKey = keyPair.getPublic();

        ///TODO: Calculate message digest, using MD5 hash function
        MessageDigest messageDigest = MessageDigest.getInstance("MD5");
        byte[] digest = messageDigest.digest(data.getBytes());

        //TODO: print the length of output digest byte[], compare the length of file shorttext.txt and longtext.txt
        System.out.println(digest.length);

        //TODO: Create RSA("RSA/ECB/PKCS1Padding") cipher object and initialize is as encrypt mode, use PRIVATE key.
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, privateKey);

        //TODO: encrypt digest message
        byte[] encrypted = cipher.doFinal(digest);
        System.out.println(encrypted.length);

        //TODO: print the encrypted message (in base64format String using Base64)
        System.out.println(Base64.getEncoder().encodeToString(encrypted));

        //TODO: Create RSA("RSA/ECB/PKCS1Padding") cipher object and initialize is as decrypt mode, use PUBLIC key.
        Cipher deCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        deCipher.init(Cipher.DECRYPT_MODE, publicKey);

        //TODO: decrypt message
        byte[] decrypted = deCipher.doFinal(encrypted);

        //TODO: print the decrypted message (in base64format String using Base64), compare with origin digest
        String base64Decrypted = Base64.getEncoder().encodeToString(decrypted);
        String base64Digest = Base64.getEncoder().encodeToString(digest);
        System.out.println(base64Decrypted);

        if (base64Decrypted.equals(base64Digest)) {
            System.out.println("same");
        } else {
            System.out.println("different");
        }
    }
}