package com.tomoare.spring.session;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Base64;
import javax.annotation.Nonnull;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.springframework.session.MapSession;
import org.springframework.session.SessionRepository;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.util.CookieGenerator;
import org.springframework.web.util.WebUtils;

/**
 *
 * @author tomoare
 */
public class CookieSessionRepository implements SessionRepository<MapSession> {

    private static final Logger logger = Logger.getLogger(CookieSessionRepository.class);

    public static final String COOKIE_NAME_PREFIX = "so";

    public static final int SIZE_PER_COOKIE = 3072;

    public static final int MAX_COOKIE_COUNT = 10;

    @Override
    public MapSession createSession() {
        return new MapSession();
    }

    @Override
    public void save(MapSession session) {
        HttpServletResponse response = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getResponse();

        final String sessionObject = convertToString(session);
        final String sessionId = session.getId();

        final StringBuilder valueBuff = new StringBuilder();
        int cookieCount = 0;
        final char[] chars = sessionObject.toCharArray();

        for (char ch : chars) {
            valueBuff.append(ch);
            if (valueBuff.length() >= SIZE_PER_COOKIE) {
                addCookie(response, sessionId, valueBuff, cookieCount);
                cookieCount++;
            }
        }
        if (valueBuff.length() > 0) {
            addCookie(response, sessionId, valueBuff, cookieCount);
            cookieCount++;
        }
        for (int i = cookieCount; i < MAX_COOKIE_COUNT; i++) {
            CookieGenerator cookie = new CookieGenerator();
            cookie.setCookieName(getCookieName(sessionId, i));
            cookie.setCookieMaxAge(0);
            cookie.removeCookie(response);

        }
    }

    @Override
    public MapSession getSession(String id) {

        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();

        final StringBuilder buff = new StringBuilder();
        for (int i = 0; i < MAX_COOKIE_COUNT; i++) {
            final String cookieName = getCookieName(id, i);
            final Cookie cookie = WebUtils.getCookie(request, cookieName);
            if (cookie == null) {
                break;
            }
            buff.append(cookie.getValue());
        }
        if (buff.length() == 0) {
            return null;
        }

        return convertToSession(buff.toString());

    }

    @Override
    public void delete(String id) {
        HttpServletResponse response = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getResponse();
        for (int i = 0; i < MAX_COOKIE_COUNT; i++) {
            CookieGenerator cookie = new CookieGenerator();
            cookie.setCookieName(getCookieName(id, i));
            cookie.setCookieMaxAge(0);
            cookie.removeCookie(response);
        }
    }

    /**
     *
     * @param sessionId
     * @param number
     *
     * @return
     */
    private String getCookieName(
            @Nonnull final String sessionId,
            @Nonnull final int number) {
        StringBuilder bul = new StringBuilder(COOKIE_NAME_PREFIX.length() + sessionId.length() + 3);
        return bul.append(COOKIE_NAME_PREFIX).append("-").append(number).append("-").append(sessionId).toString();
    }

    /**
     * convert the session into string.
     * @param session session object
     * @return string
     */
    public String convertToString(final MapSession session) {
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

    /**
     * convert string into MapSession
     * @param str 
     * @return
     */
    public MapSession convertToSession(@Nonnull final String str) {

        ObjectInputStream objectInput = null;
        try {
            final byte[] data = Base64.getDecoder().decode(str);
            objectInput = new ObjectInputStream(new ByteArrayInputStream(data));
            return (MapSession) objectInput.readObject();
        } catch (IOException | ClassNotFoundException e) {
            logger.error(e.getMessage(), e);
        } finally {
            IOUtils.closeQuietly(objectInput);
        }
        return null;
    }

    /**
     *
     * @param response
     * @param sessionId
     * @param valueBuff
     * @param cookieNumber
     */
    private void addCookie(
            @Nonnull final HttpServletResponse response,
            @Nonnull final String sessionId,
            @Nonnull final StringBuilder valueBuff,
            @Nonnull final int cookieNumber) {
        final String cookieValue = valueBuff.toString();
        valueBuff.delete(0, valueBuff.length());

        final String cookieName = getCookieName(sessionId, cookieNumber);

        CookieGenerator cookie = new CookieGenerator();
        cookie.setCookieName(cookieName);

        cookie.addCookie(response, cookieValue);
    }
}
