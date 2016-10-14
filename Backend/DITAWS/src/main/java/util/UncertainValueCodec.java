package util;

import model.UncertainValue;

import org.bson.BsonReader;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;

public class UncertainValueCodec implements Codec<UncertainValue> {


	public UncertainValueCodec() {	}

	@Override
	public void encode(BsonWriter writer, UncertainValue value, EncoderContext ec) {
		writer.writeStartDocument();
		writer.writeName("mean");
		writer.writeDouble(value.getMean());
		writer.writeName("distribution");
		writer.writeString(value.getDistribution());
		writer.writeEndDocument();
	}

	@Override
	public Class<UncertainValue> getEncoderClass() {
		return UncertainValue.class;
	}

	@Override
	public UncertainValue decode(BsonReader reader, DecoderContext dc) {
		reader.readStartDocument();
		reader.readName();
		Double mean = reader.readDouble();
		reader.readName();
		String distribution = reader.readString();
		reader.readEndDocument();
		return new UncertainValue(mean, distribution);
	}

}