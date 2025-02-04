package dev.catalkaya.abiplaner.controller;

import dev.catalkaya.abiplaner.model.Bestellung;
import dev.catalkaya.abiplaner.model.BestellungRequest;
import dev.catalkaya.abiplaner.model.UpdateBestellStatus;
import dev.catalkaya.abiplaner.repository.BestellungRepository;
import dev.catalkaya.abiplaner.repository.QrCodeRepository;
import io.quarkus.mailer.Attachment;
import io.quarkus.mailer.Mail;
import io.quarkus.mailer.Mailer;
import io.quarkus.scheduler.Scheduled;
import io.quarkus.security.Authenticated;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.jwt.Claims;
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.*;

// TODO BESTÄTIGUNGSMAIL AN ALLE EINMAL SENDEN
// test


@Path("/api/v1/bestellung")
public class BestellungController {

    final int EKPREIS = 50;
    final int AKPREIS = 20;

    @Inject
    BestellungRepository bestellungRepository;
    @Inject
    QrCodeRepository qrCodeRepository;
    @Inject
    JsonWebToken jwt;

    @Inject
    Mailer mailer;


    // alle vorhandenen Bestellungen abfragen
    @GET
    @RolesAllowed({"abiplaner-admin"})
    public List<Bestellung> getBestellungen() {
        try {
            return bestellungRepository.getBestellungen();
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new InternalServerErrorException();
        }
    }


    @PATCH
    @RolesAllowed({"abiplaner-admin"})
    public void checkBestellung(UpdateBestellStatus status){
        try {
            if(bestellungRepository.existsBestellung(status.id())){
                bestellungRepository.updateBestellStatus(status.id(), status.bezahlt());
                sendQrCode(status.id());
            }
        }
        catch (SQLException ex){
            ex.printStackTrace();
            throw new NotFoundException();
        }
    }


