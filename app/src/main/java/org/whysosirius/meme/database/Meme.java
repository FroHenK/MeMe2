package org.whysosirius.meme.database;

import org.bson.BsonTimestamp;
import org.bson.Document;
import org.bson.types.ObjectId;

public class Meme extends Documentable {
    private String url;
    private BsonTimestamp time;
    private ObjectId authorId;
    private ObjectId id;
    private String title;
    private Integer rating;


    public ObjectId getId() {
        return id;
    }

    public BsonTimestamp getTime() {
        return time;
    }

    public ObjectId getAuthorId() {
        return authorId;
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
        this.id = id;
        return this;
    }

    public Meme setTime(BsonTimestamp time) {
        this.time = time;
        return this;
    }

    public Meme setAuthorId(ObjectId authorId) {
        this.authorId = authorId;
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
        this.time = time;
    }


    public Meme(Document document) {
        super(document);
    }

    public Document toDocument() {
        Document document = new Document();
        if (url != null)
            document.append("url", url);

        if (time != null)
            document.append("time", time);

        if (authorId != null)
            document.append("author_id", authorId);

        if (id != null)
            document.append("_id", id);

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
            time = document.get("time", BsonTimestamp.class);

        if (document.containsKey("author_id"))
            authorId = document.get("author_id", ObjectId.class);

        if (document.containsKey("_id"))
            id = document.get("_id", ObjectId.class);

        if (document.containsKey("title"))
            title = document.getString("title");

        if (document.containsKey("rating"))
            rating = document.getInteger("rating");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Meme meme = (Meme) o;

        if (url != null ? !url.equals(meme.url) : meme.url != null) return false;
        if (time != null ? !time.equals(meme.time) : meme.time != null) return false;
        if (authorId != null ? !authorId.equals(meme.authorId) : meme.authorId != null)
            return false;
        if (id != null ? !id.equals(meme.id) : meme.id != null) return false;
        if (title != null ? !title.equals(meme.title) : meme.title != null) return false;
        return rating != null ? rating.equals(meme.rating) : meme.rating == null;
    }

    @Override
    public int hashCode() {
        int result = url != null ? url.hashCode() : 0;
        result = 31 * result + (time != null ? time.hashCode() : 0);
        result = 31 * result + (authorId != null ? authorId.hashCode() : 0);
        result = 31 * result + (id != null ? id.hashCode() : 0);
        result = 31 * result + (title != null ? title.hashCode() : 0);
        result = 31 * result + (rating != null ? rating.hashCode() : 0);
        return result;
    }
}