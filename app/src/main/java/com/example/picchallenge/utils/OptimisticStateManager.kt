package com.example.picchallenge.utils

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Manages optimistic UI updates with rollback capability
 */
class OptimisticStateManager<T> {
    private val _currentState = MutableStateFlow<T?>(null)
    val currentState: StateFlow<T?> = _currentState.asStateFlow()

    private val _pendingOptimisticUpdates = MutableStateFlow<List<OptimisticUpdate<T>>>(emptyList())
    val pendingOptimisticUpdates: StateFlow<List<OptimisticUpdate<T>>> = _pendingOptimisticUpdates.asStateFlow()

    /**
     * Apply an optimistic update to the current state
     * @param update The update to apply optimistically
     * @param onSuccess Called when the server confirms the update
     * @param onError Called when the server rejects the update (triggers rollback)
     */
    suspend fun applyOptimisticUpdate(
        update: OptimisticUpdate<T>,
        onSuccess: suspend () -> Result<Unit>,
        onError: suspend (Throwable) -> Unit = { rollback(update) }
    ) {
        // Apply optimistic update immediately
        applyUpdate(update)
        
        try {
            // Try to confirm with server
            val result = onSuccess()
            
            if (result.isSuccess) {
                // Server confirmed, remove from pending
                removePendingUpdate(update)
            } else {
                // Server rejected, rollback
                rollback(update)
                onError(Exception("Server rejected update"))
            }
        } catch (e: Exception) {
            // Network/server error, rollback
            rollback(update)
            onError(e)
        }
    }

    private fun applyUpdate(update: OptimisticUpdate<T>) {
        _currentState.value?.let { current ->
            _currentState.value = update.apply(current)
            _pendingOptimisticUpdates.value = _pendingOptimisticUpdates.value + update
        }
    }

    private fun rollback(update: OptimisticUpdate<T>) {
        _currentState.value?.let { current ->
            _currentState.value = update.rollback(current)
            removePendingUpdate(update)
        }
    }

    private fun removePendingUpdate(update: OptimisticUpdate<T>) {
        _pendingOptimisticUpdates.value = _pendingOptimisticUpdates.value - update
    }

    fun setInitialState(state: T) {
        _currentState.value = state
    }

    fun updateState(state: T) {
        _currentState.value = state
    }
}

/**
 * Represents an optimistic update that can be applied and rolled back
 */
interface OptimisticUpdate<T> {
    /**
     * Apply the optimistic update to the current state
     */
    fun apply(currentState: T): T
    
    /**
     * Rollback the optimistic update from the current state
     */
    fun rollback(currentState: T): T
    
    /**
     * Unique identifier for this update
     */
    val id: String
}

/**
 * Specific implementation for photo voting optimistic updates
 */
data class PhotoVoteOptimisticUpdate(
    override val id: String,
    val photoId: Int,
    val previousVoteCount: Int,
    val newVoteCount: Int,
    val userVoted: Boolean
) : OptimisticUpdate<PhotoVoteState> {
    
    override fun apply(currentState: PhotoVoteState): PhotoVoteState {
        return currentState.copy(
            voteCount = newVoteCount,
            userVoted = userVoted,
            lastOptimisticUpdate = this
        )
    }
    
    override fun rollback(currentState: PhotoVoteState): PhotoVoteState {
        return currentState.copy(
            voteCount = previousVoteCount,
            userVoted = !userVoted,
            lastOptimisticUpdate = null
        )
    }
}

/**
 * Specific implementation for photo rating optimistic updates
 */
data class PhotoRatingOptimisticUpdate(
    override val id: String,
    val photoId: Int,
    val previousRating: Float?,
    val newRating: Float,
    val previousAverageRating: Float,
    val newAverageRating: Float
) : OptimisticUpdate<PhotoRatingState> {
    
    override fun apply(currentState: PhotoRatingState): PhotoRatingState {
        return currentState.copy(
            userRating = newRating,
            averageRating = newAverageRating,
            ratingCount = if (previousRating == null) currentState.ratingCount + 1 else currentState.ratingCount,
            lastOptimisticUpdate = this
        )
    }
    
    override fun rollback(currentState: PhotoRatingState): PhotoRatingState {
        return currentState.copy(
            userRating = previousRating,
            averageRating = previousAverageRating,
            ratingCount = if (previousRating == null) currentState.ratingCount - 1 else currentState.ratingCount,
            lastOptimisticUpdate = null
        )
    }
}

/**
 * State classes for photo voting and rating
 */
data class PhotoVoteState(
    val voteCount: Int,
    val userVoted: Boolean,
    val lastOptimisticUpdate: PhotoVoteOptimisticUpdate? = null
)

data class PhotoRatingState(
    val userRating: Float?,
    val averageRating: Float,
    val ratingCount: Int,
    val lastOptimisticUpdate: PhotoRatingOptimisticUpdate? = null
)
