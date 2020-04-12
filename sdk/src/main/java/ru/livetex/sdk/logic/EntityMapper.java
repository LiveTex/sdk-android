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
import com.google.gson.reflect.TypeToken;

import ru.livetex.sdk.entity.BaseEntity;
import ru.livetex.sdk.entity.DialogState;

public class EntityMapper {
	private static final String TAG = "EntityMapper";

	protected final Gson gson = new GsonBuilder()
			.registerTypeAdapter(BaseEntity.class, new MyTypeModelDeserializer())
			.create();

	public <T extends BaseEntity> T toEntity(String jsonStr) {
		Type collectionType = new TypeToken<T>(){}.getType();
		return gson.fromJson(jsonStr, collectionType);
	}

	class MyTypeModelDeserializer implements JsonDeserializer<BaseEntity> {
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
				default: {
					Log.w(TAG, "Unknown model with type " + type);
					return null;
				}
			}
		}
	}
}
