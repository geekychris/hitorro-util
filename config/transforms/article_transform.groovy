// article_transform.groovy — transforms any document into an article format
// Demonstrates: generators, conditionals, loops, custom functions, template generators

// --- Generators ---
generator "word_count", type: "int", min: 200, max: 5000
generator "read_time", type: "int", min: 1, max: 20
generator "article_seq", type: "sequence", prefix: "ART-"
generator "rating", type: "double", min: 1.0, max: 5.0
generator "pub", type: "pick", values: ["Tech Daily", "Innovation Weekly",
        "The Data Journal", "AI Review", "Open Source Times", "Dev Insider"]
generator "byline", type: "template", template: "{first_names} {last_names}, {company_names}"

// --- Custom functions ---
def excerpt = { String text, int maxLen ->
    if (text == null) return ""
    if (text.length() <= maxLen) return text
    // Cut at last space before maxLen
    def cut = text.lastIndexOf(' ', maxLen)
    if (cut < 0) cut = maxLen
    text.substring(0, cut) + "..."
}

def hashTags = { String... words ->
    words.collect { "#${it.toLowerCase().replaceAll(/\s+/, '')}" }
}

// --- Transform ---
copyAll()

set "target.type", "article"
set "target.id.domain", "articles"
set "target.id.did", gen.uuid()
set "target.article_id", gen.next("article_seq")

// Author byline uses template generator that pulls from other generators
set "target.author", gen.next("byline")
set "target.publication", gen.next("pub")

// Title — preserve source title or generate
def sourceTitle = sourceString("title.mls[0].text")
when(sourceTitle != null) {
    mls "target.title", text: sourceTitle, lang: "en"
}
when(sourceTitle == null) {
    mls "target.title", text: "Article: ${gen.product()}", lang: "en"
}

// Body with excerpt
def bodyText = gen.lorem()
mls "target.body", text: bodyText, lang: "en"
set "target.excerpt", excerpt(bodyText, 120)

// Metadata from generators
set "target.word_count", gen.next("word_count")
set "target.read_time_minutes", gen.next("read_time")
set "target.rating", gen.next("rating")
set "target.published_date", gen.dateInRange("2024-01-01", "2026-03-31")

// Categories
times(gen.intBetween(1, 3)) {
    append "target.category", gen.pick("Technology", "Science", "Business",
            "Health", "Education", "Engineering")
}

// Carry over source tags + add generated ones + hash tags
loop("source.tags[]") { tag ->
    append "target.tags", tag
}
def generated = hashTags("trending", gen.product(), gen.company())
generated.each { tag ->
    append "target.tags", tag
}

set "target.source_url", "https://example.com/articles/${gen.uuid()}"
set "target.times.created", gen.date()
set "target.times.modified", gen.date()
