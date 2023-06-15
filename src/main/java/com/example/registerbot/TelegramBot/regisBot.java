package com.example.registerbot.TelegramBot;

import com.example.registerbot.Model.UserRegistration;
import com.example.registerbot.Service.EmailVerificationService;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import jakarta.mail.MessagingException;
import org.bson.Document;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import static com.example.registerbot.Model.connectDB.mongoUri;


public class regisBot extends TelegramLongPollingBot {

    //region Telegram Bot ConnectDB
    static MongoClientURI uri = new MongoClientURI(mongoUri);
    static MongoClient client = new MongoClient(uri);
    static MongoDatabase database = client.getDatabase("testDB");
    static MongoCollection<Document> userCollection = database.getCollection(UserRegistration.USER_REGISTRATION);

    //endregion
    public static String chatID;
    public static UserRegistration userRegis = new UserRegistration();

    @Override
    public String getBotToken() {
        return "6279609151:AAGDMMPeSNB5Zws5n1kfGfRbxYTVdTN6DtM"; //6279609151:AAGDMMPeSNB5Zws5n1kfGfRbxYTVdTN6DtM
    }

    @Override
    public String getBotUsername() {
        return "regiistBot";
    } //regiistBot

    @Override
    public void onUpdateReceived(Update update) {
        SendMessage sendMessage = new SendMessage();
        if (update.hasMessage() && update.getMessage().hasText()) {
            chatID = update.getMessage().getChatId().toString();
            sendMessage.setChatId(chatID);

            if (update.getMessage().getText().equals("/start")) {
                sendMessage.setText("Welcome to Psoft Bot. Please choose register method below\uD83D\uDE4C");
                String action = "start";
                InlineKeyboardButton(sendMessage, action);
            }

            else if (update.getMessage().getText().contains("@"))
            {
                try {
                    if (isValidEmail(update.getMessage().getText())) {
                        sendMessage.setText("Verification link has sent to your email.");
                    } else {
                        sendMessage.setText("❌Invalid format. Please try again.");
                    }
                } catch (MessagingException e) {
                    throw new RuntimeException(e);
                }
            }

            else if (update.getMessage().getText().contains("https") || update.getMessage().getText().contains("www.") )
            {
                if (isValidDomain(update.getMessage().getText()))
                {
                    sendMessage.setText("Need to send picture of Domain Certificate for verification");
                }
                else
                {
                    sendMessage.setText("Domain is invalid. Please try again.");
                }
            }
        }

        //Callback Query From InlineKeyboardButton
        else if(update.hasCallbackQuery()) {
            //Message Callback Query
            Message message = update.getCallbackQuery().getMessage();
            CallbackQuery callbackQuery = update.getCallbackQuery();
            String data = callbackQuery.getData();

            //Send Message Telegram Bot
            sendMessage.setChatId(message.getChatId());
            sendMessage.setParseMode(ParseMode.MARKDOWN);

            if (data.equals("registerEmail")) {
                sendMessage.setText("Please give us your business email for registering. \n" +
                        "Following this format : example@yoursite.com");
            }

            else if (data.equals("registerDomain")) {
                sendMessage.setText("Please give us your website domain." +
                        "Following this format : [www.*******.***] or [https://******.***]");
            }

        }

        //Check if the message is photo
        else if (update.getMessage().hasPhoto()) {
            sendMessage.setChatId(chatID);
            sendMessage.setText("Your domain certificate has been received. Please wait for verification");
        }

        //If the message is not photo, reply error message
        else if (update.hasMessage() != update.getMessage().hasText() || update.hasMessage() != update.getMessage().hasPhoto()) {
            sendMessage.setChatId(chatID);
            sendMessage.setText("Domain certificate is invalid. Please try again");
        }

        try {
            executeAsync(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void InlineKeyboardButton(SendMessage sendMessage, String action) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> inlineButtons = new ArrayList<>();
        List<InlineKeyboardButton> inlineKeyboardButtonList = new ArrayList<>();

        if (action.equals("start"))
        {
            InlineKeyboardButton email = new InlineKeyboardButton();
            InlineKeyboardButton domain = new InlineKeyboardButton();
            email.setText("Email business");
            domain.setText("Domain");
            email.setCallbackData("registerEmail");
            domain.setCallbackData("registerDomain");

            inlineKeyboardButtonList.add(email);
            inlineKeyboardButtonList.add(domain);
        }

        inlineButtons.add(inlineKeyboardButtonList);
        inlineKeyboardMarkup.setKeyboard(inlineButtons);
        sendMessage.setReplyMarkup(inlineKeyboardMarkup);
    }

    public static boolean isValidEmail(String email) throws MessagingException {
        Pattern emailPattern = Pattern.compile("^[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\\.[a-zA-Z]{2,6}$");
        Matcher emailMatcher = emailPattern.matcher(email);

        if (emailMatcher.matches()) {
            //Check if the email is exists
            if (userCollection.find(Filters.eq("email", email)).first() != null) {
                userRegis.setEmail(email);
                userRegis.setCreatedAt(LocalDateTime.now());
                userRegis.setActivated(false);
                userCollection.replaceOne(Filters.eq("email", email), userRegis);
                userCollection.replaceOne(Filters.eq("createdAt", LocalDateTime.now()), userRegis);
                userCollection.replaceOne(Filters.eq("activated", false), userRegis);

                EmailVerificationService emailVerificationService = new EmailVerificationService();
                emailVerificationService.sendHtmlEmail(email);
            }

            //If expiryTime is expired, user verify again
            else {
                EmailVerificationService emailVerificationService = new EmailVerificationService();
                emailVerificationService.sendHtmlEmail(email);

                userRegis.setId(UUID.randomUUID().toString());
                userRegis.setEmail(email);
                userRegis.setCreatedAt(LocalDateTime.now());
                userRegis.setActivated(false);

                userCollection.insertOne(userRegis);
            }
            return true;
        }
        return false;
    }

    public static void isActivated(String email) {
        regisBot bot = new regisBot();
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatID);

        long count = userCollection.countDocuments(Filters.and(Filters.eq("email", email), Filters.eq("activated", true)));

        if (count > 0) {
            sendMessage.setText("You now have been registered. Thank you for your time with us!");
        }
        else {
            sendMessage.setText("Your token has expired. Please verify again!");
        }

        try {
            bot.executeAsync(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public static boolean isValidDomain(String domain) {
        Pattern domainPattern_https = Pattern.compile("^(https?):\\/\\/[.a-z]+((?:[a-z\\d](?:[a-z\\d-]{0,63}[a-z\\d])?|\\*)\\.)+[a-z\\d][a-z\\d-]{0,63}[a-z\\d]");
        Matcher domainMatcher_https = domainPattern_https.matcher(domain);

        Pattern domainPattern_www = Pattern.compile("((?:[a-z\\d](?:[a-z\\d-]{0,63}[a-z\\d])?|\\*)\\.)+[a-z\\d][a-z\\d-]{0,63}[a-z\\d]");
        Matcher domainMatcher_www = domainPattern_www.matcher(domain);

        return domainMatcher_https.matches() || domainMatcher_www.matches();
    }

}
