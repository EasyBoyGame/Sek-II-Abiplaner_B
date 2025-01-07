package dev.catalkaya.abiplaner.repository;

import io.nayuki.qrcodegen.QrCode;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class QrCodeRepository {

    private final String path = "http://localhost:8080";


    public void newQrCode(String benutzerId, int anzahlEssenskarte, int anzahlAbendkarte) throws IOException {
        int anzahlKarten = anzahlEssenskarte + anzahlAbendkarte;

        for (int i = 1; i == anzahlKarten; i++) {
            byte[] bytes = ("" + benutzerId + i).getBytes(StandardCharsets.UTF_8);
            String url = path + "/" + bytes;

            try {
                QrCode qr0 = QrCode.encodeText(url, QrCode.Ecc.MEDIUM);
                BufferedImage img = toImage(qr0, 4, 10);
                ImageIO.write(img, "png", new File("qr-code.png"));

            }
            catch (IOException ex){
                ex.printStackTrace();
            }
        }

    }


}
