# Login and Upload Functionality Removal Todo List

## Phase 1: Remove Authentication API and Services
- [ ] Remove AuthApiService.kt
- [ ] Remove JwtAuthInterceptor.kt  
- [ ] Clean up NetworkModule.kt (remove auth interceptor)
- [ ] Clean up AuthNetworkModule.kt

## Phase 2: Remove Authentication Repository and Models
- [ ] Remove AuthRepository.kt
- [ ] Remove LoginRequest/LoginResponse models from User.kt
- [ ] Remove authentication-related models

## Phase 3: Remove Upload Functionality
- [ ] Remove PhotoUploadScreen.kt
- [ ] Remove upload methods from PhotoContestApiService.kt
- [ ] Remove upload methods from PhotoRepository.kt
- [ ] Remove upload-related models

## Phase 4: Remove Login UI Components
- [ ] Remove LoginScreen.kt
- [ ] Remove LoginViewModel.kt
- [ ] Remove login test screens
- [ ] Clean up MainActivity.kt login references

## Phase 5: Clean Up Navigation
- [ ] Remove login navigation from Navigation.kt
- [ ] Remove upload callbacks from ContestDetailScreen.kt
- [ ] Remove authentication checks

## Phase 6: Update Dependency Injection
- [ ] Clean up RepositoryModule.kt
- [ ] Clean up NetworkModule.kt
- [ ] Remove auth-related dependencies

## Phase 7: Final Cleanup
- [ ] Remove unused imports
- [ ] Clean up strings and resources
- [ ] Test app functionality
