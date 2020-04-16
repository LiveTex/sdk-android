package ru.livetex.demoapp.db;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import io.reactivex.subjects.BehaviorSubject;
import ru.livetex.demoapp.db.entity.ChatMessage;

/**
 * In real projects for persistent storage it's recommended to use DB (like Room)
 */
public class ChatState {

	public final static ChatState instance = new ChatState();

	private BehaviorSubject<List<ChatMessage>> messagesSubject = BehaviorSubject.createDefault(new ArrayList<>());

	public BehaviorSubject<List<ChatMessage>> messages() {
		return messagesSubject;
	}

	public synchronized void addMessages(List<ChatMessage> messages) {
		messagesSubject.getValue().addAll(messages);
		messagesSubject.onNext(messagesSubject.getValue());
	}

	public synchronized void addMessage(ChatMessage message) {
		messagesSubject.getValue().add(message);
		messagesSubject.onNext(messagesSubject.getValue());
	}

	/**
	 * Create local message with fake id (will be overriden by server)
	 * // todo: id scheme need improvement. now no way to distinguish between sent and local messages (fake and not fake id)
	 */
	public ChatMessage createNewMessage(String text) {
		ChatMessage chatMessage = new ChatMessage(
				UUID.randomUUID().toString(),
				text,
				new Date(),
				false
		);
		addMessage(chatMessage);
		return chatMessage;
	}
}
