import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/** 一次性工具：生成 init-data.sql 用的身份证 AES 十六进制 */
public class GenIdCardHex {
    public static void main(String[] args) throws Exception {
        String secret = args.length > 0 ? args[0] : "SLMS-SECRET-KEY1";
        byte[] keyBytes = Arrays.copyOf(secret.getBytes(StandardCharsets.UTF_8), 16);
        SecretKeySpec key = new SecretKeySpec(keyBytes, "AES");
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        String[] ids = {
            "110101200201011234", "110101200202021235", "110101200203031236", "110101200204041237",
            "110101200205051238", "110101200206061239", "110101200207071240", "110101200208081241",
            "110101200209091242", "110101200210101243", "110101200211111244", "110101200212121245",
            "320101200201011246", "320101200202021247", "320101200203031248", "320101200204041249",
            "440101200201011250", "440101200202021251", "440101200203031252", "440101200204041253"
        };
        for (String id : ids) {
            byte[] enc = cipher.doFinal(id.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder("0x");
            for (byte b : enc) hex.append(String.format("%02X", b));
            System.out.println(id + "\t" + hex);
        }
    }
}
