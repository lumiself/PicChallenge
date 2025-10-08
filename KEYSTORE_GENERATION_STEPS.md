# Keystore Generation Steps for PicChallenge App

## Steps to Generate the Keystore Yourself:

### 1. Open Command Prompt/Terminal in your project directory

### 2. Run this command:
```bash
keytool -genkey -v -keystore picchallenge-release.keystore -alias picchallenge -keyalg RSA -keysize 2048 -validity 10000
```

### 3. Answer the prompts:
- **Keystore password:** Choose a strong password (remember this!)
- **Re-enter password:** Confirm your password
- **First and last name:** Your name or company name
- **Organizational unit:** Can press Enter to skip
- **Organization name:** Your company or personal name
- **City/Locality:** Your city
- **State/Province:** Your state
- **Country code:** Two-letter code (US, UK, etc.)
- **Confirm:** Type "yes" when asked if information is correct

### 4. Important:
The keystore file `picchallenge-release.keystore` will be created in your project directory

### 5. Update your `keystore.properties` file:
Replace the placeholder passwords with your actual passwords:
- `storePassword=your_keystore_password_here` → your actual keystore password
- `keyPassword=your_key_password_here` → your actual key password (can be same as keystore)

## Security Notes:
- **BACK UP** your keystore file immediately after creation
- **NEVER** commit keystore files to version control
- **STORE** passwords securely (password manager recommended)
- **SAME** keystore must be used for all future updates to this app

Once you've completed the keystore generation, let me know and I'll continue with updating your build configuration!
