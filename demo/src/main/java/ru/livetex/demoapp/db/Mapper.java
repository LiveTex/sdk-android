package ru.livetex.demoapp.db;

import ru.livetex.demoapp.db.entity.ChatMessage;
import ru.livetex.sdk.entity.Employee;
import ru.livetex.sdk.entity.FileMessage;
import ru.livetex.sdk.entity.TextMessage;

/**
 * Transform server entities to DB variants
 */
public class Mapper {

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
