package com.studymind.ai;

/**
 * Interface representing the AI service provider contract.
 *
 * OOP / Viva Concept:
 * - Abstraction & Loose Coupling: The rest of the application depends
 *   on this interface, NOT the specific implementation (e.g. Gemini, OpenAI, Claude).
 * - Polymorphism: Any new AI provider can be swapped in without modifying controllers.
 */
public interface AIService {

    /**
     * Sends a prompt/message to the AI model and receives a response string.
     *
     * @param prompt The user's input prompt or system-augmented instructions.
     * @return Generated text response from the AI model.
     */
    String generateResponse(String prompt);
}
