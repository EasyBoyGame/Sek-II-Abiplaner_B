package dev.catalkaya.abiplaner.repository;

import dev.catalkaya.abiplaner.model.Bestellung;
import io.agroal.api.AgroalDataSource;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@ApplicationScoped
public class BestellungRepository {

    @Inject
    AgroalDataSource postgres;


    // hole alle Bestellungen
    public List<Bestellung> getBestellungen() throws SQLException {
        List<Bestellung> bestellungen = new ArrayList<>();
        String sql = "SELECT id, benutzer_id, benutzer_email, anzahl_essenskarte, anzahl_abendkarte, bezahlt FROM bestellung;";
        try (Connection con = postgres.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("id");
                    String benutzerId = rs.getString("benutzer_id");
                    String benutzerEmail = rs.getString("benutzer_email");
                    int anzahlEssenskarte = rs.getInt("anzahl_essenskarte");
                    int anzahlAbendkarte = rs.getInt("anzahl_abendkarte");
                    boolean bezahlt = rs.getBoolean("bezahlt");
                    bestellungen.add(new Bestellung(id, benutzerId, benutzerEmail, anzahlEssenskarte, anzahlAbendkarte, bezahlt));
                }
            }
        }
        return bestellungen;
    }


    // hole eine Bestellung
    public Bestellung getBestellung(int bestellungId) throws SQLException {
        String sql = "SELECT id, benutzer_id, benutzer_email, anzahl_essenskarte, anzahl_abendkarte, bezahlt FROM bestellung WHERE id = ?;";
        try (Connection con = postgres.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, bestellungId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new NoSuchElementException();
                }
                return new Bestellung(
                        rs.getInt("id"),
                        rs.getString("benutzer_id"),
                        rs.getString("benutzer_email"),
                        rs.getInt("anzahl_essenskarte"),
                        rs.getInt("anzahl_abendkarte"),
                        rs.getBoolean("bezahlt")
                );
            }
        }
    }


    public int getAnzahlEK() throws SQLException{
        int anzahl = 0;
        String sql = "SELECT SUM(anzahl_essenskarte) FROM bestellung;";
        try (Connection con = postgres.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    anzahl = rs.getInt(0);
                }
            }
            return 400 - anzahl;
        }
    }

    public int getAnzahlEK(String benutzer_id) throws SQLException{
        int anzahl = 0;
        String sql = "SELECT anzahl_essenskarte FROM bestellung WHERE benutzer_id = ?;";
        try (Connection con = postgres.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(0, benutzer_id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    anzahl = rs.getInt(0);
                }
            }
            return anzahl;
        }
    }

    public int getAnzahlAK() throws SQLException{
        int anzahl = 0;
        String sql = "SELECT SUM(anzahl_abendkarte) FROM bestellung;";
        try (Connection con = postgres.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    anzahl = rs.getInt(0);
                }
            }
            return 200 - anzahl;
        }
    }

    public int getAnzahlAK(String benutzer_id) throws SQLException{
        int anzahl = 0;
        String sql = "SELECT anzahl_abendkarte FROM bestellung WHERE benutzer_id = ?;";
        try (Connection con = postgres.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(0, benutzer_id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    anzahl = rs.getInt(0);
                }
            }
            return anzahl;
        }
    }


    // schreibe eine Bestellung
    public void newBestellung(String benutzerId, String benutzerEmail, int anzahlEssenskarte, int anzahlAbendkarte) throws SQLException {
        String sql = "INSERT INTO bestellung(benutzer_id, benutzer_email, anzahl_essenskarte, anzahl_abendkarte, bezahlt) VALUES(?, ?, ?, ?, false)";
        try (Connection con = postgres.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, benutzerId);
            ps.setString(2, benutzerEmail);
            ps.setInt(3, anzahlEssenskarte);
            ps.setInt(4, anzahlAbendkarte);
            ps.execute();
        }
    }


    // lösche eine Bestellung
    public void deleteBestellung(int bestellungId) throws SQLException {
        String sql = "DELETE FROM bestellung WHERE id = ?;";
        try (Connection con = postgres.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, bestellungId);
            ps.execute();
        }
    }


    // bearbeite eine Bestellung
    public void updateBestellung(String benutzerId, int anzahlEssenskarte, int anzahlAbendkarte, boolean bezahlt) throws SQLException {
        String sql = "UPDATE bestellung SET anzahl_essenskarte = ?, anzahl_abendkarte = ?, bezahlt = ? WHERE benutzer_id = ? ";
        try (Connection con = postgres.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, anzahlEssenskarte);
            ps.setInt(2, anzahlAbendkarte);
            ps.setBoolean(3, bezahlt);
            ps.setString(4, benutzerId);
            ps.execute();
        }
    }


    //
    public void updateBestellStatus(int bestellungsId, boolean bezahlt) throws SQLException {
        String sql = "UPDATE bestellung SET bezahlt = ? WHERE id = ? ";
        try (Connection con = postgres.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setBoolean(1, bezahlt);
            ps.setInt(2, bestellungsId);
            ps.execute();
        }
    }


    // prüfe ob Bestellung existiert
    public boolean existsBestellung(int bestellungId) throws SQLException {
        String sql = "SELECT id FROM bestellung WHERE id = ?;";
        try (Connection con = postgres.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, bestellungId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }


    // prüfe ob Benutzer existiert
    public boolean existsBenutzer(String benutzerId) throws SQLException {
        String sql = "SELECT benutzer_id FROM bestellung WHERE benutzer_id = ?;";
        try (Connection con = postgres.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, benutzerId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }


    // ändert email_versendet Status
    public void mailSend(int id) throws SQLException {
        String sql = "UPDATE bestellung SET email_versendet = true WHERE id=?";
        try (Connection con = postgres.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.execute();
        }
    }
}