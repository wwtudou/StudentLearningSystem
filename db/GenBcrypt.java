import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/** 生成演示账号 BCrypt 密码哈希 */
public class GenBcrypt {
    public static void main(String[] args) {
        BCryptPasswordEncoder enc = new BCryptPasswordEncoder();
        String[] pwds = {
            "1admin6", "1dept", "2dept",
            "1teacher", "2teacher", "3teacher",
            "1student", "2student"
        };
        for (String p : pwds) {
            System.out.println(p + "\t" + enc.encode(p));
        }
    }
}
