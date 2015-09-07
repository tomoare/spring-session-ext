package com.tomoare.spring.session.database;

import java.util.Date;

/**
 *
 * @author tomoare
 */
public class DbSessionDto {

    /**
     * session id
     */
    private String id;

    /**
     * sequence
     */
    private Integer seq;

    /**
     * value
     */
    private String value;

    /**
     * expireDt
     */
    private Date expireDt;

    /**
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return the seq
     */
    public Integer getSeq() {
        return seq;
    }

    /**
     * @param seq the seq to set
     */
    public void setSeq(Integer seq) {
        this.seq = seq;
    }

    /**
     * @return the value
     */
    public String getValue() {
        return value;
    }

    /**
     * @param value the value to set
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * @return the expireDt
     */
    public Date getExpireDt() {
        return expireDt;
    }

    /**
     * @param expireDt the expireDt to set
     */
    public void setExpireDt(Date expireDt) {
        this.expireDt = expireDt;
    }
}
