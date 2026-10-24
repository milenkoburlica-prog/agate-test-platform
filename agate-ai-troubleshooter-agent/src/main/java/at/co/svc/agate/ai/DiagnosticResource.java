package at.co.svc.agate.ai;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import java.util.Map;

@Path("/api/v1/diagnostics")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class DiagnosticResource {
 @Inject DiagnosticOrchestrator orchestrator;
 @ConfigProperty(name="agate.agent.max-chars", defaultValue="5242880") int maxChars;
 @POST @Path("/analyze")
 public Response analyze(DiagnosticRequest request){
  if(request==null || request.caseText()==null || request.caseText().isBlank())
   return Response.status(400).entity(Map.of("error","Das Feld caseText ist erforderlich.")).build();
  if(request.caseText().getBytes(java.nio.charset.StandardCharsets.UTF_8).length>maxChars)
   return Response.status(413).entity(Map.of("error","Das Protokoll ist zu groß. Maximal "+maxChars+" Zeichen.")).build();
  try{return Response.ok(orchestrator.analyze(request)).build();}
  catch(GroqClient.GroqException ex){return Response.status(ex.httpStatus).entity(Map.of("error",ex.getMessage())).build();}
  catch(IllegalStateException ex){return Response.status(503).entity(Map.of("error",ex.getMessage())).build();}
  catch(InterruptedException ex){Thread.currentThread().interrupt();return Response.status(503).entity(Map.of("error","Die Anfrage wurde unterbrochen.")).build();}
  catch(Exception ex){return Response.status(502).entity(Map.of("error","Kommunikationsfehler mit Groq. Bitte Netzwerk und Konfiguration prüfen.")).build();}
 }
 @GET @Path("/health") public Map<String,String> health(){return Map.of("status","UP");}
}
