package ChatAppPOE;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Pattern;

public class Message {

    private static final int MAX_MESSAGE_LENGTH = 250;

    // South African international format: +27 followed by nine digits.
    // See the Oracle Pattern API reference in the class documentation.
    private static final Pattern RECIPIENT_PATTERN =
            Pattern.compile("^\\+27[0-9]{9}$");

    private static final List<Message> SESSION_MESSAGES = new ArrayList<>();
    private static final Set<String> GENERATED_MESSAGE_IDS = new HashSet<>();
    private static int totalMessagesSent = 0;
    private static Path storageFile = Paths.get("messages.json");

    private final String messageID;
    private final int messageNumber;
    private final String recipient;
    private final String messageText;
    private final String messageHash;

    private MessageStatus status = MessageStatus.PENDING;
    private boolean addedToSession = false;

    private enum MessageStatus {
        PENDING,
        SENT,
        STORED,
        DISCARDED
    }

    /** Creates a message with a randomly generated ten-digit message ID. */
    public Message(int messageNumber, String recipient, String messageText) {
        this(generateMessageID(), messageNumber, recipient, messageText);
    }

    /**
     * Creates a message using a supplied ID. The ID constructor is useful for
     * deterministic unit tests; the console application uses the random-ID
     * constructor above.
     */
    public Message(String messageID, int messageNumber,
                   String recipient, String messageText) {
        this.messageID = messageID == null ? "" : messageID.trim();
        this.messageNumber = messageNumber;
        this.recipient = recipient == null ? "" : recipient.trim();
        this.messageText = messageText == null ? "" : messageText.trim();
        this.messageHash = buildMessageHash();
    }

    private static String generateMessageID() {
        // Values range from 1,000,000,000 through 9,999,999,999 (ten digits).
        // The set prevents duplicate IDs during this application session.
        while (true) {
            long candidate = ThreadLocalRandom.current().nextLong(
                    1_000_000_000L, 10_000_000_000L
            );
            String id = Long.toString(candidate);
            synchronized (GENERATED_MESSAGE_IDS) {
                if (GENERATED_MESSAGE_IDS.add(id)) {
                    return id;
                }
            }
        }
    }

    /** Ensures that a message ID contains no more than ten characters. */
    public boolean checkMessageID() {
        return !messageID.isEmpty() && messageID.length() <= 10;
    }

    /**
     * Validates the recipient number and returns the exact confirmation or
     * error message required by the Part 2 specification.
     */
    public String checkRecipientCell() {
        if (RECIPIENT_PATTERN.matcher(recipient).matches()) {
            return "Cell phone number successfully captured.";
        }

        return "Cell phone number is incorrectly formatted or does not contain "
                + "an international code. Please correct the number and try again.";
    }

    /** Returns the Part 2 message-length result, including the excess length. */
    public String checkMessageLength() {
        if (messageText.length() <= MAX_MESSAGE_LENGTH) {
            return "Message ready to send.";
        }

        int excessCharacters = messageText.length() - MAX_MESSAGE_LENGTH;
        return "Message exceeds 250 characters by " + excessCharacters
                + "; please reduce the size.";
    }

    /**
     * Creates a hash in the format first-two-ID-characters:number:first+last
     * words. The word portion is uppercase and punctuation at the word edges
     * is removed, which produces HITONIGHT for the supplied Part 2 test case.
     */
    public String createMessageHash() {
        return buildMessageHash();
    }

    private String buildMessageHash() {
        String idPrefix;
        if (messageID.length() >= 2) {
            idPrefix = messageID.substring(0, 2);
        } else {
            idPrefix = ("00" + messageID).substring(messageID.length());
        }

        String firstWord = "";
        String lastWord = "";
        String trimmedText = messageText.trim();

        if (!trimmedText.isEmpty()) {
            String[] words = trimmedText.split("\\s+");
            for (String word : words) {
                String cleanedWord = word.replaceAll("[^\\p{L}\\p{N}]", "");
                if (!cleanedWord.isEmpty()) {
                    if (firstWord.isEmpty()) {
                        firstWord = cleanedWord;
                    }
                    lastWord = cleanedWord;
                }
            }
        }

        String wordHash = (firstWord + lastWord).toUpperCase(Locale.ROOT);
        return idPrefix + ":" + messageNumber + ":" + wordHash;
    }

