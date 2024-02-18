package com.yalhyane.intellij.phpaicode;

import com.theokanning.openai.completion.chat.ChatCompletionChoice;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.service.OpenAiService;

import java.util.List;

public class PromptService {
    private static final String DEFAULT_OPENAI_MODEL = "gpt-3.5-turbo";
    private OpenAiService openAiService = null;
    private String openAiModel = DEFAULT_OPENAI_MODEL;

    public PromptService(String token) {
        this.openAiService = new OpenAiService(token);
    }


    public PromptService(String token, String model) {
        this.openAiService = new OpenAiService(token);
        this.openAiModel = model;
    }

    public String executeWithContext(String context, String prompt) throws Exception {

        ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest
                .builder()
                .model(openAiModel)
                .temperature(1.0)
                .maxTokens(200)
                .messages(
                        List.of(
                                new ChatMessage("system", context),
                                new ChatMessage("user", prompt)
                        )).build();

        List<ChatCompletionChoice> choices = openAiService.createChatCompletion(chatCompletionRequest)
                .getChoices();


        if (choices.isEmpty()) {
            throw new Exception("Empty response");
        }

        return choices.get(0).getMessage().getContent();

    }

    public String executePrompt(String prompt) throws Exception {

        ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest
                .builder()
                .model(openAiModel)
                .temperature(1.0)
                .maxTokens(200)
                .messages(
                        List.of(
                                new ChatMessage("user", prompt)
                        )).build();

        List<ChatCompletionChoice> choices = openAiService.createChatCompletion(chatCompletionRequest)
                .getChoices();


        if (choices.isEmpty()) {
            throw new Exception("Empty response");
        }

        return choices.get(0).getMessage().getContent();

    }



}
