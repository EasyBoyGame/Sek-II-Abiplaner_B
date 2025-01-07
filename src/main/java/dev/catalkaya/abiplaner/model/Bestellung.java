package dev.catalkaya.abiplaner.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public record Bestellung(@JsonProperty("id") int id,
                         @JsonProperty("benutzerId") String benutzerId,
                         @JsonProperty("benutzerEmail") String benutzerEmail,
                         @JsonProperty("anzahlEssenskarte") int anzahlEssenskarte,
                         @JsonProperty("anzahlAbendkarte") int anzahlAbendkarte,
                         @JsonProperty("bezahlt") boolean bezahlt) {
    public Bestellung(int id,
                      String benutzerId,
                      String benutzerEmail,
                      int anzahlEssenskarte,
                      int anzahlAbendkarte,
                      boolean bezahlt) {
        this.id = id;
        this.benutzerId = benutzerId;
        this.benutzerEmail = benutzerEmail;
        this.anzahlEssenskarte = anzahlEssenskarte;
        this.anzahlAbendkarte = anzahlAbendkarte;
        this.bezahlt = bezahlt;
    }

    @Override
    public int id() {
        return id;
    }

    @Override
    public String benutzerId() {
        return benutzerId;
    }

    @Override
    public String benutzerEmail() {
        return benutzerEmail;
    }

    @Override
    public int anzahlEssenskarte() {
        return anzahlEssenskarte;
    }

    @Override
    public int anzahlAbendkarte() {
        return anzahlAbendkarte;
    }

    @Override
    public boolean bezahlt() {
        return bezahlt;
    }

    @Override
    public String toString() {
        return "Bestellung[" +
                "id=" + id + ", " +
                "benutzerId=" + benutzerId + ", " +
                "benutzerEmail=" + benutzerEmail + ", " +
                "anzahlEssenskarte=" + anzahlEssenskarte + ", " +
                "anzahlAbendkarte=" + anzahlAbendkarte + ", " +
                "bezahlt=" + bezahlt + ']';
    }

}
