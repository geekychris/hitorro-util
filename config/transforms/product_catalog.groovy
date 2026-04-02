// product_catalog.groovy — generates synthetic product catalog entries
// Demonstrates: generators, working registers, conditional branches, custom functions

// --- Generators ---
generator "sku", type: "pattern", pattern: "SKU-####-??"
generator "price", type: "double", min: 4.99, max: 1299.99
generator "stock", type: "int", min: 0, max: 500
generator "weight", type: "double", min: 0.05, max: 25.0
generator "rating", type: "double", min: 1.0, max: 5.0
generator "review_count", type: "int", min: 0, max: 2500
generator "discount_pct", type: "pick", values: ["0", "5", "10", "15", "20", "25", "30"]
generator "warranty", type: "pick", values: ["30 days", "90 days", "1 year", "2 years", "3 years", "Lifetime"]
generator "condition", type: "pick", values: ["New", "Refurbished", "Open Box"]
generator "sw_version", type: "pattern", pattern: "#.#.##"

// --- Custom functions ---
def formatPrice = { Number amount, String currency ->
    def symbol = [USD: '$', EUR: '\u20ac', GBP: '\u00a3'].getOrDefault(currency, currency)
    "${symbol}${String.format('%.2f', amount)}"
}

def inStock = { int qty -> qty > 0 ? "In Stock (${qty})" : "Out of Stock" }

def generateBarcode = { ->
    // EAN-13 style: 13 digits
    (1..13).collect { gen.intBetween(0, 9) }.join('')
}

// --- Transform ---
copyAll()

set "target.type", "product"
set "target.id.domain", "products"
set "target.id.did", gen.uuid()

def productName = gen.product()
def company = gen.company()
def price = gen.next("price") as double
def currency = gen.pick("USD", "EUR", "GBP")
def category = gen.pick("Electronics", "Home & Garden", "Software",
        "Office Supplies", "Food & Beverage", "Health & Wellness")
def stockQty = gen.next("stock") as int

// Store in work register for later reference
set "work.category", category
set "work.price", price

mls "target.title", text: "${productName} by ${company}", lang: "en"
mls "target.description", text: gen.lorem(), lang: "en"

set "target.sku", gen.next("sku")
set "target.barcode", generateBarcode()
set "target.product_name", productName
set "target.manufacturer", company
set "target.price", price
set "target.price_formatted", formatPrice(price, currency)
set "target.currency", currency
set "target.stock_quantity", stockQty
set "target.availability", inStock(stockQty)
set "target.rating", gen.next("rating")
set "target.review_count", gen.next("review_count")
set "target.condition", gen.next("condition")

// Category-specific fields
when(category == "Electronics") {
    set "target.warranty", gen.next("warranty")
    set "target.weight_kg", gen.next("weight")
}
when(category == "Software") {
    set "target.license", gen.pick("MIT", "Apache-2.0", "Commercial", "Subscription")
    set "target.version", gen.next("sw_version")
    set "target.platform", gen.pick("Windows", "macOS", "Linux", "Cross-platform", "Web")
}
when(category == "Food & Beverage") {
    set "target.expiry_date", gen.dateInRange("2026-06-01", "2027-12-31")
    set "target.organic", gen.bool()
}

// Discount
def discount = gen.next("discount_pct") as int
when(discount > 0) {
    set "target.discount_percent", discount
    set "target.sale_price", price * (1 - discount / 100.0)
    set "target.sale_price_formatted", formatPrice(price * (1 - discount / 100.0), currency)
}

// Tags
append "target.tags", category.toLowerCase().replace(" & ", "-").replace(" ", "-")
append "target.tags", gen.pick("bestseller", "new-arrival", "sale", "premium", "eco-friendly")

set "target.times.created", gen.date()
set "target.times.modified", gen.date()
