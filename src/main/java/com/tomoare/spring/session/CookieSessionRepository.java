package com.tomoare.spring.session;

import com.tomoare.spring.session.converter.Base64Converter;
import com.tomoare.spring.session.converter.Converter;
import javax.annotation.Nonnull;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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

    public static int SIZE_PER_RECORD = 3072;

    public static int MAX_RECORD_COUNT = 10;

    public static int DEFAULT_AGE = 0;

    private Converter CONVERTER = new Base64Converter();

    public void setConverter(Converter converter) {
        this.CONVERTER = converter;
    }

    public void setSizePerRecord(int size) {
        SIZE_PER_RECORD = size;
    }

    public void setMaxRecordCount(int count) {
        MAX_RECORD_COUNT = count;
    }

    public void setCookieAge(int second) {
        DEFAULT_AGE = second;
    }

    @Override
    public MapSession createSession() {
        return new MapSession();
    }

    @Override
    public void save(MapSession session) {
        HttpServletResponse response = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getResponse();

        final String sessionObject = CONVERTER.convertToString(session);
        final String sessionId = session.getId();

        final StringBuilder valueBuff = new StringBuilder();
        int cookieCount = 0;
        final char[] chars = sessionObject.toCharArray();

        for (char ch : chars) {
            valueBuff.append(ch);
            if (valueBuff.length() >= SIZE_PER_RECORD) {
                addCookie(response, sessionId, valueBuff, cookieCount);
                cookieCount++;
            }
        }
        if (valueBuff.length() > 0) {
            addCookie(response, sessionId, valueBuff, cookieCount);
            cookieCount++;
        }
        for (int i = cookieCount; i < MAX_RECORD_COUNT; i++) {
            CookieGenerator cookie = new CookieGenerator();
            cookie.setCookieName(getCookieName(sessionId, i));
            cookie.setCookieMaxAge(DEFAULT_AGE);
            cookie.removeCookie(response);

        }
    }

    @Override
    public MapSession getSession(String id) {

        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();

        final StringBuilder buff = new StringBuilder();
        for (int i = 0; i < MAX_RECORD_COUNT; i++) {
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

        return CONVERTER.convertToSession(buff.toString());

    }

    @Override
    public void delete(String id) {
        HttpServletResponse response = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getResponse();
        for (int i = 0; i < MAX_RECORD_COUNT; i++) {
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
            @Nonnull final int seq) {
        StringBuilder bul = new StringBuilder(COOKIE_NAME_PREFIX.length() + sessionId.length() + 3);
        return bul.append(COOKIE_NAME_PREFIX).append("-").append(seq).append("-").append(sessionId).toString();
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
