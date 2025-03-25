# Expense Tracker Application

## Lab 5: Network Communication – Firebase and Retrofit

### Objective

Enhance the Expense Tracker app by integrating **Firebase Authentication** for signup/login and **Retrofit** for API communication.

---

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
- Automatically adds:

  - **Id**: `UUID.randomUUID().toString()`
  - **CreatedBy**: `FirebaseAuth.getInstance().getCurrentUser().getUid()`
  - **CreatedDate**: Current date in **ISO 8601 format** (e.g., `2025-03-22T15:08:08Z`).

- Uses a Date Adapter for proper serialization:

  ```java
  @JsonAdapter(ISO8601DateAdapter.class)
  ```

  Adapter source: [ISO8601DateAdapter Gist](https://gist.github.com/hangsopheak/eb7c370812be1f3f1d7a783d9fbde6f)

- Sends **POST requests** via Retrofit to add expenses.

#### Expense Detail Screen:

- Fetches and displays detailed information about a selected expense.

---

### 3. API Instructions

- Use the provided API server.
- **Expense API CURL Example**: [API Gist](https://gist.github.com/hangsopheak/2ac42cba1862fef2643a9e0bc10db231)
- Generate a GUID: [GUID Generator](https://guidgenerator.com)
- Use the generated GUID as the `X-DB-NAME` header to create a personal database.

---

### 4. Bonus Challenges (Optional)

#### **Infinite Scroll Pagination**:

- Implements infinite scrolling in the expense list using **Retrofit** and **RecyclerView**.
- Dynamically loads more expenses as users scroll down.

#### **Swipe-to-Delete**:

- Enables swipe gestures to delete expenses from the list.

---

### Debugging Tips

Common issues you might encounter include:

- **Network API Calls**: Retrofit request errors, headers, or incorrect endpoints.
- **Date Serialization/Deserialization**: Formatting and parsing issues.

To troubleshoot:

- Use the **Android Debugger** and set breakpoints.
- Check **Logcat** for error messages.
- Monitor API requests and responses using **Network Inspector**.
