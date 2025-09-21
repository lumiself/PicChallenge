# 📱 Photo Contest REST API - Final Documentation

## 🎯 Overview

Complete REST API documentation for the Photo Contest WordPress Plugin, organized by user roles and authentication requirements. All endpoints are tested and ready for Android app integration.

**Base URL**: `http://localhost:8881/wp-json/photo-contest/v1`

---

## 🗂️ Table of Contents

1. [Authentication](#-authentication)
2. [Public Endpoints (No Authentication Required)](#-public-endpoints-no-authentication-required)
3. [Contestant Endpoints (JWT Authentication Required)](#-contestant-endpoints-jwt-authentication-required)
4. [Voter Endpoints (No Authentication Required)](#-voter-endpoints-no-authentication-required)
5. [Admin Endpoints (Admin Privileges Required)](#-admin-endpoints-admin-privileges-required)
6. [Response Formats](#-response-formats)
7. [Error Handling](#-error-handling)
8. [Rate Limiting](#-rate-limiting)

---

## 🔐 Authentication

### JWT Token Authentication
**Endpoint**: `POST /wp-json/jwt-auth/v1/token`

**Request Body**:
```json
{
  "username": "wordpress_username",
  "password": "wordpress_password"
}
```

**Success Response**:
```json
{
  "token": "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9...",
  "user_email": "user@example.com",
  "user_nicename": "username",
  "user_display_name": "User Name"
}
```

**Headers for Authenticated Requests**:
```
Authorization: Bearer {jwt_token}
```

---

## 🌐 Public Endpoints (No Authentication Required)

### 📋 Contest Management

#### **Get All Contests**
- **Method**: `GET`
- **Endpoint**: `/contests`
- **Description**: Retrieve all photo contests with pagination support
- **Query Parameters**:
  - `page` (integer, optional): Page number (default: 1)
  - `per_page` (integer, optional): Items per page (default: 20)
  - `status` (string, optional): Filter by status - `active`, `ended`, `all` (default: `active`)

**Success Response**:
```json
{
  "data": [
    {
      "id": 1,
      "name": "Nature Photography Contest 2024",
      "start_date": "2024-01-01",
      "end_date": "2024-12-31",
      "vote_start_date": "2024-06-01",
      "register_end_date": "2024-11-30",
      "description": "Showcase the beauty of nature through your lens",
      "image_per_user": 5,
      "vote_frequency": 1,
      "gallery_layout": 1,
      "contest_mode": 1,
      "status": "active"
    }
  ],
  "total": 15,
  "page": 1,
  "per_page": 20,
  "pages": 1
}
```

#### **Get Contest Details**
- **Method**: `GET`
- **Endpoint**: `/contests/{id}`
- **Description**: Retrieve detailed information about a specific contest
- **Path Parameters**:
  - `id` (integer, required): Contest ID

**Success Response**:
```json
{
  "id": 1,
  "name": "Nature Photography Contest 2024",
  "start_date": "2024-01-01",
  "end_date": "2024-12-31",
  "vote_start_date": "2024-06-01",
  "register_end_date": "2024-11-30",
  "description": "Showcase the beauty of nature through your lens",
  "image_per_user": 5,
  "vote_frequency": 1,
  "gallery_layout": 1,
  "contest_mode": 1,
  "status": "active"
}
```

#### **Get Contest Photos**
- **Method**: `GET`
- **Endpoint**: `/contests/{id}/photos`
- **Description**: Retrieve all photos submitted to a specific contest
- **Path Parameters**:
  - `id` (integer, required): Contest ID
- **Query Parameters**:
  - `page` (integer, optional): Page number (default: 1)
  - `per_page` (integer, optional): Items per page (default: 20)
  - `order` (string, optional): Sort order - `date`, `votes`, `random` (default: `date`)
  - `category` (integer, optional): Filter by category ID

**Success Response**:
```json
{
  "data": [
    {
      "id": 101,
      "title": "Sunset at the Beach",
      "description": "Beautiful sunset captured during golden hour",
      "url": "http://localhost:8881/wp-content/uploads/2024/01/sunset-beach.jpg",
      "thumbnail": "http://localhost:8881/wp-content/uploads/2024/01/sunset-beach-150x150.jpg",
      "medium": "http://localhost:8881/wp-content/uploads/2024/01/sunset-beach-300x200.jpg",
      "large": "http://localhost:8881/wp-content/uploads/2024/01/sunset-beach-1024x683.jpg",
      "votes": 45,
      "views": 234,
      "author": "John Doe",
      "author_id": 5,
      "date": "2024-01-15 14:30:00",
      "contest_id": 1,
      "category_id": 3
    }
  ],
  "total": 127,
  "page": 1,
  "per_page": 20,
  "pages": 7
}
```

### 📸 Photo Management

#### **Get Popular Photos**
- **Method**: `GET`
- **Endpoint**: `/photos/popular`
- **Description**: Retrieve the most popular photos based on votes and views
- **Query Parameters**:
  - `limit` (integer, optional): Number of photos (default: 20)
  - `timeframe` (string, optional): Filter by time - `all_time`, `this_week`, `this_month` (default: `all_time`)

**Success Response**:
```json
[
  {
    "id": 101,
    "title": "Sunset at the Beach",
    "description": "Beautiful sunset captured during golden hour",
    "url": "http://localhost:8881/wp-content/uploads/2024/01/sunset-beach.jpg",
    "thumbnail": "http://localhost:8881/wp-content/uploads/2024/01/sunset-beach-150x150.jpg",
    "votes": 156,
    "views": 892,
    "author": "John Doe",
    "contest_id": 1,
    "category_id": 3
  }
]
```

#### **Get Recent Photos**
- **Method**: `GET`
- **Endpoint**: `/photos/recent`
- **Description**: Retrieve recently uploaded photos across all contests
- **Query Parameters**:
  - `limit` (integer, optional): Number of photos (default: 20)

**Success Response**: Same format as popular photos

#### **Get Photo Details**
- **Method**: `GET`
- **Endpoint**: `/photos/{id}`
- **Description**: Retrieve detailed information about a specific photo
- **Path Parameters**:
  - `id` (integer, required): Photo ID

**Success Response**:
```json
{
  "id": 101,
  "title": "Sunset at the Beach",
  "description": "Beautiful sunset captured during golden hour",
  "url": "http://localhost:8881/wp-content/uploads/2024/01/sunset-beach.jpg",
  "thumbnail": "http://localhost:8881/wp-content/uploads/2024/01/sunset-beach-150x150.jpg",
  "medium": "http://localhost:8881/wp-content/uploads/2024/01/sunset-beach-300x200.jpg",
  "large": "http://localhost:8881/wp-content/uploads/2024/01/sunset-beach-1024x683.jpg",
  "votes": 45,
  "views": 234,
  "author": "John Doe",
  "author_id": 5,
  "date": "2024-01-15 14:30:00",
  "contest_id": 1,
  "category_id": 3
}
```

### 🗳️ Voting & Rating

#### **Vote for Photo**
- **Method**: `POST`
- **Endpoint**: `/photos/{id}/vote`
- **Description**: Cast a vote for a specific photo
- **Path Parameters**:
  - `id` (integer, required): Photo ID
- **Body Parameters**:
  - `email` (string, optional): Voter email address

**Success Response**:
```json
{
  "success": true,
  "message": "Vote recorded successfully",
  "new_vote_count": 46
}
```

#### **Rate Photo**
- **Method**: `POST`
- **Endpoint**: `/photos/{id}/rate`
- **Description**: Rate a photo on a scale of 1-10
- **Path Parameters**:
  - `id` (integer, required): Photo ID
- **Body Parameters**:
  - `rating` (integer, required): Rating value (1-10)

**Success Response**:
```json
{
  "success": true,
  "message": "Rating recorded successfully",
  "new_average": 8.5,
  "total_votes": 12
}
```

### 🔍 Search & Categories

#### **Get Categories**
- **Method**: `GET`
- **Endpoint**: `/categories`
- **Description**: Retrieve all available photo categories
- **Query Parameters**:
  - `contest_id` (integer, optional): Filter categories by contest ID

**Success Response**:
```json
[
  {
    "id": 1,
    "name": "Nature",
    "contest_id": 1
  },
  {
    "id": 2,
    "name": "Portrait",
    "contest_id": 1
  }
]
```

#### **Search Photos**
- **Method**: `GET`
- **Endpoint**: `/search`
- **Description**: Search for photos by keywords
- **Query Parameters**:
  - `q` (string, required): Search query
  - `contest_id` (integer, optional): Filter by contest ID
  - `category` (integer, optional): Filter by category ID

**Success Response**: Same format as contest photos

---

## 👤 Contestant Endpoints (JWT Authentication Required)

### 📤 Photo Management

#### **Upload Photo**
- **Method**: `POST`
- **Endpoint**: `/photos/upload`
- **Description**: Upload a new photo to a contest
- **Headers**: `Authorization: Bearer {jwt_token}`
- **Form Data**:
  - `image` (file, required): Image file to upload
  - `contest_id` (integer, required): Contest ID
  - `title` (string, optional): Photo title
  - `description` (string, optional): Photo description
  - `category_id` (integer, optional): Category ID
  - `camera_model` (string, optional): Camera model used

**Success Response**:
```json
{
  "success": true,
  "message": "Photo uploaded successfully",
  "photo_id": 102,
  "photo": {
    "id": 102,
    "title": "Mountain Landscape",
    "description": "Beautiful mountain scenery",
    "url": "http://localhost:8881/wp-content/uploads/2024/01/mountain-landscape.jpg",
    "thumbnail": "http://localhost:8881/wp-content/uploads/2024/01/mountain-landscape-150x150.jpg",
    "author": "Jane Smith",
    "date": "2024-01-20 10:15:00",
    "contest_id": 1,
    "category_id": 1
  }
}
```

#### **Update Photo**
- **Method**: `PUT`
- **Endpoint**: `/photos/{id}`
- **Description**: Update photo title, description, or category
- **Headers**: `Authorization: Bearer {jwt_token}`
- **Path Parameters**:
  - `id` (integer, required): Photo ID (must belong to current user)
- **Body Parameters**:
  - `title` (string, optional): New photo title
  - `description` (string, optional): New photo description
  - `category_id` (integer, optional): New category ID

**Success Response**:
```json
{
  "success": true,
  "message": "Photo updated successfully",
  "photo": {
    "id": 102,
    "title": "Updated Mountain Landscape",
    "description": "Updated description",
    "url": "http://localhost:8881/wp-content/uploads/2024/01/mountain-landscape.jpg",
    "author": "Jane Smith",
    "date": "2024-01-20 10:15:00",
    "contest_id": 1,
    "category_id": 2
  }
}
```

#### **Delete Photo**
- **Method**: `DELETE`
- **Endpoint**: `/photos/{id}`
- **Description**: Delete a photo (must belong to current user)
- **Headers**: `Authorization: Bearer {jwt_token}`
- **Path Parameters**:
  - `id` (integer, required): Photo ID

**Success Response**:
```json
{
  "success": true,
  "message": "Photo deleted successfully"
}
```

### 👤 User Profile Management

#### **Get User Photos**
- **Method**: `GET`
- **Endpoint**: `/user/photos`
- **Description**: Retrieve all photos uploaded by the current user
- **Headers**: `Authorization: Bearer {jwt_token}`
- **Query Parameters**:
  - `page` (integer, optional): Page number (default: 1)
  - `contest_id` (integer, optional): Filter by contest ID

**Success Response**: Same format as contest photos

#### **Update User Profile**
- **Method**: `PUT`
- **Endpoint**: `/user/profile`
- **Description**: Update current user's profile information
- **Headers**: `Authorization: Bearer {jwt_token}`
- **Body Parameters** (all optional):
  - `first_name` (string): First name
  - `last_name` (string): Last name
  - `email` (string): Email address
  - `description` (string): Bio/description
  - `country` (string): Country
  - `state` (string): State/Province
  - `city` (string): City
  - `date_of_birth` (string): Date of birth
  - `phone` (string): Phone number
  - `www` (string): Website URL
  - `fb_page` (string): Facebook page URL
  - `twitter_page` (string): Twitter page URL
  - `instagram_page` (string): Instagram page URL

**Success Response**:
```json
{
  "success": true,
  "message": "Profile updated successfully"
}
```

#### **Upload User Avatar**
- **Method**: `POST`
- **Endpoint**: `/user/avatar`
- **Description**: Upload or update user profile avatar
- **Headers**: `Authorization: Bearer {jwt_token}`
- **Form Data**:
  - `image` (file, required): Avatar image file

**Success Response**:
```json
{
  "success": true,
  "message": "Avatar uploaded successfully",
  "avatar_id": 203,
  "avatar_url": "http://localhost:8881/wp-content/uploads/2024/01/user-avatar.jpg"
}
```

#### **Change User Password**
- **Method**: `PUT`
- **Endpoint**: `/user/password`
- **Description**: Change current user's password
- **Headers**: `Authorization: Bearer {jwt_token}`
- **Body Parameters**:
  - `current_password` (string, required): Current password
  - `new_password` (string, required): New password
  - `confirm_password` (string, required): Confirm new password

**Success Response**:
```json
{
  "success": true,
  "message": "Password changed successfully"
}
```

### 🏆 Contest Participation

#### **Get User Contests**
- **Method**: `GET`
- **Endpoint**: `/user/contests`
- **Description**: Retrieve contests the user has participated in
- **Headers**: `Authorization: Bearer {jwt_token}`
- **Query Parameters**:
  - `status` (string, optional): Filter by status - `all`, `active`, `ended` (default: `all`)

**Success Response**:
```json
[
  {
    "id": 1,
    "name": "Nature Photography Contest 2024",
    "start_date": "2024-01-01",
    "end_date": "2024-12-31",
    "status": "active",
    "user_photos_count": 3
  }
]
```

#### **Get User Voting History**
- **Method**: `GET`
- **Endpoint**: `/user/votes`
- **Description**: Retrieve user's voting history
- **Headers**: `Authorization: Bearer {jwt_token}`
- **Query Parameters**:
  - `contest_id` (integer, optional): Filter by contest ID
  - `page` (integer, optional): Page number (default: 1)

**Success Response**:
```json
{
  "data": [
    {
      "id": 45,
      "photo_id": 101,
      "photo_title": "Sunset at the Beach",
      "contest_id": 1,
      "contest_name": "Nature Photography Contest 2024",
      "vote_date": "2024-01-25 15:30:00",
      "email": "voter@example.com"
    }
  ],
  "total": 12,
  "page": 1,
  "per_page": 20,
  "pages": 1
}
```

---

## 🗳️ Voter Endpoints (No Authentication Required)

### Voting & Rating

#### **Vote for Photo** (Already covered in Public Endpoints)
- Same as public voting endpoint - allows anonymous voting with email

#### **Rate Photo** (Already covered in Public Endpoints)
- Same as public rating endpoint - allows anonymous rating

---

## ⚙️ Admin Endpoints (Admin Privileges Required)

### 🏆 Contest Management

#### **Create Contest**
- **Method**: `POST`
- **Endpoint**: `/contests`
- **Description**: Create a new photo contest
- **Headers**: `Authorization: Bearer {jwt_token}` (Admin user)
- **Body Parameters**:
  - `contest_name` (string, required): Contest name
  - `contest_start` (string, required): Start date (MM/DD/YYYY)
  - `contest_end` (string, required): End date (MM/DD/YYYY)
  - `contest_vote_start` (string, required): Voting start date (MM/DD/YYYY)
  - `contest_register_end` (string, required): Registration end date (MM/DD/YYYY)
  - `contest_condition` (string, optional): Contest rules and description
  - `image_per_user` (integer, optional): Max photos per user (default: 5)
  - `vote_frequency` (integer, optional): Vote frequency limit (default: 1)
  - `gallery_layout` (integer, optional): Gallery layout type (default: 1)
  - `contest_mode` (integer, optional): Contest mode (default: 1)

**Success Response**:
```json
{
  "success": true,
  "message": "Contest created successfully",
  "contest_id": 2
}
```

#### **Update Contest**
- **Method**: `PUT`
- **Endpoint**: `/contests/{id}`
- **Description**: Update existing contest details
- **Headers**: `Authorization: Bearer {jwt_token}` (Admin user)
- **Path Parameters**:
  - `id` (integer, required): Contest ID
- **Body Parameters**: Any of the create contest parameters (all optional)

**Success Response**:
```json
{
  "success": true,
  "message": "Contest updated successfully"
}
```

#### **Delete Contest**
- **Method**: `DELETE`
- **Endpoint**: `/contests/{id}`
- **Description**: Delete a contest and all associated data
- **Headers**: `Authorization: Bearer {jwt_token}` (Admin user)
- **Path Parameters**:
  - `id` (integer, required): Contest ID

**Success Response**:
```json
{
  "success": true,
  "message": "Contest deleted successfully"
}
```

### 📊 Photo Management (Admin View)

#### **Get All Photos (Admin View)**
- **Method**: `GET`
- **Endpoint**: `/admin/photos`
- **Description**: Retrieve all photos with admin-specific data
- **Headers**: `Authorization: Bearer {jwt_token}` (Admin user)
- **Query Parameters**:
  - `page` (integer, optional): Page number (default: 1)
  - `contest_id` (integer, optional): Filter by contest ID
  - `status` (string, optional): Filter by status - `pending`, `approved`, `rejected`
  - `user_id` (integer, optional): Filter by user ID

**Success Response**:
```json
{
  "data": [
    {
      "id": 101,
      "title": "Sunset at the Beach",
      "description": "Beautiful sunset captured during golden hour",
      "url": "http://localhost:8881/wp-content/uploads/2024/01/sunset-beach.jpg",
      "thumbnail": "http://localhost:8881/wp-content/uploads/2024/01/sunset-beach-150x150.jpg",
      "author": "John Doe",
      "author_id": 5,
      "date": "2024-01-15 14:30:00",
      "contest_id": 1,
      "category_id": 3,
      "status": "approved",
      "rejection_reason": null,
      "ip_address": "192.168.1.100"
    }
  ],
  "total": 234,
  "page": 1,
  "per_page": 20,
  "pages": 12
}
```

#### **Approve Photo**
- **Method**: `PUT`
- **Endpoint**: `/admin/photos/{id}/approve`
- **Description**: Approve a pending photo for public display
- **Headers**: `Authorization: Bearer {jwt_token}` (Admin user)
- **Path Parameters**:
  - `id` (integer, required): Photo ID

**Success Response**:
```json
{
  "success": true,
  "message": "Photo approved successfully"
}
```

#### **Reject Photo**
- **Method**: `PUT`
- **Endpoint**: `/admin/photos/{id}/reject`
- **Description**: Reject a photo with optional reason
- **Headers**: `Authorization: Bearer {jwt_token}` (Admin user)
- **Path Parameters**:
  - `id` (integer, required): Photo ID
- **Body Parameters**:
  - `reason` (string, optional): Rejection reason

**Success Response**:
```json
{
  "success": true,
  "message": "Photo rejected successfully"
}
```

### 📈 Voting & Statistics

#### **Get All Votes (Admin View)**
- **Method**: `GET`
- **Endpoint**: `/admin/votes`
- **Description**: Retrieve all voting data with detailed information
- **Headers**: `Authorization: Bearer {jwt_token}` (Admin user)
- **Query Parameters**:
  - `page` (integer, optional): Page number (default: 1)
  - `contest_id` (integer, optional): Filter by contest ID
  - `photo_id` (integer, optional): Filter by photo ID
  - `user_id` (integer, optional): Filter by user ID

**Success Response**:
```json
{
  "data": [
    {
      "id": 45,
      "photo_id": 101,
      "photo_title": "Sunset at the Beach",
      "contest_id": 1,
      "contest_name": "Nature Photography Contest 2024",
      "user_id": 12,
      "user_name": "Jane Smith",
      "vote_date": "2024-01-25 15:30:00",
      "email": "jane.smith@example.com",
      "ip_address": "192.168.1.50"
    }
  ],
  "total": 892,
  "page": 1,
  "per_page": 20,
  "pages": 45
}
```

#### **Export Voting Data**
- **Method**: `GET`
- **Endpoint**: `/admin/votes/export`
- **Description**: Export voting data in JSON or CSV format
- **Headers**: `Authorization: Bearer {jwt_token}` (Admin user)
- **Query Parameters**:
  - `contest_id` (integer, required): Contest ID to export
  - `format` (string, optional): Export format - `json`, `csv` (default: `json`)

**Success Response**:
- **JSON Format**: Returns array of vote objects
- **CSV Format**: Downloads CSV file with voting data

#### **Get Statistics**
- **Method**: `GET`
- **Endpoint**: `/admin/statistics`
- **Description**: Get comprehensive contest statistics
- **Headers**: `Authorization: Bearer {jwt_token}` (Admin user)
- **Query Parameters**:
  - `contest_id` (integer, optional): Filter by contest ID
  - `date_from` (string, optional): Start date for statistics (MM/DD/YYYY)
  - `date_to` (string, optional): End date for statistics (MM/DD/YYYY)

**Success Response**:
```json
{
  "votes": {
    "total_votes": 1247,
    "unique_voters": 456,
    "voted_photos": 89,
    "contests_with_votes": 3
  },
  "photos": {
    "total_photos": 567,
    "unique_contributors": 234,
    "contests_with_photos": 5
  },
  "contests": {
    "total_contests": 8,
    "active_contests": 3,
    "ended_contests": 5
  }
}
```

---

## 📊 Response Formats

### Standard Response Structure
All list endpoints follow this pagination format:
```json
{
  "data": [...],        // Array of items
  "total": 100,         // Total count
  "page": 1,            // Current page
  "per_page": 20,       // Items per page
  "pages": 5            // Total pages
}
```

### Single Item Response
Individual item endpoints return the object directly:
```json
{
  "id": 1,
  "name": "Contest Name",
  // ... other properties
}
```

### Success Response
Action endpoints return success confirmation:
```json
{
  "success": true,
  "message": "Action completed successfully",
  // ... additional data
}
```

---

## ❌ Error Handling

### Error Response Format
```json
{
  "code": "error_code",
  "message": "Human readable error message",
  "data": {
    "status": 404
  }
}
```

### Common Error Codes

| Code | Description | HTTP Status |
|------|-------------|-------------|
| `not_found` | Resource not found | 404 |
| `invalid_photo` | Photo not associated with contest | 400 |
| `contest_not_found` | Contest not found | 404 |
| `contest_ended` | Contest has ended | 400 |
| `voting_not_started` | Voting period not started | 400 |
| `registration_ended` | Registration period ended | 400 |
| `photo_limit_reached` | User reached photo upload limit | 400 |
| `unauthorized` | Insufficient permissions | 403 |
| `no_auth` | Missing authentication header | 401 |
| `invalid_token` | Invalid or expired JWT token | 401 |
| `update_failed` | Failed to update resource | 500 |
| `delete_failed` | Failed to delete resource | 500 |

---

## ⚡ Rate Limiting

### Public Endpoints
- **Voting**: 1 vote per email per photo per contest
- **Rating**: Unlimited ratings allowed
- **Search**: No specific limits

### Authenticated Endpoints
- **Photo Upload**: Limited by `image_per_user` contest setting
- **Profile Updates**: No specific limits
- **Password Changes**: No specific limits

### Admin Endpoints
- **Contest Creation**: No limits (admin privilege)
- **Photo Management**: No limits (admin privilege)
- **Data Export**: No limits (admin privilege)

---

## 🚀 Quick Start for Android Development

### Base Configuration
```kotlin
// Base URL
const val BASE_URL = "http://localhost:8881/wp-json/photo-contest/v1/"

// Authentication
const val JWT_TOKEN = "your_jwt_token_here"
const val AUTH_HEADER = "Bearer $JWT_TOKEN"
```

### Retrofit Interface Examples

#### Public Endpoints
```kotlin
interface PhotoContestPublicApi {
    @GET("contests")
    suspend fun getContests(
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 20,
        @Query("status") status: String = "active"
    ): ContestResponse

    @GET("photos/popular")
    suspend fun getPopularPhotos(
        @Query("limit") limit: Int = 20,
        @Query("timeframe") timeframe: String = "all_time"
    ): List<Photo>

    @POST("photos/{id}/vote")
    suspend fun votePhoto(
        @Path("id") photoId: Int,
        @Body voteRequest: VoteRequest
    ): VoteResponse
}
```

#### Authenticated Endpoints
```kotlin
interface PhotoContestAuthApi {
    @GET("user/photos")
    suspend fun getUserPhotos(
        @Header("Authorization") token: String,
        @Query("contest_id") contestId: Int? = null,
        @Query("page") page: Int = 1
    ): PhotosResponse

    @Multipart
    @POST("photos/upload")
    suspend fun uploadPhoto(
        @Header("Authorization") token: String,
        @Part image: MultipartBody.Part,
        @Part("contest_id") contestId: RequestBody,
        @Part("title") title: RequestBody? = null,
        @Part("description") description: RequestBody? = null
    ): UploadResponse

    @PUT("user/profile")
    suspend fun updateProfile(
        @Header("Authorization") token: String,
        @Body profileUpdate: ProfileUpdateRequest
    ): SuccessResponse
}
```

#### Admin Endpoints
```kotlin
interface PhotoContestAdminApi {
    @POST("contests")
    suspend fun createContest(
        @Header("Authorization") token: String,
        @Body contestRequest: CreateContestRequest
    ): CreateContestResponse

    @GET("admin/statistics")
    suspend fun getStatistics(
        @Header("Authorization") token: String,
        @Query("contest_id") contestId: Int? = null
    ): StatisticsResponse

    @PUT("admin/photos/{id}/approve")
    suspend fun approvePhoto(
        @Header("Authorization") token: String,
        @Path("id") photoId: Int
    ): SuccessResponse
}
```

---

## 📚 Additional Resources

### Testing Tools
- **Interactive Test Interface**: `test-api-interactive.html`
- **PHP Test Script**: `test-rest-api.php`
- **JWT Test Page**: `test-jwt-auth.html`

### Documentation Files
- **Integration Guide**: `android-app-integration-guide.md`
- **Development Roadmap**: `android-app-development-roadmap.md`
- **Test Interface Guide**: `test-api-interactive-guide.md`

### Sample Data Creation
Before testing, create sample data in WordPress Admin:
1. Go to **WordPress Admin → Photo Contest**
2. Create 2-3 test contests with different dates
3. Upload sample photos to each contest
4. Test voting and rating functionality
5. Create test user accounts for authentication testing

---

## 🎉 **Ready for Android App Development!**

This comprehensive API documentation provides everything needed to build a fully functional Android app for the Photo Contest WordPress Plugin. All endpoints are tested, documented, and ready for integration.

**Key Features Available:**
- ✅ Complete contest browsing and participation
- ✅ Photo upload and management
- ✅ Voting and rating system
- ✅ User authentication and profiles
- ✅ Admin contest management
- ✅ Comprehensive statistics and reporting
- ✅ Search and filtering capabilities

**Start building your Android photo contest app today!** 📱✨
