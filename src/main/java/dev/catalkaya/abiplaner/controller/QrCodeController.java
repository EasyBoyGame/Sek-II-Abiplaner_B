package dev.catalkaya.abiplaner.controller;

import dev.catalkaya.abiplaner.model.CheckinRequest;
import dev.catalkaya.abiplaner.repository.QrCodeRepository;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

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

    /*
    @POST
    public Response checkInCustomer(CheckinRequest request){
        System.out.println("THIS IS THE CARD NUMBER: " + request.kartenNr());

        if(request.kartenNr().isEmpty()){
            return Response.status(Response.Status.BAD_REQUEST).entity("Missing kartenNr").build();
        }

        boolean valid = qrCodeRepository.validateQrCode(URLDecoder.decode(request.kartenNr(), StandardCharsets.UTF_8));
        System.out.println("THIS IS VALID OR NOT: " + valid);
        return valid ? Response.status(Response.Status.OK).build() : Response.status(Response.Status.BAD_REQUEST).entity("Invalid kartenNr").build();
    }
     */
}
