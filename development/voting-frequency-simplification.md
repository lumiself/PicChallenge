# Voting Frequency Simplification - Implementation Notes

## Overview
This document outlines the changes made to simplify the voting frequency implementation in the PicChallenge app. The original implementation supported multiple voting frequencies (0-6) but was causing issues with vote frequency 6 showing 24-hour countdowns instead of per-photo restrictions.

## Changes Made

### 1. VoteTrackingRepository Simplification
**File**: `app/src/main/java/com/example/picchallenge/data/repository/VoteTrackingRepository.kt`

**Changes**:
- Removed complex `when` statement with multiple voting frequency cases
- Implemented simplified daily voting logic as default for all contests
- Now enforces 24-hour voting restrictions regardless of WordPress `voteFrequency` setting

**Logic**:
```kotlin
// Simplified implementation: Default to daily voting for all contests
val lastVoteTime = voteHistory
    .filter { it.contestId == contestId }
    .maxOfOrNull { it.timestamp } ?: 0

val hoursSinceLastVote = TimeUnit.MILLISECONDS.toHours(currentTime - lastVoteTime)

return if (hoursSinceLastVote < 24 && lastVoteTime > 0) {
    val remainingHours = 24 - hoursSinceLastVote
    VoteEligibilityResult.NotAllowed(
        "You can vote again in $remainingHours hours.",
        remainingHours.toInt()
    )
} else {
    VoteEligibilityResult.Allowed
}
```

### 2. ContestViewModel Updates
**File**: `app/src/main/java/com/example/picchallenge/ui/viewmodel/ContestViewModel.kt`

**Changes**:
- Modified `checkVotingEligibility()` to ignore `voteFrequency` parameter
- Updated `updateVotingEligibilityForContest()` to use daily voting logic
- Both methods now explicitly use `VOTE_FREQUENCY_DAILY` constant

**Key Changes**:
```kotlin
// For now, ignore the voteFrequency parameter and use daily voting logic
return voteTrackingRepository.canVote(contestId, photoId, VoteTrackingRepository.VOTE_FREQUENCY_DAILY)
```

## Current Behavior

### Voting Restrictions
- **24-hour cooldown**: Users can vote once per contest every 24 hours
- **Contest-specific**: Voting restrictions apply per contest, not globally
- **Consistent messaging**: All users see "You can vote again in X hours" message

### User Experience
- Clear visual feedback when voting is restricted
- Countdown timer showing remaining hours until next vote
- Proper error handling with user-friendly messages

## Future Implementation Plan

When ready to implement the full voting frequency feature, the following steps will be needed:

### 1. Restore Complex Logic
- Re-implement the full `when` statement in `VoteTrackingRepository.canVote()`
- Add proper handling for each vote frequency type (0-6)

### 2. Update ContestViewModel
- Remove hardcoded `VOTE_FREQUENCY_DAILY` usage
- Restore dynamic vote frequency handling

### 3. Enhanced UI
- Add vote frequency display in contest details
- Implement different UI states for different voting types
- Add per-photo voting indicators

### 4. Testing
- Test each vote frequency scenario
- Verify WordPress integration works correctly
- Ensure backward compatibility

## Benefits of Current Simplification

1. **Consistency**: All contests now behave the same way
2. **Reliability**: Eliminates confusion from mixed voting behaviors
3. **User Understanding**: Simple 24-hour rule is easy to understand
4. **Development Focus**: Allows focus on other app features
5. **Foundation**: Provides solid base for future enhancements

## Technical Notes

- Voting history is still tracked per photo (photoId is recorded)
- Data structure supports future per-photo voting implementation
- No breaking changes to API or data models
- Backward compatible with existing vote records

## Files Modified

1. `VoteTrackingRepository.kt` - Core voting logic
2. `ContestViewModel.kt` - Voting eligibility checks
3. `ContestDetailScreen.kt` - UI presentation (no changes needed)

## Next Steps

1. Monitor user feedback on 24-hour voting restriction
2. Collect requirements for specific voting frequency needs
3. Plan implementation timeline for full feature
4. Consider A/B testing different voting frequencies
