# Build Signed APK Guide for PicChallenge App

## Prerequisites Completed:
✅ Build configuration updated with signing config  
✅ keystore.properties file created  
✅ .gitignore updated to exclude signing files  

## Next Steps After You Generate the Keystore:

### 1. Generate Keystore (Follow KEYSTORE_GENERATION_STEPS.md)
### 2. Update keystore.properties with your actual passwords
### 3. Build the Signed APK

## Method 1: Build Using Android Studio

### Steps:
1. **Open Android Studio**
2. **Build → Generate Signed Bundle/APK**
3. **Choose "APK"** and click Next
4. **Key store path:** Browse to `picchallenge-release.keystore`
5. **Key store password:** Enter your keystore password
6. **Key alias:** `picchallenge`
7. **Key password:** Enter your key password
8. **Next → Choose "release" build type**
9. **Signature versions:** Check both V1 and V2
10. **Finish** - APK will be generated in `app/release/`

## Method 2: Build Using Command Line

### Build the signed APK:
```bash
./gradlew assembleRelease
```

### Output location:
```
app/build/outputs/apk/release/app-release.apk
```

### Verify the APK is signed:
```bash
jarsigner -verify -verbose -certs app/build/outputs/apk/release/app-release.apk
```

## Method 3: Build Using Gradle Wrapper (Windows)

### Build the signed APK:
```cmd
gradlew.bat assembleRelease
```

### Clean and build:
```cmd
gradlew.bat clean assembleRelease
```

## Important Notes:

### Security:
- **NEVER** commit keystore files to version control
- **BACK UP** your keystore file securely
- **STORE** passwords in a password manager
- **SAME** keystore must be used for all app updates

### Distribution Ready APK:
The generated APK will be:
- ✅ Signed with your release key
- ✅ Optimized with ProGuard
- ✅ Ready for Google Play Store or direct distribution

### File Locations:
- **Keystore:** `picchallenge-release.keystore` (project root)
- **Properties:** `keystore.properties` (project root)
- **Signed APK:** `app/build/outputs/apk/release/app-release.apk`

## Troubleshooting:

### If build fails:
1. Check keystore.properties has correct passwords
2. Verify keystore file exists in project root
3. Run `./gradlew clean` before building
4. Check Android Studio logs for specific errors

### If signing fails:
1. Verify keystore password is correct
2. Check key alias matches "picchallenge"
3. Ensure key password is correct

Once you've generated your keystore and updated the properties file, run the build command and you'll have a signed APK ready for distribution!
