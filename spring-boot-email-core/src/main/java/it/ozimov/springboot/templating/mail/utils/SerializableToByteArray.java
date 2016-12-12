package it.ozimov.springboot.templating.mail.utils;

import it.ozimov.springboot.templating.mail.service.exception.CannotSerializeException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.io.*;
import java.util.function.Function;

public class SerializableToByteArray<O extends Serializable> implements Function<O, byte[]> {

    @Override
    public byte[] apply(@NonNull final O serializable) {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            ObjectOutput out = new ObjectOutputStream(bos);
            out.writeObject(serializable);
            out.flush();
            return bos.toByteArray();
        } catch (IOException e) {
            throw new CannotSerializeException(String.format("Cannot serialize object of type %s to byte[].",
                    serializable.getClass().getName()), e);
        }
    }

}
