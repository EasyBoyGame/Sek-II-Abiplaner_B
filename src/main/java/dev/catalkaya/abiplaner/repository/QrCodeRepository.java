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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@ApplicationScoped
public class QrCodeRepository {

    @Inject
    AgroalDataSource postgres;

    private final String BASEURL = "http://localhost:5173";
    private final String OUTPUTPATH = "output";


    public void createAll(String benutzerID, int anzahlEssenskarte, int anzahlAbendkarte){
        int anzahlKarten = anzahlAbendkarte + anzahlEssenskarte;
        String kartenName;

        //creates QRCode for every available card
        for (int i = 1; i <= anzahlKarten; i++) {
            kartenName = i + "-" + benutzerID;
            byte[] bytes = (kartenName).getBytes(StandardCharsets.UTF_8);

            //byte[] bytes2 = "%5BB@7ed67150".getBytes(StandardCharsets.UTF_8);

            //creates the final URL that will be put inside QRCode
            String url = BASEURL + "/" + bytes;

            try {
                String filePath = createFilePath(benutzerID, i);

                BitMatrix matrix = new MultiFormatWriter().encode(url, BarcodeFormat.QR_CODE, 250, 250);
                MatrixToImageWriter.writeToPath(matrix, "jpg", Paths.get(filePath));

                addCheckIn(kartenName);

            } catch (WriterException | IOException | SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }


    /**
     * Checks if targetfolder and parent directories already exist and creates them if needed
     * @param benutzerID benutzerId des Nutzers
     * @param cardID kartennummer
     * @return finished file path
     * @throws IOException ioexception
     */
    private String createFilePath(String benutzerID, int cardID) throws IOException {
        Path folderPath = Paths.get(OUTPUTPATH + "/" + benutzerID);

        if (!Files.exists(folderPath)) {
            Files.createDirectories(folderPath);
        }

        return OUTPUTPATH + "/" + benutzerID + "/"  + cardID + "-" + benutzerID + ".jpg";
    }


    // adds the order to the checkIn table for later
    private void addCheckIn(String kartenName) throws SQLException {
        String sql = "INSERT INTO checkIn(karten_nr) VALUES(?)";
        try (Connection con = postgres.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, kartenName);
            ps.execute();
        }
    }
}
