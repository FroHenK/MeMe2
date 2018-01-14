package org.whysosirius.meme.database;

import org.bson.Document;

public abstract class Documentable {
    public Documentable(Document document) {
        this();
        this.parseFromDocument(document);
    }

    public Documentable() {
    }

    public abstract Document toDocument();

    public abstract void parseFromDocument(Document document);
}
