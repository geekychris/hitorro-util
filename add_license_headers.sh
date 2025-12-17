#!/bin/bash

# MIT License header to add
LICENSE_HEADER='/*
 * Copyright (c) 2006-2025 Chris Collins
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */'

# Find all Java files
find /Users/chris/hitorro/hitorro-util/src -name "*.java" -type f | while read -r file; do
    # Check if file already has copyright notice
    if ! head -5 "$file" | grep -q "Copyright.*Chris Collins"; then
        # Check if file starts with a comment block
        if head -1 "$file" | grep -q "^/\*"; then
            # File has a comment block - need to remove it
            # Find the end of the comment block
            awk '
                BEGIN { in_comment = 0; found_package = 0; }
                /^\/\*/ { in_comment = 1; next; }
                in_comment && /\*\// { in_comment = 0; next; }
                in_comment { next; }
                !found_package && /^package / { found_package = 1; }
                found_package || /^import / || /^$/ || /^public / || /^class / || /^interface / || /^enum / || /^@/ {
                    print;
                }
            ' "$file" > "${file}.tmp"
            
            # Add license header
            {
                echo "$LICENSE_HEADER"
                cat "${file}.tmp"
            } > "$file"
            rm "${file}.tmp"
        else
            # No comment block - just prepend license
            {
                echo "$LICENSE_HEADER"
                cat "$file"
            } > "${file}.tmp"
            mv "${file}.tmp" "$file"
        fi
        echo "Updated: $file"
    fi
done

echo "License headers added to all Java files!"
