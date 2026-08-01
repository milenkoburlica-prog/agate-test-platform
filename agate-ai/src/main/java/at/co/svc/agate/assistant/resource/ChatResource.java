package at.co.svc.agate.assistant.resource;

import at.co.svc.agate.assistant.service.AgateAiService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

@Path("/ai")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ChatResource {

    @Inject
    AgateAiService agateAiService; // Centralna komponenta

    public static record ChatRequest(String message) {}
    public static record ChatResponse(String response) {}

    @POST
    @Path("/chat")
    public ChatResponse askAi(ChatRequest request) {
        System.out.println("🤖 Sending to Ollama: " + request.message());
        
        // Poziv ka Ollami preko servisa
        String aiAnswer = agateAiService.chat(request.message());
        
        return new ChatResponse(aiAnswer);
    }
}