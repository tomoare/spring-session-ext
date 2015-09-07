package com.tomoare.spring.session.converter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

/**
 *
 * @author tomoare
 */
public class Base64Converter<T> implements Converter {

    private static final Logger logger = Logger.getLogger(Base64Converter.class);

    @Override
    public <T> String convertToString(T session) {
        if (session == null) {
            return null;
        }

        ByteArrayOutputStream byteArrayOutput = null;
        ObjectOutputStream objectOutput = null;
        try {
            byteArrayOutput = new ByteArrayOutputStream();
            objectOutput = new ObjectOutputStream(byteArrayOutput);
            objectOutput.writeObject(session);
            final String sessionStr = new String(Base64.getEncoder().encode(byteArrayOutput.toByteArray()));
            return sessionStr;

        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        } finally {
            IOUtils.closeQuietly(byteArrayOutput);
            IOUtils.closeQuietly(objectOutput);
        }
        return null;
    }

    @Override
    public T convertToSession(String str) {
        ObjectInputStream objectInput = null;
        try {
            final byte[] data = Base64.getDecoder().decode(str);
            objectInput = new ObjectInputStream(new ByteArrayInputStream(data));
            return (T) objectInput.readObject();
        } catch (IOException | ClassNotFoundException e) {
            logger.error(e.getMessage(), e);
        } finally {
            IOUtils.closeQuietly(objectInput);
        }
        return null;
    }

}
