package ru.livetex.sdk.entity;

// For history messages list
public interface GenericMessage {
	Creator getCreator();
	void setCreator(Creator creator);
}
