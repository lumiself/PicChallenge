package com.example.picchallenge.ui.blog

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun EnhancedContentText(
    content: String,
    modifier: Modifier = Modifier
) {
    // Process the content to handle basic formatting
    val processedContent = processContent(content)
    
    Column(modifier = modifier.fillMaxWidth()) {
        processedContent.forEach { contentBlock ->
            when (contentBlock.type) {
                ContentBlockType.PARAGRAPH -> {
                    Text(
                        text = contentBlock.text,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 24.sp,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
                ContentBlockType.HEADING -> {
                    Text(
                        text = contentBlock.text,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                ContentBlockType.QUOTE -> {
                    Text(
                        text = contentBlock.text,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                        modifier = Modifier
                            .padding(vertical = 8.dp, horizontal = 16.dp)
                            .fillMaxWidth()
                    )
                }
                ContentBlockType.LIST_ITEM -> {
                    Row(modifier = Modifier.padding(vertical = 2.dp)) {
                        Text(
                            text = "• ",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = contentBlock.text,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

private fun processContent(content: String): List<ContentBlock> {
    val blocks = mutableListOf<ContentBlock>()
    val cleanContent = stripHtml(content)
    
    // Split by double newlines to identify paragraphs
    val paragraphs = cleanContent.split("\n\n")
    
    for (paragraph in paragraphs) {
        val trimmed = paragraph.trim()
        if (trimmed.isEmpty()) continue
        
        when {
            // Check for quotes (text in quotes or starting with quotation marks)
            trimmed.startsWith("\"") || trimmed.startsWith("“") -> {
                blocks.add(ContentBlock(ContentBlockType.QUOTE, trimmed))
            }
            // Check for list items
            trimmed.startsWith("-") || trimmed.startsWith("•") -> {
                blocks.add(ContentBlock(ContentBlockType.LIST_ITEM, trimmed.removePrefix("-").removePrefix("•").trim()))
            }
            // Check for short text that might be headings
            trimmed.length < 80 && (trimmed.contains(":") || trimmed.uppercase() == trimmed) -> {
                blocks.add(ContentBlock(ContentBlockType.HEADING, trimmed))
            }
            // Default to paragraph
            else -> {
                blocks.add(ContentBlock(ContentBlockType.PARAGRAPH, trimmed))
            }
        }
    }
    
    return blocks
}

private fun stripHtml(html: String): String {
    return html
        .replace(Regex("<.*?>"), "") // Remove HTML tags
        .replace("&#8217;", "'") // Right single quotation mark
        .replace("&#8220;", "\"") // Left double quotation mark
        .replace("&#8221;", "\"") // Right double quotation mark
        .replace("&#8230;", "...") // Ellipsis
        .replace("&#8211;", "–") // En dash
        .replace("&#8212;", "—") // Em dash
        .replace("&amp;", "&") // Ampersand
        .replace("&lt;", "<") // Less than
        .replace("&gt;", ">") // Greater than
        .replace("&nbsp;", " ") // Non-breaking space
        .replace(Regex("\\s+"), " ") // Collapse multiple spaces
        .trim()
}

private data class ContentBlock(
    val type: ContentBlockType,
    val text: String
)

private enum class ContentBlockType {
    PARAGRAPH,
    HEADING,
    QUOTE,
    LIST_ITEM
}
