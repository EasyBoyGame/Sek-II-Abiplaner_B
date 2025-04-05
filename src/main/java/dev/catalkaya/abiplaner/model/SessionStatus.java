package dev.catalkaya.abiplaner.model;

import jakarta.enterprise.context.SessionScoped;

import java.io.Serializable;

@SessionScoped
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
