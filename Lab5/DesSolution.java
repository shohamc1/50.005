import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import javax.crypto.*;
import java.nio.charset.StandardCharsets;
import java.util.Base64;


public class DesSolution {
    public static void main(String[] args) throws Exception {
        String fileName = "longtext.txt";
        String data = "";
        String line;
        BufferedReader bufferedReader = new BufferedReader(new FileReader(fileName));
        while ((line = bufferedReader.readLine()) != null) {
            data = data + "\n" + line;
        }
        System.out.println("Original content: " + data);

        //TODO: generate secret key using DES algorithm
        KeyGenerator keyGenerator = KeyGenerator.getInstance("DES");
        SecretKey desKey = keyGenerator.generateKey();

        //TODO: create cipher object, initialize the ciphers with the given key, choose encryption mode as DES
        Cipher desCipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
        desCipher.init(Cipher.ENCRYPT_MODE, desKey);

        //TODO: do encryption, by calling method Cipher.doFinal().
        byte[] encrypted = desCipher.doFinal(data.getBytes());

        //TODO: print the length of output encrypted byte[], compare the length of file shorttext.txt and longtext.txt
        // shorttext.txt -> 1480
        // longtext.txt -> 17360
        System.out.println(encrypted.length);

        // System.out.println(new String(encrypted));

        //TODO: do format conversion. Turn the encrypted byte[] format into base64format String using Base64
        String base64 = Base64.getEncoder().encodeToString(encrypted);

        //TODO: print the encrypted message (in base64format String format)
        System.out.println(base64);

        //TODO: create cipher object, initialize the ciphers with the given key, choose decryption mode as DES
        Cipher deCipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
        deCipher.init(Cipher.DECRYPT_MODE, desKey);

        //TODO: do decryption, by calling method Cipher.doFinal().
        byte[] decrypted = deCipher.doFinal(encrypted);

        //TODO: do format conversion. Convert the decrypted byte[] to String, using "String a = new String(byte_array);"
        String a = new String(decrypted);

        //TODO: print the decrypted String text and compare it with original text
        System.out.println(a);

        if (a.equals(data)) {
            System.out.println("same");
        } else {
            System.out.println("different");
        }
    }
}