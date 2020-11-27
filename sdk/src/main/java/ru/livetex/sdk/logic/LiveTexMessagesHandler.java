package ru.livetex.sdk.logic;

import android.util.Log;
import android.util.Pair;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;
import okio.ByteString;
import ru.livetex.sdk.entity.AttributesEntity;
import ru.livetex.sdk.entity.AttributesRequest;
import ru.livetex.sdk.entity.BaseEntity;
import ru.livetex.sdk.entity.ButtonPressedEvent;
import ru.livetex.sdk.entity.Department;
import ru.livetex.sdk.entity.DepartmentRequestEntity;
import ru.livetex.sdk.entity.DialogState;
import ru.livetex.sdk.entity.EmployeeTypingEvent;
import ru.livetex.sdk.entity.FileMessage;
import ru.livetex.sdk.entity.FileUploadedResponse;
import ru.livetex.sdk.entity.GetHistoryRequest;
import ru.livetex.sdk.entity.HistoryEntity;
import ru.livetex.sdk.entity.KeyboardEntity;
import ru.livetex.sdk.entity.RatingEvent;
import ru.livetex.sdk.entity.ResponseEntity;
import ru.livetex.sdk.entity.TextMessage;
import ru.livetex.sdk.entity.TypingEvent;
import ru.livetex.sdk.network.NetworkManager;

public class LiveTexMessagesHandler {
	protected final String TAG = "MessagesHandler";

	private boolean isWebsocketLoggingEnabled = false;
	private final PublishSubject<BaseEntity> entitySubject = PublishSubject.create();
	private final PublishSubject<DialogState> dialogStateSubject = PublishSubject.create();
	private final PublishSubject<HistoryEntity> historyUpdateSubject = PublishSubject.create();
	private final PublishSubject<EmployeeTypingEvent> employeeTypingSubject = PublishSubject.create();
	private final PublishSubject<AttributesRequest> attributesRequestSubject = PublishSubject.create();
	private final PublishSubject<DepartmentRequestEntity> departmentRequestSubject = PublishSubject.create();
	private Pair<String, Subject<Integer>> getHistorySubscription = null;
	protected final HashMap<String, Subject> subscriptions = new HashMap<>();

	protected EntityMapper mapper = new EntityMapper();

	public void init(boolean isWebsocketLoggingEnabled) {
		this.isWebsocketLoggingEnabled = isWebsocketLoggingEnabled;

		Disposable disposable = NetworkManager.getInstance().connectionState()
				.filter(state -> state == NetworkManager.ConnectionState.DISCONNECTED)
				.subscribe(ignore -> {
					// Cleanup on disconnect
					if (isWebsocketLoggingEnabled) {
						Log.i(TAG, "Disconnect detected, clearing subscriptions");
					}
					for (Subject subj : subscriptions.values()) {
						if (!subj.hasComplete()) {
							subj.onError(new IllegalStateException("Websocket disconnect"));
						}
					}
					subscriptions.clear();
					if (getHistorySubscription != null) {
						getHistorySubscription.second.onError(new IllegalStateException("Websocket disconnect"));
						getHistorySubscription = null;
					}
				}, thr -> Log.e(TAG, "", thr));
	}

	public synchronized void onMessage(String text) {
		if (isWebsocketLoggingEnabled) {
			Log.d(TAG, "onMessage " + text);
		}
		BaseEntity entity = null;

		try {
			entity = mapper.toEntity(text);
		} catch (Exception e) {
			Log.e(TAG, "Error when parsing message", e);
		}
		if (entity == null) {
			return;
		}

		entitySubject.onNext(entity);

		// Subjects to notify client
		if (entity instanceof DialogState) {
			dialogStateSubject.onNext((DialogState) entity);
		} else if (entity instanceof HistoryEntity) {
			historyUpdateSubject.onNext((HistoryEntity) entity);
			// Notify about count of previous messages loaded by get history request
			if (getHistorySubscription != null && Objects.equals(entity.correlationId, getHistorySubscription.first)) {
				getHistorySubscription.second.onNext(((HistoryEntity) entity).messages.size());
				getHistorySubscription = null;
			}
		} else if (entity instanceof EmployeeTypingEvent) {
			employeeTypingSubject.onNext((EmployeeTypingEvent) entity);
		} else if (entity instanceof AttributesRequest) {
			attributesRequestSubject.onNext((AttributesRequest) entity);
		} else if (entity instanceof DepartmentRequestEntity) {
			Collections.sort(((DepartmentRequestEntity) entity).departments);
			departmentRequestSubject.onNext((DepartmentRequestEntity) entity);
		}

		Subject subscription = subscriptions.get(entity.correlationId);

		if (subscription != null) {
			// Currently client need to handle only first response to request. Another "responses" have only human logic meaning.
			if (entity instanceof ResponseEntity) {
				if (!subscription.hasComplete()) {
					subscription.onNext(entity);
				}
				subscriptions.remove(entity.correlationId);
			}
		}
	}

	/**
	 * Only if you need custom entity mapper
	 */
	public void setMapper(EntityMapper mapper) {
		this.mapper = mapper;
	}

