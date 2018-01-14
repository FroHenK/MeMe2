package org.whysosirius.meme.database;

import org.bson.Document;

public abstract class Documentable {
    public abstract Document toDocument();

    public abstract void parseFromDocument(Document document);

    public Documentable(Document document) {
        this();
        this.parseFromDocument(document);
    }

    public Documentable() {
    }
}
