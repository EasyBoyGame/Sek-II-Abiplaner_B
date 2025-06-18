package dev.catalkaya.abiplaner.controller;

import dev.catalkaya.abiplaner.model.Bestellung;
import dev.catalkaya.abiplaner.model.CheckinRequest;
import dev.catalkaya.abiplaner.repository.BestellungRepository;
import dev.catalkaya.abiplaner.repository.QrCodeRepository;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.LocalTime;
import java.util.List;


@Path("/api/v1/checkin")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class QrCodeController {

    @Inject
    QrCodeRepository qrCodeRepository;
    BestellungRepository bestellungRepository;

    @POST
    public Response checkInCustomer(CheckinRequest request) throws SQLException {
        if(request.kartenNr().isEmpty()){
            return Response.status(Response.Status.NO_CONTENT).entity("Missing kartenNr").build();
        }

        boolean istEK = istEK(request.kartenNr());                              // Ist Karte EK oder AK?
        if(!istEK){
            LocalTime now = LocalTime.now();
            LocalTime targetTime = LocalTime.of(22, 0);

            if(now.isBefore(targetTime)) return Response.status(Response.Status.SERVICE_UNAVAILABLE).build();
        }

        boolean valid = qrCodeRepository.validateQrCode(request.kartenNr());    // Karte ist gültig

        return valid ? Response.status(Response.Status.OK).build() : Response.status(Response.Status.BAD_REQUEST).entity("Invalid kartenNr").build();
    }

    private boolean istEK(String kartenNr) throws SQLException {
        String[] user = getUserId(kartenNr);

        Bestellung bestellung = bestellungRepository.getBestellungByUID(user[1]);
        if(Integer.parseInt(user[0]) > bestellung.anzahlEssenskarte()){
            return false;
        }
        else return true;
    }


    private String[] getUserId(String kartenNr){
        String part1 = kartenNr.substring(0, kartenNr.length() - 36);
        String part2 = kartenNr.substring(kartenNr.length() - 36);

        return new String[]{part1, part2};
    }
}
