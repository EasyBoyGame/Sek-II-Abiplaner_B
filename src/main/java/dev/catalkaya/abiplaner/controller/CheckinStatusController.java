package dev.catalkaya.abiplaner.controller;

import dev.catalkaya.abiplaner.model.SessionStatus;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;


@Path("/api/v1/checkin-status")
public class CheckinStatusController {
    @Inject
    SessionStatus sessionStatus;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public Response getCheckinStatus(){
        String status = sessionStatus.getCheckinStatus();

        if(status == null){
            return Response.status(Response.Status.NOT_FOUND).entity("No checkin-status found!").build();
        }

        return Response.ok(status).build();
    }
}