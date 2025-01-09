package dev.catalkaya.abiplaner.repository;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import jakarta.enterprise.context.ApplicationScoped;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@ApplicationScoped
public class QrCodeRepository {

    private final String BASEURL = "http://localhost:5173";
    private final String OUTPUTPATH = "output";


    public void createAll(String benutzerID, int anzahlEssenskarte, int anzahlAbendkarte){
        int anzahlKarten = anzahlAbendkarte + anzahlEssenskarte;

        //creates QRCode for every available card
        for (int i = 1; i == anzahlKarten; i++) {
            byte[] bytes = (benutzerID + "-" + i).getBytes(StandardCharsets.UTF_8);

            //byte[] bytes2 = "%5BB@7ed67150".getBytes(StandardCharsets.UTF_8);

            //creates the final URL that will be put inside QRCode
            String url = BASEURL + "/" + bytes;

            try {
                String filePath = createFilePath(benutzerID, i);

                BitMatrix matrix = new MultiFormatWriter().encode(url, BarcodeFormat.QR_CODE, 250, 250);
                MatrixToImageWriter.writeToPath(matrix, "jpg", Paths.get(filePath));
            } catch (WriterException | IOException e) {
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

        return OUTPUTPATH + "/" + benutzerID + "/" + benutzerID + "_" + cardID + ".jpg";
    }
}
