
package com.iedcs.security;

/**
 *
 * @author Andre
 */
import java.io.IOException;
import java.math.BigInteger;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Date;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v1CertificateBuilder;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;

public class X509Certkeygen {

    public X509CertificateHolder genX509(PublicKey pubKey, PrivateKey privKey) throws IOException, OperatorCreationException {

        ContentSigner sigGen = new JcaContentSignerBuilder("SHA1withRSA").setProvider("BC").build(privKey);
        byte[] encoded = pubKey.getEncoded();
        SubjectPublicKeyInfo subPubKeyInfo = new SubjectPublicKeyInfo(
                ASN1Sequence.getInstance(encoded));
        byte[] otherEncoded = subPubKeyInfo.getPublicKey().getEncoded();

        Date startDate = new Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000);
        Date endDate = new Date(System.currentTimeMillis() + 365 * 24 * 60 * 60 * 1000);

        X509v1CertificateBuilder v1CertGen = new X509v1CertificateBuilder(
                new X500Name("CN=Test"),
                BigInteger.ONE,
                startDate, endDate,
                new X500Name("CN=Test"),
                subPubKeyInfo);

        X509CertificateHolder certHolder = v1CertGen.build(sigGen);


        return certHolder; 
        

    }
}
