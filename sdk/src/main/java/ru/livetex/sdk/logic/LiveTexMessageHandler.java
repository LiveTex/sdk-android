package ru.livetex.sdk.logic;

import android.util.Log;

import io.reactivex.subjects.PublishSubject;
import okio.ByteString;
import ru.livetex.sdk.entity.BaseEntity;
import ru.livetex.sdk.entity.DialogState;
import ru.livetex.sdk.entity.TextMessage;
import ru.livetex.sdk.entity.TypingEvent;
import ru.livetex.sdk.network.NetworkManager;

// todo: interface
public class LiveTexMessageHandler {

	// Generic subject for all entities
	private final PublishSubject<BaseEntity> entitySubject = PublishSubject.create();

	private final PublishSubject<DialogState> dialogStateSubject = PublishSubject.create();

	// todo: customizable
	private final EntityMapper mapper = new EntityMapper();

	public void onMessage(String text) {
		Log.d("LiveTexMessageHandler", "onMessage " + text);
		BaseEntity entity = mapper.toEntity(text);

		if (entity == null) {
			return;
		}

		entitySubject.onNext(entity);

		if (entity instanceof DialogState) {
			dialogStateSubject.onNext((DialogState) entity);
		}
	}

	public void onDataMessage(ByteString bytes) {
		// not used
	}

	public void sendTypingEvent(String text) {
		TypingEvent event = new TypingEvent(text);
		String json = EntityMapper.gson.toJson(event);
		if (NetworkManager.getInstance().getWebSocket() != null) {
			NetworkManager.getInstance().getWebSocket().send(json);
		}
	}

	public void sendTextEvent(String text) {
		TextMessage event = new TextMessage(text);
		String json = EntityMapper.gson.toJson(event);
		if (NetworkManager.getInstance().getWebSocket() != null) {
			NetworkManager.getInstance().getWebSocket().send(json);
		}
	}

	public PublishSubject<BaseEntity> entity() {
		return entitySubject;
	}

	public PublishSubject<DialogState> dialogStateUpdate() {
		return dialogStateSubject;
	}
}
