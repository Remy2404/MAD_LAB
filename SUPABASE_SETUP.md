# Setting Up Supabase Storage for Receipt Images

This document provides instructions for setting up Supabase Storage to store receipt images for the Expense Tracker app.

## 1. Create a Supabase Account

1. Go to [Supabase](https://supabase.com/) and sign up for an account if you don't have one
2. Log in to your Supabase account

## 2. Create a New Project

1. Click on "New Project" button
2. Enter a name for your project (e.g., "Expense Tracker")
3. Choose a secure password for the database
4. Select the region closest to your users
5. Click "Create New Project"

## 3. Configure Supabase Storage

1. Wait for your project to initialize (it may take a few minutes)
2. In the left sidebar, click on "Storage"
3. Click "Create a new bucket"
4. Enter "receipt_images" as the bucket name
5. Set the following bucket configurations:
   - Public bucket: Yes (so images can be accessed without authentication)
   - RLS (Row Level Security): Disabled for testing purposes, but for production, enable it and configure access policies
6. Click "Create bucket"

## 4. Configure RLS Policies (Optional but Recommended for Production)

For better security in a production environment, you should enable RLS and create policies:

1. Go to the "receipt_images" bucket
2. Click on the "Policies" tab
3. Click "Add Policy"
4. For insertion policy:
   - Policy name: "Allow authenticated uploads"
   - Using template: "Insert objects for authenticated users only"
   - Click "Create Policy"
5. For selection policy:
   - Policy name: "Allow public access to images"
   - Using template: "Select objects for everyone"
   - Click "Create Policy"

## 5. Get Your Supabase Credentials

1. In the left sidebar, click on "Project Settings"
2. Click on "API"
3. Find your "Project URL" and "anon/public" key
4. **IMPORTANT: Do not hardcode these credentials in your app!**
5. Instead, follow the instructions in [CREDENTIALS_SETUP.md](./CREDENTIALS_SETUP.md) to securely store them
6. The credentials will be automatically included in your app via BuildConfig during compilation

## 6. Testing the Storage

You can manually upload a file to test the storage setup:

1. Go to the "Storage" section
2. Click on the "receipt_images" bucket
3. Click "Upload File" and select an image
4. If the upload is successful, you can view the image by clicking on it
5. The public URL will be something like: `https://[your-project-id].supabase.co/storage/v1/object/public/receipt_images/[file-name]`

## Troubleshooting

If you encounter issues with storage access:

1. Check that the bucket is marked as public
2. Verify that your credentials in the app are correct
3. Look at the RLS policies to ensure they're not blocking access
4. Check the console for any error messages

## Storage Management

Remember that Supabase has storage limits based on your plan. Monitor your storage usage in the Supabase dashboard under "Project Settings" > "Usage".
