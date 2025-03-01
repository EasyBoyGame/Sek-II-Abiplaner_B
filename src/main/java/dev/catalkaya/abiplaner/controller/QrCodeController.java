package dev.catalkaya.abiplaner.controller;

import dev.catalkaya.abiplaner.repository.QrCodeRepository;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.lang.annotation.Repeatable;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;


@Path("/api/v1/checkin")
public class QrCodeController {

    @Inject
    QrCodeRepository qrCodeRepository;

    @GET
    @Consumes(MediaType.TEXT_PLAIN)
    public Response checkInCustomer(@QueryParam("kartenNr") String kartenNr){
        if(kartenNr == null || kartenNr.isEmpty()){
            return Response.status(Response.Status.BAD_REQUEST).entity("Missing kartenNr").build();
        }

        if(qrCodeRepository.validateQrCode(URLDecoder.decode(kartenNr, StandardCharsets.UTF_8))){
            return Response.status(Response.Status.OK).build();
        } else {
          return Response.status(Response.Status.NOT_FOUND).build();
        }
    }
}
