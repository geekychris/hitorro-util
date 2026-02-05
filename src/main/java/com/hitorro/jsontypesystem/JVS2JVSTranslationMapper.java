/*
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
 */
package com.hitorro.jsontypesystem;

import com.fasterxml.jackson.databind.JsonNode;
import com.hitorro.util.core.Log;
import com.hitorro.util.core.iterator.mappers.BaseMapper;
import com.hitorro.util.json.keys.propaccess.Propaccess;

import java.util.ArrayList;
import java.util.List;

/**
 * A mapper that translates multilingual string (mls) fields in JVS objects to multiple target languages.
 * 
 * This mapper can be used in functional patterns where an iterator of JVS objects needs to be
 * transformed to include additional language translations.
 * 
 * Example usage:
 * <pre>
 * // Define a translator (can wrap AIService or any translation backend)
 * Translator translator = (text, source, target) -> aiService.translate(text, source, target);
 * 
 * List<String> targetLanguages = List.of("de", "fr", "es", "ja");
 * List<String> mlsFields = List.of("title.mls", "body.mls");
 * 
 * JVS2JVSTranslationMapper mapper = new JVS2JVSTranslationMapper(translator, targetLanguages, mlsFields, "en");
 * 
 * // Use with iterator
 * AbstractIterator&lt;JVS&gt; translatedDocs = sourceIterator.map(mapper);
 * </pre>
 */
public class JVS2JVSTranslationMapper extends BaseMapper<JVS, JVS> {
    
    /**
     * Functional interface for text translation.
     */
    @FunctionalInterface
    public interface Translator {
        /**
         * Translate text from source language to target language.
         * 
         * @param text The text to translate
         * @param sourceLanguage ISO 639-1 language code of source (e.g., "en")
         * @param targetLanguage ISO 639-1 language code of target (e.g., "de")
         * @return Translated text
         */
        String translate(String text, String sourceLanguage, String targetLanguage);
    }
    
    private final Translator translator;
    private final List<String> targetLanguages;
    private final List<Propaccess> mlsFields;
    private final String sourceLanguage;
    private final boolean skipOnError;
    
    /**
     * Create a translation mapper.
     * 
     * @param translator The translator function to use for translations
     * @param targetLanguages List of ISO 639-1 language codes to translate to (e.g., "de", "fr", "es")
     * @param mlsFieldPaths List of paths to mls fields (e.g., "title.mls", "body.mls")
     * @param sourceLanguage The source language code (e.g., "en")
     */
    public JVS2JVSTranslationMapper(Translator translator, List<String> targetLanguages, 
                                     List<String> mlsFieldPaths, String sourceLanguage) {
        this(translator, targetLanguages, mlsFieldPaths, sourceLanguage, true);
    }
    
    /**
     * Create a translation mapper.
     * 
     * @param translator The translator function to use for translations
     * @param targetLanguages List of ISO 639-1 language codes to translate to (e.g., "de", "fr", "es")
     * @param mlsFieldPaths List of paths to mls fields (e.g., "title.mls", "body.mls")
     * @param sourceLanguage The source language code (e.g., "en")
     * @param skipOnError If true, continue processing on translation errors; if false, return null on error
     */
    public JVS2JVSTranslationMapper(Translator translator, List<String> targetLanguages, 
                                     List<String> mlsFieldPaths, String sourceLanguage, 
                                     boolean skipOnError) {
        this.translator = translator;
        this.targetLanguages = new ArrayList<>(targetLanguages);
        this.mlsFields = new ArrayList<>();
        for (String path : mlsFieldPaths) {
            this.mlsFields.add(new Propaccess(path));
        }
        this.sourceLanguage = sourceLanguage;
        this.skipOnError = skipOnError;
    }
    
    @Override
    public JVS apply(JVS jvs) {
        if (jvs == null || translator == null) {
            return jvs;
        }
        
        try {
            for (Propaccess mlsField : mlsFields) {
                translateMlsField(jvs, mlsField);
            }
        } catch (Exception e) {
            Log.util.error("Error translating JVS: %s", e.getMessage());
            if (!skipOnError) {
                return null;
            }
        }
        
        return jvs;
    }
    
