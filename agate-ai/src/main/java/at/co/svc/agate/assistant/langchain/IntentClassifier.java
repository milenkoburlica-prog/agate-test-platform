package at.co.svc.agate.assistant.langchain;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

public interface IntentClassifier {

    @SystemMessage("""
        Analyze the user's input and classify its intent.
        
        Respond with ONLY one word from the following:
        - TEST_CASE_CREATION: If the user wants to create, design, or generate a test case, test step, or automation script.
        - GENERAL_CHAT: For greetings, general questions, or any topic unrelated to creating Agate test cases.
        
        Do not provide explanations. Just the enum value.
        """)
    MessageIntent classify(@UserMessage String text);
}