package dev.catalkaya.abiplaner.repository;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import io.agroal.api.AgroalDataSource;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


@ApplicationScoped
public class QrCodeRepository {

    @Inject
    AgroalDataSource postgres;

    private final String BASEURL = "https://abiplaner.catalkaya.dev/checkin";
    private final String OUTPUTPATH = "/home/abiplaner/Abiplaner/qrcodes/";

    public void createAll(int id, String benutzerId, int anzahlEssenskarte, int anzahlAbendkarte) {
        int anzahlKarten = anzahlEssenskarte + anzahlAbendkarte;
        String kartenNr;

        //creates QRCode for every available card
        for (int i = 1; i <= anzahlKarten; i++) {
            kartenNr = i + benutzerId;

            System.out.println(URLEncoder.encode(kartenNr, StandardCharsets.UTF_8));
            //creates the final URL that will be put inside the QRCode
            String url = BASEURL + "?kartenNr=" + kartenNr;

            try {
                String filePath = createFilePath(id, i);

                BitMatrix matrix = new MultiFormatWriter().encode(url, BarcodeFormat.QR_CODE, 250, 250);
                MatrixToImageWriter.writeToPath(matrix, "jpg", Paths.get(filePath));

                addCheckIn(kartenNr);

            } catch (WriterException | IOException e) {
                throw new RuntimeException(e);
            }
        }
    }


    /**
     * Checks if targetfolder and parent directories already exist and creates them if needed
     *
     * @param id       BestellungsID des Nutzers (OAuth)
     * @param kartenNr kartennummer
     * @return finished file path
     * @throws IOException ioexception
     */
    private String createFilePath(int id, int kartenNr) {
        try {
            Path folderPath = Paths.get(OUTPUTPATH + "/" + id);
            if (!Files.exists(folderPath)) {
                Files.createDirectories(folderPath);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return OUTPUTPATH + "/" + id + "/" + kartenNr + "-" + id + ".jpg";
    }


    // adds the order to the checkIn table for later
    private void addCheckIn(String kartenNr) {
        String sql = "INSERT INTO checkIn(karten_nr) VALUES(?)";
        try (Connection con = postgres.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, kartenNr);
            ps.execute();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }


    public boolean validateQrCode(String kartenNr) {
        String sql = "SELECT checked FROM checkin WHERE karten_nr = ?";
        try (Connection con = postgres.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, kartenNr);

            ResultSet rs = ps.executeQuery();

            if(rs.next()){
                if(rs.getBoolean(1)) return false;
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        sql = "UPDATE checkin SET checked = true WHERE karten_nr = ?";
        try (Connection con = postgres.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, kartenNr);
            ps.execute();
            return true;
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return false;
    }
}