    /**
     * Translate a single mls field to all target languages.
     */
    private void translateMlsField(JVS jvs, Propaccess mlsField) {
        try {
            // Get the source text from the source language
            String sourceTextPath = mlsField.getPath() + "[" + sourceLanguage + "].text";
            JsonNode sourceTextNode = jvs.get(sourceTextPath);
            
            if (sourceTextNode == null || !sourceTextNode.isTextual()) {
                // No source text found, skip this field
                return;
            }
            
            String sourceText = sourceTextNode.textValue();
            if (sourceText == null || sourceText.trim().isEmpty()) {
                return;
            }
            
            // Translate to each target language
            for (String targetLang : targetLanguages) {
                if (targetLang.equals(sourceLanguage)) {
                    // Skip if target is same as source
                    continue;
                }
                
                try {
                    // Check if translation already exists
                    String targetTextPath = mlsField.getPath() + "[" + targetLang + "].text";
                    JsonNode existingNode = jvs.get(targetTextPath);
                    
                    if (existingNode != null && existingNode.isTextual() 
                        && existingNode.textValue() != null 
                        && !existingNode.textValue().trim().isEmpty()) {
                        // Translation already exists, skip
                        continue;
                    }
                    
                    // Perform translation
                    String translatedText = translator.translate(sourceText, sourceLanguage, targetLang);
                    
                    if (translatedText != null && !translatedText.trim().isEmpty()) {
                        // Set the translated text - this will create the language element if it doesn't exist
                        jvs.set(targetTextPath, translatedText);
                    }
                } catch (Exception e) {
                    Log.util.warn("Failed to translate field %s to %s: %s", 
                        mlsField.getPath(), targetLang, e.getMessage());
                    if (!skipOnError) {
                        throw new RuntimeException(e);
                    }
                }
            }
        } catch (Exception e) {
            Log.util.warn("Error processing mls field %s: %s", mlsField.getPath(), e.getMessage());
            if (!skipOnError) {
                throw new RuntimeException(e);
            }
        }
    }
    
    @Override
    public boolean isThreadSafe() {
        // Not thread-safe due to potential translator state
        return false;
    }
    
    @Override
    public String getDescription() {
        return "Translates mls fields to target languages: " + targetLanguages;
    }
    
    /**
     * Builder for creating JVS2JVSTranslationMapper with fluent API.
     */
    public static class Builder {
        private Translator translator;
        private List<String> targetLanguages = new ArrayList<>();
        private List<String> mlsFieldPaths = new ArrayList<>();
        private String sourceLanguage = "en";
        private boolean skipOnError = true;
        
        public Builder translator(Translator translator) {
            this.translator = translator;
            return this;
        }
        
        public Builder targetLanguages(List<String> languages) {
            this.targetLanguages = new ArrayList<>(languages);
            return this;
        }
        
        public Builder targetLanguages(String... languages) {
            this.targetLanguages = List.of(languages);
            return this;
        }
        
        public Builder mlsFields(List<String> fields) {
            this.mlsFieldPaths = new ArrayList<>(fields);
            return this;
        }
        
        public Builder mlsFields(String... fields) {
            this.mlsFieldPaths = List.of(fields);
            return this;
        }
        
        public Builder sourceLanguage(String lang) {
            this.sourceLanguage = lang;
            return this;
        }
        
        public Builder skipOnError(boolean skip) {
            this.skipOnError = skip;
            return this;
        }
        
        public JVS2JVSTranslationMapper build() {
            if (translator == null) {
                throw new IllegalStateException("Translator is required");
            }
            if (targetLanguages.isEmpty()) {
                throw new IllegalStateException("At least one target language is required");
            }
            if (mlsFieldPaths.isEmpty()) {
                throw new IllegalStateException("At least one mls field path is required");
            }
            return new JVS2JVSTranslationMapper(translator, targetLanguages, mlsFieldPaths, 
                                                 sourceLanguage, skipOnError);
        }
    }
    
    /**
     * Create a new builder.
     */
    public static Builder builder() {
        return new Builder();
    }
}
