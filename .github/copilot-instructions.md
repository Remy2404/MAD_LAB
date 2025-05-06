project for enhancing the **Expense Tracker** app by implementing implicit intents and permission handling. You need to add a **Receipt Image Feature**, properly handle runtime permissions for camera access, and integrate cloud storage for saving receipt images.

### Here’s how you can approach it:

#### **1. Adding Receipt Image Feature**

- Add a new field in the expense model: `receiptImageUrl`.
- In the **add new expense** fragment:
  - Allow users to capture receipt images via the camera or select from the gallery (two buttons).
  - Display the selected/taken receipt image below the buttons.
  - Implement **runtime permission handling** for camera access.

#### **2. Saving Image & Expense Data**

- When users click the **save** button, perform two actions:
  1. **Save the image** in cloud storage and store the URL in the expense model & and Display it on screen make sure is responsive UI.
     - **Option 1:** Explore other cloud storage options using Supabase
  2. **Call the add expense API** (used in Lab 5) to save the expense data, including the new field `receiptImageUrl`.

#### **3. Displaying the Image**

- In the **expense detail screen**, display the receipt image from `receiptImageUrl`.
- Use the **Glide** library to load the image from the network.

### **Resources to Help You**

- Android Permissions: [Official Guide](https://developer.android.com/training/permissions/requesting)
- Glide Library: [GitHub Repository](https://github.com/bumptech/glide)
- Supabase Storage: [Official Documentation](https://supabase.com/docs/guides/storage)
- Android Camera Intent: [Official Guide](https://developer.android.com/training/camera/photobasics)
