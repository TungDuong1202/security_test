package com.example.user;

import com.example.user.config.AesConfig;
import com.example.user.utils.AesUtil;
import com.example.user.utils.RsaUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import javax.crypto.SecretKey;
import java.security.PrivateKey;
import java.security.PublicKey;

@SpringBootTest // Load toàn bộ context của Spring để test Config
@TestPropertySource(properties = {
        // --- 1. CONFIG DATABASE (Dùng H2 giả lập MySQL) ---
        // Ghi đè URL để dùng H2 thay vì MySQL
        "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=MySQL",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.datasource.driver-class-name=org.h2.Driver",

        // Quan trọng: Bắt Hibernate nói chuyện kiểu H2 để không bị lỗi cú pháp SQL
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create-drop", // Tạo bảng khi chạy, xóa khi tắt

        // --- 2. CONFIG CRYPTO (Giá trị giả định) ---
        // Key AES giả (Base64 của chuỗi 32 ký tự '12345678901234567890123456789012')
        "crypto.aes.key=YAp6F5djB/yOfx7nr/v9jz9jMNYYqsVYTrtWshcgNUQ=",

        // Đường dẫn Keystore (Lưu ý: File này PHẢI CÓ THẬT trong src/test/resources hoặc src/main/resources)
        // Nếu bạn chưa có file thật, test sẽ lỗi FileNotFound.
        "crypto.rsa.keystore-path=security/keystore.p12",

        // Mật khẩu giả (Phải khớp với file keystore nếu bạn muốn test sâu, còn nếu chỉ test load bean thì điền gì cũng được)
        "crypto.rsa.keystore-password=duongduong",
        "crypto.rsa.alias=rsa-key"
})
class CryptoConfigTest {

    @Autowired
    private AesConfig aesCryptoConfig;

    @Autowired
    private PrivateKey privateKey; // Inject Bean PrivateKey từ RsaCryptoConfig

    @Autowired
    private PublicKey publicKey;   // Inject Bean PublicKey từ RsaCryptoConfig

    @Test
    @DisplayName("Kiểm tra AES Config: Key không null và Mã hóa/Giải mã OK")
    void testAesConfig() {
        // 1. Kiểm tra Key đã được load chưa
        SecretKey key = aesCryptoConfig.getAesSecretKey();
        Assertions.assertNotNull(key, "AES Key không được null (Lỗi Config hoặc @Value)");

        // 2. Test thử Mã hóa & Giải mã
        String originalText = "";
        String encrypted = AesUtil.encrypt(originalText, key);
        String decrypted = AesUtil.decrypt(encrypted, key);

        System.out.println("AES Encrypted: " + encrypted);
        System.out.println("AES Decrypted: " + decrypted);

        Assertions.assertEquals(originalText, decrypted, "Dữ liệu sau khi giải mã phải giống ban đầu");
    }

    @Test
    @DisplayName("Test RSA Visual - Hiển thị chi tiết kết quả")
    void testRsaUtilVisual() {
        System.out.println("\n================ BẮT ĐẦU TEST RSA ================");

        // 1. Chuẩn bị
        String originalData = "Giao dịch chuyển khoản 1 tỷ đồng";
        String signature = RsaUtil.sign(originalData, privateKey);

        System.out.println("📄 Dữ liệu gốc: " + originalData);
        System.out.println("Lx Chữ ký tạo ra: " + signature);

        System.out.println("\n--- TIẾN HÀNH KIỂM TRA 3 TRƯỜNG HỢP ---");

        // ---------------------------------------------------------
        // CASE 1: Mọi thứ đều chuẩn (Happy Case)
        // ---------------------------------------------------------
        boolean check1 = RsaUtil.verify(originalData, signature, publicKey);

        System.out.println("1️⃣ Test Data CHUẨN + Chữ ký CHUẨN");
        System.out.println("   -> Kỳ vọng: true");
        System.out.println("   -> Thực tế: " + check1);

        if (check1) System.out.println("   => ✅ PASS");
        else System.out.println("   => ❌ FAIL");

        Assertions.assertTrue(check1, "Case 1 phải True");

        // ---------------------------------------------------------
        // CASE 2: Dữ liệu bị Hacker sửa (Tampered Data)
        // ---------------------------------------------------------
        String hackedData = "Giao dịch chuyển khoản 9 tỷ đồng"; // Sửa số tiền
        boolean check2 = RsaUtil.verify(hackedData, signature, publicKey);

        System.out.println("\n2️⃣ Test Data BỊ HACK (Sửa 1 tỷ -> 9 tỷ)");
        System.out.println("   -> Kỳ vọng: false (Hệ thống phải phát hiện ra)");
        System.out.println("   -> Thực tế: " + check2);

        if (!check2) System.out.println("   => ✅ PASS (Đã chặn thành công)");
        else System.out.println("   => ❌ FAIL (Nguy hiểm! Hệ thống không phát hiện ra)");

        Assertions.assertFalse(check2, "Case 2 phải False");

        // ---------------------------------------------------------
        // CASE 3: Chữ ký giả mạo (Fake Signature)
        // ---------------------------------------------------------
        String fakeSig = signature.substring(0, signature.length() - 5) + "ABCDE"; // Sửa đuôi chữ ký
        boolean check3 = RsaUtil.verify(originalData, fakeSig, publicKey);

        System.out.println("\n3️⃣ Test Chữ ký GIẢ MẠO (Sửa chữ ký)");
        System.out.println("   -> Kỳ vọng: false");
        System.out.println("   -> Thực tế: " + check3);

        if (!check3) System.out.println("   => ✅ PASS (Đã chặn thành công)");
        else System.out.println("   => ❌ FAIL");

        Assertions.assertFalse(check3, "Case 3 phải False");

        System.out.println("\n================ KẾT THÚC TEST ================");
    }
}