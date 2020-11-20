package ru.livetex.sdk.network;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.OpenableColumns;
import android.webkit.MimeTypeMap;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.function.Supplier;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.BufferedSink;
import okio.Okio;
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
                                     RequestBody.create(MediaType.parse("text/plain"), file)
                    )
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

    public Single<FileUploadedResponse> uploadFile(Context context, Uri uri) {
        return Single.create(emitter -> {
            if (authToken == null) {
                emitter.tryOnError(new IllegalStateException("uploadFile called with null auth token"));
                return;
            }
            ParcelFileDescriptor pfd = context.getContentResolver().openFileDescriptor(uri, "r");
            if (pfd == null) {
                emitter.tryOnError(new IOException("failed to open file"));
                return;
            }

            String fileName = null;
            if ("content".equals(uri.getScheme())) {
                final Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
                if (cursor != null) {
                    try {
                        if (cursor.moveToFirst()) {
                            fileName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                        }
                    } finally {
                        cursor.close();
                    }
                }
            }
            if (fileName == null) {
                final String path = uri.getPath();
                if (path != null) {
                    int pos = path.lastIndexOf('/');
                    fileName = pos >= 0 ? path.substring(pos + 1) : path;
                }
            }
            String extension = null;
            if (fileName != null) {
                int pos = fileName.lastIndexOf('.');
                extension = pos >= 0 ? fileName.substring(pos + 1) : null;
            }

            String mimeType = null;
            if (extension != null) {
                mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
            }

            MediaType mediaType = MediaType.parse(mimeType != null ? mimeType : "application/zip");

            DataProvider dataProvider = () -> context.getContentResolver().openInputStream(uri);

            uploadFile(fileName, dataProvider, mediaType, emitter);
        });
    }

    public Single<FileUploadedResponse> uploadFile(String fileName, DataProvider dataGetter, MediaType mediaType) {
        return Single.create(emitter -> {
            if (authToken == null) {
                emitter.tryOnError(new IllegalStateException("uploadFile called with null auth token"));
                return;
            }

            uploadFile(fileName, dataGetter, mediaType, emitter);
        });
    }

    private void uploadFile(
            String fileName,
            DataProvider dataGetter,
            MediaType mediaType,
            SingleEmitter<FileUploadedResponse> emitter
    ) {
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(
                        "fileUpload",
                        fileName,
                        new RequestBody() {
                            @Override
                            public MediaType contentType() {
                                return mediaType;
                            }

                            @Override
                            public void writeTo(BufferedSink sink) throws IOException {
                                try (InputStream inputStream = dataGetter.get()) {
                                    sink.writeAll(Okio.source(inputStream));
                                }
                            }
                        }
                )
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
    }

    public interface DataProvider {
        InputStream get() throws IOException;
    }
}
