package ru.livetex.demoapp.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;
import io.reactivex.Completable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.internal.functions.Functions;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;
import ru.livetex.demoapp.BuildConfig;
import ru.livetex.demoapp.Const;
import ru.livetex.demoapp.R;
import ru.livetex.demoapp.db.ChatState;
import ru.livetex.demoapp.db.Mapper;
import ru.livetex.demoapp.db.entity.ChatMessage;
import ru.livetex.demoapp.db.entity.MessageSentState;
import ru.livetex.demoapp.ui.adapter.ChatItem;
import ru.livetex.demoapp.ui.adapter.ChatMessageDiffUtil;
import ru.livetex.demoapp.ui.adapter.MessagesAdapter;
import ru.livetex.sdk.LiveTex;
import ru.livetex.sdk.entity.Department;
import ru.livetex.sdk.entity.DepartmentRequestEntity;
import ru.livetex.sdk.entity.DialogState;
import ru.livetex.sdk.entity.EmployeeTypingEvent;
import ru.livetex.sdk.entity.GenericMessage;
import ru.livetex.sdk.entity.HistoryEntity;
import ru.livetex.sdk.entity.LiveTexError;
import ru.livetex.sdk.entity.TextMessage;
import ru.livetex.sdk.logic.LiveTexMessagesHandler;
import ru.livetex.sdk.network.NetworkManager;

// todo: use ViewModel
public class MainActivity extends AppCompatActivity {
	private static final String TAG = "MainActivity";

	private final CompositeDisposable disposables = new CompositeDisposable();

	private Toolbar toolbarView;
	private EditText inputView;
	private RecyclerView messagesView;
	private ImageView employeeAvatarView;

	private SharedPreferences sp;
	private final LiveTexMessagesHandler messagesHandler = LiveTex.getInstance().getMessagesHandler();
	private final NetworkManager networkManager = LiveTex.getInstance().getNetworkManager();

	private MessagesAdapter adapter = new MessagesAdapter();

