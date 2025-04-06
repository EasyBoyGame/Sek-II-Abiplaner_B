package dev.catalkaya.abiplaner.controller;

import dev.catalkaya.abiplaner.repository.QrCodeRepository;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;


@Path("/api/v1/checkin")
public class QrCodeController {

    @Inject
    QrCodeRepository qrCodeRepository;

    @GET
    @Consumes(MediaType.TEXT_PLAIN)
    public Response checkInCustomer(@QueryParam("kartenNr") String kartenNr){
        if(kartenNr == null || kartenNr.isEmpty()){
            return createRedirectResponse("failure");
            //return Response.status(Response.Status.BAD_REQUEST).entity("Missing kartenNr").build();
        }

        boolean valid = qrCodeRepository.validateQrCode(URLDecoder.decode(kartenNr, StandardCharsets.UTF_8));
        return createRedirectResponse(valid ? "success" : "failure");
    }

    private Response createRedirectResponse(String status) {
        String redirectUrl = "/checkin/" + status;
        String cookieValue = String.format("checkinStatus=%s; Path=/checkin; Max-Age=60; HttpOnly; SameSite=Strict", status);
        return Response
                .seeOther(URI.create(redirectUrl))
                .header("Set-Cookie", cookieValue)
                .build();
    }
}
