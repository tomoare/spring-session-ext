package com.tomoare.spring.session.database;

import com.google.common.collect.Lists;
import com.tomoare.spring.session.converter.Base64Converter;
import com.tomoare.spring.session.converter.Converter;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import org.apache.log4j.Logger;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.session.MapSession;
import org.springframework.session.SessionRepository;

/**
 *
 * @author tomoare
 */
public class DbSessionRepository implements SessionRepository<MapSession> {

    private static final Logger logger = Logger.getLogger(DbSessionRepository.class);

    public static int SIZE_PER_RECORD = 3072;

    public static int MAX_RECORD_COUNT = 10;

    private JdbcTemplate jdbcTemplate;

    private String DELETE_SQL = "delete from session where id = ?";

    private String SELECT_SQL = "select id, seq, value, expireDt from session where id = ? order by seq";

    private String INSERT_SQL = "insert into session (id, seq, value, expireDt) values (?, ?, ?, ?)";

    private Converter CONVERTER = new Base64Converter();

    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void setDeleteSql(String query) {
        this.DELETE_SQL = query;
    }

    public void setInsertSql(String query) {
        this.INSERT_SQL = query;
    }

    public void setSelectSql(String query) {
        this.SELECT_SQL = query;
    }

    public void setConverter(Converter converter) {
        this.CONVERTER = converter;
    }

    public void setSizePerRecord(int size) {
        SIZE_PER_RECORD = size;
    }

    public void setMaxRecordCount(int count) {
        MAX_RECORD_COUNT = count;
    }

    @Override
    public MapSession createSession() {
        return new MapSession();
    }

    @Override
    public void save(MapSession mapSession) {
        final String sessionObject = CONVERTER.convertToString(mapSession);
        final String sessionId = mapSession.getId();

        final StringBuilder valueBuff = new StringBuilder();
        int cookieCount = 0;
        final char[] chars = sessionObject.toCharArray();

        Date expireDt = new Date();
        List<DbSessionDto> sessions = Lists.newArrayList();
        for (char ch : chars) {
            valueBuff.append(ch);
            if (valueBuff.length() >= SIZE_PER_RECORD) {
                sessions.add(toDbSessionDto(sessionId, cookieCount, valueBuff.toString(), expireDt));
                valueBuff.delete(0, valueBuff.length());
                cookieCount++;
            }
        }
        if (valueBuff.length() > 0) {
            sessions.add(toDbSessionDto(sessionId, cookieCount, valueBuff.toString(), expireDt));
            valueBuff.delete(0, valueBuff.length());
            cookieCount++;
        }

        delete(sessionId);

        jdbcTemplate.batchUpdate(INSERT_SQL, new BatchPreparedStatementSetter() {

            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ps.setString(1, sessions.get(i).getId());
                ps.setInt(2, sessions.get(i).getSeq());
                ps.setString(3, sessions.get(i).getValue());
                ps.setDate(4, null); // TODO
            }

            @Override
            public int getBatchSize() {
                return sessions.size();
            }
        });

    }

    @Override
    public MapSession getSession(String id) {
        RowMapper<DbSessionDto> mapper = new BeanPropertyRowMapper<>(DbSessionDto.class);
        List<DbSessionDto> sessions = jdbcTemplate.query(SELECT_SQL, mapper, id);

        final StringBuilder buff = new StringBuilder();
        sessions.stream().forEach((session) -> buff.append(session.getValue()));
        if (buff.length() == 0) {
            return null;
        }

        return CONVERTER.convertToSession(buff.toString());

    }

    @Override
    public void delete(String id) {
        jdbcTemplate.update(DELETE_SQL, id);
    }

    private DbSessionDto toDbSessionDto(String sessionId, Integer seq, String value, Date expireDt) {
        DbSessionDto session = new DbSessionDto();
        session.setId(sessionId);
        session.setSeq(seq);
        session.setValue(value);
        session.setExpireDt(expireDt);
        return session;
    }

}
