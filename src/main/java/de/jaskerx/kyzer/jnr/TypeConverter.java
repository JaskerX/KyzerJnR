package de.jaskerx.kyzer.jnr;

import java.nio.ByteBuffer;
import java.util.UUID;

public class TypeConverter {

    public byte[] uuidToByteArray(UUID uuid) {
        return ByteBuffer.wrap(new byte[16])
                .putLong(uuid.getMostSignificantBits())
                .putLong(uuid.getLeastSignificantBits())
                .array();
    }

    public UUID byteArrayToUUID(byte[] bytes) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
        return new UUID(byteBuffer.getLong(), byteBuffer.getLong());
    }

}
