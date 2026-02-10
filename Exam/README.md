# ExamPrep Platform (Android + Backend)

This repository now contains:
1. **Native Android app** (Kotlin + Jetpack Compose, Android Studio)
2. **Backend APIs** (Node + Express)
3. **Admin panel** (`/backend/admin`) for uploading tests/resources

## Android features implemented
- Tesbook-style UI screens:
  - Home dashboard
  - Live test attempt
  - Analysis/report screen
- Firebase Auth integration scaffolding:
  - email/password login
  - signup
  - logout
- Subscription flow scaffolding:
  - Google Play Billing integration class
  - plan screen UI
- Offline resource scaffolding:
  - Room entities/DAO for downloads
  - WorkManager worker placeholder for PDF/eBook download jobs
- API client scaffolding:
  - Retrofit + Kotlin serialization
  - endpoints for tests/resources/current affairs

## Backend features implemented
- Auth placeholder endpoint: `POST /api/auth/login`
- Test APIs: `GET /api/tests`
- Resource APIs (PDF/eBook/quiz): `GET /api/resources`
- Current affairs APIs: `GET /api/current-affairs`
- Subscription/payment endpoints:
  - `POST /api/subscriptions/checkout`
  - `POST /api/subscriptions/webhook`
- Admin APIs for upload:
  - `POST /api/admin/tests`
  - `POST /api/admin/resources`
- Admin panel UI at `/admin`

## Open Android app in Android Studio
1. Open Android Studio
2. Select this repository root
3. Add Firebase config file: `app/google-services.json`
4. Sync Gradle and run on emulator/device

## Run backend
```bash
cd backend
npm install
npm run start
```

Then Android emulator can call backend at `http://10.0.2.2:8080`.
