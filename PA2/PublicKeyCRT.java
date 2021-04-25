// FOR CLIENT

import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.PublicKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Base64;

public class PublicKeyCRT {
  public static PublicKey getPublicKeyFromCRT(String filename) throws Exception {
    InputStream fileInputStream = new FileInputStream(filename);
    CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
    X509Certificate CAcert =
        (X509Certificate) certificateFactory.generateCertificate(fileInputStream);

    PublicKey publicKey = CAcert.getPublicKey();
    CAcert.checkValidity();
    //        CAcert.verify(publicKey); why this fail?????

    return publicKey;
  }

  public static X509Certificate getCertificate(String filename) throws Exception {
    InputStream fileInputStream = new FileInputStream(filename);
    CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
    X509Certificate CAcert =
        (X509Certificate) certificateFactory.generateCertificate(fileInputStream);

    return CAcert;
  }

  public static void main(String[] args) throws Exception {
    // doesnt work, verify fails
    X509Certificate certificate = getCertificate("certificate/certificate_1004351.crt");
    System.out.println(Base64.getEncoder().encodeToString(certificate.getEncoded()));
  }
}
