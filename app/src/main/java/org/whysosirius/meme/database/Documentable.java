package org.whysosirius.meme.database;

import org.bson.BsonTimestamp;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.time.LocalDateTime;

public abstract class Documentable {
    public abstract Document toDocument();

    public abstract void parseFromDocument(Document document);

    public Documentable(Document document) {
        this();
        this.parseFromDocument(document);
    }

    public Documentable() {
    }

    public static BsonTimestamp toBsonTimestamp(Long dateTime) {
        if (dateTime == null)
            return null;
        return new BsonTimestamp();
    }

    public static Long fromBsonTimestamp(BsonTimestamp timestamp) {
        if (timestamp == null)
            return null;
        return timestamp.getValue();
    }

    public static ObjectId toObjectId(String id) {
        if (id == null)
            return null;
        return new ObjectId(id);
    }

    public static String fromObjectId(ObjectId objectId) {
        if (objectId == null)
            return null;
        return objectId.toHexString();
    }

}