    /**
     * Interactive form required by the brief. The overload taking a choice is
     * used by QuickChat and by unit tests so the decision logic is testable.
     */
    public String SentMessage() {
        java.util.Scanner scanner = new java.util.Scanner(System.in);
        System.out.println("1) Send Message");
        System.out.println("2) Disregard Message");
        System.out.println("3) Store Message to send later");
        System.out.print("Choose an option: ");
        return SentMessage(scanner.nextLine());
    }

    /** Processes choices 1/send, 2/disregard, or 3/store. */
    public String SentMessage(String choice) {
        if (choice == null) {
            return "Invalid message option. Please select 1, 2, or 3.";
        }

        String selectedOption = choice.trim().toLowerCase(Locale.ROOT);

        if (selectedOption.equals("1")
                || selectedOption.equals("send")
                || selectedOption.equals("send message")) {
            return sendMessage();
        }

        if (selectedOption.equals("2")
                || selectedOption.equals("0")
                || selectedOption.equals("disregard")
                || selectedOption.equals("discard")
                || selectedOption.equals("disregard message")) {
            if (status != MessageStatus.PENDING) {
                return "This message has already been processed.";
            }
            status = MessageStatus.DISCARDED;
            addToSessionOnce();
            return "Press 0 to delete the message.";
        }

        if (selectedOption.equals("3")
                || selectedOption.equals("store")
                || selectedOption.equals("store message")
                || selectedOption.equals("store to send later")) {
            return storeMessage();
        }

        return "Invalid message option. Please select 1, 2, or 3.";
    }

    private String sendMessage() {
        if (status != MessageStatus.PENDING) {
            return "This message has already been processed.";
        }
        if (!checkMessageID()) {
            return "Message ID must be between one and ten characters long.";
        }
        String lengthResult = checkMessageLength();
        if (!"Message ready to send.".equals(lengthResult)) {
            return lengthResult;
        }
        String recipientResult = checkRecipientCell();
        if (!"Cell phone number successfully captured.".equals(recipientResult)) {
            return recipientResult;
        }

        status = MessageStatus.SENT;
        totalMessagesSent++;
        addToSessionOnce();
        return "Message successfully sent.";
    }

    /** Stores this message as JSON in messages.json, or the configured path. */
    public String storeMessage() {
        if (status != MessageStatus.PENDING) {
            return "This message has already been processed.";
        }
        if (!checkMessageID()) {
            return "Message ID must be between one and ten characters long.";
        }
        String lengthResult = checkMessageLength();
        if (!"Message ready to send.".equals(lengthResult)) {
            return lengthResult;
        }
        String recipientResult = checkRecipientCell();
        if (!"Cell phone number successfully captured.".equals(recipientResult)) {
            return recipientResult;
        }

        status = MessageStatus.STORED;
        try {
            appendStoredMessageToJson();
            addToSessionOnce();
            return "Message successfully stored.";
        } catch (IOException exception) {
            status = MessageStatus.PENDING;
            return "Message could not be stored. Please check file permissions and try again.";
        }
    }

    /** Returns the full details of each successfully sent message this session. */
    public String printMessages() {
        StringBuilder output = new StringBuilder();
        synchronized (SESSION_MESSAGES) {
            for (Message message : SESSION_MESSAGES) {
                if (message.status == MessageStatus.SENT) {
                    if (output.length() > 0) {
                        output.append(System.lineSeparator());
                    }
                    output.append(message.getFullDetails());
                }
            }
        }

        if (output.length() == 0) {
            return "No messages have been sent yet.";
        }
        return output.toString();
    }

    /** Returns the total number of messages successfully sent this session. */
    public int returnTotalMessagess() {
        return getTotalMessagesSent();
    }

    public static int getTotalMessagesSent() {
        return totalMessagesSent;
    }

    public String getMessageID() {
        return messageID;
    }

