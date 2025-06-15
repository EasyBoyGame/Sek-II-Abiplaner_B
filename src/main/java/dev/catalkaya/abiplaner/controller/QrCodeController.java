package dev.catalkaya.abiplaner.controller;

import dev.catalkaya.abiplaner.repository.QrCodeRepository;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import javax.print.attribute.standard.Media;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;


@Path("/api/v1/checkin")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class QrCodeController {

    @Inject
    QrCodeRepository qrCodeRepository;

    @POST
    public Response checkInCustomer(@QueryParam("kartenNr") String kartenNr){
        System.out.println("THIS IS THE CARD NUMBER: " + kartenNr);

        if(kartenNr == null || kartenNr.isEmpty()){
            return Response.status(Response.Status.BAD_REQUEST).entity("Missing kartenNr").build();
        }

        boolean valid = qrCodeRepository.validateQrCode(URLDecoder.decode(kartenNr, StandardCharsets.UTF_8));
        System.out.println("THIS IS VALID OR NOT: " + valid);
        return valid ? Response.status(Response.Status.OK).build() : Response.status(Response.Status.BAD_REQUEST).entity("Invalid kartenNr").build();
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
