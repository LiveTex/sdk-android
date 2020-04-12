package ru.livetex.demoapp.ui;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import io.reactivex.Completable;
import io.reactivex.schedulers.Schedulers;
import ru.livetex.demoapp.R;
import ru.livetex.sdk.network.NetworkManager;

public class MainActivity extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.a_main);


		Completable.fromAction(() -> {
			NetworkManager.getInstance().auth();
			NetworkManager.getInstance().connectWebSocket();
		})
				.subscribeOn(Schedulers.io())
				.subscribe();
	}
}