	private final long TEXT_TYPING_DELAY = 500; // milliseconds
	private PublishSubject<String> textSubject = PublishSubject.create();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.a_main);

		sp = getSharedPreferences("livetex-demo", Context.MODE_PRIVATE);

		toolbarView = findViewById(R.id.toolbarView);
		inputView = findViewById(R.id.inputView);
		messagesView = findViewById(R.id.messagesView);
		employeeAvatarView = findViewById(R.id.employeeAvatarView);

		setupUI();
		subscribe();
		connect();
	}

	private void setupUI() {
		setupInput();

		adapter.setOnMessageClickListener(item -> {
			// Try to re-send failed message
			if (item.sentState == MessageSentState.FAILED) {
				ChatMessage message = ChatState.instance.getMessage(item.id);
				if (message != null) {
					sendMessage(message);
				}
			}
		});

		messagesView.setAdapter(adapter);
		DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(messagesView.getContext(),
				DividerItemDecoration.VERTICAL);
		dividerItemDecoration.setDrawable(getResources().getDrawable(R.drawable.divider));
		messagesView.addItemDecoration(dividerItemDecoration);
		((SimpleItemAnimator) messagesView.getItemAnimator()).setSupportsChangeAnimations(false);

		disposables.add(ChatState.instance.messages()
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(this::setMessages, thr -> Log.e(TAG, "", thr)));
	}

	private void setMessages(List<ChatMessage> chatMessages) {
		List<ChatItem> items = new ArrayList<>();
		boolean needScroll = chatMessages.size() > adapter.getItemCount();

		for (ChatMessage chatMessage : chatMessages) {
			items.add(new ChatItem(chatMessage));
		}
		Collections.sort(items);

		ChatMessageDiffUtil diffUtil =
				new ChatMessageDiffUtil(adapter.getData(), items);
		DiffUtil.DiffResult productDiffResult = DiffUtil.calculateDiff(diffUtil);

		adapter.setData(items);
		productDiffResult.dispatchUpdatesTo(adapter);

		if (needScroll) {
			messagesView.smoothScrollToPosition(adapter.getItemCount() - 1);
		}
	}

	private void setupInput() {
		inputView.setOnEditorActionListener((v, actionId, event) -> {
			if (actionId == EditorInfo.IME_ACTION_SEND) {
				sendMessage();
				return true;
			}
			return false;
		});

		Disposable disposable = textSubject
				.throttleLast(TEXT_TYPING_DELAY, TimeUnit.MILLISECONDS)
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(messagesHandler::sendTypingEvent, thr -> {
					Log.e(TAG, "", thr);
				});
		disposables.add(disposable);

		inputView.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

			}

			@Override
			public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
			}

			@Override
			public void afterTextChanged(Editable editable) {
				String text = editable.toString().trim();
				// Send typing event, not faster than TEXT_TYPING_DELAY
				if (!TextUtils.isEmpty(text)) {
					textSubject.onNext(text);
				}
			}
		});
	}

	/**
	 * Subscribe to connection state and chat events. Should be done before connect.
	 */
	private void subscribe() {
		disposables.add(networkManager.connectionState()
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(this::onConnectionStateUpdate, thr -> {
					Log.e(TAG, "", thr);
				}));

		disposables.add(messagesHandler.history()
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(this::updateHistory, thr -> {
					Log.e(TAG, "", thr);
				}));

		disposables.add(messagesHandler.departmentRequest()
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(this::onDepartmentRequest, thr -> {
					Log.e(TAG, "", thr);
				}));

		disposables.add(messagesHandler.attributesRequest()
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(attributesRequest -> {
					Disposable d = Completable.fromAction(() -> messagesHandler.sendAttributes("Demo user", null, null, null))
							.subscribeOn(Schedulers.io())
							.observeOn(AndroidSchedulers.mainThread())
							.subscribe(Functions.EMPTY_ACTION, thr -> Log.e(TAG, "", thr));
				}, thr -> {
					Log.e(TAG, "", thr);
				}));

		disposables.add(messagesHandler.dialogStateUpdate()
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(this::updateDialogState, thr -> {
					Log.e(TAG, "", thr);
				}));

		disposables.add(messagesHandler.employeeTyping()
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(this::updateEmployeeTypingState, thr -> {
					Log.e(TAG, "", thr);
				}));
	}

	// todo: file messages
	private void updateHistory(HistoryEntity historyEntity) {
		List<ChatMessage> messages = new ArrayList<>();
		for (GenericMessage genericMessage : historyEntity.messages) {
			if (genericMessage instanceof TextMessage) {
				ChatMessage chatMessage = Mapper.toChatMessage((TextMessage) genericMessage);
				messages.add(chatMessage);
			}
		}
		ChatState.instance.addMessages(messages);
	}

	private void onDepartmentRequest(DepartmentRequestEntity departmentRequestEntity) {
		List<Department> departments = departmentRequestEntity.departments;

		// For test only // todo: remove
		if (BuildConfig.DEBUG) {
			departments.add(new Department("Тайная комната"));
		}

		if (departments.isEmpty()) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this)
					.setTitle("Ошибка")
					.setMessage("Список комнат пуст, свяжитесь с поддержкой");
			builder.show();
			return;
		}

		List<String> departmentNames = new ArrayList<>();
		for (Department dep : departments) {
			departmentNames.add(dep.id);
		}

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Выберите комнату");
		builder.setCancelable(false);
		builder.setItems(departmentNames.toArray(new String[0]), (dialogInterface, i) -> {
			selectDepartment(departments.get(i));
		});
		AlertDialog dialog = builder.create();
		dialog.show();
	}

	private void sendMessage() {
		String text = inputView.getText().toString().trim();

		if (TextUtils.isEmpty(text)) {
			Toast.makeText(this, "Введите сообщение", Toast.LENGTH_SHORT).show();
			return;
		}

		ChatMessage chatMessage = ChatState.instance.createNewMessage(text);
		inputView.setText(null);

		sendMessage(chatMessage);
	}

	private void sendMessage(ChatMessage chatMessage) {
		Disposable d = messagesHandler.sendTextEvent(chatMessage.content)
				.doOnSubscribe(ignore -> {
					chatMessage.setSentState(MessageSentState.SENDING);
					ChatState.instance.updateMessage(chatMessage);
				})
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(resp -> {
					// remove message with local id
					ChatState.instance.removeMessage(chatMessage.id);

					chatMessage.id = resp.sentMessage.id;
					chatMessage.setSentState(MessageSentState.SENT);
					// server time considered as correct one
					// also this is time when message was actually sent, not created
					chatMessage.createdAt = resp.sentMessage.createdAt;

					// in real project here should be saving (upsert) in persistent storage
					ChatState.instance.addMessage(chatMessage);
				}, thr -> {
					Log.e(TAG, "sendMessage", thr);
					Toast.makeText(this, "Ошибка отправки " + thr.getMessage(), Toast.LENGTH_LONG).show();

					chatMessage.setSentState(MessageSentState.FAILED);
					ChatState.instance.updateMessage(chatMessage);
				});
	}

	private void selectDepartment(Department department) {
		Disposable d = messagesHandler.sendDepartmentSelectionEvent(department.id)
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(response -> {
					if (response.error != null && response.error.contains(LiveTexError.INVALID_DEPARTMENT)) {
						Toast.makeText(this, "Была выбрана невалидная комната", Toast.LENGTH_SHORT).show();
					}
				}, thr -> Log.e(TAG, "", thr));
	}

	private void updateEmployeeTypingState(EmployeeTypingEvent employeeTypingEvent) {
		// todo: implement UI indicator
	}

	private void onConnectionStateUpdate(NetworkManager.ConnectionState connectionState) {
		switch (connectionState) {
			case DISCONNECTED:
				Toast.makeText(this, "Вебсокет отключен", Toast.LENGTH_SHORT).show();
				break;
			case CONNECTING:
				break;
			case CONNECTED: {
				Toast.makeText(this, "Вебсокет подключен", Toast.LENGTH_SHORT).show();
				break;
			}
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		disposables.clear();
		NetworkManager.getInstance().forceDisconnect();
	}

	private void connect() {
		String clientId = sp.getString(Const.KEY_CLIENTID, null);
		disposables.add(networkManager.connect(clientId)
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(this::onAuthSuccess, this::onAuthError));
	}

	private void updateDialogState(DialogState dialogState) {
		if (dialogState.employee != null) {
			toolbarView.setTitle(dialogState.employee.name);
			if (!TextUtils.isEmpty(dialogState.employee.avatarUrl)) {
				Glide.with(this)
						.load(dialogState.employee.avatarUrl)
						.placeholder(R.drawable.ic_user)
						.error(R.drawable.ic_user)
						.centerCrop()
						.dontAnimate()
						.apply(RequestOptions.circleCropTransform())
						.into(employeeAvatarView);
			} else {
				employeeAvatarView.setImageResource(R.drawable.ic_user);
			}
		} else {
			toolbarView.setTitle("Диалог");
			employeeAvatarView.setImageResource(R.drawable.ic_user);
		}

		switch (dialogState.status) {
			case UNASSIGNED:
				toolbarView.setSubtitle("Диалог не назначен");
				break;
			case QUEUE:
				toolbarView.setSubtitle("Диалог в очереди");
				break;
			case ASSIGNED:
				switch (dialogState.employeeStatus) {
					case ONLINE:
						toolbarView.setSubtitle("Онлайн");
						break;
					case OFFLINE:
						toolbarView.setSubtitle("Оффлайн");
						break;
				}
				break;
			case BOT:
				toolbarView.setSubtitle("Диалог с ботом");
				break;
		}
	}

	private void onAuthError(Throwable e) {
		Log.e(TAG, "", e);
		Toast.makeText(this, "Ошибка соединения " + e.getMessage(), Toast.LENGTH_SHORT).show();
	}

	private void onAuthSuccess(String clientId) {
		sp.edit().putString(Const.KEY_CLIENTID, clientId).apply();
	}
}
