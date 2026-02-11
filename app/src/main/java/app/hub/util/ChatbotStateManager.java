package app.hub.util;

import com.servicehub.model.Message;
import java.util.ArrayList;
import java.util.List;

/**
 * Singleton class to manage chatbot conversation state across bottom sheet dismissals.
 * Preserves message history so users can continue conversations when reopening the chatbot.
 */
public class ChatbotStateManager {
    private static ChatbotStateManager instance;
    private final List<Message> messageHistory;

    private ChatbotStateManager() {
        messageHistory = new ArrayList<>();
    }

    /**
     * Get the singleton instance of ChatbotStateManager.
     * Thread-safe implementation using synchronized block.
     */
    public static synchronized ChatbotStateManager getInstance() {
        if (instance == null) {
            instance = new ChatbotStateManager();
        }
        return instance;
    }

    /**
     * Get a copy of the message history to prevent external modification.
     */
    public List<Message> getMessageHistory() {
        return new ArrayList<>(messageHistory);
    }

    /**
     * Add a message to the conversation history.
     */
    public void addMessage(Message message) {
        if (message != null) {
            messageHistory.add(message);
        }
    }

    /**
     * Clear all messages from the conversation history.
     */
    public void clearHistory() {
        messageHistory.clear();
    }

    /**
     * Check if there are any messages in the history.
     */
    public boolean hasHistory() {
        return !messageHistory.isEmpty();
    }
}
