package ru.livetex.demoapp.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import ru.livetex.demoapp.R;

public final class MessagesAdapter extends RecyclerView.Adapter {
	private static final int VIEW_TYPE_MESSAGE_INCOMING = 1;
	private static final int VIEW_TYPE_MESSAGE_OUTGOING = 2;

	private List<ChatItem> messages = new ArrayList<>();

	@NonNull
	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		View view;

		if (viewType == VIEW_TYPE_MESSAGE_INCOMING) {
			view = LayoutInflater.from(parent.getContext())
					.inflate(R.layout.i_chat_message_in, parent, false);
			return new IncomingMessageHolder(view);
		} else if (viewType == VIEW_TYPE_MESSAGE_OUTGOING) {
			view = LayoutInflater.from(parent.getContext())
					.inflate(R.layout.i_chat_message_out, parent, false);
			return new OutgoingMessageHolder(view);
		}

		return null;
	}

	@Override
	public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
		ChatItem message = messages.get(position);

		switch (holder.getItemViewType()) {
			case VIEW_TYPE_MESSAGE_INCOMING:
				((IncomingMessageHolder) holder).bind(message);
				break;
			case VIEW_TYPE_MESSAGE_OUTGOING:
				((OutgoingMessageHolder) holder).bind(message);
		}
	}

	@Override
	public int getItemViewType(int position) {
		ChatItem message = messages.get(position);

		if (message.isIncoming) {
			return VIEW_TYPE_MESSAGE_INCOMING;
		} else {
			return VIEW_TYPE_MESSAGE_OUTGOING;
		}
	}

	@Override
	public int getItemCount() {
		return messages.size();
	}

	public List<ChatItem> getData() {
		return messages;
	}

	public void setData(List<ChatItem> chatMessages) {
		this.messages.clear();
		this.messages.addAll(chatMessages);
	}

	private static class IncomingMessageHolder extends RecyclerView.ViewHolder {
		TextView messageText;

		IncomingMessageHolder(View itemView) {
			super(itemView);

			messageText = itemView.findViewById(R.id.text);
		}

		void bind(ChatItem message) {
			messageText.setText(message.content);
		}
	}

	private static class OutgoingMessageHolder extends RecyclerView.ViewHolder {
		TextView messageText;

		OutgoingMessageHolder(View itemView) {
			super(itemView);

			messageText = itemView.findViewById(R.id.text);
		}

		void bind(ChatItem message) {
			messageText.setText(message.content);
		}
	}
}
