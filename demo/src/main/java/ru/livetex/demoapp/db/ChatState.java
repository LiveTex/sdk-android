package ru.livetex.demoapp.db;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import androidx.annotation.Nullable;
import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;
import ru.livetex.demoapp.db.entity.ChatMessage;

/**
 * In real projects for persistent storage it's recommended to use DB (like Room)
 */
public final class ChatState {

	public final static ChatState instance = new ChatState();

	private Map<String, ChatMessage> messages = new ConcurrentHashMap<>();
	private BehaviorSubject<List<ChatMessage>> messagesSubject = BehaviorSubject.createDefault(Collections.emptyList());

	public Observable<List<ChatMessage>> messages() {
		return messagesSubject
				.map(Collections::unmodifiableList);
	}

	public synchronized void addMessages(List<ChatMessage> newMessages) {
		for (ChatMessage message : newMessages) {
			messages.put(message.id, message);
		}
		messagesSubject.onNext(new ArrayList<>(messages.values()));
	}

	public synchronized void addMessage(ChatMessage message) {
		messages.put(message.id, message);
		messagesSubject.onNext(new ArrayList<>(messages.values()));
	}

	public synchronized void removeMessage(String id) {
		messages.remove(id);
		//messagesSubject.onNext(new ArrayList<>(messages.values()));
	}

	public synchronized void updateMessage(ChatMessage message) {
		messages.remove(message.id);
		addMessage(message);
	}

	@Nullable
	public ChatMessage getMessage(String id) {
		return messages.get(id);
	}

	/**
	 * Create local message with fake id (will be overriden by server)
	 * // todo: id scheme need improvement. now no way to distinguish between sent and local messages (fake and not fake id)
	 */
	public synchronized ChatMessage createNewMessage(String text) {
		ChatMessage chatMessage = new ChatMessage(
				UUID.randomUUID().toString(),
				text,
				new Date()
		);
		addMessage(chatMessage);
		return chatMessage;
	}
}
