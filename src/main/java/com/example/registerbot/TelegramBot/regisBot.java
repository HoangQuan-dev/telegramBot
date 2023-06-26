package com.example.registerbot.TelegramBot;

import com.cloudinary.*;
import com.cloudinary.utils.ObjectUtils;
import com.example.registerbot.Model.UserRegistration;
import com.example.registerbot.Service.EmailVerificationService;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import io.github.cdimascio.dotenv.Dotenv;
import jakarta.mail.MessagingException;
import org.bson.Document;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import static com.example.registerbot.Model.connectDB.mongoUri;


public class regisBot extends TelegramLongPollingBot {

    //region Telegram Bot ConnectDB
    static MongoClientURI uri = new MongoClientURI("mongodb+srv://quanphamlsc:quan_2002@chatbot.trqhh6o.mongodb.net/testDB");
    static MongoClient client = new MongoClient(uri);
    static MongoDatabase database = client.getDatabase("testDB");
    static MongoCollection<Document> userCollection = database.getCollection(UserRegistration.USER_REGISTRATION);

    //endregion
    public static String chatID;
    public static UserRegistration userRegis = new UserRegistration();
    public static String domainUrl;

    Dotenv dotenv = Dotenv.load();

    @Override
    public String getBotToken() {
        return "6279609151:AAGC-SlBJzlqq0W9RRo_gd2Xz-mNejFiKeo";
    }

    @Override
    public String getBotUsername() {
        return "regiistBot";
    }

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

