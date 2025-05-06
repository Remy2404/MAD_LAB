# Securely Setting Up Supabase Credentials

To set up the Supabase credentials for the Expense Tracker app while maintaining security, follow these steps:

## 1. Create local.properties File

1. Create or open the `local.properties` file in the root directory of the project
2. Add your Supabase credentials in the following format:

```properties
# SDK location (automatically added by Android Studio)
sdk.dir=C:\\Users\\YourUsername\\AppData\\Local\\Android\\Sdk

# Supabase credentials
supabase.url="https://your-supabase-id.supabase.co"
supabase.anon.key="your-supabase-anon-key"
```

> **IMPORTANT**: Never commit your `local.properties` file to version control. It should already be in the `.gitignore` file.

## 2. Building the Project

The app's build process will automatically include these credentials in the app's BuildConfig, making them accessible in the code without hardcoding them.

## 3. For Team Members

When sharing the project with team members:

1. Tell them to create their own `local.properties` file
2. Share the Supabase credentials through a secure channel (not in the repository)
3. They should add the credentials to their local file

## 4. Alternative Approaches

If you prefer not to use `local.properties`, here are other options:

### Option A: Environment Variables

1. Set environment variables on your development machine
2. Access them in the Gradle build file

### Option B: Encrypted Properties File

1. Use a tool like [android-keystore-password-recover](https://github.com/nelenkov/android-keystore-password-recover) to encrypt sensitive data
2. Decrypt at runtime using a secure key

### Option C: Remote Config

1. Use Firebase Remote Config to store non-sensitive configuration
2. Use a service like AWS Secrets Manager or HashiCorp Vault for more sensitive data

## 5. CI/CD Pipelines

If you're using continuous integration:

1. Set the Supabase credentials as secrets in your CI environment (e.g., GitHub Secrets, CircleCI Environment Variables)
2. Have your build script inject these credentials during the build process

These approaches ensure your Supabase credentials remain secure while still being accessible for development and deployment.
