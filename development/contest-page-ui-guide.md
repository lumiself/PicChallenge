\
# Guide: Creating a Visual Contest Page in Jetpack Compose

This guide provides detailed instructions to create a contest page with a visual grid layout, similar to the provided design, using Jetpack Compose.

## 1. Define Data Structures

First, define the data structures that will hold the contest information.

### `Contest.kt`
Create a data class for your contest and an enum for its status:

```kotlin
package com.example.picchallenge.data.model // Or your appropriate package

data class Contest(
    val id: String, // Unique identifier
    val title: String,
    val imageUrl: String, // URL for the main image
    val status: ContestStatus,
    val timeLeft: String? = null, // Formatted time string, e.g., "2d", "2d 12h", "12h"
    // Add any other relevant fields:
    // val description: String? = null,
    // val participantCount: Int = 0,
)

enum class ContestStatus {
    ACTIVE,
    ENDED,
    UPCOMING // Example of another possible status
}
```

## 2. Create the `ContestCard` Composable

This composable will represent each individual item in the contest grid.

### `ContestCard.kt`

```kotlin
package com.example.picchallenge.ui.components // Or your appropriate package

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import com.example.picchallenge.data.model.Contest
import com.example.picchallenge.data.model.ContestStatus
// import com.example.picchallenge.ui.theme.* // Your app's theme

@Composable
fun ContestCard(contest: Contest, modifier: Modifier = Modifier, onClick: () -> Unit = {}) {
    val isEnded = contest.status == ContestStatus.ENDED
    val cardElevation = if (isEnded) 2.dp else 6.dp
    val imageSaturation = if (isEnded) 0f else 1f // 0f for grayscale, 1f for normal

    Card(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(0.75f), // Adjust this ratio to match your design preference
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = cardElevation),
        onClick = onClick
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // --- Image ---
            Image(
                painter = rememberAsyncImagePainter(
                    ImageRequest.Builder(LocalContext.current)
                        .data(data = contest.imageUrl)
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
                    .background(
                        Color.Black.copy(alpha = 0.6f),
                        // To get rounded corners only at the bottom if the card itself wasn't clipped
                        // shape = RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp)
                    )
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
            if (contest.status == ContestStatus.ACTIVE || contest.status == ContestStatus.ENDED) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp),
                    shape = RoundedCornerShape(6.dp),
                    color = if (contest.status == ContestStatus.ACTIVE) Color(0xFFF9A825) /* Yellowish Gold */ else Color.Gray,
                    contentColor = Color.Black // Or Color.White depending on badge color
                ) {
                    Text(
                        text = contest.status.name.uppercase(), // "ACTIVE" or "ENDED"
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // --- Time Left Overlay (Bottom Right - Only for Active) ---
            if (contest.status == ContestStatus.ACTIVE && !contest.timeLeft.isNullOrBlank()) {
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        // Adjust padding to position it above the title overlay if they overlap
                        .padding(end = 12.dp, bottom = 40.dp), // Increased bottom padding
                    horizontalAlignment = Alignment.End
                ) {
                    // Assuming timeLeft might be "2d 12h" or "2d" or "12h"
                    val timeParts = contest.timeLeft.split(' ')
                    timeParts.forEachIndexed { index, part ->
                        if (part.isNotBlank()) {
                             Text(
                                text = "Time Left: $part",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                textAlign = TextAlign.End,
                                // Add a slight shadow or darker background if text visibility is an issue
                                // style = LocalTextStyle.current.copy(shadow = Shadow(color = Color.Black.copy(alpha=0.7f), offset = Offset(1f,1f), blurRadius = 2f))
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 200)
@Composable
fun PreviewContestCardActive() {
    // MaterialTheme { // Apply your app's theme for accurate preview
        ContestCard(
            contest = Contest(
                id = "1",
                title = "Nature's Beauty Extended Title",
                imageUrl = "https://example.com/image.jpg", // Replace with a real test URL or placeholder logic
                status = ContestStatus.ACTIVE,
                timeLeft = "2d 12h"
            )
        )
    // }
}

@Preview(showBackground = true, widthDp = 200)
@Composable
fun PreviewContestCardEnded() {
    // MaterialTheme { // Apply your app's theme for accurate preview
        ContestCard(
            contest = Contest(
                id = "2",
                title = "Street Life Adventures",
                imageUrl = "https://example.com/image2.jpg",
                status = ContestStatus.ENDED,
                timeLeft = null
            )
        )
    // }
}
```

