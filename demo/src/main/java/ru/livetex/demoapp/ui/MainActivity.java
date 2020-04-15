package ru.livetex.demoapp.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;
import io.reactivex.Completable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.internal.functions.Functions;
import io.reactivex.schedulers.Schedulers;
import ru.livetex.demoapp.Const;
import ru.livetex.demoapp.R;
import ru.livetex.sdk.LiveTex;
import ru.livetex.sdk.entity.DialogState;
import ru.livetex.sdk.logic.LiveTexMessagesHandler;
import ru.livetex.sdk.network.AuthConnectionListener;
import ru.livetex.sdk.network.NetworkManager;

public class MainActivity extends AppCompatActivity {
	private static final String TAG = "MainActivity";

	private final CompositeDisposable disposables = new CompositeDisposable();

	private Toolbar toolbarView;
	private EditText inputView;
	private RecyclerView messagesView;

	private SharedPreferences sp;
	private final LiveTexMessagesHandler messagesHandler = LiveTex.getInstance().getMessagesHandler();
	private final NetworkManager networkManager = LiveTex.getInstance().getNetworkManager();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.a_main);

		sp = getSharedPreferences("livetex-demo", Context.MODE_PRIVATE);

		toolbarView = findViewById(R.id.toolbarView);
		inputView = findViewById(R.id.inputView);
		messagesView = findViewById(R.id.messagesView);

		setupUI();
		subscribe();
		connect();
	}

	private void setupUI() {
		inputView.setOnEditorActionListener((v, actionId, event) -> {
			if (actionId == EditorInfo.IME_ACTION_SEND) {
				sendMessage();
				return true;
			}
			return false;
		});
	}

	private void sendMessage() {
		String text = inputView.getText().toString().trim();

		if (TextUtils.isEmpty(text)) {
			Toast.makeText(this, "Message is empty", Toast.LENGTH_SHORT).show();
			return;
		}

		// todo: move to utils
		InputMethodManager imm = (InputMethodManager) getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(inputView.getWindowToken(), 0);

		// todo: create local message and try to send
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

		disposables.add(messagesHandler.dialogStateUpdate()
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(this::updateDialogState, thr -> {
					Log.e(TAG, "", thr);
				}));
	}

	private void onConnectionStateUpdate(NetworkManager.ConnectionState connectionState) {
		switch (connectionState) {
			case DISCONNECTED:
				break;
			case CONNECTING:
				break;
			case CONNECTED: {
				Disposable d = messagesHandler.sendTextEvent("123")
						.subscribeOn(Schedulers.io())
						.observeOn(AndroidSchedulers.mainThread())
						.subscribe(resp -> {
							Log.i(TAG, resp.toString());
						}, thr -> Log.e(TAG, "", thr));
				break;
			}
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		disposables.clear();
	}

	private void connect() {
		String clientId = sp.getString(Const.KEY_CLIENTID, null);
		disposables.add(Completable
				.fromAction(() -> {
					networkManager.connect(clientId,
							authConnectionListener
					);
				})
				.subscribeOn(Schedulers.io())
				.subscribe(Functions.EMPTY_ACTION, e -> {
					Log.e(TAG, "", e);
				}));
	}

	private void updateDialogState(DialogState dialogState) {
		if (dialogState.employee != null) {
			toolbarView.setTitle(dialogState.employee.name);
		} else {
			toolbarView.setTitle("Диалог");
		}

		switch (dialogState.status) {
			case UNASSIGNED:
				toolbarView.setSubtitle("Диалог не назначен");
				break;
			case QUEUE:
				toolbarView.setSubtitle("Диалог в очереди");
				break;
			case ASSIGNED:
				toolbarView.setSubtitle("Диалог с оператором");
				break;
			case BOT:
				toolbarView.setSubtitle("Диалог с ботом");
				break;
		}
	}

	private AuthConnectionListener authConnectionListener = new AuthConnectionListener() {
		@Override
		public void onAuthSuccess(String clientId) {
			sp.edit().putString(Const.KEY_CLIENTID, clientId).apply();
		}

		@Override
		public void onAuthError(Throwable throwable) {
			Log.e(TAG, "", throwable);
		}
	};
}
