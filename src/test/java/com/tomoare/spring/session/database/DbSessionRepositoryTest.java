package com.tomoare.spring.session.database;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.session.MapSession;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 *
 * @author tomoare
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"file:src/test/resources/test-context.xml"})
public class DbSessionRepositoryTest {

    @Autowired
    private DbSessionRepository dbSessionRepository;

    @Test
    public void testInsert() {

        String sessionId = "test";

        MapSession session = dbSessionRepository.createSession();
        session.setId(sessionId);
        session.setAttribute("value1", "value1");
        session.setAttribute("value2", "value2");

        dbSessionRepository.save(session);

        MapSession session2 = dbSessionRepository.getSession(sessionId);

        Assert.assertThat(session.getAttribute("value1"), CoreMatchers.is(session2.getAttribute("value1")));

    }
}