## 3. Create the `ContestGrid` Composable

This composable will arrange `ContestCard`s in a vertical grid.

### `ContestGrid.kt`

```kotlin
package com.example.picchallenge.ui.screens.contests // Or your appropriate package

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel // For ViewModel integration
// import com.example.picchallenge.viewmodel.ContestViewModel // Your ViewModel
import com.example.picchallenge.data.model.Contest
import com.example.picchallenge.data.model.ContestStatus
import com.example.picchallenge.ui.components.ContestCard
// import com.example.picchallenge.ui.theme.* // Your app's theme

@Composable
fun ContestGrid(
    contests: List<Contest>,
    modifier: Modifier = Modifier,
    onContestClick: (Contest) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2), // 2 columns as in the design
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(contests, key = { it.id }) { contest ->
            ContestCard(
                contest = contest,
                onClick = { onContestClick(contest) }
            )
        }
    }
}

// Example Screen using the ContestGrid
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContestScreen(
    // contestViewModel: ContestViewModel = viewModel(), // Hilt or manual ViewModel
    // onNavigateToContestDetail: (String) -> Unit
) {
    // val contests by contestViewModel.contests.collectAsStateWithLifecycle() // Example with ViewModel
    // Dummy data for example:
    val sampleContests = listOf(
        Contest("1", "Nature's Beauty", "url1", ContestStatus.ACTIVE, "2d 12h"),
        Contest("2", "Active Adventure", "url2", ContestStatus.ACTIVE, "5d"),
        Contest("3", "Urban Explorer", "url3", ContestStatus.ACTIVE, "22h"),
        Contest("4", "Street Life", "url4", ContestStatus.ENDED),
        Contest("5", "Golden Hour", "url5", ContestStatus.ENDED),
        Contest("6", "Monochrome World", "url6", ContestStatus.ENDED)
    )

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Photo Contests") })
        }
    ) { paddingValues ->
        ContestGrid(
            contests = sampleContests, // Replace with 'contests' from ViewModel
            modifier = Modifier.padding(paddingValues),
            onContestClick = { contest ->
                // onNavigateToContestDetail(contest.id)
                println("Clicked on: ${contest.title}")
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewContestScreen() {
    // MaterialTheme { // Apply your app's theme
        ContestScreen()
    // }
}

```

## 4. Key Considerations & Next Steps

*   **Image Loading Library (Coil):**
    *   The example uses [Coil](https://coil-kt.github.io/coil/compose/) for image loading. Add the dependency to your `app/build.gradle.kts`:
        ```gradle
        implementation("io.coil-kt:coil-compose:2.6.0") // Check for the latest version
        ```
    *   Initialize Coil in your `Application` class if needed for custom configurations.
*   **Data Fetching & State Management:**
    *   Replace the sample data in `ContestScreen` with actual data fetched from your ViewModel.
    *   Use `StateFlow` or `LiveData` in your ViewModel to expose the list of contests and collect it as state in your composable (e.g., using `collectAsStateWithLifecycle()`).
*   **`timeLeft` Formatting:**
    *   The `timeLeft` string ("2d 12h", "5d", "22h") needs to be provided by your data source or formatted in your ViewModel before being passed to the `ContestCard`. The card attempts a basic split for multi-line display.
*   **Styling & Theme:**
    *   Adjust fonts, colors, paddings, and shapes to perfectly match your desired UI. Utilize your app's `MaterialTheme` for consistency. The provided colors for the status badge are examples.
*   **Navigation:**
    *   Implement navigation from the `ContestCard` to a contest detail screen using the `onContestClick` lambda.
*   **Accessibility:**
    *   Ensure proper `contentDescription` for images and interactive elements for screen readers.
*   **Error and Loading States:**
    *   Implement UI states to handle loading (e.g., shimmer placeholders) while contests are being fetched and error states if data fetching fails.
*   **Placeholders:**
    *   Consider adding placeholder drawables in `Coil` for a better user experience while images are loading or if they fail to load.

This guide provides a foundational structure. You will need to adapt it to your specific project structure, theme, and data sources.
