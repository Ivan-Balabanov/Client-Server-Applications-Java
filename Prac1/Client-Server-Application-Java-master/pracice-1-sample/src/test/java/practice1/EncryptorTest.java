package practice1;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.apache.commons.codec.binary.Hex;

public class EncryptorTest {

    Encryptor encryptor = new Encryptor();
    Message input = Message.fromString(1, 42, "Hello, World!");

    @Test
    void processInput () throws Exception {

        byte[] testArr1 = encryptor.entry_take(input);

        Assertions.assertThat(Hex.encodeHexString(testArr1)).isEqualTo("130d00000000000000820000002058781fa743c7a3a906dca0d4871a41fa862b5d717fb4236e7031d47963b8b42cf6cc7bf4");
    }
}


