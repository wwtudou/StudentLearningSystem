package com.sls.util;

import com.sls.common.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.regex.Pattern;

/**
 * 身份证号工具：格式校验、AES 加密存储、脱敏展示
 */
@Component
public class IdCardUtil {

    // 18 位身份证号正则
    private static final Pattern ID_CARD_PATTERN = Pattern.compile(
            "^[1-9]\\d{5}(18|19|20)\\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\\d|3[01])\\d{3}[\\dXx]$");

    private final SecretKeySpec keySpec;

    /** 从配置文件读取 AES 密钥 */
    public IdCardUtil(@Value("${slms.id-card-secret}") String secret) {
        byte[] keyBytes = Arrays.copyOf(secret.getBytes(StandardCharsets.UTF_8), 16);
        this.keySpec = new SecretKeySpec(keyBytes, "AES");
    }

    /** 校验身份证号格式 */
    public void validate(String idCard) {
        if (idCard == null || !ID_CARD_PATTERN.matcher(idCard).matches()) {
            throw new BusinessException("身份证号格式不正确，须为18位有效号码");
        }
    }

    /** AES 加密，存入数据库 VARBINARY 字段 */
    public byte[] encrypt(String idCard) {
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);
            return cipher.doFinal(idCard.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new BusinessException("身份证号加密失败");
        }
    }

    /** AES 解密（仅服务端内部使用） */
    public String decrypt(byte[] encrypted) {
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, keySpec);
            return new String(cipher.doFinal(encrypted), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new BusinessException("身份证号解密失败");
        }
    }

    /** 脱敏：110***********1234 */
    public String mask(String idCard) {
        if (idCard == null || idCard.length() < 8) {
            return "********";
        }
        return idCard.substring(0, 3) + "***********" + idCard.substring(idCard.length() - 4);
    }
}
