package ru.livetex.demoapp.ui.adapter;

import java.util.ArrayList;
import java.util.List;

import androidx.recyclerview.widget.DiffUtil;

public final class ChatMessageDiffUtil extends DiffUtil.Callback {

	private final List<AdapterItem> oldList = new ArrayList<>();
	private final List<AdapterItem> newList = new ArrayList<>();

	public ChatMessageDiffUtil(List<AdapterItem> oldList, List<AdapterItem> newList) {
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
		AdapterItem oldProduct = oldList.get(oldItemPosition);
		AdapterItem newProduct = newList.get(newItemPosition);

		if (oldProduct.getAdapterItemType() == newProduct.getAdapterItemType()) {
			if (oldProduct.getAdapterItemType() == ItemType.CHAT_MESSAGE) {
				return ((ChatItem)oldProduct).id.equals(((ChatItem)newProduct).id);
			} else if (oldProduct.getAdapterItemType() == ItemType.DATE) {
				return ((DateItem)oldProduct).text.equals(((DateItem)newProduct).text);
			}
		} else
			return false;

		return oldProduct == newProduct;
	}

	@Override
	public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
		AdapterItem oldProduct = oldList.get(oldItemPosition);
		AdapterItem newProduct = newList.get(newItemPosition);
		// here should be checked only variables which affect UI
		return oldProduct.equals(newProduct);
	}
}
