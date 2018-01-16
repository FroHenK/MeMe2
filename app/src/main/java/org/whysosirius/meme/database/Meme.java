package org.whysosirius.meme.database;

import org.bson.BsonTimestamp;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.time.LocalDateTime;

public class Meme extends Documentable {
    private String url;
    private LocalDateTime time;
    private String authorId;
    private String id;
    private String title;
    private Integer rating;


    public ObjectId getId() {
        return toObjectId(id);
    }

    public BsonTimestamp getTime() {
        return toBsonTimestamp(time);
    }

    public ObjectId getAuthorId() {
        return toObjectId(authorId);
    }

    public String getTitle() {
        return title;
    }

    public String getUrl() {
        return url;
    }

    public Integer getRating() {
        return rating != null ? rating : 0;
    }

    public Meme setId(ObjectId id) {
        this.id = fromObjectId(id);
        return this;
    }

    public Meme setTime(BsonTimestamp time) {
        this.time = fromBsonTimestamp(time);
        return this;
    }

    public Meme setAuthorId(ObjectId authorId) {
        this.authorId = fromObjectId(authorId);
        return this;
    }

    public Meme setTitle(String title) {
        this.title = title;
        return this;
    }

    public Meme setUrl(String url) {
        this.url = url;
        return this;
    }

    public Meme setRating(Integer rating) {
        this.rating = rating;
        return this;
    }

    public Meme() {
        super();
    }

    public Meme(String url) {
        this.url = url;
    }

    public Meme(String url, BsonTimestamp time) {
        this.url = url;
        this.time = fromBsonTimestamp(time);
    }


    public Meme(Document document) {
        super(document);
    }

    public Document toDocument() {
        Document document = new Document();
        if (url != null)
            document.append("url", url);

        if (time != null)
            document.append("time", toBsonTimestamp(time));

        if (authorId != null)
            document.append("author_id", toObjectId(authorId));

        if (id != null)
            document.append("_id", toObjectId(id));

        if (title != null)
            document.append("title", title);

        if (rating != null)
            document.append("rating", rating);

        return document;
    }

    public void parseFromDocument(Document document) {
        if (document.containsKey("url"))
            url = document.getString("url");

        if (document.containsKey("time"))
            time = fromBsonTimestamp(document.get("time", BsonTimestamp.class));

        if (document.containsKey("author_id"))
            authorId = fromObjectId(document.get("author_id", ObjectId.class));

        if (document.containsKey("_id"))
            id = fromObjectId(document.get("_id", ObjectId.class));

        if (document.containsKey("title"))
            title = document.getString("title");

        if (document.containsKey("rating"))
            rating = document.getInteger("rating");
    }


}