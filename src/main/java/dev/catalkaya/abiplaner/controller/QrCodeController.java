package dev.catalkaya.abiplaner.controller;

import dev.catalkaya.abiplaner.repository.QrCodeRepository;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;


@Path("/api/v1/checkin")
public class QrCodeController {

    @Inject
    QrCodeRepository qrCodeRepository;

    @POST
    @Path("{kartenNr:.*}")
    @Consumes(MediaType.TEXT_PLAIN)
    public void checkInCustomer(@PathParam("kartenNr") String kartenNr){
        try {

            System.out.println(URLDecoder.decode(kartenNr, StandardCharsets.UTF_8));


            if(qrCodeRepository.validateQrCode(URLDecoder.decode(kartenNr, StandardCharsets.UTF_8))){
                System.out.println("YAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAY");
            }
            else System.out.println("BOOOOOOOOOOOOOOOOOOOOOOOOOO");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
