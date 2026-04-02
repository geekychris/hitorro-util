// enrich_person.groovy — takes any document as a template and enriches with synthetic person data
// Demonstrates: generator definitions, custom functions, MLS, variable-length arrays

// --- Define generators specific to this transform ---
generator "age", type: "int", min: 18, max: 75
generator "salary", type: "double", min: 35000.0, max: 250000.0
generator "emp_id", type: "sequence", prefix: "EMP-"
generator "dept", type: "pick", values: ["Engineering", "Product", "Sales", "Marketing", "Operations", "Finance", "HR"]
generator "seniority", type: "pick", values: ["Junior", "Mid-Level", "Senior", "Staff", "Principal"]
generator "dob", type: "date", from: "1955-01-01", to: "2005-12-31"

// --- Custom functions (plain Groovy closures) ---
def formatPhone = { String raw ->
    // Normalize any phone to (XXX) XXX-XXXX format
    def digits = raw.replaceAll(/[^0-9]/, '')
    if (digits.length() >= 10) {
        digits = digits[-10..-1]  // take last 10
        return "(${digits[0..2]}) ${digits[3..5]}-${digits[6..9]}"
    }
    return raw
}

def slugify = { String text ->
    text.toLowerCase()
        .replaceAll(/[^a-z0-9\s-]/, '')
        .replaceAll(/\s+/, '-')
        .replaceAll(/-+/, '-')
        .replaceAll(/^-|-$/, '')
}

def titleCase = { String text ->
    text.split(/\s+/).collect { it.capitalize() }.join(' ')
}

// --- Transform ---
copyAll()

set "target.type", "person"
set "target.id.domain", "synthetic"
set "target.id.did", gen.uuid()

def first = gen.firstName()
def last = gen.lastName()
def fullName = titleCase("${first} ${last}")

set "target.first_name", first
set "target.last_name", last
set "target.full_name", fullName
set "target.slug", slugify(fullName)

// Contact info — format the phone number
set "target.email", gen.email()
set "target.phone", formatPhone(gen.phone())
set "target.birth_date", gen.next("dob")

// Employment
set "target.employee_id", gen.next("emp_id")
set "target.department", gen.next("dept")
set "target.seniority", gen.next("seniority")
set "target.salary", gen.next("salary")

// Title and bio as MLS
mls "target.title", text: fullName, lang: "en"
mls "target.body", text: gen.lorem(), lang: "en"

// Skills — variable-length array (2-6 skills)
times(gen.intBetween(2, 6)) { i ->
    append "target.skills", gen.pick("Java", "Python", "Go", "Rust", "TypeScript",
            "SQL", "Kubernetes", "Machine Learning", "NLP", "Data Engineering",
            "React", "System Design", "DevOps", "Security")
}

set "target.address", gen.address()
set "target.company", gen.company()
set "target.times.created", gen.date()
set "target.times.modified", gen.date()