    // erstellen einer neuen Bestellung
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Authenticated
    public Response newBestellung(BestellungRequest request){
        try {
            int anzahlEK = bestellungRepository.getAnzahlEK();
            int anzahlAK = bestellungRepository.getAnzahlAK();
            if (!bestellungRepository.existsBenutzer(jwt.getSubject())){
                if(request.anzahlEssenskarten() <= anzahlEK && request.anzahlAbendkarten() <= anzahlAK){
                    bestellungRepository.newBestellung(jwt.getSubject(), jwt.getClaim(Claims.email), request.anzahlEssenskarten(), request.anzahlAbendkarten());
                    sendPaymentRequest(jwt.getClaim(Claims.email), request.anzahlEssenskarten(), request.anzahlAbendkarten());
                } else {
                    return Response.status(Response.Status.BAD_REQUEST).entity("Die gewünschte Anzahl an Karten ist leider nicht mehr verfügbar.\nBitte Kontakt mit dem Finanzkomitee aufnehmen!").build();
                }
            }
            else {
                if(request.anzahlEssenskarten() <= anzahlEK + bestellungRepository.getAnzahlEK(jwt.getSubject()) &&
                        request.anzahlAbendkarten() <= anzahlAK + bestellungRepository.getAnzahlAK(jwt.getSubject())){
                    bestellungRepository.updateBestellung(jwt.getSubject(), request.anzahlEssenskarten(), request.anzahlAbendkarten(), false);
                    sendPaymentRequest(jwt.getClaim(Claims.email), request.anzahlEssenskarten(), request.anzahlAbendkarten());
                } else {
                    return Response.status(Response.Status.BAD_REQUEST).entity("Die gewünschte Anzahl an Karten ist leider nciht mehr verfügbar.\nBitte Kontakt mit dem Finanzkomitee aufnehmen!").build();
                }
            }
        }
        catch (SQLException ex){
            ex.printStackTrace();
            throw new InternalServerErrorException();
        }

        int summe = request.anzahlEssenskarten() * EKPREIS + request.anzahlAbendkarten() * AKPREIS;
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



    public void sendPaymentRequest(String benutzerEmail, int anzahlEssenskarten, int anzahlAbendkarten){
        String regex = "^(?:(?<vorname>[a-zA-Z]+)\\.)?(?<nachname>[a-zA-Z]+)@jsg-vechelde.de\\.com$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(benutzerEmail);

        String text = "";
        String vorname = matcher.group("vorname");
        String nachname = matcher.group("nachname");
        int summe = anzahlEssenskarten * 50 + anzahlAbendkarten * 20;
        //region if vorname != null
        if(vorname != null && anzahlEssenskarten > 0 && anzahlAbendkarten > 0){
            text = "Hallo " + capitilize(vorname) + " " + capitilize(nachname) + ",\n\n" +
                    "deine Bestellung über " + anzahlEssenskarten + " Essenskarten und " + anzahlAbendkarten + " Abendkarten ist bei uns eingegangen.\n" +
                    "Wir bitten dich den offenen Betrag von " + summe + "€ an das Konto mit der IBAN DE45 2505 0000 0202 0775 41 zu überweisen.\n\n" +
                    "Nach dem Eingang deiner Zahlung, werden die Abiballkarten an dich versendet.\n\n\n" +
                    "Vielen Dank für deine Bestellung! Wir freuen uns auf dich!\n\n" +
                    "Bei Problemen oder Änderungen mit deiner Bestellung, melde dich bitte beim Finanzkomitee.";
        } else if(vorname != null && anzahlEssenskarten > 0 && anzahlAbendkarten == 0){
            text = "Hallo " + capitilize(vorname) + " " + capitilize(nachname) + ",\n\n" +
                    "deine Bestellung über " + anzahlEssenskarten + " Essenskarten ist bei uns eingegangen.\n" +
                    "Wir bitten dich den offenen Betrag von " + summe + "€ an das Konto mit der IBAN DE45 2505 0000 0202 0775 41 zu überweisen.\n\n" +
                    "Nach dem Eingang deiner Zahlung, werden die Abiballkarten an dich versendet.\n\n\n" +
                    "Vielen Dank für deine Bestellung! Wir freuen uns auf dich!\n\n" +
                    "Bei Problemen oder Änderungen mit deiner Bestellung, melde dich bitte beim Finanzkomitee.";
        } else if(vorname != null && anzahlEssenskarten == 0 && anzahlAbendkarten > 0){
            text = "Hallo " + capitilize(vorname) + " " + capitilize(nachname) + ",\n\n" +
                    "deine Bestellung über " + anzahlAbendkarten + " Abendkarten ist bei uns eingegangen.\n" +
                    "Wir bitten dich den offenen Betrag von " + summe + "€ an das Konto mit der IBAN DE45 2505 0000 0202 0775 41 zu überweisen.\n\n" +
                    "Nach dem Eingang deiner Zahlung, werden die Abiballkarten an dich versendet.\n\n\n" +
                    "Vielen Dank für deine Bestellung! Wir freuen uns auf dich!\n\n" +
                    "Bei Problemen oder Änderungen mit deiner Bestellung, melde dich bitte beim Finanzkomitee.";
        }
        //endregion
        //region if vorname == null
        if(vorname == null && anzahlEssenskarten > 0 && anzahlAbendkarten > 0){
            text = "Hallo Herr/Frau " + capitilize(nachname) + ",\n\n" +
                    "Ihre Bestellung über " + anzahlEssenskarten + " Essenskarten und " + anzahlAbendkarten + " Abendkarten ist bei uns eingegangen.\n" +
                    "Wir bitten Sie den offenen Betrag von " + summe + "€ an das Konto mit der IBAN DE45 2505 0000 0202 0775 41 zu überweisen.\n\n" +
                    "Nach dem Eingang Ihrer Zahlung, werden die Abiballkarten an Sie versendet.\n\n\n" +
                    "Vielen Dank für Ihre Bestellung! Wir freuen uns auf Sie!\n\n" +
                    "Bei Problemen oder Änderungen mit Ihrer Bestellung, melden Sie sich bitte beim Finanzkomitee.";
        } else if(vorname != null && anzahlEssenskarten > 0 && anzahlAbendkarten == 0){
            text = "Hallo Herr/Frau " + capitilize(nachname) + ",\n\n" +
                    "Ihre Bestellung über " + anzahlEssenskarten + " Essenskarten ist bei uns eingegangen.\n" +
                    "Wir bitten Sie den offenen Betrag von " + summe + "€ an das Konto mit der IBAN DE45 2505 0000 0202 0775 41 zu überweisen.\n\n" +
                    "Nach dem Eingang Ihrer Zahlung, werden die Abiballkarten an Sie versendet.\n\n\n" +
                    "Vielen Dank für Ihre Bestellung! Wir freuen uns auf Sie!\n\n" +
                    "Bei Problemen oder Änderungen mit Ihrer Bestellung, melden Sie sich bitte beim Finanzkomitee.";
        } else if(vorname != null && anzahlEssenskarten == 0 && anzahlAbendkarten > 0){
            text = "Hallo Herr/Frau " + capitilize(nachname) + ",\n\n" +
                    "Ihre Bestellung über " + anzahlAbendkarten + " Abendkarten ist bei uns eingegangen.\n" +
                    "Wir bitten Sie den offenen Betrag von " + summe + "€ an das Konto mit der IBAN DE45 2505 0000 0202 0775 41 zu überweisen.\n\n" +
                    "Nach dem Eingang Ihrer Zahlung, werden die Abiballkarten an Sie versendet.\n\n\n" +
                    "Vielen Dank für Ihre Bestellung! Wir freuen uns auf Sie!\n\n" +
                    "Bei Problemen oder Änderungen mit Ihrer Bestellung, melden Sie sich bitte beim Finanzkomitee.";
        }
        //endregion

        mailer.send(Mail.withText(benutzerEmail, "Abiballkarten - Bestätigung deiner Bestellung", text));
    }


    // Überprüfe ob Bestellungen bezahlt wurden
    public void sendQrCode(int id) {
        try {
            Bestellung bestellung = bestellungRepository.getBestellung(id);
            qrCodeRepository.createAll(bestellung.id(), bestellung.anzahlEssenskarte(), bestellung.anzahlAbendkarte());

            String regex = "^(?:(?<vorname>[a-zA-Z]+)\\.)?(?<nachname>[a-zA-Z]+)@jsg-vechelde.de\\.com$";
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(bestellung.benutzerEmail());

            String text = "";
            String vorname = matcher.group("vorname");
            String nachname = matcher.group("nachname");

            //region if vorname != null
            if(vorname != null && bestellung.anzahlEssenskarte() > 0 && bestellung.anzahlAbendkarte() > 0){
                text = "Hallo " + capitilize(vorname) + " " + capitilize(nachname) + ",\n\n"+
                        "im Anhang befinden sich deine Eintrittskarte(n) für den Abiball!\n\n" +
                        "Die Karte(n) bis " + bestellung.anzahlEssenskarte() + "-" + bestellung.id() + " ist/sind die Essenskarte(n). Die restlichen Karte(n) sind die Abendkarte(n) für die Aftershow.\n\n" +
                        "Der reguläre Eintritt beginnt um 18:00 Uhr, die Aftershow beginnt ab 22:00 Uhr.";
            } else if(vorname != null && bestellung.anzahlEssenskarte() > 0 && bestellung.anzahlAbendkarte() == 0){
                text = "Hallo " + capitilize(vorname) + " " + capitilize(nachname) + ",\n\n"+
                        "im Anhang befinden sich deine Essenskarte(n). Der Eintritt findet ab 18:00 Uhr statt.\n\n\n";
            } else if (vorname != null && bestellung.anzahlEssenskarte() == 0 && bestellung.anzahlAbendkarte() > 0) {
                text = "Hallo " + capitilize(vorname) + " " + capitilize(nachname) + ",\n\n"+
                        "im Anhang befinden sich deine Abendkarte(n) für die Aftershow ab 22:00 Uhr.";
            }
            //endregion
            //region if vorname == null
            if(vorname == null && bestellung.anzahlEssenskarte() > 0 && bestellung.anzahlAbendkarte() > 0){
                text = "Hallo Herr/Frau " + capitilize(nachname) + ",\n\n"+
                        "im Anhang befinden sich Ihre Eintrittskarte(n) für den Abiball!\n\n" +
                        "Die Karte(n) bis " + bestellung.anzahlEssenskarte() + "-" + bestellung.id() + " ist/sind die Essenskarte(n). Die restlichen Karte(n) sind die Abendkarte(n) für die Aftershow.\n\n" +
                        "Der reguläre Eintritt beginnt um 18:00 Uhr, die Aftershow beginnt ab 22:00 Uhr.";
            } else if(vorname == null && bestellung.anzahlEssenskarte() > 0 && bestellung.anzahlAbendkarte() == 0){
                text = "Hallo Herr/Frau " + capitilize(nachname) + ",\n\n"+
                        "im Anhang befinden sich Ihre Essenskarte(n). Der Eintritt findet ab 18:00 Uhr statt.\n\n\n";
            } else if (vorname == null && bestellung.anzahlEssenskarte() == 0 && bestellung.anzahlAbendkarte() > 0) {
                text = "Hallo Herr/Frau " + capitilize(nachname) + ",\n\n"+
                        "im Anhang befinden sich Ihre Abendkarte(n) für die Aftershow ab 22:00 Uhr.";
            }
            //endregion
            text += "\n\n\nViel Spaß beim Abiball!\n\n\n\n\n"+ "Adresse:\n" + "Ilseder Hütte 14,\n31241 Ilsede,\nDeutschland";

            //String path = "D:/Dokumente/Schule/2_EK_Informatik/AbiplanerQuark/abiplaner/build/classes/java/main/output/" + bestellung.id();
            String path = "/home/abiplaner/Abiplaner/qrcodes/" + bestellung.id();
            File dir = new File(path);
            File[] files = dir.listFiles();

            ArrayList<Attachment> attachments = new ArrayList<>();
            for(File file: files){
                attachments.add(new Attachment(file.getName(), file, "image/jpg"));
            }

            mailer.send(Mail.withText(bestellung.benutzerEmail(), "Abiballkarten!",text).setAttachments(attachments));

            bestellungRepository.mailSend(bestellung.id());
        }
        catch (SQLException ex){
            ex.printStackTrace();
        }
    }

    public String capitilize(String input){
        return Character.toUpperCase(input.charAt(0)) + input.substring(1);
    }
}