	/**
	 * Request chunk of previous messages in chat history
	 *
	 * @return subscription with count of previous messages loaded
	 */
	public Single<Integer> getHistory(String messageId) {
		return getHistory(messageId, 20);
	}

	/**
	 * Request chunk of previous messages in chat history
	 *
	 * @param count
	 * 		- count (limit) of messages to load
	 *
	 * @return subscription with count of previous messages loaded
	 */
	public Single<Integer> getHistory(String messageId, int count) {
		GetHistoryRequest event = new GetHistoryRequest(messageId, count);
		String json = EntityMapper.gson.toJson(event);

		if (NetworkManager.getInstance().getWebSocket() != null) {
			getHistorySubscription = Pair.create(event.correlationId, PublishSubject.create());
			try {
				sendJson(json);
			} catch (Exception e) {
				return Single.error(e);
			}
			return getHistorySubscription.second.take(1).singleOrError();
		} else {
			return Single.error(new IllegalStateException("Trying to send data when websocket is null"));
		}
	}

	/**
	 * Send event about user types text message.
	 */
	public void sendTypingEvent(@NonNull String text) {
		TypingEvent event = new TypingEvent(text);
		String json = EntityMapper.gson.toJson(event);
		sendJson(json);
	}

	/**
	 * Send answer to attributesRequest event.
	 *
	 * @param attrs
	 * 		- custom attributes if required by your project.
	 */
	public void sendAttributes(@Nullable String name,
							   @Nullable String phone,
							   @Nullable String email,
							   @Nullable Map<String, Object> attrs) {
		AttributesEntity event = new AttributesEntity(name, phone, email, attrs);
		String json = EntityMapper.gson.toJson(event);
		sendJson(json);
	}

	/**
	 * Send feedback about dialog quality.
	 */
	public void sendRatingEvent(boolean isPositiveFeedback) {
		RatingEvent event = new RatingEvent(isPositiveFeedback ? "1" : "0");
		String json = EntityMapper.gson.toJson(event);
		sendJson(json);
	}

	/**
	 * Send payload from pressed message action button ({@link KeyboardEntity.Button}
	 */
	public void sendButtonPressedEvent(@NonNull String payload) {
		ButtonPressedEvent event = new ButtonPressedEvent(payload);
		String json = EntityMapper.gson.toJson(event);
		sendJson(json);
	}

	public Single<ResponseEntity> sendTextMessage(String text) {
		TextMessage event = new TextMessage(text);
		String json = EntityMapper.gson.toJson(event);
		return sendAndSubscribe(json, event.correlationId);
	}

	public Single<ResponseEntity> sendFileMessage(FileUploadedResponse response) {
		FileMessage event = new FileMessage(response);
		String json = EntityMapper.gson.toJson(event);
		return sendAndSubscribe(json, event.correlationId);
	}

	/**
	 * Send answer to departmentRequest event.
	 *
	 * @param departmentId
	 * 		- id of chosen department.
	 */
	public Single<ResponseEntity> sendDepartmentSelectionEvent(String departmentId) {
		Department event = new Department(departmentId);
		String json = EntityMapper.gson.toJson(event);
		return sendAndSubscribe(json, event.correlationId);
	}

	/**
	 * Generic subscription for custom usage.
	 */
	public Observable<BaseEntity> entity() {
		return entitySubject;
	}

	/**
	 * Event when server sends current dialog state
	 */
	public Observable<DialogState> dialogStateUpdate() {
		return dialogStateSubject;
	}

	/**
	 * Event when server sends chat history.
	 * HistoryEntity contains some part of chat messaging history. It can be newer or older messages.
	 */
	public Observable<HistoryEntity> historyUpdate() {
		return historyUpdateSubject;
	}

	/**
	 * Event when operator types message to user. It will come several times as the operator types the text, once every few seconds
	 */
	public Observable<EmployeeTypingEvent> employeeTyping() {
		return employeeTypingSubject;
	}

	/**
	 * Event when server decides to get attributes from user.
	 * You MUST answer on it with sendAttributes() for correct flow!
	 */
	public Observable<AttributesRequest> attributesRequest() {
		return attributesRequestSubject;
	}

	/**
	 * Event when server decides to get choose of chat department (room) from user.
	 * You MUST answer on it with sendDepartmentSelectionEvent() for correct flow!
	 */
	public Observable<DepartmentRequestEntity> departmentRequest() {
		return departmentRequestSubject;
	}

	public void onDataMessage(ByteString bytes) {
		// not used
	}

	private Single<ResponseEntity> sendAndSubscribe(String json, String correlationId) {
		Subject<ResponseEntity> subscription = PublishSubject.create();
		if (NetworkManager.getInstance().getWebSocket() != null) {
			subscriptions.put(correlationId, subscription);
			try {
				sendJson(json);
			} catch (Exception e) {
				return Single.error(e);
			}
			return subscription.take(1).singleOrError();
		} else {
			return Single.error(new IllegalStateException("Trying to send data when websocket is null"));
		}
	}

	private void sendJson(String json) {
		if (NetworkManager.getInstance().getWebSocket() != null) {
			Log.d(TAG, "Sending: " + json);
			NetworkManager.getInstance().getWebSocket().send(json);
		} else {
			throw new IllegalStateException("Trying to send data when websocket is null");
		}
	}
}
