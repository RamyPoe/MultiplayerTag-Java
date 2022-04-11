package dependencies;

import java.util.Base64;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.security.*;


public class AES_Cipher {
    //Indicates keys have been exchanged
    public boolean ready = false;
    private PublicKey publickey;
    KeyAgreement keyAgreement;
    byte[] sharedsecret;

    String ALGO = "AES";

    public AES_Cipher() {
        makeKeyExchangeParams();
    }

    private void makeKeyExchangeParams() {
        KeyPairGenerator kpg = null;
        try {
            kpg = KeyPairGenerator.getInstance("EC");
            kpg.initialize(128);
            KeyPair kp = kpg.generateKeyPair();
            publickey = kp.getPublic();
            keyAgreement = KeyAgreement.getInstance("ECDH");
            keyAgreement.init(kp.getPrivate());

        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            e.printStackTrace();
        }
    }

    public void setReceiverPublicKey(PublicKey publickey) {
        try {
            keyAgreement.doPhase(publickey, true);
            sharedsecret = keyAgreement.generateSecret();
            ready = true;
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }
    }

    public String encrypt(String msg) {
        try {
            Key key = generateKey();
            Cipher c = Cipher.getInstance(ALGO);
            c.init(Cipher.ENCRYPT_MODE, key);
            byte[] encVal = c.doFinal(msg.getBytes());
            return Base64.getEncoder().encodeToString(encVal);
        } catch (BadPaddingException | InvalidKeyException | NoSuchPaddingException | IllegalBlockSizeException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return msg;
    }

    public String decrypt(String encryptedData) {
        try {
            Key key = generateKey();
            Cipher c = Cipher.getInstance(ALGO);
            c.init(Cipher.DECRYPT_MODE, key);
            byte[] decValue = c.doFinal(Base64.getDecoder().decode(encryptedData));
            return new String(decValue);
        } catch (BadPaddingException | InvalidKeyException | NoSuchPaddingException | IllegalBlockSizeException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return encryptedData;
    }

    public PublicKey getPublickey() {
        return publickey;
    }

    public Key generateKey() {
        return new SecretKeySpec(sharedsecret, ALGO);
    }

//------------------------------------------------------------------------------------------------------------------------------------------

    public String getPublickeyString() {
        return Base64.getEncoder().encodeToString(getPublickey().getEncoded());
    }

    public void setReceiverPublicKeyString(String keyString) {
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("EC");
            setReceiverPublicKey(keyFactory.generatePublic(new X509EncodedKeySpec(Base64.getDecoder().decode(keyString))));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}