package ru.livetex.sdk.logic;

import android.util.Log;

import java.util.HashMap;

import io.reactivex.Single;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;
import okio.ByteString;
import ru.livetex.sdk.entity.BaseEntity;
import ru.livetex.sdk.entity.DialogState;
import ru.livetex.sdk.entity.SentMessage;
import ru.livetex.sdk.entity.TextMessage;
import ru.livetex.sdk.entity.TypingEvent;
import ru.livetex.sdk.network.NetworkManager;

// todo: interface
public class LiveTexMessagesHandler {

	// Generic subject for all entities
	private final PublishSubject<BaseEntity> entitySubject = PublishSubject.create();
	private final PublishSubject<DialogState> dialogStateSubject = PublishSubject.create();
	// todo: clear and dispose on disconnect
	private final HashMap<String, Subject> subscriptions = new HashMap<>();

	// todo: customizable
	private final EntityMapper mapper = new EntityMapper();

	public void onMessage(String text) {
		Log.d("LiveTexMessagesHandler", "onMessage " + text);
		BaseEntity entity = null;

		try {
			entity = mapper.toEntity(text);
		} catch (Exception e) {
			Log.e("LiveTexMessagesHandler", "Error when parsing message", e);
		}
		if (entity == null) {
			return;
		}

		entitySubject.onNext(entity);

		if (entity instanceof DialogState) {
			dialogStateSubject.onNext((DialogState) entity);
		}

		Subject subscription = subscriptions.get(entity.correlationId);

		if (subscription != null) {
			subscription.onNext(entity);
			subscription.onComplete();
			subscriptions.remove(entity.correlationId);
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

	// todo: improve handling?
	public Single<SentMessage> sendTextEvent(String text) {
		TextMessage event = new TextMessage(text);
		String json = EntityMapper.gson.toJson(event);

		Subject<SentMessage> subscription = PublishSubject.<SentMessage> create();
		if (NetworkManager.getInstance().getWebSocket() != null) {
			subscriptions.put(event.correlationId, subscription);
			NetworkManager.getInstance().getWebSocket().send(json);
		}
		return subscription.take(1).singleOrError();
	}

	public PublishSubject<BaseEntity> entity() {
		return entitySubject;
	}

	public PublishSubject<DialogState> dialogStateUpdate() {
		return dialogStateSubject;
	}
}
