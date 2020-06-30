package ru.livetex.sdk.logic;

import android.util.Log;

import java.lang.reflect.Type;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
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
import ru.livetex.sdk.entity.Employee;
import ru.livetex.sdk.entity.EmployeeTypingEvent;
import ru.livetex.sdk.entity.FileMessage;
import ru.livetex.sdk.entity.GenericMessage;
import ru.livetex.sdk.entity.HistoryEntity;
import ru.livetex.sdk.entity.ResponseEntity;
import ru.livetex.sdk.entity.SystemUser;
import ru.livetex.sdk.entity.TextMessage;
import ru.livetex.sdk.entity.TypingEvent;
import ru.livetex.sdk.entity.User;

public class EntityMapper {
	private static final String TAG = "EntityMapper";

	public final static Gson gson = new GsonBuilder()
			.registerTypeAdapter(BaseEntity.class, new LivetexTypeModelDeserializer())
			.create();

	public BaseEntity toEntity(String jsonStr) {
		return gson.fromJson(jsonStr, BaseEntity.class);
	}

	static class LivetexTypeModelDeserializer implements JsonDeserializer<BaseEntity> {
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
					TextMessage message = parseTextMessage(json);
					return message;
				}
				case FileMessage.TYPE: {
					return parseFileMessage(json);
				}
				case TypingEvent.TYPE: {
					return gson.fromJson(json, TypingEvent.class);
				}
				case Department.TYPE: {
					return gson.fromJson(json, Department.class);
				}
				case ResponseEntity.TYPE: {
					return gson.fromJson(json, ResponseEntity.class);
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
					HistoryEntity history = gson.fromJson(json, HistoryEntity.class);

					JsonArray messages = jsonObject.getAsJsonArray("messages");

					for (int i = 0; i < messages.size(); i++) {
						JsonElement message = messages.get(i);

						String msgType = message.getAsJsonObject().get("type").getAsString();
						GenericMessage msg = null;

						switch (msgType) {
							case TextMessage.TYPE: {
								msg = parseTextMessage(message);
								break;
							}
							case FileMessage.TYPE: {
								msg = parseFileMessage(message);
								break;
							}
						}

						if (msg != null) {
							history.messages.add(msg);
						}
					}

					return history;
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

		private TextMessage parseTextMessage(JsonElement json) {
			TextMessage message = gson.fromJson(json, TextMessage.class);
			parseCreator(message, json);
			return message;
		}

		private FileMessage parseFileMessage(JsonElement json) {
			FileMessage message = gson.fromJson(json, FileMessage.class);
			parseCreator(message, json);
			return message;
		}

		private void parseCreator(GenericMessage message, JsonElement json) {
			JsonObject jsonObject = json.getAsJsonObject();

			JsonObject creator = jsonObject.getAsJsonObject("creator");
			String creatorType = creator.get("type").getAsString();

			switch (creatorType) {
				case Employee.TYPE: {
					JsonObject creatorObject = creator.getAsJsonObject("employee");
					message.setCreator(gson.fromJson(creatorObject, Employee.class));
					break;
				}
				case User.TYPE: {
					message.setCreator(gson.fromJson(creator, User.class));
					break;
				}
				case SystemUser.TYPE: {
					message.setCreator(gson.fromJson(creator, SystemUser.class));
					break;
				}
			}
		}
	}
}
