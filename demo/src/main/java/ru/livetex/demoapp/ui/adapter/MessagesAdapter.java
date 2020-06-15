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
import com.bumptech.glide.request.RequestOptions;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import io.reactivex.functions.Consumer;
import ru.livetex.demoapp.R;
import ru.livetex.demoapp.db.ChatState;

public final class MessagesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
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
		TextView messageView;
		ImageView avatarView;
		TextView nameView;

		IncomingMessageHolder(View itemView) {
			super(itemView);

			nameView = itemView.findViewById(R.id.nameView);
			messageView = itemView.findViewById(R.id.text);
			avatarView = itemView.findViewById(R.id.avatarView);
		}

		void bind(ChatItem message) {
			messageView.setText(message.content);

			// For better implementation see https://bumptech.github.io/glide/int/recyclerview.html
			String avatarUrl = null;
			String opName = null;
			if (ChatState.instance.getDialogState() != null && ChatState.instance.getDialogState().employee != null) {
				avatarUrl = ChatState.instance.getDialogState().employee.avatarUrl;
				opName = ChatState.instance.getDialogState().employee.name;
			}

			if (!TextUtils.isEmpty(avatarUrl)) {
				Glide.with(avatarView.getContext())
						.load(avatarUrl)
						.placeholder(R.drawable.logo)
						.error(R.drawable.logo)
						.centerCrop()
						.dontAnimate()
						.apply(RequestOptions.circleCropTransform())
						.into(avatarView);
			} else {
				avatarView.setImageResource(R.drawable.logo);
			}

			nameView.setText(opName);
		}
	}

	private static class OutgoingMessageHolder extends RecyclerView.ViewHolder {
		TextView messageView;

		OutgoingMessageHolder(View itemView) {
			super(itemView);

			messageView = itemView.findViewById(R.id.text);
		}

		void bind(ChatItem message) {
			messageView.setText(message.content);
			messageView.setBackgroundResource(R.drawable.rounded_rectangle_blue);
		}
	}

	private static class IncomingFileHolder extends RecyclerView.ViewHolder {
		ImageView imageView;
		ImageView avatarView;
		TextView nameView;

		IncomingFileHolder(View itemView) {
			super(itemView);

			imageView = itemView.findViewById(R.id.image);
			nameView = itemView.findViewById(R.id.nameView);
			avatarView = itemView.findViewById(R.id.avatarView);
		}

		void bind(ChatItem message) {
			loadImage(message, imageView);

			// For better implementation see https://bumptech.github.io/glide/int/recyclerview.html
			String avatarUrl = null;
			String opName = null;
			if (ChatState.instance.getDialogState() != null && ChatState.instance.getDialogState().employee != null) {
				avatarUrl = ChatState.instance.getDialogState().employee.avatarUrl;
				opName = ChatState.instance.getDialogState().employee.name;
			}

			if (!TextUtils.isEmpty(avatarUrl)) {
				Glide.with(avatarView.getContext())
						.load(avatarUrl)
						.placeholder(R.drawable.logo)
						.error(R.drawable.logo)
						.centerCrop()
						.dontAnimate()
						.apply(RequestOptions.circleCropTransform())
						.into(avatarView);
			} else {
				avatarView.setImageResource(R.drawable.logo);
			}

			nameView.setText(opName);
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
			imageView.setBackgroundResource(R.drawable.rounded_rectangle_blue);
		}
	}

	// For better implementation see https://bumptech.github.io/glide/int/recyclerview.html
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
