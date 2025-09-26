package com.example.picchallenge.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.picchallenge.data.model.ContestStatus
import com.example.picchallenge.ui.theme.*
import com.example.picchallenge.utils.HtmlContentParser
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ContestCard(
    contest: com.example.picchallenge.data.model.Contest,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    val isEnded = contest.status.equals("ended", ignoreCase = true)
    val cardElevation = if (isEnded) 2.dp else 6.dp
    val imageSaturation = if (isEnded) 0f else 1f // 0f for grayscale, 1f for normal

    Card(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(0.75f) // Adjust this ratio to match your design preference
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp), // More rounded corners for modern look
        colors = CardDefaults.cardColors(
            containerColor = CardWhite
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = cardElevation)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // --- Image ---
            // Extract first image from description if imageUrl is not available
            val firstImageFromDescription = HtmlContentParser.parseImages(contest.description).firstOrNull()
            val imageUrl = contest.imageUrl ?: firstImageFromDescription ?: "https://via.placeholder.com/600x800.png?text=No+Image"
            
            Image(
                painter = rememberAsyncImagePainter(
                    ImageRequest.Builder(LocalContext.current)
                        .data(data = imageUrl)
                        .apply(block = fun ImageRequest.Builder.() {
                            crossfade(true)
                        }).build()
                ),
                contentDescription = contest.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
                colorFilter = ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(imageSaturation) })
            )

            // --- Title Overlay (Bottom of Image) ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(Color.Black.copy(alpha = 0.6f))
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text(
                    text = contest.name,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // --- Status Badge (Top Right) ---
            val statusText = contest.status
            val badgeColor = when (contest.status.lowercase()) {
                "active" -> Color(0xFFF9A825) // Yellowish Gold
                "ended" -> Color.Gray
                "upcoming" -> Color(0xFF7E57C2) // Purple like
                else -> Color.Gray
            }
            Surface(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp),
                shape = RoundedCornerShape(6.dp),
                color = badgeColor,
                contentColor = Color.White
            ) {
                Text(
                    text = statusText.uppercase(),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            // --- Time Left Overlay (Bottom Right - Only for Active) ---
            if (contest.status.equals("active", ignoreCase = true)) {
                // Calculate time left based on end date
                val timeLeft = calculateTimeLeft(contest.endDate)
                if (timeLeft.isNotEmpty()) {
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(end = 12.dp, bottom = 40.dp), // Position above title overlay
                        horizontalAlignment = Alignment.End
                    ) {
                        val timeParts = timeLeft.split(' ')
                        timeParts.forEach { part ->
                            if (part.isNotBlank()) {
                                Text(
                                    text = "Time Left: $part",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    textAlign = TextAlign.End
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ContestCardPreview() {
    ContestCard(
        contest = com.example.picchallenge.data.model.Contest(
            id = 1,
            name = "Sample Contest",
            startDate = "2024-01-01",
            endDate = "2024-01-31",
            voteStartDate = "2024-01-15",
            registerEndDate = "2024-01-10",
            description = "This is a sample contest",
            imagePerUser = 5,
            voteFrequency = 1,
            galleryLayout = 1,
            contestMode = 1,
            status = "active",
            imageUrl = "https://via.placeholder.com/600x800.png?text=Sample+Contest"
        ),
        onClick = {}
    )
}

/**
 * Calculates time left until contest end date
 * Returns formatted string like "2d 12h" or "5h 30m"
 */
private fun calculateTimeLeft(endDate: String): String {
    return try {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val end = dateFormat.parse(endDate)
        val now = Date()
        
        if (end == null || end.before(now)) {
            return ""
        }
        
        val diff = end.time - now.time
        val days = diff / (24 * 60 * 60 * 1000)
        val hours = (diff % (24 * 60 * 60 * 1000)) / (60 * 60 * 1000)
        val minutes = (diff % (60 * 60 * 1000)) / (60 * 1000)
        
        val parts = mutableListOf<String>()
        if (days > 0) parts.add("${days}d")
        if (hours > 0) parts.add("${hours}h")
        if (days == 0L && minutes > 0) parts.add("${minutes}m")
        
        parts.joinToString(" ")
    } catch (e: Exception) {
        ""
    }
}
