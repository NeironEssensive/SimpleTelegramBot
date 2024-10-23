package org.example.demotelegrambot.service;

import com.vdurmont.emoji.EmojiParser;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.units.qual.K;
import org.example.demotelegrambot.config.BotConfig;
import org.example.demotelegrambot.model.User;
import org.example.demotelegrambot.model.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Component
@Slf4j
public class TelegramBot extends TelegramLongPollingBot {

    @Autowired
    private UserRepository userRepository;
    private final BotConfig config;
    static final String HELP_TEXT = "Хз ребят пока не придумал как вам помочь)" +
            "\n\nМожете потыкать все команды из меню" +
            "\n\n/mydata для просмотра информации о вас и о взаимодействии с ботом" +
            "\n/settings для настройки бота под ваш вкус" +
            "\n/randomjoke для поднятия настроения!" +
            "\n/roll для трушных мидеров" +
            "\n/authorsend (сообщение)  для отправки анонимного письма автору" +
            "\n\nСтарый бог, 52!";


    public TelegramBot(BotConfig config) {
        this.config = config;
        List<BotCommand> listOfCommands = new ArrayList<>();
        listOfCommands.add(new BotCommand("/start", "Начать работу с ботом"));
        listOfCommands.add(new BotCommand("/help", "Информация по использованию бота"));
        listOfCommands.add(new BotCommand("/mydata", "Информация о вас"));
        listOfCommands.add(new BotCommand("/settings", "Настройки"));
        listOfCommands.add(new BotCommand("/randomjoke", "Шуточки"));
        listOfCommands.add(new BotCommand("/roll", "Ну ролл)"));
        listOfCommands.add(new BotCommand("/authorsend", "Анонимно отправить сообщение автору"));

        try {
            this.execute(new SetMyCommands(listOfCommands, new BotCommandScopeDefault(), null));
        } catch (TelegramApiException e) {
            log.error("Ошибка настройки команд: " + e.getMessage());
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

        if (update.hasMessage() && update.getMessage().hasText()) {
            User user = new User();
            String message = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();
            String username = update.getMessage().getChat().getUserName();
            if(message.contains("/send") && config.getOwnerId()==chatId){
                var textToSend = message.substring(message.indexOf(" "));
                var users = userRepository.findAll();
                for(User user1 : users){
                    sendMessage(user1.getChatId(),textToSend);
                }
            }
            if(message.contains("/authorsend")){
                try {
                    var textToSend = message.substring(message.indexOf(" "));
                    sendMessage(1963457651, textToSend);
                    sendMessage(chatId, "Сообщение успешно доставлено");
                    log.info("@" + username + " отправил мне сообщение");
                }
                catch (StringIndexOutOfBoundsException e){
                    log.info("@" + username + " попытался отправить /authorSend");  
                    sendMessage(chatId, "Необходимо написать сообщение для отправки");
                    log.error(e.getMessage());
                }
            }
            else {
                switch (message) {
                    case "/start":
                        registerUser(update.getMessage());
                        startCommandReceived(chatId, update.getMessage().getChat().getFirstName());
                        log.info("@" + username + " начал работать с ботом");
                        break;
                    case "/help":
                        sendMessage(chatId, HELP_TEXT);
                        log.info("@" + username + " написал help");

                        break;
                    case "/mydata":
                        sendMessage(chatId, "Информация о вас\n" +
                                "chatId: " + chatId + "\n" +
                                "name: " + update.getMessage().getChat().getFirstName() + "\n" +
                                "username: " + username + "\n");
                        log.info("@" + username + " написал mydata");

                        break;
                    case "/randomjoke":
                        String uwuEmoji = EmojiParser.parseToUnicode(":pleading_face:");
                        String[] jokes = new String[]{"гойда!",
                                "Зверя нет сильней китайца старый бог соси нам яйца",
                                "Баращкэ))", "UwU" + uwuEmoji, "Вам снился этот человек?"};
                        Random rand = new Random();
                        int randomIndex = rand.nextInt(jokes.length);
                        sendMessage(chatId, jokes[randomIndex]);
                        log.info("@" + username + " зарандомил шутку");
                        break;
                    case "/roll":
                        roll(chatId);
                        log.info("@" + username + " заролил");
                        break;
                    default:
                        sendMessage(chatId, "Ты видел какие у меня огромные команды? ЙООУ??");
                        log.info("@" + username + " написал сообщение боту: " + message);
                }
            }
        } else if (update.hasCallbackQuery()) {
            String callBackData = update.getCallbackQuery().getData();
            long messageId = update.getCallbackQuery().getMessage().getMessageId();
            long chatId = update.getCallbackQuery().getMessage().getChatId();
            if (callBackData.equals("YES_BUTTON")) {
                Random tgBot = new Random();
                Random user = new Random();
                int tgBotNumber = tgBot.nextInt(101);
                int userNumber = user.nextInt(101);
                String botWin = "Число бота = " + tgBotNumber +
                        "\nТвое число = " + userNumber +
                        "\nTinyAirlines к вашим услугам";
                String userWin = "Число бота = " + tgBotNumber +
                        "\nТвое число = " + userNumber +
                        "\nДобро пожаловать на центральный коридор";

                EditMessageText message = new EditMessageText();
                message.setChatId(String.valueOf(chatId));
                if (tgBotNumber > userNumber) {
                    message.setText(botWin);
                } else {
                    message.setText(userWin);
                }
                message.setMessageId((int) messageId);
                try {
                    execute(message);
                } catch (TelegramApiException e) {
                    log.error("Поймана ошибка:" + e.getMessage());
                }
            } else if (callBackData.equals("NO_BUTTON")) {
                String text = "зассал)";
                EditMessageText message = new EditMessageText();
                message.setChatId(String.valueOf(chatId));
                message.setText(text);
                message.setMessageId((int) messageId);
                try {
                    execute(message);
                }
                catch (TelegramApiException e){
                    log.error("Поймана ошибка:" + e.getMessage());
                }
            }
        }
    }

    private void roll(long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("Ты действительно хочешь заролить число от 0 до 100?");
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();
        var yesButton = new InlineKeyboardButton();
        yesButton.setText("ЛетсГоу");
        yesButton.setCallbackData("YES_BUTTON");
        var noButton = new InlineKeyboardButton();
        noButton.setText("Ненене");
        noButton.setCallbackData("NO_BUTTON");
        rowInline.add(yesButton);
        rowInline.add(noButton);
        rowsInline.add(rowInline);
        markupInline.setKeyboard(rowsInline);
        message.setReplyMarkup(markupInline);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Поймана ошибка:" + e.getMessage());
        }
    }

