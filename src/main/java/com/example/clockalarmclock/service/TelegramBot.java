package com.example.clockalarmclock.service;

import com.example.clockalarmclock.config.BotConfig;
import com.example.clockalarmclock.model.Notification;
import com.example.clockalarmclock.model.NotificationRepository;
import com.example.clockalarmclock.model.User;
import com.example.clockalarmclock.model.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;


import java.sql.Timestamp;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class TelegramBot extends TelegramLongPollingBot {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    final BotConfig config;
    static final String HELP_TEXT = "This bot set alarm time. You can execute commands from the main menu on the left or by typing a command: \n\n" +
            "Type /start to see welcome message and registered you\n" +
            "Type /registr to registration for bot\n" +
            "Type /alarmtime set alarm time";

    public TelegramBot(BotConfig config){
        this.config = config;
        List<BotCommand> listOfCommands = new ArrayList<>();
        listOfCommands.add(new BotCommand("/help", "how to use this bot"));
        listOfCommands.add(new BotCommand("/start", "get a welcome message and registered you"));
        listOfCommands.add(new BotCommand("/delreg", "remove registration for bot"));
        listOfCommands.add(new BotCommand("/alarmtime", "set alarm time"));

        try {
            this.execute(new SetMyCommands(listOfCommands, new BotCommandScopeDefault(),null));
        }catch (TelegramApiException e){
            log.error("Error setting bot's command list");
        }
    }
    @Override
    public String getBotUsername() {
        return config.getBotName();
    }

    @Override
    public String getBotToken() {
        return config.getToken();
    }

    @Override
    public void onUpdateReceived(Update update) {
        if(update.hasMessage() && update.getMessage().hasText()){
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();

            if(update.getMessage().getText().contains("/alarmtime")){
                setAlarmTime(update.getMessage());
            }

            switch (messageText){

                case "/help":
                    sendMessage(chatId, HELP_TEXT);
                    break;
                case "/start":
                    registeredUser(update.getMessage());
                    startCommandRecieved(chatId, update.getMessage().getChat().getFirstName());
                    break;
                case "/delreg":
                    deleteRegistrationUser(update.getMessage());
                    break;

                default:
                    sendMessage(chatId, "Sorry, command was not recognized");
            }
        }
    }

    private void registeredUser(Message msg) {
        if(userRepository.findById(msg.getChatId()).isEmpty()){
            var chatId = msg.getChatId();
            var chat = msg.getChat();

            User user = new User();
            user.setChatId(chatId);
            user.setFirstName(chat.getFirstName());
            user.setLastName(chat.getLastName());
            user.setUserName(chat.getUserName());
            user.setRegisteredAt(new Timestamp(System.currentTimeMillis()));

            userRepository.save(user);
            log.info("user saved: " + user);
        }
    }

    private void deleteRegistrationUser(Message msg){
        if(userRepository.findById(msg.getChatId()).isEmpty()){
            sendMessage(msg.getChatId(), "Sorry, you are not registered");
        }else{
            User user = userRepository.findById(msg.getChatId()).get();
            userRepository.delete(user);
            sendMessage(msg.getChatId(), "Remove you registration");
        }
    }

    private void setAlarmTime(Message msg){
        String text = msg.getText().replace("/alarmtime", "");
        User user = userRepository.findById(msg.getChatId()).get();
        Notification notification = new Notification();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        notification.setTime(LocalTime.parse(text, formatter));
        notification.setUser(user);
    }

    private void startCommandRecieved(long chatId, String firstName) {
        String answer = "Hi," + firstName + ", nice to meet you!";

        sendMessage(chatId, answer);
    }

    private void sendMessage(long chatId, String textToSend) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(textToSend);

        try{
            execute(message);
        }catch (TelegramApiException e){
            //throw new TelegramApiException(e);
        }
    }

    @Scheduled(cron="0 * * * * *")
    private void sendNotification(){
        var notifications = notificationRepository.findAll();
        for(Notification notification : notifications){
            if(((Notification) ((ArrayList) notifications).get(0)).getTime().compareTo(LocalTime.from(LocalTime.now())) == 1){
                sendMessage(notification.getUser().getChatId(), "Alarm!!!!!");
            }
        }

    }
}
