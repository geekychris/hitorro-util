// ai_enrich.groovy — enriches documents with AI-generated translations and summaries
// Demonstrates: translate into MLS arrays, summarize, ask, with graceful degradation
// Requires: Ollama or other AI service running (hitorro.ai.enabled=true)

copyAll()

// --- AI operations (require AI service) ---
when(aiAvailable()) {

    // Translate the title — append as MLS elements to title.mls[]
    def titleText = sourceString("title.mls[0].text")
    when(titleText != null) {
        translate "source.title.mls[0].text", from: "en", to: "de", into: "target.title.mls"
        translate "source.title.mls[0].text", from: "en", to: "fr", into: "target.title.mls"
        translate "source.title.mls[0].text", from: "en", to: "es", into: "target.title.mls"
    }

    // Translate + summarize the body
    def bodyText = sourceString("body.mls[0].text")
    when(bodyText != null) {
        // Translate body — append as MLS elements to body.mls[]
        translate "source.body.mls[0].text", from: "en", to: "de", into: "target.body.mls"

        // Summarize into a new MLS field
        summarize "source.body.mls[0].text", maxWords: 30, into: "target.summary_text"
        mls "target.description", text: target("summary_text")?.asText(), lang: "en"
        delete "target.summary_text"

        // Ask a question about the content
        ask "source.body.mls[0].text",
            question: "What is the main topic of this text? Reply in 3-5 words.",
            into: "target.topic"
    }
}

// Fallback when AI is not available
when(!aiAvailable()) {
    set "target.ai_status", "AI service not available — skipped translate/summarize"
}

// Always add metadata regardless of AI availability
set "target.id.did", gen.uuid()
set "target.times.modified", gen.date()
set "target.processed_by", "ai_enrich.groovy"
