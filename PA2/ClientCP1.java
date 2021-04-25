import java.io.*;
import java.net.Socket;
import java.security.PublicKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;

public class ClientCP1 {
  // variables
  private static String csertificate = "certificate/cacsertificate.crt";
  private static PublicKey serverPublicKey;
  private static byte[] nonce = new byte[32];

  public static void main(String[] args) {
    ArrayList<String> files = new ArrayList<String>();
    if (args.length == 0) {
      System.out.println("No file provided");
      return;
    }
    for (int i = 0; i < args.length; i++) {
      files.add(args[i]);
    }
    String inputFile = args[0];

    int numBytes = 0;
    Socket clientSocket = null;
    DataOutputStream toServer = null;
    DataInputStream fromServer = null;
    FileInputStream fileInputStream = null;
    BufferedInputStream bufferedFileInputStream = null;
    PrintWriter out = null;
    BufferedReader in = null;

    try {
      System.out.println("Establishing connection to server...");
      // Connect to server and get the input and output streams
      clientSocket = new Socket("localhost", 4321);
      toServer = new DataOutputStream(clientSocket.getOutputStream());
      fromServer = new DataInputStream(clientSocket.getInputStream());

      out = new PrintWriter(clientSocket.getOutputStream(), true);
      in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

      // get certificate, extract public key
      serverPublicKey = getPublicKey(toServer, fromServer);

      // generate nonce
      nonce = Utils.generateNonce();

      // send to server, get encrypted nonce, decrypt and compare
      boolean success = authenticateNonce(toServer, fromServer, nonce, serverPublicKey);

      if (!success) {
        System.out.println("oof 100, no authenticate");
        toServer.close();
        fromServer.close();
        clientSocket.close();
        return;
      } else {
        System.out.println("Authenticated");
      }

      fileInputStream = new FileInputStream(inputFile);
      bufferedFileInputStream = new BufferedInputStream(fileInputStream);

      //      int fileSize = fileInputStream.available();
      //      System.out.println("file size: " + fileSize);

      System.out.println("================================");
      System.out.println("Initiating file transfer...");

      long timeStarted = System.nanoTime();

      for (String f : files) {
        transferFileName(toServer, f, serverPublicKey);
        transferFileBody(fromServer, toServer, f, serverPublicKey);
      }
      long timeEnded = System.nanoTime();
      System.out.println("================================");
      System.out.println("End of file transfer.");
      double timeTaken = (timeEnded - timeStarted) / 1000000.0;
      System.out.println("Time taken: " + timeTaken);

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private static boolean authenticateNonce(
      DataOutputStream toServer,
      DataInputStream fromServer,
      byte[] nonce,
      PublicKey serverPublicKey)
      throws Exception {
    // send nonce
    toServer.writeInt(2);
    toServer.writeInt(nonce.length);
    toServer.write(nonce);

    // get encrypted nonce
    int length = fromServer.readInt();
    byte[] encryptedNonce = new byte[length];
    fromServer.readFully(encryptedNonce, 0, length);

    // decrypt nonce using public key
    byte[] decryptedNonce = Utils.decryptPublicKey(encryptedNonce, serverPublicKey);
    return Arrays.equals(decryptedNonce, nonce);
  }

  // gets server ca signed certificate
  private static PublicKey getPublicKey(DataOutputStream toServer, DataInputStream fromServer)
      throws Exception {
    toServer.writeInt(3);

    int packetSize = fromServer.readInt();
    //    byte[] certicateData = new byte[packetSize];
    //    fromServer.readFully(certicateData, 0, packetSize);
    String certificateData = fromServer.readUTF();

    byte[] cert = Base64.getDecoder().decode(certificateData);
    ByteArrayInputStream inputStream = new ByteArrayInputStream(cert);

    CertificateFactory cf = CertificateFactory.getInstance("X.509");
    X509Certificate certificate = (X509Certificate) cf.generateCertificate(inputStream);
    return certificate.getPublicKey();
  }

  public static void transferFileName(
      DataOutputStream toServer, String filename, PublicKey publicKey) {
    try {
      System.out.println("Sending filename");
      toServer.writeInt(0);
      String[] name = filename.split("/");
      byte[] encryptedFileName = Utils.encryptPublicKey(name[1].getBytes(), publicKey);
      toServer.writeInt(encryptedFileName.length);
      toServer.write(encryptedFileName);
      toServer.flush();
      System.out.println("Sent filename");
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static void transferFileBody(
      DataInputStream fromServer, DataOutputStream toServer, String filename, PublicKey publicKey) {
    try {
      System.out.println("Sending filebody");
      FileInputStream fileInputStream = new FileInputStream(filename);
      BufferedInputStream bufferedFileInputStream = new BufferedInputStream(fileInputStream);

      byte[] fromFileBuffer = new byte[117];
      boolean send = true;
      while (send) {
        int numBytes = bufferedFileInputStream.read(fromFileBuffer);
        // encryption
        byte[] encryptedFileBuffer = Utils.encryptPublicKey(fromFileBuffer, publicKey);
        int encryptedNumBytes = encryptedFileBuffer.length;

        // sending
        toServer.writeInt(1);
        toServer.writeInt(numBytes);
        toServer.writeInt(encryptedNumBytes);
        toServer.write(encryptedFileBuffer);
        toServer.flush();

        send = numBytes == fromFileBuffer.length;
      }

      bufferedFileInputStream.close();
      fileInputStream.close();

      System.out.println("Sent file body");
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
