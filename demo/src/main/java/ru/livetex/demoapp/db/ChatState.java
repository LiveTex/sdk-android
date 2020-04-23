package ru.livetex.demoapp.db;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;
import ru.livetex.demoapp.db.entity.ChatMessage;

/**
 * In real projects for persistent storage it's recommended to use DB (like Room)
 */
public final class ChatState {

	public final static ChatState instance = new ChatState();

	private BehaviorSubject<Set<ChatMessage>> messagesSubject = BehaviorSubject.createDefault(new HashSet<>());

	public Observable<List<ChatMessage>> messages() {
		return messagesSubject.map(ArrayList::new);
	}

	public synchronized void addMessages(List<ChatMessage> messages) {
		messagesSubject.getValue().addAll(messages);
		messagesSubject.onNext(messagesSubject.getValue());
	}

	public synchronized void addMessage(ChatMessage message) {
		messagesSubject.getValue().add(message);
		messagesSubject.onNext(messagesSubject.getValue());
	}

	public synchronized void removeMessage(ChatMessage message) {
		messagesSubject.getValue().remove(message);
		//messagesSubject.onNext(messagesSubject.getValue());
	}

	/**
	 * Create local message with fake id (will be overriden by server)
	 * // todo: id scheme need improvement. now no way to distinguish between sent and local messages (fake and not fake id)
	 */
	public synchronized ChatMessage createNewMessage(String text) {
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
