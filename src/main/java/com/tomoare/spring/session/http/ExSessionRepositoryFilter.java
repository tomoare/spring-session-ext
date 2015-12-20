package com.tomoare.spring.session.http;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.session.ExpiringSession;
import org.springframework.session.SessionRepository;
import org.springframework.session.web.http.SessionRepositoryFilter;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

/**
 *
 * @author tomoare
 */
public class ExSessionRepositoryFilter<S extends ExpiringSession> extends SessionRepositoryFilter<S> {

    private List<String> excludeUrls = Collections.emptyList();

    public ExSessionRepositoryFilter(SessionRepository<S> sessionRepository) {
        super(sessionRepository);
    }

    public void setExcludeUrls(List<String> excludeUrls) {
        if (CollectionUtils.isEmpty(excludeUrls)) {
            return;
        }
        this.excludeUrls = excludeUrls;
    }

    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String path = request.getPathInfo();
        boolean isExclude = false;
        if (!StringUtils.isEmpty(path)) {
            for (String excludeUrl : excludeUrls) {
                if (path.startsWith(excludeUrl)) {
                    isExclude = true;
                    break;
                }
            }
        }
        if (isExclude) {
            filterChain.doFilter(request, response);
        } else {
            super.doFilterInternal(request, response, filterChain);
        }
    }
}
