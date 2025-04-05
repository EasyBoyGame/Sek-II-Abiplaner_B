package dev.catalkaya.abiplaner.controller;

import dev.catalkaya.abiplaner.model.SessionStatus;
import dev.catalkaya.abiplaner.repository.QrCodeRepository;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
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

    @Inject
    SessionStatus sessionStatus;

    @GET
    @Consumes(MediaType.TEXT_PLAIN)
    public Response checkInCustomer(@QueryParam("kartenNr") String kartenNr){
        if(kartenNr == null || kartenNr.isEmpty()){
            sessionStatus.setCheckinStatus("failure");
            return Response
                    .seeOther(java.net.URI.create("https://abiplaner.catalkaya.dev/checkin?status=failure"))
                    .build();
            //return Response.status(Response.Status.BAD_REQUEST).entity("Missing kartenNr").build();
        }

        boolean valid = qrCodeRepository.validateQrCode(URLDecoder.decode(kartenNr, StandardCharsets.UTF_8));
        if(valid){
            sessionStatus.setCheckinStatus("success");
            return Response
                    .seeOther(java.net.URI.create("https://abiplaner.catalkaya.dev/checkin?status=success"))
                    .build();
        } else {
            sessionStatus.setCheckinStatus("failure");
            return Response
                    .seeOther(java.net.URI.create("https://abiplaner.catalkaya.dev/checkin?status=failure"))
                    .build();
        }
    }
}
