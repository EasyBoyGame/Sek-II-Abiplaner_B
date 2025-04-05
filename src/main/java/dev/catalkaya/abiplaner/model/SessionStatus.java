package dev.catalkaya.abiplaner.model;

import jakarta.enterprise.context.ApplicationScoped;

import java.io.Serializable;

@ApplicationScoped
public class SessionStatus implements Serializable {
    private static final long serialVersionUID = 1L;

    private String checkinStatus;
    public String getCheckinStatus(){
        return checkinStatus;
    }

    public void setCheckinStatus(String checkinStatus){
        this.checkinStatus = checkinStatus;
    }
}
