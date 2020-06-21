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
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import io.reactivex.functions.Consumer;
import ru.livetex.demoapp.R;
import ru.livetex.demoapp.utils.DateUtils;
import ru.livetex.sdk.entity.Employee;

public final class MessagesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
	private static final int VIEW_TYPE_MESSAGE_INCOMING = 1;
	private static final int VIEW_TYPE_MESSAGE_OUTGOING = 2;
	private static final int VIEW_TYPE_IMAGE_INCOMING = 3;
	private static final int VIEW_TYPE_IMAGE_OUTGOING = 4;
	private static final int VIEW_TYPE_FILE_INCOMING = 5;
	private static final int VIEW_TYPE_FILE_OUTGOING = 6;


	private List<ChatItem> messages = new ArrayList<>();
	@Nullable
	private Consumer<ChatItem> onMessageClickListener = null;

	@NonNull
	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		View view;

		switch (viewType) {
			case VIEW_TYPE_MESSAGE_INCOMING:
				view = LayoutInflater.from(parent.getContext())
						.inflate(R.layout.i_chat_message_in, parent, false);
				return new IncomingMessageHolder(view);
			case VIEW_TYPE_MESSAGE_OUTGOING:
				view = LayoutInflater.from(parent.getContext())
						.inflate(R.layout.i_chat_message_out, parent, false);
				return new OutgoingMessageHolder(view);
			case VIEW_TYPE_IMAGE_INCOMING:
				view = LayoutInflater.from(parent.getContext())
						.inflate(R.layout.i_chat_message_image_in, parent, false);
				return new IncomingImageHolder(view);
			case VIEW_TYPE_IMAGE_OUTGOING:
				view = LayoutInflater.from(parent.getContext())
						.inflate(R.layout.i_chat_message_image_out, parent, false);
				return new OutgoingImageHolder(view);
			case VIEW_TYPE_FILE_INCOMING:
				view = LayoutInflater.from(parent.getContext())
						.inflate(R.layout.i_chat_message_in, parent, false);
				return new IncomingFileHolder(view);
			case VIEW_TYPE_FILE_OUTGOING:
				view = LayoutInflater.from(parent.getContext())
						.inflate(R.layout.i_chat_message_out, parent, false);
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
			case VIEW_TYPE_IMAGE_INCOMING:
				((IncomingImageHolder) holder).bind(message);
				break;
			case VIEW_TYPE_IMAGE_OUTGOING:
				((OutgoingImageHolder) holder).bind(message);
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
			// todo: will be something better in future
			boolean isImgFile = message.fileUrl.contains("jpg") ||
					message.fileUrl.contains("jpeg") ||
					message.fileUrl.contains("png") ||
					message.fileUrl.contains("bmp");

			if (isImgFile) {
				if (message.isIncoming) {
					return VIEW_TYPE_IMAGE_INCOMING;
				} else {
					return VIEW_TYPE_IMAGE_OUTGOING;
				}
			} else {
				if (message.isIncoming) {
					return VIEW_TYPE_FILE_INCOMING;
				} else {
					return VIEW_TYPE_FILE_OUTGOING;
				}
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
		TextView timeView;

		IncomingMessageHolder(View itemView) {
			super(itemView);

			nameView = itemView.findViewById(R.id.nameView);
			messageView = itemView.findViewById(R.id.messageView);
			avatarView = itemView.findViewById(R.id.avatarView);
			timeView = itemView.findViewById(R.id.timeView);
		}

		void bind(ChatItem message) {
			messageView.setText(message.content);

			// For better implementation see https://bumptech.github.io/glide/int/recyclerview.html
			String avatarUrl = ((Employee) message.creator).avatarUrl;
			String opName = ((Employee) message.creator).name;

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

			timeView.setText(DateUtils.timestampToTime(message.createdAt));
		}
	}

	private static class OutgoingMessageHolder extends RecyclerView.ViewHolder {
		TextView messageView;
		TextView timeView;

		OutgoingMessageHolder(View itemView) {
			super(itemView);

			messageView = itemView.findViewById(R.id.messageView);
			timeView = itemView.findViewById(R.id.timeView);
		}

		void bind(ChatItem message) {
			messageView.setText(message.content);

			timeView.setText(DateUtils.timestampToTime(message.createdAt));
		}
	}

	private static class IncomingFileHolder extends RecyclerView.ViewHolder {
		TextView messageView;
		ImageView avatarView;
		TextView nameView;
		TextView timeView;

		IncomingFileHolder(View itemView) {
			super(itemView);

			nameView = itemView.findViewById(R.id.nameView);
			messageView = itemView.findViewById(R.id.messageView);
			avatarView = itemView.findViewById(R.id.avatarView);
			timeView = itemView.findViewById(R.id.timeView);
		}

		void bind(ChatItem message) {
			messageView.setText(message.content);

			// For better implementation see https://bumptech.github.io/glide/int/recyclerview.html
			String avatarUrl = ((Employee) message.creator).avatarUrl;
			String opName = ((Employee) message.creator).name;

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

			messageView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.doc, 0, 0, 0);
			messageView.setCompoundDrawablePadding(messageView.getResources().getDimensionPixelOffset(R.dimen.chat_message_file_icon_padding));
			messageView.setTextIsSelectable(false);

			timeView.setText(DateUtils.timestampToTime(message.createdAt));
		}
	}

	private static class OutgoingFileHolder extends RecyclerView.ViewHolder {
		TextView messageView;
		TextView timeView;

		OutgoingFileHolder(View itemView) {
			super(itemView);

			messageView = itemView.findViewById(R.id.messageView);
			timeView = itemView.findViewById(R.id.timeView);
		}

		void bind(ChatItem message) {
			messageView.setText(message.content);

			timeView.setText(DateUtils.timestampToTime(message.createdAt));

			messageView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.doc, 0, 0, 0);
			messageView.setCompoundDrawablePadding(messageView.getResources().getDimensionPixelOffset(R.dimen.chat_message_file_icon_padding));
			messageView.setTextIsSelectable(false);
		}
	}

	private static class IncomingImageHolder extends RecyclerView.ViewHolder {
		ImageView imageView;
		ImageView avatarView;
		TextView nameView;
		TextView timeView;

		IncomingImageHolder(View itemView) {
			super(itemView);

			imageView = itemView.findViewById(R.id.image);
			nameView = itemView.findViewById(R.id.nameView);
			avatarView = itemView.findViewById(R.id.avatarView);
			timeView = itemView.findViewById(R.id.timeView);
		}

		void bind(ChatItem message) {
			loadImage(message, imageView);

			// For better implementation see https://bumptech.github.io/glide/int/recyclerview.html
			String avatarUrl = ((Employee) message.creator).avatarUrl;
			String opName = ((Employee) message.creator).name;

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

			timeView.setText(DateUtils.timestampToTime(message.createdAt));
		}
	}

	private static class OutgoingImageHolder extends RecyclerView.ViewHolder {
		ImageView imageView;
		TextView timeView;

		OutgoingImageHolder(View itemView) {
			super(itemView);

			imageView = itemView.findViewById(R.id.image);
			timeView = itemView.findViewById(R.id.timeView);
		}

		void bind(ChatItem message) {
			loadImage(message, imageView);

			timeView.setText(DateUtils.timestampToTime(message.createdAt));
		}
	}

	// For better implementation see https://bumptech.github.io/glide/int/recyclerview.html
	private static void loadImage(ChatItem message, ImageView imageView) {
		int cornersRadius = imageView.getResources().getDimensionPixelOffset(R.dimen.chat_image_corner_radius);

		Glide.with(imageView.getContext())
				.load(message.fileUrl)
				.placeholder(R.drawable.placeholder)
				.error(R.drawable.placeholder)
				.centerCrop()
				.dontAnimate()
				.transform(new RoundedCorners(cornersRadius))
				.into(imageView);
	}
}
