package it.ozimov.springboot.templating.mail.utils;

import it.ozimov.springboot.templating.mail.service.exception.CannotDeserializeException;
import it.ozimov.springboot.templating.mail.service.exception.CannotSerializeException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.io.*;
import java.util.function.Function;

public class ByteArrayToSerializable<O extends Serializable> implements Function<byte[], O> {

    @Override
    public O apply(@NonNull final byte[] bytes) {
        try (ByteArrayInputStream bis = new ByteArrayInputStream(bytes)) {
            ObjectInput in = new ObjectInputStream(bis);
            return (O) in.readObject();
        } catch (ClassNotFoundException | IOException e) {
            throw new CannotDeserializeException("Cannot deserialize from byte[].", e);
        }
    }

}