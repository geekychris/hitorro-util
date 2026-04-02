// product_catalog.groovy — generates synthetic product catalog entries
// Demonstrates: working registers, computed fields, conditional branching

copyAll()

set "target.type", "product"
set "target.id.domain", "products"
set "target.id.did", gen.uuid()

def productName = gen.product()
def company = gen.company()
def price = gen.doubleBetween(9.99, 999.99)
def category = gen.pick("Electronics", "Home & Garden", "Software",
        "Office Supplies", "Food & Beverage", "Health & Wellness")

// Store intermediate values in work register
set "work.price", price
set "work.category", category

mls "target.title", text: "${productName} by ${company}", lang: "en"
mls "target.description", text: gen.lorem(), lang: "en"

set "target.product_name", productName
set "target.manufacturer", company
set "target.price", price
set "target.currency", gen.pick("USD", "EUR", "GBP")

// Category-specific fields
when(category == "Electronics") {
    set "target.warranty_months", gen.intBetween(12, 36)
    set "target.weight_kg", gen.doubleBetween(0.1, 15.0)
}
when(category == "Software") {
    set "target.license", gen.pick("MIT", "Apache-2.0", "Commercial", "Subscription")
    set "target.version", "${gen.intBetween(1,5)}.${gen.intBetween(0,9)}.${gen.intBetween(0,99)}"
}
when(category == "Food & Beverage") {
    set "target.expiry_date", gen.dateInRange("2026-06-01", "2027-12-31")
}

// Tags
append "target.tags", category.toLowerCase().replace(" & ", "-").replace(" ", "-")
append "target.tags", gen.pick("bestseller", "new-arrival", "sale", "premium", "eco-friendly")

set "target.times.created", gen.date()
set "target.times.modified", gen.date()