            else if (update.getMessage().getText().equals("/newpage")) {
                sendMessage.setText("You are registering website advertisement." + "\nFirst, give us your <b>Website Logo</b> image");
                sendMessage.setParseMode(ParseMode.HTML);
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

            else if (update.getMessage().getText().contains("https") || update.getMessage().getText().contains("www.") ) {
                if (isValidDomain(update.getMessage().getText())) {
                    sendMessage.setText("Need to send picture of Domain Certificate for verification");
                } else {
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

            else if (data.contains("_cat"))
            {
                sendMessage.setText("Give us your <b>Website Tags</b>" + "\nFor example: #cloting, #jewelry, #lifestyle,...");
                sendMessage.setParseMode(ParseMode.HTML);
            }

            else if (data.startsWith("#"))
            {
                sendMessage.setText("Write <b>Description</b> about your website. Starts with: ' - '" +
                        "\n<i>For example: - Your website description...</i>");
                sendMessage.setParseMode(ParseMode.HTML);

            }

            else if (data.startsWith("-"))
            {
                sendMessage.setText("Write <b>Catalogue</b> of your website. Starts with: '_'" +
                        "\n<i>For example: _Your website catalogue...</i>");
            }

        }

        //Check if the message is photo
        else if (update.getMessage().hasPhoto()) {
//            String path = null;
            List<PhotoSize> photos = update.getMessage().getPhoto();
//            String fileId = Objects.requireNonNull(photos.stream().max(Comparator.comparing(PhotoSize::getFileSize))
//                    .orElse(null)).getFileId();
//            GetFile getFile = new GetFile();
//            getFile.setFileId(fileId);
//
//            try {
//                File file = execute(getFile);
//                path = String.valueOf(new URL("https://api.telegram.org/file/bot" + getBotToken() + "/" + file.getFilePath()));
//            }
//
//            catch (TelegramApiException e) {
//                e.printStackTrace();
//            } catch (MalformedURLException e) {
//                throw new RuntimeException(e);
//            }
//
//            Cloudinary cloudinary = new Cloudinary(dotenv.get("CLOUDINARY_URL"));
//            cloudinary.config.secure = true;
//
//            try
//            {
//                Map uploadResult = cloudinary.uploader().upload(path, ObjectUtils.emptyMap());
//                domainUrl = uploadResult.get("url").toString();
//                System.out.println(uploadResult.get("url"));
//            }
//
//            catch (IOException e)
//            {
//                throw new RuntimeException(e);
//            }

            PhotoSize photoSize = photos.get(0);
            int width = photoSize.getWidth();
            int height = photoSize.getHeight();

            sendMessage.setChatId(chatID);

            if (width == 90 || height == 51) {
                String action = "category";
                sendMessage.setText("Choose your <b>Website Category</b>");
                sendMessage.setParseMode(ParseMode.HTML);
                InlineKeyboardButton(sendMessage, action);
            }
            else
            {
                //sendMessage.setText("Your domain certificate has been received. Please wait for verification");
            }
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

        switch (action) {
            case "start" -> {
                InlineKeyboardButton email = new InlineKeyboardButton();
                InlineKeyboardButton domain = new InlineKeyboardButton();
                email.setText("Email business");
                domain.setText("Domain");
                email.setCallbackData("registerEmail");
                domain.setCallbackData("registerDomain");
                inlineKeyboardButtonList.add(email);
                inlineKeyboardButtonList.add(domain);
                inlineButtons.add(inlineKeyboardButtonList);
                inlineKeyboardMarkup.setKeyboard(inlineButtons);
                sendMessage.setReplyMarkup(inlineKeyboardMarkup);
            }
            case "category" -> {
                InlineKeyboardButton[] cats = new InlineKeyboardButton[10];
                int i, count = 0;
                for (i = 0; i < cats.length; i++) {
                    cats[i] = new InlineKeyboardButton();
                    cats[i].setText("cat" + i);
                }
                for (i = 0; i < cats.length; i++) {
                    switch (cats[i].getText()) {
                        case "cat0" -> {
                            cats[i].setText("E-commerce");
                            cats[i].setCallbackData("E-commerce_cat");
                        }
                        case "cat1" -> {
                            cats[i].setText("Business");
                            cats[i].setCallbackData("Business_cat");
                        }
                        case "cat2" -> {
                            cats[i].setText("Blogs");
                            cats[i].setCallbackData("Blogs_cat");
                        }
                        case "cat3" -> {
                            cats[i].setText("Portfolio");
                            cats[i].setCallbackData("Portfolio_cat");
                        }
                        case "cat4" -> {
                            cats[i].setText("Social media");
                            cats[i].setCallbackData("Social_media_cat");
                        }
                        case "cat5" -> {
                            cats[i].setText("Forum");
                            cats[i].setCallbackData("Forum_cat");
                        }
                        case "cat6" -> {
                            cats[i].setText("News and magazine");
                            cats[i].setCallbackData("News_and_magazine_cat");
                        }
                        case "cat7" -> {
                            cats[i].setText("Educational");
                            cats[i].setCallbackData("Educational_cat");
                        }
                        case "cat8" -> {
                            cats[i].setText("Service provider");
                            cats[i].setCallbackData("Service_provider_cat");
                        }
                        case "cat9" -> {
                            cats[i].setText("Entertainment");
                            cats[i].setCallbackData("Entertainment_cat");
                        }
                    }

                    inlineKeyboardButtonList.add(cats[i]);
                    count++;

                    if (count % 2 == 0) {
                        inlineButtons.add(inlineKeyboardButtonList);
                        inlineKeyboardMarkup.setKeyboard(inlineButtons);

                        //Create a new ArrayList for the next set of buttons
                        inlineKeyboardButtonList = new ArrayList<>();
                        sendMessage.setReplyMarkup(inlineKeyboardMarkup);
                    }
                }
            }
        }

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

        if (domainMatcher_https.matches() || domainMatcher_www.matches()) {
            userRegis.setId(UUID.randomUUID().toString());
            userRegis.setDomain(domain);
            userRegis.setDomainCertificate(domainUrl);
            userRegis.setCreatedAt(LocalDateTime.now());
            userCollection.insertOne(userRegis);
        }
        else
        {
            return false;
        }

        return domainMatcher_https.matches() || domainMatcher_www.matches();
    }

}
