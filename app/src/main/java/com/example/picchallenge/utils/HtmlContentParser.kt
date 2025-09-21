package com.example.picchallenge.utils

import android.util.Log
import java.util.regex.Pattern

object HtmlContentParser {
    
    private const val TAG = "HtmlContentParser"
    
    /**
     * Parse all image URLs from HTML content
     */
    fun parseImages(description: String): List<String> {
        val images = mutableListOf<String>()
        
        try {
            // Pattern to match img tags with src attribute
            val imgPattern = Pattern.compile("<img[^>]+src\\s*=\\s*['\"]([^'\"]+)['\"][^>]*>")
            val matcher = imgPattern.matcher(description)
            
            while (matcher.find()) {
                val imageUrl = matcher.group(1)
                if (!imageUrl.isNullOrEmpty()) {
                    images.add(imageUrl)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing images from HTML", e)
        }
        
        return images
    }
    
    /**
     * Extract text content from HTML, removing all tags
     */
    fun parseText(description: String): String {
        return try {
            // Remove all HTML tags
            description.replace(Regex("<[^>]*>"), "")
                .replace(Regex("&[^;]+;"), " ") // Replace HTML entities with space
                .trim()
                .replace(Regex("\\s+"), " ") // Normalize whitespace
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing text from HTML", e)
            description
        }
    }
    
    /**
     * Extract the first image URL from HTML content
     */
    fun extractFirstImage(description: String): String {
        val images = parseImages(description)
        return images.firstOrNull() ?: ""
    }
    
    /**
     * Check if the description contains any images
     */
    fun hasImages(description: String): Boolean {
        return parseImages(description).isNotEmpty()
    }
    
    /**
     * Get image count from HTML content
     */
    fun getImageCount(description: String): Int {
        return parseImages(description).size
    }
    
    /**
     * Extract contest rules and prizes from description
     * Looks for specific patterns or sections
     */
    fun extractRulesAndPrizes(description: String): Pair<String, String> {
        val rules = StringBuilder()
        val prizes = StringBuilder()
        
        try {
            // Look for common section headers
            val lines = description.split("\n")
            var inRulesSection = false
            var inPrizesSection = false
            
            for (line in lines) {
                val lowerLine = line.lowercase()
                
                when {
                    lowerLine.contains("rules") || lowerLine.contains("terms") || lowerLine.contains("conditions") -> {
                        inRulesSection = true
                        inPrizesSection = false
                        continue
                    }
                    lowerLine.contains("prize") || lowerLine.contains("reward") || lowerLine.contains("win") -> {
                        inRulesSection = false
                        inPrizesSection = true
                        continue
                    }
                    lowerLine.contains("description") || lowerLine.contains("about") -> {
                        inRulesSection = false
                        inPrizesSection = false
                        continue
                    }
                }
                
                when {
                    inRulesSection -> rules.appendLine(line)
                    inPrizesSection -> prizes.appendLine(line)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error extracting rules and prizes", e)
        }
        
        return Pair(rules.toString().trim(), prizes.toString().trim())
    }
    
    /**
     * Clean and format HTML content for display
     */
    fun formatForDisplay(description: String): String {
        return try {
            var cleaned = description
            
            // Remove script and style tags
            cleaned = cleaned.replace(Regex("<script[^>]*>[\\s\\S]*?</script>"), "")
            cleaned = cleaned.replace(Regex("<style[^>]*>[\\s\\S]*?</style>"), "")
            
            // Replace common HTML entities
            cleaned = cleaned.replace("&nbsp;", " ")
                .replace("&amp;", "&")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&quot;", "\"")
                .replace("&#39;", "'")
            
            // Clean up extra whitespace
            cleaned.replace(Regex("\\s+"), " ").trim()
        } catch (e: Exception) {
            Log.e(TAG, "Error formatting HTML for display", e)
            description
        }
    }
}
