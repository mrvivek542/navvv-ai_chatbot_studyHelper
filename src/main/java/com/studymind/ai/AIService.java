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

    /**
     * Sends a prompt using a specific requested model name.
     *
     * @param prompt The prompt to execute.
     * @param modelName Specific model identifier (e.g. gemini-3.5-flash-lite, gemini-3.8-flash, gemini-2.5-pro).
     * @return Generated text response.
     */
    String generateResponseWithModel(String prompt, String modelName);
}
