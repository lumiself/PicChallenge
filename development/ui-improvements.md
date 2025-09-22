// This file contains the complete code for a highly visual Android app screen,
// structured into multiple classes and layouts for clarity and reusability.

import android.graphics.ColorFilter
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView

// --- 1. DATA MODEL ---
// Data class for a single contest item.
data class Contest(
val id: String,
val title: String,
val imageUrl: String,
val status: String, // "active" or "ended"
val timeLeft: String? = null
)

// --- 2. RECYCLERVIEW ADAPTER ---
// Adapts the contest data to the RecyclerView grid.
class ContestAdapter(private val contests: List<Contest>) :
RecyclerView.Adapter<ContestAdapter.ContestViewHolder>() {

    class ContestViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val image: ImageView = view.findViewById(R.id.contestImage)
        val title: TextView = view.findViewById(R.id.contestTitle)
        val badge: TextView = view.findViewById(R.id.statusBadge)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContestViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_contest_card, parent, false)
        return ContestViewHolder(view)
    }

    override fun onBindViewHolder(holder: ContestViewHolder, position: Int) {
        val contest = contests[position]
        val context = holder.itemView.context
        val isEnded = contest.status == "ended"

        holder.title.text = contest.title

        // NOTE: For a real app, you would use an image loading library like Glide or Coil here.
        // For this example, we'll assume a drawable resource or a simple placeholder.
        // For example: Glide.with(context).load(contest.imageUrl).into(holder.image)

        // Visual Logic for Minimum Text Design
        if (isEnded) {
            // 1. Image Grayscale/Desaturation (Visual Status)
            holder.image.setColorFilter(
                ContextCompat.getColor(context, android.R.color.darker_gray),
                PorterDuff.Mode.MULTIPLY // Applies a color filter
            )
            // 2. Badge Styling
            holder.badge.text = "ENDED"
            holder.badge.setBackgroundResource(R.drawable.badge_background_ended)

        } else {
            // Active Contest
            holder.image.colorFilter = null // Removes filter
            holder.badge.text = "TIME LEFT: ${contest.timeLeft}"
            holder.badge.setBackgroundResource(R.drawable.badge_background_active)
        }
    }

    override fun getItemCount() = contests.size
}

// --- 3. MAIN ACTIVITY ---
// The main screen that hosts the RecyclerView and other UI elements.
class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ContestAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.contestsRecyclerView)

        // Set up the two-column grid layout
        recyclerView.layoutManager = GridLayoutManager(this, 2)

        // Load mock data for demonstration
        val mockContests = createMockContests()

        // Set the adapter for the RecyclerView
        adapter = ContestAdapter(mockContests)
        recyclerView.adapter = adapter
    }

    private fun createMockContests(): List<Contest> {
        // This is sample data. In a real app, this would come from a network request.
        return listOf(
            Contest("1", "Nature's Beauty", "https://example.com/image1.jpg", "active", "2d"),
            Contest("2", "Urban Explorer", "https://example.com/image2.jpg", "active", "12h"),
            Contest("3", "Street Life", "https://example.com/image3.jpg", "ended"),
            Contest("4", "Golden Hour", "https://example.com/image4.jpg", "ended"),
            Contest("5", "Summer Vibe", "https://example.com/image5.jpg", "active", "7d"),
            Contest("6", "Old Town", "https://example.com/image6.jpg", "ended")
        )
    }
}
