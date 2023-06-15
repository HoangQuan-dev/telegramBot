package com.example.registerbot.Controller;

import com.example.registerbot.Model.UserRegistration;
import com.example.registerbot.Service.EmailVerificationService;
import com.example.registerbot.TelegramBot.regisBot;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.sql.Timestamp;

@RestController
public class UserController {
    //region ConnectDB
    static MongoClientURI uri = new MongoClientURI("mongodb+srv://quanphamlsc:quan_2002@chatbot.trqhh6o.mongodb.net/testDB");
    static MongoClient client = new MongoClient(uri);
    static MongoDatabase database = client.getDatabase("testDB");
    static MongoCollection<Document> userCollection = database.getCollection(UserRegistration.USER_REGISTRATION);
    //endregion
    public static UserRegistration userRegis = new UserRegistration();

    @Autowired
    EmailVerificationService emailVerificationService;

    @GetMapping("/activation")
    public String activation(@RequestParam("email") String email, @RequestParam("token") String token, Model model) {
        Timestamp currentTimestamp = new Timestamp(System.currentTimeMillis());

        try {
            if (token == null) {
                return "Your verification token is invalid!";
            }

            else {

                if (currentTimestamp.after(emailVerificationService.expTimeInSec))
                {
                    regisBot.isActivated(email);
                    emailVerificationService.setExpTime(true);
                    return "Your token has expired. Please enter your email and verify again!";
                }

                    Document filter = new Document("email", email);
                    Update updateDB = new Update();
                    updateDB.set("activated", "true");

                    userRegis.setActivated(true);
                    Document update = new Document("$set", new Document("activated", true));
                    userCollection.updateOne(filter, update);
                    regisBot.isActivated(email);

                    return "Your account is activated";

            }

        }
        catch (Exception e)
        {
            return "Error: " + e.getMessage();
        }
    }

}
