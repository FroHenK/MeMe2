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

    //doesnt work
    @Deprecated
    public static BsonTimestamp toBsonTimestamp(LocalDateTime dateTime) {
        if (dateTime == null)
            return null;
        return new BsonTimestamp();
    }

    //doesn't work
    @Deprecated
    public static LocalDateTime fromBsonTimestamp(BsonTimestamp timestamp) {
        if (timestamp == null)
            return null;

        return null;
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
