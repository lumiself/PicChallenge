\
# Guide: Creating a Visual Contest Page in Jetpack Compose

This guide provides detailed instructions to create a contest page with a visual grid layout, similar to the provided design, using Jetpack Compose.

## 0. Target UI Description and Comparison

This section describes the target User Interface (UI) based on the provided image and highlights its differences from the previous UI of the `ContestListScreen.kt` before the recent refactoring.

### Description of the Target UI (from Image)

The desired UI for the contest listing page, as shown in the reference image, has the following key characteristics:

*   **Layout:** A two-column vertical grid displaying contest cards.
*   **Contest Card Appearance:**
    *   **Prominent Image:** Each card is dominated by a large image that fills most of the card's area, with a specific aspect ratio (appears taller than wider, e.g., 0.75f).
    *   **Rounded Corners:** Cards have noticeably rounded corners (e.g., 12.dp).
    *   **Title Overlay:** The contest title is displayed in white text with a bold font, centered within a semi-transparent dark (blackish) overlay at the bottom edge of the image.
    *   **Status Badge:**
        *   A small, pill-shaped badge is located at the top-right corner of the image.
        *   It displays the contest status (e.g., "Active", "Ended").
        *   The badge has a distinct background color based on the status (e.g., yellowish-gold for "Active", gray for "Ended") with contrasting text (white or black).
    *   **Time Left Indicator (for Active Contests):**
        *   For active contests, text indicating the time remaining (e.g., "Time Left: 2d", "Time Left: 12h") is displayed directly on the image.
        *   This text is typically white, positioned towards the bottom-right but above the title overlay, and can span multiple lines if days and hours are shown separately.
    *   **Visual State for Ended Contests:** Images for "Ended" contests are desaturated (grayscale) and the card might have a flatter appearance (less elevation).
*   **Overall Aesthetic:** Clean, modern, and visually engaging, with a strong emphasis on the contest imagery.

### Comparison with the Previous `ContestListScreen.kt` UI

The previous implementation of `ContestListScreen.kt` (before applying the changes detailed in this guide) had some similarities but differed significantly in visual execution:

1.  **Card Structure and Image Emphasis:**
    *   **Previous:** While it used a two-column grid, the individual `ContestCard` (defined within `ContestListScreen.kt`) was structured with an image at the top, followed by a content area below it containing the title and a "View Contest" button.
    *   **Target:** The new design makes the image the primary element, with information like title, status, and time left overlaid directly on or at the edges of the image. The explicit "View Contest" button is removed in favor of making the entire card clickable.
2.  **Information Overlay vs. Separate Content Area:**
    *   **Previous:** Contest name and status were typically in a `Column` below the image.
    *   **Target:** The new design uses overlays for the title (bottom of image) and places the status badge and time left directly on the image, creating a more integrated look.
3.  **Status Badge Styling:**
    *   **Previous:** Status was displayed, often with a background color, but its styling (shape, exact positioning, font) was different from the distinct top-right pill-shaped badge of the target UI.
    *   **Target:** A clear, consistently styled badge for status (e.g., `Surface` with `RoundedCornerShape`).
4.  **Time Left Display:**
    *   **Previous:** This information was not displayed on the contest cards.
    *   **Target:** A new visual element showing "Time Left" for active contests, enhancing urgency and information at a glance.
5.  **Ended Contest Visuals:**
    *   **Previous:** Applied a grayscale filter for ended contests, which is consistent.
    *   **Target:** Continues the grayscale effect and may use different elevation to make active cards pop more.
6.  **Overall Card Aesthetics:**
    *   **Previous:** Used more standard Material Design card components with distinct sections for image and text content.
    *   **Target:** A more custom, image-focused card design with specific styling for corner rounding, elevation, typography, and overlay effects to match the reference image.
7.  **Clickability:**
    *   **Previous:** Had an explicit "View Contest" button.
    *   **Target:** The entire card is intended to be clickable to navigate to contest details.

By adopting the new `ContestCard` design and integrating it into `ContestListScreen.kt` as described in this guide, the goal is to shift from a more traditional card layout to the visually richer and more integrated presentation shown in the target UI image.

## 1. Define Data Structures

First, define the data structures that will hold the contest information.

### `Contest.kt` (Recommended Structure)
It is highly recommended to adapt your main `Contest` data model to include `ContestStatus` as an enum and an optional `timeLeft` string for full feature compatibility and type safety.

```kotlin
package com.example.picchallenge.data.model // Or your appropriate package

data class Contest(
    val id: String, // Or Int, ensure consistency with your data source
    val title: String, // Use 'title' consistently or map from 'name'
    val imageUrl: String?, // URL for the main image, make nullable for safety
    val status: ContestStatus, // Enum for type safety
    val timeLeft: String? = null, // Formatted time string, e.g., "2d", "2d 12h", "12h"
    val description: String? = null // Keep other necessary fields
    // val participantCount: Int = 0,
)

enum class ContestStatus {
    ACTIVE,
    ENDED,
    UPCOMING
}
```

