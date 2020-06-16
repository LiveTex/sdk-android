package ru.livetex.demoapp.ui;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.google.android.material.button.MaterialButton;
import com.tbruyelle.rxpermissions2.RxPermissions;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;
import ru.livetex.demoapp.R;
import ru.livetex.demoapp.db.ChatState;
import ru.livetex.demoapp.db.entity.ChatMessage;
import ru.livetex.demoapp.db.entity.MessageSentState;
import ru.livetex.demoapp.ui.adapter.ChatItem;
import ru.livetex.demoapp.ui.adapter.ChatMessageDiffUtil;
import ru.livetex.demoapp.ui.adapter.MessagesAdapter;
import ru.livetex.demoapp.utils.FileUtils;
import ru.livetex.demoapp.utils.InputUtils;
import ru.livetex.demoapp.utils.TextWatcherAdapter;
import ru.livetex.sdk.entity.Department;
import ru.livetex.sdk.entity.DialogState;
import ru.livetex.sdk.network.NetworkManager;

public class ChatActivity extends AppCompatActivity {
	private static final String TAG = "MainActivity";
	private static final int PICKFILE_REQUEST_CODE = 1000;

	private final CompositeDisposable disposables = new CompositeDisposable();
	private ChatViewModel viewModel;

	private EditText inputView;
	private ImageView addView;
	private ImageView sendView;
	private RecyclerView messagesView;
	private ViewGroup inputContainerView;
	private ViewGroup attributesContainerView;
	private ViewGroup departmentsContainerView;
	private ViewGroup departmentsButtonContainerView;
	private View attributesSendView;
	private EditText attributesNameView;
	private EditText attributesPhoneView;
	private EditText attributesEmailView;

	private final RxPermissions rxPermissions = new RxPermissions(this);

	private MessagesAdapter adapter = new MessagesAdapter();

	private final long TEXT_TYPING_DELAY = 500; // milliseconds
	private PublishSubject<String> textSubject = PublishSubject.create();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.a_chat);

		inputView = findViewById(R.id.inputView);
		sendView = findViewById(R.id.sendView);
		addView = findViewById(R.id.addView);
		messagesView = findViewById(R.id.messagesView);
		inputContainerView = findViewById(R.id.inputContainerView);
		attributesContainerView = findViewById(R.id.attributesContainerView);
		departmentsContainerView = findViewById(R.id.departmentsContainerView);
		departmentsButtonContainerView = findViewById(R.id.departmentsButtonContainerView);
		attributesSendView = findViewById(R.id.attributesSendView);
		attributesNameView = findViewById(R.id.attributesNameView);
		attributesPhoneView = findViewById(R.id.attributesPhoneView);
		attributesEmailView = findViewById(R.id.attributesEmailView);

		viewModel = new ChatViewModelFactory(getSharedPreferences("livetex-demo", Context.MODE_PRIVATE)).create(ChatViewModel.class);

		setupUI();
		subscribeViewModel();
	}

	private void subscribeViewModel() {
		viewModel.viewStateLiveData.observe(this, this::setViewState);
		viewModel.errorLiveData.observe(this, this::onError);
		viewModel.connectionStateLiveData.observe(this, this::onConnectionStateUpdate);
		viewModel.departmentsLiveData.observe(this, this::showDepartments);
		viewModel.dialogStateUpdateLiveData.observe(this, this::updateDialogState);
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
		// --- Chat input
		sendView.setOnClickListener(v -> sendMessage());

		inputView.setOnEditorActionListener((v, actionId, event) -> {
			if (actionId == EditorInfo.IME_ACTION_SEND) {
				sendMessage();
				return true;
			}
			return false;
		});

		addView.setOnClickListener(v -> {
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

		// --- Attributes

		attributesSendView.setOnClickListener(v -> {
			String name = attributesNameView.getText().toString().trim();
			String phone = attributesPhoneView.getText().toString().trim();
			String email = attributesEmailView.getText().toString().trim();

			// Check for required fields. In demo only name is required, in real app depends on your configs.
			if (TextUtils.isEmpty(name)) {
				attributesNameView.setError("Заполните поле");
				attributesNameView.requestFocus();
				return;
			}

			viewModel.sendAttributes(name, phone, email);
		});
	}

	private void setViewState(ChatViewState viewState) {
		if (viewState == null) {
			return;
		}

		switch (viewState) {
			case NORMAL:
				inputContainerView.setVisibility(View.VISIBLE);
				attributesContainerView.setVisibility(View.GONE);
				departmentsContainerView.setVisibility(View.GONE);
				break;
			case ATTRIBUTES:
				InputUtils.hideKeyboard(this);
				inputContainerView.setVisibility(View.GONE);
				attributesContainerView.setVisibility(View.VISIBLE);
				break;
			case DEPARTMENTS:
				InputUtils.hideKeyboard(this);
				inputContainerView.setVisibility(View.GONE);
				attributesContainerView.setVisibility(View.GONE);
				departmentsContainerView.setVisibility(View.VISIBLE);
				break;
		}
	}

	private void showDepartments(List<Department> departments) {
		departmentsButtonContainerView.removeAllViews();

		for (Department department : departments) {
			MaterialButton view = (MaterialButton) View.inflate(this, R.layout.l_department_button, null);
			view.setText(department.id);
			view.setOnClickListener(v -> viewModel.selectDepartment(department));

			departmentsButtonContainerView.addView(view);
		}
	}

	private void onError(String msg) {
		if (TextUtils.isEmpty(msg)) {
			return;
		}
		Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
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

	private void updateDialogState(DialogState dialogState) {
		// Here you can use dialog status and employee data
	}
}
