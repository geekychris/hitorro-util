// lib/common.groovy — shared functions for transform scripts
// Import via: evaluate(new File(registry.generatorsDir.parent, "transforms/lib/common.groovy"))

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

def excerpt = { String text, int maxLen ->
    if (text == null || text.length() <= maxLen) return text ?: ""
    def cut = text.lastIndexOf(' ', maxLen)
    if (cut < 0) cut = maxLen
    text.substring(0, cut) + "..."
}

def formatPhone = { String raw ->
    def digits = raw.replaceAll(/[^0-9]/, '')
    if (digits.length() >= 10) {
        digits = digits[-10..-1]
        return "(${digits[0..2]}) ${digits[3..5]}-${digits[6..9]}"
    }
    return raw
}

def formatPrice = { Number amount, String currency ->
    def symbol = [USD: '$', EUR: '\u20ac', GBP: '\u00a3'].getOrDefault(currency, currency)
    "${symbol}${String.format('%.2f', amount)}"
}

// Return a map so scripts can pick what they need
[slugify: slugify, titleCase: titleCase, excerpt: excerpt,
 formatPhone: formatPhone, formatPrice: formatPrice]
