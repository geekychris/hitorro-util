// article_transform.groovy — transforms any document into an article format
// Demonstrates: conditional logic, loops, MLS creation, data generation

copyAll()

set "target.type", "article"
set "target.id.domain", "articles"
set "target.id.did", gen.uuid()

// Author
set "target.author", gen.fullName()
set "target.publication", gen.pick("Tech Daily", "Innovation Weekly",
        "The Data Journal", "AI Review", "Open Source Times")

// Title — use source title if it has one, otherwise generate
def sourceTitle = sourceString("title.mls[0].text")
when(sourceTitle != null) {
    mls "target.title", text: sourceTitle, lang: "en"
}
when(sourceTitle == null) {
    mls "target.title", text: "Article: ${gen.product()} — ${gen.lorem().substring(0, 40)}", lang: "en"
}

// Body — enrich with generated content
mls "target.body", text: gen.lorem(), lang: "en"

// Dates
set "target.published_date", gen.dateInRange("2024-01-01", "2026-03-31")
set "target.times.created", gen.date()
set "target.times.modified", gen.date()

// Categories
times(gen.intBetween(1, 3)) {
    append "target.category", gen.pick("Technology", "Science", "Business",
            "Health", "Education", "Engineering")
}

// Tags from source if available, plus generated ones
loop("source.tags[]") { tag ->
    append "target.tags", tag
}
times(gen.intBetween(1, 3)) {
    append "target.tags", gen.pick("trending", "featured", "review",
            "tutorial", "analysis", "opinion")
}

// Source URL
set "target.source_url", "https://example.com/articles/${gen.uuid()}"