    public int getMessageNumber() {
        return messageNumber;
    }

    public String getRecipient() {
        return recipient;
    }

    public String getMessageText() {
        return messageText;
    }

    public String getMessageHash() {
        return messageHash;
    }

    public String getStatus() {
        return status.name();
    }

    public boolean isCompleted() {
        return status != MessageStatus.PENDING;
    }

    /** Displays details in the order required by the Part 2 brief. */
    public String getFullDetails() {
        return "Message ID: " + messageID + System.lineSeparator()
                + "Message Hash: " + messageHash + System.lineSeparator()
                + "Recipient: " + recipient + System.lineSeparator()
                + "Message: " + messageText;
    }

    private void addToSessionOnce() {
        if (!addedToSession) {
            synchronized (SESSION_MESSAGES) {
                SESSION_MESSAGES.add(this);
            }
            addedToSession = true;
        }
    }

    private void appendStoredMessageToJson() throws IOException {
        Path absolutePath = storageFile.toAbsolutePath();
        Path parent = absolutePath.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }

        String existingJson = Files.exists(absolutePath)
                ? Files.readString(absolutePath, StandardCharsets.UTF_8).trim()
                : "";
        String jsonObject = toJsonObject();
        String updatedJson;

        if (existingJson.isEmpty() || "[]".equals(existingJson)) {
            updatedJson = "[\n" + indentJson(jsonObject) + "\n]\n";
        } else if (existingJson.startsWith("[") && existingJson.endsWith("]")) {
            String body = existingJson.substring(1, existingJson.length() - 1)
                    .stripTrailing();
            if (body.trim().isEmpty()) {
                updatedJson = "[\n" + indentJson(jsonObject) + "\n]\n";
            } else {
                updatedJson = "[" + body + ",\n" + indentJson(jsonObject)
                        + "\n]\n";
            }
        } else {
            throw new IOException("Existing JSON file is not a JSON array.");
        }

        Files.writeString(absolutePath, updatedJson, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING,
                StandardOpenOption.WRITE);
    }

    private String toJsonObject() {
        return "{\n"
                + "  \"messageID\": \"" + escapeJson(messageID) + "\",\n"
                + "  \"messageNumber\": " + messageNumber + ",\n"
                + "  \"recipient\": \"" + escapeJson(recipient) + "\",\n"
                + "  \"message\": \"" + escapeJson(messageText) + "\",\n"
                + "  \"messageHash\": \"" + escapeJson(messageHash) + "\",\n"
                + "  \"status\": \"" + status.name() + "\"\n"
                + "}";
    }

    private static String indentJson(String json) {
        return "  " + json.replace("\n", "\n  ");
    }

    /** Escapes control characters and quotation marks for valid JSON strings. */
    private static String escapeJson(String value) {
        StringBuilder escaped = new StringBuilder();
        for (int index = 0; index < value.length(); index++) {
            char character = value.charAt(index);
            switch (character) {
                case '"':
                    escaped.append("\\\"");
                    break;
                case '\\':
                    escaped.append("\\\\");
                    break;
                case '\b':
                    escaped.append("\\b");
                    break;
                case '\f':
                    escaped.append("\\f");
                    break;
                case '\n':
                    escaped.append("\\n");
                    break;
                case '\r':
                    escaped.append("\\r");
                    break;
                case '\t':
                    escaped.append("\\t");
                    break;
                default:
                    if (character < 0x20) {
                        escaped.append(String.format("\\u%04x", (int) character));
                    } else {
                        escaped.append(character);
                    }
                    break;
            }
        }
        return escaped.toString();
    }

    // Package-private test helpers; these do not affect normal application use.
    static void resetSessionForTesting() {
        synchronized (SESSION_MESSAGES) {
            SESSION_MESSAGES.clear();
        }
        totalMessagesSent = 0;
        synchronized (GENERATED_MESSAGE_IDS) {
            GENERATED_MESSAGE_IDS.clear();
        }
        storageFile = Paths.get("messages.json");
    }

    static void setStorageFilePathForTesting(Path path) {
        storageFile = path;
    }
}
