package ru.livetex.demoapp.ui.adapter;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import com.bumptech.glide.Glide;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import io.reactivex.functions.Consumer;
import ru.livetex.demoapp.R;
import ru.livetex.demoapp.db.entity.MessageSentState;

public final class MessagesAdapter extends RecyclerView.Adapter {
	private static final int VIEW_TYPE_MESSAGE_INCOMING = 1;
	private static final int VIEW_TYPE_MESSAGE_OUTGOING = 2;
	private static final int VIEW_TYPE_FILE_INCOMING = 3;
	private static final int VIEW_TYPE_FILE_OUTGOING = 4;

	private List<ChatItem> messages = new ArrayList<>();
	@Nullable
	private Consumer<ChatItem> onMessageClickListener = null;

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
		} else if (viewType == VIEW_TYPE_FILE_INCOMING) {
			view = LayoutInflater.from(parent.getContext())
					.inflate(R.layout.i_chat_message_file_in, parent, false);
			return new IncomingFileHolder(view);
		} else if (viewType == VIEW_TYPE_FILE_OUTGOING) {
			view = LayoutInflater.from(parent.getContext())
					.inflate(R.layout.i_chat_message_file_out, parent, false);
			return new OutgoingFileHolder(view);
		}

		return null;
	}

	@Override
	public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
		final ChatItem message = messages.get(position);

		switch (holder.getItemViewType()) {
			case VIEW_TYPE_MESSAGE_INCOMING:
				((IncomingMessageHolder) holder).bind(message);
				break;
			case VIEW_TYPE_MESSAGE_OUTGOING:
				((OutgoingMessageHolder) holder).bind(message);
				break;
			case VIEW_TYPE_FILE_INCOMING:
				((IncomingFileHolder) holder).bind(message);
				break;
			case VIEW_TYPE_FILE_OUTGOING:
				((OutgoingFileHolder) holder).bind(message);
				break;
		}

		if (onMessageClickListener != null) {
			holder.itemView.setOnClickListener(view -> {
				try {
					onMessageClickListener.accept(message);
				} catch (Exception e) {
					e.printStackTrace();
				}
			});
		}
	}

	@Override
	public int getItemViewType(int position) {
		ChatItem message = messages.get(position);

		if (!TextUtils.isEmpty(message.fileUrl)) {
			if (message.isIncoming) {
				return VIEW_TYPE_FILE_INCOMING;
			} else {
				return VIEW_TYPE_FILE_OUTGOING;
			}
		} else {
			if (message.isIncoming) {
				return VIEW_TYPE_MESSAGE_INCOMING;
			} else {
				return VIEW_TYPE_MESSAGE_OUTGOING;
			}
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

	public void setOnMessageClickListener(@NonNull Consumer<ChatItem> onMessageClickListener) {
		this.onMessageClickListener = onMessageClickListener;
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
			if (message.sentState == MessageSentState.FAILED) {
				messageText.setBackgroundResource(R.drawable.rounded_rectangle_red);
			} else {
				messageText.setBackgroundResource(R.drawable.rounded_rectangle_blue);
			}
		}
	}

	private static class IncomingFileHolder extends RecyclerView.ViewHolder {
		ImageView imageView;

		IncomingFileHolder(View itemView) {
			super(itemView);

			imageView = itemView.findViewById(R.id.image);
		}

		void bind(ChatItem message) {
			loadImage(message, imageView);
		}
	}

	private static class OutgoingFileHolder extends RecyclerView.ViewHolder {
		ImageView imageView;

		OutgoingFileHolder(View itemView) {
			super(itemView);

			imageView = itemView.findViewById(R.id.image);
		}

		void bind(ChatItem message) {
			loadImage(message, imageView);
			if (message.sentState == MessageSentState.FAILED) {
				imageView.setBackgroundResource(R.drawable.rounded_rectangle_red);
			} else {
				imageView.setBackgroundResource(R.drawable.rounded_rectangle_blue);
			}
		}
	}

	// todo: https://bumptech.github.io/glide/int/recyclerview.html
	private static void loadImage(ChatItem message, ImageView imageView) {
		Glide.with(imageView.getContext())
				.load(message.fileUrl)
				.placeholder(R.drawable.placeholder)
				.error(R.drawable.placeholder)
				.centerCrop()
				.dontAnimate()
				.into(imageView);
	}
}
