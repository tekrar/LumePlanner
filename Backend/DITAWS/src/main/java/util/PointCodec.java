package util;

import java.util.ArrayList;
import java.util.List;

import org.bson.BsonReader;
import org.bson.BsonType;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import org.geojson.Point;

public class PointCodec implements Codec<Point> {


	public PointCodec() {	}

	@Override
	public void encode(BsonWriter writer, Point point, EncoderContext ec) {
		writer.writeStartDocument();
		writer.writeName("type");
		writer.writeString("Point");
		writer.writeName("coordinates");
		writer.writeStartArray();
		writer.writeDouble(point.getCoordinates().getLongitude());
		writer.writeDouble(point.getCoordinates().getLatitude());
		writer.writeEndArray();
		writer.writeEndDocument();
	}

	@Override
	public Class<Point> getEncoderClass() {
		return Point.class;
	}

	@Override
	public Point decode(BsonReader reader, DecoderContext dc) {
		reader.readStartDocument();
		reader.readName();
		reader.readString();
		reader.readName();
		reader.readStartArray();
		List<Double> coords = new ArrayList<Double>();
		while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
			coords.add(reader.readDouble());
		}
		reader.readEndArray();
		reader.readEndDocument();
		return new Point(coords.get(0), coords.get(1));
	}

}