    private void startCommandReceived(long chatId, String name) {
        String answer = EmojiParser.parseToUnicode(name + " писят два тебе от всех наших!\n52!" + ":clown:");
        log.info("Бот ответил пользователю: " + name);
        sendMessage(chatId, answer);
    }

    private void registerUser(Message msg) {
        if (userRepository.findById(msg.getChatId()).isEmpty()) {
            var chatId = msg.getChatId();
            var chat = msg.getChat();
            User user = new User();
            user.setChatId(chatId);
            user.setFirstName(chat.getFirstName());
            user.setLastName(chat.getLastName());
            user.setUsername(chat.getUserName());
            user.setRegisteredAt(new Timestamp(System.currentTimeMillis()));
            userRepository.save(user);
            log.info("Пользователь сохранен:" + user);
        }
    }

    public void spam() {
        for (int i = 0; i < 10000; i++) {
            sendMessage(881873848, "UWU");
        }
    }

    private void keyboard(SendMessage message) {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboardRows = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();
        row.add("/randomJoke)");
        row.add("/roll");
        keyboardRows.add(row);
        row = new KeyboardRow();
        row.add("/help");
        row.add("/mydata");
        row.add("/settings");
        keyboardRows.add(row);
        keyboardMarkup.setKeyboard(keyboardRows);
        message.setReplyMarkup(keyboardMarkup);
    }

    private void sendMessage(long chatId, String textToSend) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(textToSend);
        keyboard(message);
        try {
            execute(message);
        } catch (TelegramApiException e) {}
    }
}
