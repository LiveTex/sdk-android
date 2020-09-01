package ru.livetex.sdk.network;

import java.io.File;
import java.io.IOException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import io.reactivex.Single;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import ru.livetex.sdk.entity.FileUploadedResponse;

/**
 * Class responsible for all non-websocket API methods
 */
public final class ApiManager {

	private final OkHttpManager okHttpManager;
	private String authToken = null;

	ApiManager(OkHttpManager okHttpManager) {
		this.okHttpManager = okHttpManager;
	}

	/**
	 * Normally called by okHttpManager
	 */
	public void setAuthToken(String authToken) {
		this.authToken = authToken;
	}

	public Single<FileUploadedResponse> uploadFile(File file) {
		return Single.create(emitter -> {
			if (authToken == null) {
				emitter.tryOnError(new IllegalStateException("uploadFile called with null auth token"));
				return;
			}

			RequestBody requestBody = new MultipartBody.Builder()
					.setType(MultipartBody.FORM)
					.addFormDataPart("fileUpload", file.getName(),
							RequestBody.create(MediaType.parse("text/plain"), file))
					.build();

			Request request = new Request.Builder()
					.addHeader("Authorization", "Bearer " + authToken)
					.url(NetworkManager.getInstance().getUploadEndpoint())
					.post(requestBody)
					.build();

			try {
				okHttpManager.getClient().newCall(request).enqueue(new Callback() {
					@Override
					public void onFailure(final Call call, final IOException e) {
						emitter.tryOnError(e);
					}

					@Override
					public void onResponse(final Call call, final Response response) {
						if (!response.isSuccessful()) {
							// todo: something better? need error handling
							emitter.tryOnError(new IOException("response is " + response));
							return;
						}
						try {
							String string = response.body().string();
							Gson gson = new GsonBuilder().create();
							FileUploadedResponse fResp = gson.fromJson(string, FileUploadedResponse.class);
							emitter.onSuccess(fResp);
						} catch (IOException e) {
							emitter.tryOnError(e);
						}
					}
				});
			} catch (Exception ex) {
				emitter.tryOnError(ex);
			}
		});
	}
}
