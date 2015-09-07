package com.tomoare.spring.session.converter;

import javax.annotation.Nonnull;

/**
 *
 * @author tomoare
 */
public interface Converter {

    public <T> String convertToString(@Nonnull final T session);

    public <T> T convertToSession(@Nonnull final String str);

}
