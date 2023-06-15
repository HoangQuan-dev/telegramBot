package com.example.registerbot;

import com.example.registerbot.TelegramBot.regisBot;
import jakarta.mail.MessagingException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@SpringBootApplication
public class RegisterBotApplication {
    public static void main(String[] args) throws MessagingException {
        SpringApplication.run(RegisterBotApplication.class, args);

        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(new regisBot());
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
