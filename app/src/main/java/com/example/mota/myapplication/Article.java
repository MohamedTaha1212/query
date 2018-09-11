package com.example.mota.myapplication;

/**
 * Created by Ahmed Taha on 08/09/2018.
 */

public class Article {
    public final String id;

    public final String name;

    public final String description;

    public final String image;

    /**
     * Constructs a new {@link Article}.
     *
     * @param ArticleId          is the title of the earthquake event
     * @param ArticleName        is the time the earhtquake happened
     * @param ArticleDescription is whether or not a tsunami alert was issued
     */
    public Article (String ArticleId, String ArticleName, String ArticleDescription, String ArticleImage) {
        id = ArticleId;
        name = ArticleName;
        description = ArticleDescription;
        image = ArticleImage;
    }
}
