/* Packet types
 * 0 -> client sending filename
 * 1 -> client sending file packet
 * 2 -> client requests encrypted nonce
 * 3 -> client requests server certificate (signed public key)
 * 4 -> client closes connection
 * 5 -> client sends session key
 */

import javax.crypto.spec.SecretKeySpec;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.FileOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Base64;

public class ServerCP2 {
  // certificates
  private static final String certFileName = "certificate/certificate_1004351.crt";
  private static final String certPrivateKey = "certificate/private_key.der";
  private static PrivateKey privateKey;
  private static X509Certificate certificate;
  private static SecretKeySpec sessionKey;

  public static void main(String[] args) {
    int port = 4321;
    if (args.length > 0) {
      port = Integer.parseInt(args[0]);
    }

    ServerSocket welcomeSocket = null;
    Socket connectionSocket = null;
    DataOutputStream toClient = null;
    DataInputStream fromClient = null;

    FileOutputStream fileOutputStream = null;
    BufferedOutputStream bufferedFileOutputStream = null;

    try {
      // get server private key
      privateKey = PrivateKeyReader.get(certPrivateKey);
      certificate = PublicKeyCRT.getCertificate(certFileName);

      int packet = 0; // counter for packet

      welcomeSocket = new ServerSocket(port);
      connectionSocket = welcomeSocket.accept();
      fromClient = new DataInputStream(connectionSocket.getInputStream());
      toClient = new DataOutputStream(connectionSocket.getOutputStream());

      while (!connectionSocket.isClosed()) {
        int packetType;
        try{
          packetType = fromClient.readInt();
        }catch (EOFException e){
          packetType = 4;
        }

        if (packetType == 0) { // If the packet is for transferring the filename
          System.out.println("Receiving file...");
          packet = 0; // filename should be the first packet

          int numBytes = fromClient.readInt();
          byte[] filename = new byte[numBytes];
          // Must use read fully!
          // See:
          // https://stackoverflow.com/questions/25897627/datainputstream-read-vs-datainputstream-readfully
          fromClient.readFully(filename, 0, numBytes);

          byte[] filenameDecrypted = Utils.decryptAES(filename, sessionKey);

          fileOutputStream =
              new FileOutputStream("outputfiles/recv_" + new String(filenameDecrypted));
          bufferedFileOutputStream = new BufferedOutputStream(fileOutputStream);
        } else if (packetType == 1) { // If the packet is for transferring a chunk of the file
          int numBytes = fromClient.readInt(); // encrypted stream
          int encryptedNumBytes = fromClient.readInt();
          byte[] block = new byte[encryptedNumBytes];
          fromClient.readFully(block, 0, encryptedNumBytes);

          packet++;

          byte[] decryptedBlock = Utils.decryptAES(block, sessionKey);

          if (numBytes > 0) bufferedFileOutputStream.write(decryptedBlock, 0, numBytes);

          if (numBytes < 117) {
            System.out.println("Closing connection...");
            System.out.printf("Packet %s received", packet);

            if (bufferedFileOutputStream != null) {
              bufferedFileOutputStream.close();
            }

            if (bufferedFileOutputStream != null && fileOutputStream != null) {
              fileOutputStream.close();
            }
          }
        } else if (packetType == 2) { // request for encrypted nonce
          sendEncryptedNonce(fromClient, toClient);
        } else if (packetType == 3) { // client requests public key
          sendSignedCertificate(toClient, certificate);
        } else if (packetType == 4) { // client closes connection
          System.out.println("Closing connection");
          fromClient.close();
          toClient.close();
          connectionSocket.close();

          System.out.println("Connection closed");
        } else if (packetType == 5) { // client sends session key
          String sessionKeyEncrypted = fromClient.readUTF();
          byte[] sessionKeyDecrypted = Utils.decrypt(Base64.getDecoder().decode(sessionKeyEncrypted), privateKey);
          sessionKey = new SecretKeySpec(sessionKeyDecrypted, 0, sessionKeyDecrypted.length, "AES");
          System.out.println("Session key: " + Base64.getEncoder().encodeToString(sessionKey.getEncoded()));
        } else { // handle edge case
          System.out.println("Ignored packet: invalid packet type");
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private static void sendSignedCertificate(DataOutputStream toClient, X509Certificate certificate)
          throws Exception {
    String encodedCert = Base64.getEncoder().encodeToString(certificate.getEncoded());
    //    byte[] encodedCert =
    // Base64.getEncoder().encodeToString(certificate.getEncoded()).getBytes();
    System.out.println("Sending client signed certificate");
    toClient.writeInt(encodedCert.length());
    toClient.writeUTF(encodedCert);
    toClient.flush();
    System.out.println("Sent signed certificate");
  }

  private static void sendEncryptedNonce(DataInputStream fromClient, DataOutputStream toClient)
          throws Exception {
    System.out.println("Sending encrypted nonce");

    // get nonce from client
    int length = fromClient.readInt();
    byte[] nonce = new byte[length];
    fromClient.readFully(nonce, 0, length); // read as string for byte[] type
    byte[] encryptedNonce = Utils.encrypt(nonce, privateKey);

    // send encrypted nonce to client
    toClient.writeInt(encryptedNonce.length);
    toClient.write(encryptedNonce);
    toClient.flush();
    System.out.println("Sent encrypted nonce");
  }
}
