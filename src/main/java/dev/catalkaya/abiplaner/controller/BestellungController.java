package dev.catalkaya.abiplaner.controller;

import dev.catalkaya.abiplaner.model.Bestellung;
import dev.catalkaya.abiplaner.repository.BestellungRepository;
import dev.catalkaya.abiplaner.repository.QrCodeRepository;
import io.quarkus.mailer.Mail;
import io.quarkus.mailer.Mailer;
import io.quarkus.scheduler.Scheduled;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

//TODO GENERIERUNG QR-CODE + ANHÄNGEN AN EMAIL
//TODO LOGIN VIA ISERV


@Path("/api/v1/bestellung")
public class BestellungController {

    final int EKPREIS = 50;
    final int AKPREIS = 20;

    @Inject
    BestellungRepository bestellungRepository;
    @Inject
    QrCodeRepository qrCodeRepository;

    @Inject
    Mailer mailer;


    // alle vorhandenen Bestellungen abfragen
    @GET
    public List<Bestellung> getBestellungen() {
        try {
            return bestellungRepository.getBestellungen();
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new InternalServerErrorException();
        }
    }


    // erstellen einer neuen Bestellung
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response newBestellung(Bestellung request) throws SQLException {
        if (!bestellungRepository.existsBenutzer(request.benutzerId())) {
            try {
                bestellungRepository.newBestellung(request.benutzerId(), request.benutzerEmail(), request.anzahlEssenskarte(), request.anzahlAbendkarte());
            } catch (SQLException ex) {
                ex.printStackTrace();
                throw new InternalServerErrorException();
            }
        } else {
            try {
                bestellungRepository.updateBestellung(request.benutzerId(), request.anzahlEssenskarte(), request.anzahlAbendkarte(), request.bezahlt());
            } catch (SQLException ex) {
                ex.printStackTrace();
                throw new InternalServerErrorException();
            }
        }
        int summe = request.anzahlEssenskarte() * EKPREIS + request.anzahlAbendkarte() * AKPREIS;
        return Response.ok(summe).build();
    }


    // Bestellung löschen
    @DELETE
    @Path("{bestellungId:\\d+}")
    public void deleteBestellung(@PathParam("bestellungId") int bestellungId) {
        try {
            if (!bestellungRepository.existsBestellung(bestellungId)) {
                throw new NotFoundException("Bestellung existiert nicht");
            }
            Bestellung bestellung = bestellungRepository.getBestellung(bestellungId);
            if (bestellung.bezahlt()) {
                throw new ForbiddenException("Bestellung ist bereits bezahlt");
            }
            bestellungRepository.deleteBestellung(bestellungId);

        } catch (SQLException ex) {
            ex.printStackTrace();
            throw new InternalServerErrorException();
        }
    }


    // Überprüfe ob Bestellungen bezahlt wurden
    @Scheduled(every = "1m")
    void cronJobCheckForPayment() throws SQLException {
        try {
            List<Bestellung> bestellungen = bestellungRepository.checkPayment();

            for(Bestellung bestellung:bestellungen){
                qrCodeRepository.newQrCode(bestellung.benutzerId(), bestellung.anzahlEssenskarte(), bestellung.anzahlAbendkarte());
                String benutzerEmail = bestellung.benutzerEmail();
                String[] nameArray = benutzerEmail.split("@");
                nameArray = nameArray[0].split("\\.");

                String text = "";
                if(bestellung.anzahlEssenskarte() > 0 && bestellung.anzahlAbendkarte() > 0){
                    text="Hallo " + nameArray[0] + " " + nameArray[1] + ",\n\n"+
                         "im Anhang befinden sich ihre Eintrittskare(n) für den Abiball!\n\n" +
                         "Die Karte(n) bis KARTENNR_EK ist/sind die Essenskarte(n). Die restlichen Karte(n) sind die Abendkarte(n) für die Aftershow.\n\n" +
                         "Der reguläre Eintritt beginnt um 18:00 Uhr, die Aftershow beginnt ab 22:00 Uhr.";
                } else if(bestellung.anzahlEssenskarte() > 0 && bestellung.anzahlAbendkarte() == 0){
                    text = "Hallo " + nameArray[0] + " " + nameArray[1] + ",\n\n"+
                           "im Anhang befinden sich ihre Essenskarte(n). Der Eintritt findet ab 18:00 Uhr statt.\n\n\n";
                } else if (bestellung.anzahlEssenskarte() == 0 && bestellung.anzahlAbendkarte() > 0) {
                    text = "Hallo " + nameArray[0] + " " + nameArray[1] + ",\n\n"+
                           "im Anhang befinden sich ihre Abendkarte(n) für die Aftershow ab 22:00 Uhr.";
                }

                text += "\n\n\nViel Spaß beim Abiball!\n\n\n\n\n"+ "Adresse:\n" + "Ilseder Hütte 14,\n31241 Ilsede,\nDeutschland";

                mailer.send(Mail.withText(bestellung.benutzerEmail(), "Abiballkarten",text));

                bestellungRepository.mailSend(bestellung.id());
            }
        }
        catch (SQLException ex){
            throw new Error("cronJob failed!");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
