package ru.livetex.sdk.logic;

import android.util.Log;

import java.lang.reflect.Type;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import ru.livetex.sdk.entity.AttributesEntity;
import ru.livetex.sdk.entity.AttributesRequest;
import ru.livetex.sdk.entity.BaseEntity;
import ru.livetex.sdk.entity.Department;
import ru.livetex.sdk.entity.DepartmentRequestEntity;
import ru.livetex.sdk.entity.DialogState;
import ru.livetex.sdk.entity.EmployeeTypingEvent;
import ru.livetex.sdk.entity.FileMessage;
import ru.livetex.sdk.entity.HistoryEntity;
import ru.livetex.sdk.entity.SentMessage;
import ru.livetex.sdk.entity.TextMessage;
import ru.livetex.sdk.entity.TypingEvent;

public class EntityMapper {
	private static final String TAG = "EntityMapper";

	public final static Gson gson = new GsonBuilder()
			.registerTypeAdapter(BaseEntity.class, new MyTypeModelDeserializer())
			.create();

	public BaseEntity toEntity(String jsonStr) {
		return gson.fromJson(jsonStr, BaseEntity.class);
	}

	static class MyTypeModelDeserializer implements JsonDeserializer<BaseEntity> {
		@Override
		public BaseEntity deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context)
				throws JsonParseException {

			JsonObject jsonObject = json.getAsJsonObject();

			JsonElement jsonType = jsonObject.get("type");
			String type = jsonType.getAsString();

			switch (type) {
				case DialogState.TYPE: {
					return gson.fromJson(json, DialogState.class);
				}
				case TextMessage.TYPE: {
					return gson.fromJson(json, TextMessage.class);
				}
				case TypingEvent.TYPE: {
					return gson.fromJson(json, TypingEvent.class);
				}
				case Department.TYPE: {
					return gson.fromJson(json, Department.class);
				}
				case SentMessage.TYPE: {
					return gson.fromJson(json, SentMessage.class);
				}
				case FileMessage.TYPE: {
					return gson.fromJson(json, FileMessage.class);
				}
				case AttributesEntity.TYPE: {
					return gson.fromJson(json, AttributesEntity.class);
				}
				case AttributesRequest.TYPE: {
					return gson.fromJson(json, AttributesRequest.class);
				}
				case DepartmentRequestEntity.TYPE: {
					return gson.fromJson(json, DepartmentRequestEntity.class);
				}
				case HistoryEntity.TYPE: {
					return gson.fromJson(json, HistoryEntity.class);
				}
				case EmployeeTypingEvent.TYPE: {
					return gson.fromJson(json, EmployeeTypingEvent.class);
				}
				default: {
					Log.w(TAG, "Unknown model with type " + type);
					return null;
				}
			}
		}
	}
}
