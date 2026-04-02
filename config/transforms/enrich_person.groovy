// enrich_person.groovy — takes a person document template and enriches with synthetic data
// Input: any document (used as template)
// Output: person-type document with generated names, contact info, biography

copyAll()

// Override identity fields
set "target.type", "person"
set "target.id.domain", "synthetic"
set "target.id.did", gen.uuid()

// Generate person details
def first = gen.firstName()
def last = gen.lastName()

set "target.first_name", first
set "target.last_name", last
set "target.full_name", "${first} ${last}"

// Contact info
set "target.email", gen.email()
set "target.phone", gen.phone()
set "target.birth_date", gen.dateInRange("1960-01-01", "2005-12-31")

// Title as MLS
mls "target.title", text: "${first} ${last}", lang: "en"

// Biography as MLS
mls "target.body", text: gen.lorem(), lang: "en"

// Skills as array
times(gen.intBetween(2, 5)) { i ->
    append "target.skills", gen.pick("Java", "Python", "Go", "Rust", "TypeScript",
            "SQL", "Kubernetes", "Machine Learning", "NLP", "Data Engineering")
}

// Address
set "target.address", gen.address()
set "target.company", gen.company()

// Timestamps
set "target.times.created", gen.date()
set "target.times.modified", gen.date()
