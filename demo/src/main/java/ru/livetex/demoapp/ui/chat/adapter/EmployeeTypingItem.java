package ru.livetex.demoapp.ui.chat.adapter;

import ru.livetex.demoapp.db.entity.ChatMessage;

public class EmployeeTypingItem extends ChatItem {

	public EmployeeTypingItem(ChatMessage message) {
		super(message);
	}

	@Override
	public ItemType getAdapterItemType() {
		return ItemType.EMPLOYEE_TYPING;
	}
}