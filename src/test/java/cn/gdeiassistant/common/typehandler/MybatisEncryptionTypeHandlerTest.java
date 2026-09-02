package cn.gdeiassistant.common.typehandler;

import cn.gdeiassistant.common.pojo.Encryption.AESEncryptConfig;
import cn.gdeiassistant.common.tools.Utils.StringEncryptUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MybatisEncryptionTypeHandlerTest {

    private final StringEncryptUtils encryptUtils = new StringEncryptUtils();
    private final MybatisEncryptionTypeHandler handler = new MybatisEncryptionTypeHandler();

    @BeforeEach
    void setUp() {
        AESEncryptConfig config = new AESEncryptConfig();
        config.setPrivateKey("test-only-private-key");
        encryptUtils.setEncryptConfig(config);
    }

    @AfterEach
    void tearDown() {
        encryptUtils.setEncryptConfig(null);
    }

    @Test
    void invalidStoredCiphertextFailsClosed() throws Exception {
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.getString("password")).thenReturn("legacy-ciphertext");

        assertThrows(SQLException.class,
                () -> handler.getNullableResult(resultSet, "password"));
    }
}