**Note:** If you are using a different structure for `Contest.kt` (e.g., `id` is `Int`, `status` is `String`, `title` is `name`), you will need to map your existing model to the structure expected by the `ContestCard` within `ContestListScreen.kt`, or make the `ContestCard` more flexible.

## 2. Create the `ContestCard` Composable

This composable will represent each individual item in the contest grid. It should be placed in a shared components directory, e.g., `com.example.picchallenge.ui.components.ContestCard.kt`.

### `ContestCard.kt`

```kotlin
package com.example.picchallenge.ui.components // Or your appropriate package

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
import coil.compose.rememberAsyncImagePainter // Import Coil
import coil.request.ImageRequest
import com.example.picchallenge.data.model.Contest // Assumes this is the new model with enum status
import com.example.picchallenge.data.model.ContestStatus
// import com.example.picchallenge.ui.theme.* // Your app's theme

@Composable
fun ContestCard(
    contest: Contest, // Expects the new Contest model
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    val isEnded = contest.status == ContestStatus.ENDED
    val cardElevation = if (isEnded) 2.dp else 6.dp
    val imageSaturation = if (isEnded) 0f else 1f // 0f for grayscale, 1f for normal

    Card(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(0.75f) // Adjust this ratio to match your design preference
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = cardElevation)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // --- Image ---
            Image(
                painter = rememberAsyncImagePainter(
                    ImageRequest.Builder(LocalContext.current)
                        .data(data = contest.imageUrl ?: "https://via.placeholder.com/600x800.png?text=No+Image")
                        .apply(block = fun ImageRequest.Builder.() {
                            crossfade(true)
                            // placeholder(R.drawable.your_placeholder) // Optional placeholder
                            // error(R.drawable.your_error_image) // Optional error image
                        }).build()
                ),
                contentDescription = contest.title,
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
                    text = contest.title,
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
            val statusText = contest.status.name
            val badgeColor = when (contest.status) {
                ContestStatus.ACTIVE -> Color(0xFFF9A825) // Yellowish Gold
                ContestStatus.ENDED -> Color.Gray
                ContestStatus.UPCOMING -> Color(0xFF7E57C2) // Purple like
            }
            Surface(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp),
                shape = RoundedCornerShape(6.dp),
                color = badgeColor,
                contentColor = Color.White // Or Color.Black depending on badge color
            ) {
                Text(
                    text = statusText.uppercase(),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            // --- Time Left Overlay (Bottom Right - Only for Active) ---
            if (contest.status == ContestStatus.ACTIVE && !contest.timeLeft.isNullOrBlank()) {
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 12.dp, bottom = 40.dp), // Position above title overlay
                    horizontalAlignment = Alignment.End
                ) {
                    val timeParts = contest.timeLeft.split(' ')
                    timeParts.forEach {
                        if (it.isNotBlank()) {
                             Text(
                                text = "Time Left: $it",
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

// Add @Preview functions as in the previous version of the guide for testing
```

## 3. The `ContestListScreen.kt` (Main Screen Integration)

This file is responsible for fetching and displaying the grid of contests using the `ContestCard` created above. The following shows the structure and logic within `ContestListScreen.kt` after the UI update.

### Location: `app/src/main/java/com/example/picchallenge/ui/contest/ContestListScreen.kt`

