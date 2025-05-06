# Expense Tracker Application

## Features & Implementation

### 1. Firebase Authentication

#### Signup Screen:
- Users can register using **email** and **password**.
- Registration is powered by **Firebase Authentication**.
- Displays error messages for invalid inputs.

#### Login Screen:
- Users log in with **email** and **password**.
- Authentication is handled via **Firebase**.
- On successful login, users navigate to the main app screen.

#### Sign-Out Button:
- Adds a sign-out option in the settings fragment or menu.
- Calls:
  ```java
  FirebaseAuth.getInstance().getCurrentUser().signOut()
  ```
- Redirects to the login screen after signing out.

---

### 2. Retrofit for Expense Tracking API

#### Expense List Fragment:
- Fetches expense data using **Retrofit**.
- Displays the data in a **RecyclerView**.
- Each user accesses their own data using the `X-DB-NAME` header.

#### Add Expense Fragment:
- Implements expense addition functionality with the following fields:
  - **Amount**
  - **Currency**
  - **Category**
  - **Remark**
- Allows saving expense data to the API via **POST** request.

#### Expense Detail Fragment:
- Shows detailed information about a selected expense.
- Fetches the specific expense data using its **ID**.
- Provides a delete option for removing expenses.

---

### 3. Receipt Image Feature

#### Image Capture & Selection:
- Allows users to capture receipt images using the device camera
- Provides option to select images from the gallery
- Implements proper runtime permission handling for camera access

#### Supabase Storage Integration:
- Integrates with **Supabase Storage** for saving receipt images
- Uploads images and retrieves public URLs for display
- Stores image URLs in the expense model as `receiptImageUrl`

#### Image Display:
- Shows receipt image preview in the Add Expense screen
- Displays receipt images in the Expense Detail screen
- Uses the **Glide** library for efficient image loading

---

## Setup Instructions

### Firebase Configuration:
1. Create a Firebase project at [Firebase Console](https://console.firebase.google.com/)
2. Add your app to the project and download the `google-services.json` file
3. Place the file in the `app` directory

### Supabase Storage Setup:
1. Create a Supabase account at [Supabase](https://supabase.com/)
2. Set up a new project and create a storage bucket named `receipt_images`
3. Update the `SupabaseConfig.java` file with your Supabase URL and anon key
4. See the `SUPABASE_SETUP.md` file for detailed instructions

---

## Libraries Used

- **Firebase Authentication**: For user authentication
- **Retrofit**: For API communication
- **Glide**: For efficient image loading
- **Supabase Storage**: For cloud storage of receipt images
- **Room**: For local database operations

---

## Implementation Notes

- The app uses the MVVM architecture pattern
- Implements proper error handling for network operations
- Uses Kotlin Coroutines via Java interop for asynchronous operations
- Provides detailed logging for troubleshooting

See the [SUPABASE_SETUP.md](./SUPABASE_SETUP.md) file for detailed instructions on setting up Supabase Storage.
