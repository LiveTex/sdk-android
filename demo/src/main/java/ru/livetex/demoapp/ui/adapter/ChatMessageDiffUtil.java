package ru.livetex.demoapp.ui.adapter;

import java.util.ArrayList;
import java.util.List;

import androidx.recyclerview.widget.DiffUtil;
import ru.livetex.demoapp.db.entity.ChatMessage;

public final class ChatMessageDiffUtil extends DiffUtil.Callback {

	private final List<ChatItem> oldList = new ArrayList<>();
	private final List<ChatItem> newList = new ArrayList<>();

	public ChatMessageDiffUtil(List<ChatItem> oldList, List<ChatItem> newList) {
		this.oldList.addAll(oldList);
		this.newList.addAll(newList);
	}

	@Override
	public int getOldListSize() {
		return oldList.size();
	}

	@Override
	public int getNewListSize() {
		return newList.size();
	}

	@Override
	public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
		ChatItem oldProduct = oldList.get(oldItemPosition);
		ChatItem newProduct = newList.get(newItemPosition);
		return oldProduct.id.equals(newProduct.id);
	}

	@Override
	public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
		ChatItem oldProduct = oldList.get(oldItemPosition);
		ChatItem newProduct = newList.get(newItemPosition);
		return oldProduct.equals(newProduct);
	}
}