```kotlin
package com.example.picchallenge.ui.contest

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Warning // Changed icon for empty/error
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
// Your existing Contest model (com.example.picchallenge.data.model.Contest)
// Make sure to distinguish it if you also create the new one from Step 1 for the card.
import com.example.picchallenge.data.model.ContestStatus
import com.example.picchallenge.ui.components.ContestCard // The new ContestCard
import com.example.picchallenge.ui.theme.*
import com.example.picchallenge.ui.viewmodel.ContestViewModel
import com.example.picchallenge.utils.HtmlContentParser
import com.example.picchallenge.utils.NetworkResult

// Mapper function from your ViewModel's Contest model to the Card's expected model
// This is crucial if your ViewModel provides a Contest model different from what ContestCard expects.
fun mapToDisplayContest(
    viewModelContest: com.example.picchallenge.data.model.Contest // Assuming this is your OLD model
): com.example.picchallenge.data.model.Contest { // Assuming this is your NEW model for the card
    val newStatus = when (viewModelContest.status?.lowercase()) { // viewModelContest.status is String?
        "active" -> ContestStatus.ACTIVE
        "ended" -> ContestStatus.ENDED
        "upcoming" -> ContestStatus.UPCOMING
        else -> ContestStatus.ENDED // Default or handle appropriately
    }
    return com.example.picchallenge.data.model.Contest(
        id = viewModelContest.id.toString(), // Ensure ID is String for the card
        title = viewModelContest.name, // Map 'name' to 'title'
        imageUrl = viewModelContest.imageUrl ?: HtmlContentParser.parseImages(viewModelContest.description).firstOrNull(),
        status = newStatus,
        timeLeft = null, // Populate this if your ViewModel/API provides it
        description = viewModelContest.description
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContestListScreen(
    onContestClick: (com.example.picchallenge.data.model.Contest) -> Unit, // Click uses ViewModel's model
    onProfileClick: () -> Unit,
    contestViewModel: ContestViewModel = hiltViewModel()
) {
    val contestsResult by contestViewModel.contests.collectAsState()

    LaunchedEffect(Unit) {
        contestViewModel.loadContests(status = "all")
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Photo Contests", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold) },
                actions = { IconButton(onClick = onProfileClick) { Icon(Icons.Default.Person, contentDescription = "Profile") } },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimaryBlue,
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val result = contestsResult) {
                is NetworkResult.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is NetworkResult.Success -> {
                    val viewModelContests = result.data.data
                    if (viewModelContests.isEmpty()) {
                        EmptyState(message = "No contests found")
                    } else {
                        // Map contests from ViewModel's model to the display model for the Card
                        val displayContests = viewModelContests.map { mapToDisplayContest(it) }
                        ActualContestGrid(
                            contests = displayContests, // Pass the mapped list
                            // Pass the original ViewModel contest to the click handler
                            onOriginalContestClick = { displayContest ->
                                val originalContest = viewModelContests.find { it.id.toString() == displayContest.id }
                                originalContest?.let { onContestClick(it) }
                            }
                        )
                    }
                }
                is NetworkResult.Error -> {
                    ErrorState(
                        message = result.message,
                        onRetry = { contestViewModel.loadContests(status = "all") }
                    )
                }
            }
        }
    }
}

@Composable
private fun ActualContestGrid(
    contests: List<com.example.picchallenge.data.model.Contest>, // Expects list of NEW Display Models
    onOriginalContestClick: (com.example.picchallenge.data.model.Contest) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(contests, key = { it.id }) { contest ->
            ContestCard(
                contest = contest, // Pass the display-ready contest model
                onClick = { onOriginalContestClick(contest) }
            )
        }
    }
}

@Composable
private fun EmptyState(message: String) {
    // ... (EmptyState implementation as before, consider a more relevant icon)
    Box(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CardWhite),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Default.Warning, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(64.dp))
                Spacer(modifier = Modifier.height(16.dp))
                Text(message, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center, color = TextGray)
            }
        }
    }
}

@Composable
private fun ErrorState(message: String?, onRetry: () -> Unit) {
    // ... (ErrorState implementation as before, consider a more relevant icon)
    Box(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CardWhite),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Default.Warning, contentDescription = null, tint = StatusRed, modifier = Modifier.size(64.dp))
                Spacer(modifier = Modifier.height(16.dp))
                Text("Error loading contests", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextGray)
                Spacer(modifier = Modifier.height(8.dp))
                Text(message ?: "Unknown error", style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center, color = TextGray.copy(alpha = 0.7f))
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onRetry) { Text("Retry") }
            }
        }
    }
}

```

### Explanation of Changes in `ContestListScreen.kt`:

1.  **ViewModel and Data Fetching:** It continues to use `ContestViewModel` to fetch a `NetworkResult` containing a list of contests (presumably your original `Contest` model where `status` is a `String`).
2.  **Network State Handling:** The `when` block handles `Loading`, `Success`, and `Error` states appropriately, showing a progress indicator, the contest grid, or an error message.
3.  **Data Model Mapping (`mapToDisplayContest`):**
    *   A crucial step is mapping your ViewModel's `Contest` model to the `Contest` model expected by the new `ContestCard` (which ideally has `status` as an enum and includes `timeLeft`).
    *   The `mapToDisplayContest` function is introduced for this conversion. It converts the `status` string to a `ContestStatus` enum and maps `name` to `title`. It sets `timeLeft` to `null` for now; this should be populated from your data source if available.
    *   This mapping occurs in the `NetworkResult.Success` block before rendering the grid.
4.  **`ActualContestGrid` Composable:**
    *   The `LazyVerticalGrid` logic is encapsulated in a private `ActualContestGrid` composable.
    *   This grid now takes the list of *mapped* `Contest` objects (the display model).
    *   It uses the new `com.example.picchallenge.ui.components.ContestCard` for each item.
5.  **Click Handling:** The `onContestClick` lambda needs to receive the original `Contest` object from the ViewModel, not the mapped one, if subsequent navigation or logic relies on the original model structure. The example above shows how to find the original contest when a card (which uses the display model) is clicked.
6.  **Prerequisites:** This integration assumes you have:
    *   Defined the `ContestStatus` enum (as in Step 1 of this guide).
    *   Created the new `ContestCard.kt` in the `ui.components` package with the code from Step 2 of this guide.

## 4. Key Considerations & Next Steps (Recap)

*   **Image Loading Library (Coil):** Ensure Coil is set up.
*   **Data Model Unification (Highly Recommended):** The most robust solution is to update your primary `Contest` data model in `com.example.picchallenge.data.model.Contest.kt` to match the structure expected by `ContestCard` (enum status, `timeLeft` field). This simplifies code and avoids manual mapping in the UI layer. Your `ContestViewModel` should then provide data in this updated format.
*   **`timeLeft` Data:** For the "Time Left" feature to work, your API and ViewModel need to provide this data.
*   **Styling, Navigation, Accessibility, Error/Loading states:** Continue to refine these aspects as per your app's requirements.

This guide provides a foundational structure. You will need to adapt it to your specific project structure, theme, and data sources.
