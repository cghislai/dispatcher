package com.charlyghislain.dispatcher.api.rendering;


import javax.mail.Address;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Set;


public class RenderedMailHeaders {

    @NotNull
    private Address from;
    @NotNull
    @Size(min = 1)
    private Set<Address> to;
    @NotNull
    private Set<Address> cc;
    @NotNull
    private Set<Address> bcc;
    @NotNull
    private String subject;

    public Address getFrom() {
        return from;
    }

    public void setFrom(Address from) {
        this.from = from;
    }

    public Set<Address> getTo() {
        return to;
    }

    public void setTo(Set<Address> to) {
        this.to = to;
    }

    public Set<Address> getCc() {
        return cc;
    }

    public void setCc(Set<Address> cc) {
        this.cc = cc;
    }

    public Set<Address> getBcc() {
        return bcc;
    }

    public void setBcc(Set<Address> bcc) {
        this.bcc = bcc;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }
}
