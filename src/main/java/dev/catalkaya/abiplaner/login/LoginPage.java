package dev.catalkaya.abiplaner.login;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/api/v1")
public class LoginPage {
    @GET
    @Path("/login")
    @Produces(MediaType.TEXT_PLAIN)
    public String getMessage(){
        return "Hello from Quarkus!";
    }
}
