package com.example.picchallenge.data.model

import com.google.gson.annotations.SerializedName

data class WordPressPost(
    @SerializedName("id") val id: Int,
    @SerializedName("date") val date: String,
    @SerializedName("date_gmt") val dateGmt: String,
    @SerializedName("modified") val modified: String,
    @SerializedName("slug") val slug: String,
    @SerializedName("status") val status: String,
    @SerializedName("type") val type: String,
    @SerializedName("link") val link: String,
    @SerializedName("title") val title: Title,
    @SerializedName("content") val content: Content,
    @SerializedName("excerpt") val excerpt: Excerpt,
    @SerializedName("author") val author: Int,
    @SerializedName("featured_media") val featuredMedia: Int,
    @SerializedName("comment_status") val commentStatus: String,
    @SerializedName("ping_status") val pingStatus: String,
    @SerializedName("sticky") val sticky: Boolean,
    @SerializedName("template") val template: String,
    @SerializedName("format") val format: String,
    @SerializedName("meta") val meta: Meta,
    @SerializedName("categories") val categories: List<Int>,
    @SerializedName("tags") val tags: List<Int>,
    @SerializedName("class_list") val classList: List<String>,
    @SerializedName("_links") val links: Links
)

data class Title(
    @SerializedName("rendered") val rendered: String
)

data class Content(
    @SerializedName("rendered") val rendered: String,
    @SerializedName("protected") val protected: Boolean
)

data class Excerpt(
    @SerializedName("rendered") val rendered: String,
    @SerializedName("protected") val protected: Boolean
)

data class Meta(
    @SerializedName("footnotes") val footnotes: String
)

data class Links(
    @SerializedName("self") val self: List<Self>,
    @SerializedName("collection") val collection: List<Collection>,
    @SerializedName("about") val about: List<About>,
    @SerializedName("author") val author: List<Author>,
    @SerializedName("replies") val replies: List<Replies>,
    @SerializedName("version-history") val versionHistory: List<VersionHistory>,
    @SerializedName("predecessor-version") val predecessorVersion: List<PredecessorVersion>,
    @SerializedName("wp:featuredmedia") val wpFeaturedmedia: List<WpFeaturedmedia>,
    @SerializedName("wp:attachment") val wpAttachment: List<WpAttachment>,
    @SerializedName("wp:term") val wpTerm: List<WpTerm>,
    @SerializedName("curies") val curies: List<Curies>
)

data class Self(
    @SerializedName("href") val href: String,
    @SerializedName("targetHints") val targetHints: TargetHints?
)

data class TargetHints(
    @SerializedName("allow") val allow: List<String>
)

data class Collection(
    @SerializedName("href") val href: String
)

data class About(
    @SerializedName("href") val href: String
)

data class Author(
    @SerializedName("embeddable") val embeddable: Boolean,
    @SerializedName("href") val href: String
)

data class Replies(
    @SerializedName("embeddable") val embeddable: Boolean,
    @SerializedName("href") val href: String
)

data class VersionHistory(
    @SerializedName("count") val count: Int,
    @SerializedName("href") val href: String
)

data class PredecessorVersion(
    @SerializedName("id") val id: Int,
    @SerializedName("href") val href: String
)

data class WpFeaturedmedia(
    @SerializedName("embeddable") val embeddable: Boolean,
    @SerializedName("href") val href: String
)

data class WpAttachment(
    @SerializedName("href") val href: String
)

data class WpTerm(
    @SerializedName("taxonomy") val taxonomy: String,
    @SerializedName("embeddable") val embeddable: Boolean,
    @SerializedName("href") val href: String
)

data class Curies(
    @SerializedName("name") val name: String,
    @SerializedName("href") val href: String,
    @SerializedName("templated") val templated: Boolean
)

// Simplified model for UI display
data class BlogPostDisplay(
    val id: Int,
    val title: String,
    val excerpt: String,
    val content: String,
    val date: String,
    val link: String,
    val featuredMediaId: Int,
    val authorId: Int
)
