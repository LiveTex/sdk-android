package ru.livetex.demoapp.ui;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
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
import com.tbruyelle.rxpermissions2.RxPermissions;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;
import ru.livetex.demoapp.Const;
import ru.livetex.demoapp.R;
import ru.livetex.demoapp.db.ChatState;
import ru.livetex.demoapp.db.entity.ChatMessage;
import ru.livetex.demoapp.db.entity.MessageSentState;
import ru.livetex.demoapp.ui.adapter.ChatItem;
import ru.livetex.demoapp.ui.adapter.ChatMessageDiffUtil;
import ru.livetex.demoapp.ui.adapter.MessagesAdapter;
import ru.livetex.demoapp.utils.FileUtils;
import ru.livetex.demoapp.utils.TextWatcherAdapter;
import ru.livetex.sdk.entity.Department;
import ru.livetex.sdk.entity.DepartmentRequestEntity;
import ru.livetex.sdk.entity.DialogState;
import ru.livetex.sdk.network.NetworkManager;

public class ChatActivity extends AppCompatActivity {
	private static final String TAG = "MainActivity";
	private static final int PICKFILE_REQUEST_CODE = 1000;

	private final CompositeDisposable disposables = new CompositeDisposable();
	private ChatViewModel viewModel;

	private Toolbar toolbarView;
	private EditText inputView;
	private ImageView attachmentView;
	private RecyclerView messagesView;
	private ImageView employeeAvatarView;

	private final RxPermissions rxPermissions = new RxPermissions(this);
	private SharedPreferences sp;

	private MessagesAdapter adapter = new MessagesAdapter();

	private final long TEXT_TYPING_DELAY = 500; // milliseconds
	private PublishSubject<String> textSubject = PublishSubject.create();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.a_chat);

		sp = getSharedPreferences("livetex-demo", Context.MODE_PRIVATE);

		toolbarView = findViewById(R.id.toolbarView);
		inputView = findViewById(R.id.inputView);
		attachmentView = findViewById(R.id.attachmentView);
		messagesView = findViewById(R.id.messagesView);
		employeeAvatarView = findViewById(R.id.employeeAvatarView);

		viewModel = new ViewModelProvider(this).get(ChatViewModel.class);

		setupUI();
		subscribeViewModel();
		connect();
	}

	private void subscribeViewModel() {
		viewModel.errorLiveData.observe(this, this::onError);
		viewModel.connectionStateLiveData.observe(this, this::onConnectionStateUpdate);
		viewModel.departmentRequestLiveData.observe(this, this::onDepartmentRequest);
		viewModel.dialogStateUpdateLiveData.observe(this, this::updateDialogState);
	}

	private void onError(String msg) {
		if (TextUtils.isEmpty(msg)) {
			return;
		}
		Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == PICKFILE_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
			Uri uri = data.getData();
			if (uri == null) {
				Toast.makeText(this, "Не удалось открыть файл", Toast.LENGTH_SHORT).show();
				return;
			}
			Disposable d = Single
					.fromCallable(() -> FileUtils.getPath(this, uri))
					.subscribeOn(Schedulers.io())
					.subscribe(path -> {
						viewModel.sendFile(path);
					}, thr -> Log.e(TAG, "onFile", thr));
		}
	}

	private void setupUI() {
		setupInput();

		adapter.setOnMessageClickListener(item -> {
			// Try to re-send failed message
			if (item.sentState == MessageSentState.FAILED) {
				ChatMessage message = ChatState.instance.getMessage(item.id);
				if (message != null) {
					viewModel.resendMessage(message);
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
				.subscribe(this::setMessages, thr -> Log.e(TAG, "messages observe", thr)));
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

		attachmentView.setOnClickListener(v -> {
			disposables.add(rxPermissions
					.request(Manifest.permission.READ_EXTERNAL_STORAGE)
					.subscribe(granted -> {
						if (granted) {
							Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
							intent.setType("*/*");
							//intent.addCategory(Intent.CATEGORY_OPENABLE);

							try {
								startActivityForResult(
										Intent.createChooser(intent, "Выберите файл для загрузки"),
										PICKFILE_REQUEST_CODE);
							} catch (android.content.ActivityNotFoundException ex) {
								Toast.makeText(this, "Установите файл менеджер",
										Toast.LENGTH_SHORT).show();
							}
						} else {
							// Oops permission denied
						}
					}));
		});

		Disposable disposable = textSubject
				.throttleLast(TEXT_TYPING_DELAY, TimeUnit.MILLISECONDS)
				.observeOn(Schedulers.io())
				.subscribe(viewModel::sendTypingEvent, thr -> {
					Log.e(TAG, "typing observe", thr);
				});
		disposables.add(disposable);

		inputView.addTextChangedListener(new TextWatcherAdapter() {
			@Override
			public void afterTextChanged(Editable editable) {
				// notify about typing
				textSubject.onNext(editable.toString());
			}
		});
	}

	private void onDepartmentRequest(DepartmentRequestEntity departmentRequestEntity) {
		List<Department> departments = departmentRequestEntity.departments;

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
			viewModel.selectDepartment(departments.get(i));
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

		ChatMessage chatMessage = ChatState.instance.createNewTextMessage(text);
		inputView.setText(null);

		viewModel.sendMessage(chatMessage);
	}

	private void onConnectionStateUpdate(NetworkManager.ConnectionState connectionState) {
		switch (connectionState) {
			case DISCONNECTED: {
				Toast.makeText(this, "Вебсокет отключен", Toast.LENGTH_SHORT).show();
				break;
			}
			case CONNECTING: {
				break;
			}
			case CONNECTED: {
				Toast.makeText(this, "Вебсокет подключен", Toast.LENGTH_SHORT).show();
				break;
			}
			default:
				break;
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		disposables.clear();
	}

	private void connect() {
		String clientId = sp.getString(Const.KEY_CLIENTID, null);
		Consumer<String> onAuthSuccess = clientIdReceived -> {
			sp.edit().putString(Const.KEY_CLIENTID, clientIdReceived).apply();
		};
		viewModel.connect(clientId, onAuthSuccess);
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
}
