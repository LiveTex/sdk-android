package ru.livetex.demoapp.db;

import java.util.ArrayList;
import java.util.List;

import ru.livetex.demoapp.db.entity.ChatMessage;
import ru.livetex.sdk.entity.Employee;
import ru.livetex.sdk.entity.FileMessage;
import ru.livetex.sdk.entity.TextMessage;

/**
 * Transform server entities to DB and vice versa
 */
public class Mapper {

	public static List<ChatMessage> toChatMessages(List<TextMessage> textMessages) {
		List<ChatMessage> messages = new ArrayList<>();
		for (TextMessage textMessage : textMessages) {
			messages.add(toChatMessage(textMessage));
		}
		return messages;
	}

	public static ChatMessage toChatMessage(TextMessage textMessage) {
		ChatMessage chatMessage = new ChatMessage(textMessage.id,
				textMessage.content,
				textMessage.createdAt,
				textMessage.creator instanceof Employee,
				textMessage.creator
				);
		return chatMessage;
	}

	public static ChatMessage toChatMessage(FileMessage fileMessage) {
		ChatMessage chatMessage = new ChatMessage(fileMessage.id,
				fileMessage.name,
				fileMessage.createdAt,
				fileMessage.creator instanceof Employee,
				fileMessage.url,
				fileMessage.creator
		);
		return chatMessage;
	}
}
