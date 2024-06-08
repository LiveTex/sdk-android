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
import ru.livetex.sdk.entity.Bot;
import ru.livetex.sdk.entity.Creator;
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
import ru.livetex.sdk.entity.Visitor;

public class EntityMapper {
	private static final String TAG = "EntityMapper";

	public final static Gson gson = new GsonBuilder()
			.registerTypeAdapter(BaseEntity.class, new LivetexTypeModelDeserializer())
			.registerTypeAdapter(GenericMessage.class, new LivetexGenericMessageDeserializer())
			.registerTypeAdapter(Creator.class, new LivetexCreatorDeserializer())
			.registerTypeAdapter(DialogState.class, new LivetexDialogStateDeserializer())
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
	}

	static class LivetexGenericMessageDeserializer implements JsonDeserializer<GenericMessage> {
		@Override
		public GenericMessage deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
			String msgType = json.getAsJsonObject().get("type").getAsString();
			GenericMessage msg = null;

			switch (msgType) {
				case TextMessage.TYPE: {
					msg = parseTextMessage(json);
					break;
				}
				case FileMessage.TYPE: {
					msg = parseFileMessage(json);
					break;
				}
			}

			return msg;
		}

		private TextMessage parseTextMessage(JsonElement json) {
			TextMessage message = gson.fromJson(json, TextMessage.class);
			return message;
		}

		private FileMessage parseFileMessage(JsonElement json) {
			FileMessage message = gson.fromJson(json, FileMessage.class);
			return message;
		}
	}

	static class LivetexCreatorDeserializer implements JsonDeserializer<Creator> {
		@Override
		public Creator deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
			JsonObject obj = json.getAsJsonObject();
			String creatorType = obj.get("type").getAsString();

			switch (creatorType) {
				case Employee.TYPE: {
					if (obj.has("employee")) {
						// temporary solution
						return gson.fromJson(obj.get("employee"), Employee.class);
					}
					return gson.fromJson(obj, Employee.class);
				}
				case Visitor.TYPE: {
					return gson.fromJson(obj, Visitor.class);
				}
				case SystemUser.TYPE: {
					return gson.fromJson(obj, SystemUser.class);
				}
				case Bot.TYPE: {
					return gson.fromJson(obj, Bot.class);
				}
			}
			return null;
		}
	}

	static class LivetexDialogStateDeserializer implements JsonDeserializer<DialogState> {
		@Override
		public DialogState deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
			JsonObject obj = json.getAsJsonObject();
			Gson tempGson = new GsonBuilder().create();
			DialogState result = tempGson.fromJson(json, DialogState.class);


			JsonObject employee = obj.get("employee").getAsJsonObject();
			if (employee.has("employee")) {
				// temporary solution
				result.employee = gson.fromJson(employee.get("employee"), Employee.class);
			}

			return result;
		}
	}
}
