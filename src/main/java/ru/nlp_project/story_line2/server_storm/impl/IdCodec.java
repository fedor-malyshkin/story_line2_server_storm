package ru.nlp_project.story_line2.server_storm.impl;

import org.bson.BsonReader;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import org.bson.types.ObjectId;

import ru.nlp_project.story_line2.server_storm.model.Id;

class IdCodec implements Codec<Id> {

	@Override
	public void encode(BsonWriter writer, Id value, EncoderContext encoderContext) {
		writer.writeObjectId(new ObjectId(value.getValue()));
	}

	@Override
	public Class<Id> getEncoderClass() {
		return Id.class;
	}

	@Override
	public Id decode(BsonReader reader, DecoderContext decoderContext) {
		ObjectId objectId = reader.readObjectId();
		return new Id(objectId.toHexString());
	}

}
