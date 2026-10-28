# FLORATEND — ULTIMATE CODEX MASTER BUILD PROMPT
## Premium Plant Care, Watering Reminder & Growth Tracker
### Amazon Fire Tablets | Android | Multilingual Voice | Worldwide Localisation | SEO + AI Search

## 1. YOUR ROLE AND MISSION

Act as an autonomous team of senior Android engineers, plant-care product designers, horticultural content researchers, voice interaction engineers, localisation architects, accessibility specialists, Amazon Appstore monetisation experts, SEO/ASO strategists, and QA engineers.

Build a complete, production-quality Android application called:

**FloraTend – Plant Care & Watering Tracker**

**Tagline:** Every plant deserves a little attention.

**Primary platform:** Amazon Fire tablets through the Amazon Appstore.

**Secondary platform:** Compatible Android phones and tablets.

**Core promise:** Help people keep their plants organised, understand their care needs, and develop manageable plant-care routines.

The app must feel premium, calming, practical, visually beautiful, and trustworthy.

Target users include houseplant owners, beginner gardeners, apartment residents, plant collectors, and people managing multiple indoor or outdoor plants.

Do not stop at planning, wireframes, mockups, or sample code.

Implement working source code, build the app, test it, fix defects, create original assets, and prepare Amazon Appstore release deliverables.

Preserve existing repository work.

Treat the name FloraTend as provisionally selected until final trademark and Appstore clearance.

---

# 2. CORE PRODUCT FEATURES

Build these main modules:

1. Personal plant collection
2. Watering scheduler
3. Fertilising reminders
4. Repotting and pruning tracker
5. Plant growth journal
6. Plant-care guidance
7. Optional photo-assisted plant identification
8. Voice-powered plant assistant
9. Plant health observations
10. Care calendar
11. Premium analytics
12. Offline plant records
13. Multilingual settings
14. Premium subscription or lifetime unlock

All core plant-management functions must work offline.

Do not require account registration.

# 3. TECHNICAL ARCHITECTURE

Use:

- Kotlin
- Jetpack Compose
- Material 3 with a custom design system
- MVVM and Clean Architecture
- Room database
- DataStore
- Kotlin Coroutines and Flow
- Hilt where appropriate
- WorkManager where appropriate
- Android CameraX where supported
- Android SpeechRecognizer
- Android TextToSpeech
- Android notification APIs
- Amazon-compatible In-App Purchasing
- Gradle Kotlin DSL
- JUnit and Android UI testing

Avoid mandatory Google Play Services.

Separate modules for:

- Plant library
- Care schedules
- Notifications
- Growth journal
- Photo management
- Plant identification
- Voice commands
- Localisation
- Premium entitlements
- Data export
- Backup
- Analytics

Use offline-first architecture.

# 4. PREMIUM VISUAL DESIGN

Create a distinctive FloraTend brand identity.

Suggested palette:

- Forest green: #315C45
- Sage green: #8FAE91
- Warm ivory: #FAF8F2
- Terracotta: #C8795B
- Charcoal: #2D332E

Design characteristics:

- Beautiful plant photography
- Elegant botanical illustrations
- Premium plant cards
- Soft rounded corners
- Spacious layouts
- Calm animations
- Clear typography
- Light and dark themes
- Accessible contrast
- Large touch targets

Optimise for 7–10-inch Fire tablets.

Support portrait and landscape orientations.

Use adaptive layouts, navigation rails, and split views where appropriate.

Do not stretch phone interfaces across tablet screens.

Make primary touch targets at least 48dp.

# 5. ORIGINAL PREMIUM ASSETS

Create:

- High-resolution master app icon
- Android adaptive icon layers
- Monochrome icon
- Splash-screen artwork
- Onboarding illustrations
- Plant category icons
- Watering illustrations
- Fertilising illustrations
- Repotting graphics
- Growth journal artwork
- Empty-state illustrations
- Voice assistant graphics
- Premium upgrade artwork
- Screenshot marketing templates
- Promotional banners
- Light and dark variants

## App icon concept

Create an elegant leaf emerging from a subtle water-drop or circular care symbol.

Use a recognisable silhouette and premium botanical styling.

All assets must be original or appropriately licensed.

Do not claim assets have been generated unless actual files exist.

# 6. ONBOARDING

Create a short onboarding flow.

Allow users to:

- Choose language
- Select experience level
- Choose indoor or outdoor plants
- Add their first plant
- Configure optional reminders
- Select regional measurement preferences
- Review privacy settings

Do not require account registration.

Allow users to skip optional steps.

# 7. PLANT LIBRARY

Users can:

- Add plants
- Edit plants
- Delete plants
- Duplicate records
- Add photos
- Assign nicknames
- Record species
- Add location
- Set acquisition date
- Record pot size
- Record soil type
- Record light conditions
- Add personal notes
- Search and filter plants

Default categories:

- Indoor Plants
- Outdoor Plants
- Succulents
- Cacti
- Herbs
- Flowering Plants
- Trees
- Other

Support multiple plant locations.

# 8. SMART WATERING SCHEDULER

Implement:

- Watering intervals
- Last-watered date
- Next suggested watering date
- Custom schedules
- Watering history
- Snooze reminders
- Pause schedules
- Seasonal adjustments
- Optional soil-moisture observations

Do not assume all plants should be watered on fixed schedules.

Present watering dates as reminders to check plant conditions, not guaranteed instructions to water.

Example:

"Your Monstera may be ready for a soil-moisture check."

Allow users to record whether the soil is dry, moist, or wet.

Avoid automatic recommendations that could cause overwatering.

# 9. FERTILISING, PRUNING AND REPOTTING

Implement care schedules for:

- Fertilising
- Pruning
- Repotting
- Leaf cleaning
- Pest inspections
- Soil checks
- Seasonal care

Allow custom tasks and intervals.

Provide clear history records.

Do not prescribe chemical treatments or fertiliser dosages without reliable, appropriately sourced guidance.

# 10. PLANT GROWTH JOURNAL

Create a premium visual growth journal.

Support:

- Progress photos
- Date-based entries
- Height measurements
- Leaf-count observations
- Flowering notes
- Health observations
- Custom notes
- Before-and-after comparisons
- Growth timeline

Use actual user records.

Do not fabricate growth measurements.

# 11. PLANT HEALTH OBSERVATIONS

Allow users to record:

- Yellowing leaves
- Brown leaf tips
- Wilting
- Leaf drop
- Visible pests
- Soil moisture
- Light exposure
- General condition

Provide cautious educational guidance.

Do not present symptoms as definitive diagnoses.

Include uncertainty and encourage appropriate expert advice for persistent or severe problems.

# 12. OPTIONAL PHOTO-ASSISTED PLANT IDENTIFICATION

Implement an optional plant identification workflow.

Users can:

1. Take or select a plant photo.
2. Submit it to an available identification engine.
3. Review possible matches.
4. See confidence or uncertainty information where supported.
5. Confirm or correct the plant identity.
6. Save the confirmed result.

Prefer a tested on-device solution where practical.

If an external identification service is used:

- Require explicit consent.
- Disclose network usage.
- Avoid retaining images unnecessarily.
- Protect API credentials.
- Provide manual identification fallback.

Never fabricate an identification result.

Do not advertise identification as fully offline unless verified.

# 13. PREMIUM VOICE INTERACTION

Voice is a mandatory feature.

Implement useful voice commands.

Examples:

- "Add a new plant."
- "Which plants need watering today?"
- "Mark my Monstera as watered."
- "When should I check my snake plant?"
- "Show my plant-care calendar."
- "Add a growth note."
- "Read my care reminders."
- "Set a reminder for Saturday."
- "Show plants in the living room."

## Voice output

Provide spoken feedback such as:

"Your Monstera watering record has been updated."

"You have three plant-care reminders today."

"Your next soil-moisture check is scheduled for Friday."

## Technical requirements

- Push-to-talk microphone activation
- Speech recognition capability detection
- Microphone permission handling
- Visible listening indicator
- Transcription preview
- Structured command parsing
- Locale-aware voice dictionaries
- Android TextToSpeech responses
- Confirmation for destructive actions
- Error recovery
- Full touch and text fallback

Do not assume Fire tablets include suitable speech recognition engines.

Do not implement continuous background listening.

Do not store raw microphone recordings by default.

Do not claim offline voice recognition unless implemented and tested.

# 14. PLANT CARE CALENDAR

Create daily, weekly, and monthly calendar views.

Show:

- Watering checks
- Fertilising
- Repotting
- Pruning
- Pest inspections
- Custom care tasks

Allow users to:

- Complete tasks
- Reschedule
- Snooze
- Filter by plant
- Filter by care type

Use reliable recurrence and timezone handling.

# 15. PREMIUM ANALYTICS

Implement:

- Total plants
- Care tasks completed
- Watering history
- Fertilising history
- Growth journal entries
- Plant collection breakdown
- Upcoming care tasks
- Monthly care trends

Do not claim that task completion proves plant health.

Use accessible charts and clear labels.

# 16. OFFLINE-FIRST PRIVACY

Store locally:

- Plant records
- Care schedules
- Photos
- Growth journal
- Reminders
- Notes
- Settings

No account required.

No unnecessary analytics.

No third-party advertising SDKs in version 1.

Provide:

- JSON export
- CSV export
- Data deletion
- Optional encrypted backup
- Secure photo storage

Cloud backup must be opt-in.

Do not claim cloud sync is implemented unless a real provider integration exists and has been tested.

# 17. PREMIUM MONETISATION

Use a freemium model.

## Free tier

- Up to five plants
- Basic watering reminders
- Basic care calendar
- Manual plant entry
- Basic growth notes

## FloraTend Premium

Proposed test price: £3.99 one-time unlock.

Premium includes:

- Unlimited plants
- Advanced care schedules
- Multiple plant locations
- Growth photo timelines
- Advanced statistics
- Expanded voice commands
- Data export
- Premium themes
- Optional identification features where supported

Use Amazon-compatible In-App Purchasing.

Implement:

- Store-provided prices
- Purchase verification
- Restore purchases
- Offline entitlement persistence
- Purchase failure handling

Do not use Google Play Billing in the Amazon build.

Do not simulate successful real purchases.

# 18. MANDATORY WORLDWIDE LOCALISATION

**This requirement is non-negotiable.**

FloraTend must be designed as an international application from the beginning.

Localise the complete app, voice interactions, screenshots, store descriptions, and marketing website.

## Initial target locales

- en-GB — English UK
- en-US — English US
- en-AU — English Australia
- es-ES — Spanish Spain
- es-419 — Spanish Latin America
- fr-FR — French France
- fr-CA — French Canada
- de-DE — German
- it-IT — Italian
- pt-BR — Portuguese Brazil
- pt-PT — Portuguese Portugal
- nl-NL — Dutch
- pl-PL — Polish
- sv-SE — Swedish
- ja-JP — Japanese
- hi-IN — Hindi
- ar — Arabic
- tr-TR — Turkish
- id-ID — Indonesian

Verify the current Amazon Appstore marketplace and supported listing-language requirements using official documentation.

Maintain a marketplace-to-locale matrix.

Distinguish:

1. App interface languages
2. Store listing languages
3. Distribution countries
4. Speech recognition languages
5. TextToSpeech languages

Do not assume these are identical.

## Localise every user-facing feature

Translate:

- Onboarding
- Navigation
- Plant categories
- Care schedules
- Reminder text
- Plant-care guidance
- Calendar labels
- Growth journal UI
- Health observations
- Voice prompts
- Voice command dictionaries
- Premium paywall
- Settings
- Notifications
- Errors
- Empty states
- Accessibility labels
- Privacy notices
- Support content

Support:

- Pluralisation
- Text expansion
- Right-to-left layouts
- Locale-aware dates
- Regional measurements
- Temperature units
- Localised number formats
- Culturally appropriate examples

Do not automatically translate user-entered plant nicknames or personal notes.

# 19. AUTOMATED TRANSLATION PIPELINE

Create a working development-time translation system.

Use a configurable translation provider such as DeepL or another approved API.

Implement:

- Source-string extraction
- Translation API integration
- Translation caching
- Terminology glossary
- Placeholder protection
- Plural validation
- Missing-string detection
- Android resource generation
- Translation quality reports
- Human review workflow
- Right-to-left testing
- Screenshot localisation
- Store metadata localisation

Never embed translation API credentials in the APK.

If credentials are unavailable, create the pipeline and mark translations as pending.

Do not claim machine-translated content is human-reviewed.

# 20. PREMIUM LOCALISED AMAZON APPSTORE SCREENSHOTS

Generate eight premium screenshot compositions for every approved store-listing locale.

Every screenshot must show the actual app in that locale.

## Screenshot 1 — Hero

**Plant Care & Watering Tracker**

"Keep your plants happy and beautifully organised."

Show the plant dashboard.

## Screenshot 2 — Watering

**Plant Watering Reminders**

"Know when to check your plants."

Show watering reminders.

## Screenshot 3 — Plant Library

**Organise Your Houseplants**

"Keep every plant's details in one place."

Show plant cards.

## Screenshot 4 — Care Calendar

**Plant Care Schedule**

"Plan watering, fertilising and repotting."

Show the calendar.

## Screenshot 5 — Growth Journal

**Plant Growth Tracker**

"Capture photos and celebrate every new leaf."

Show growth photos and timeline.

## Screenshot 6 — Voice

**Voice-Powered Plant Care**

"Manage your plant collection with supported voice commands."

Show the voice interface.

## Screenshot 7 — Offline

**Offline Plant Care Planner**

"Access saved plant records without internet."

Show offline plant information.

## Screenshot 8 — Insights

**Track Your Plant Care Progress**

"See your care history and upcoming tasks."

Show analytics.

## Screenshot production requirements

- Capture actual implemented screens.
- Use deterministic demonstration data.
- Create Fire tablet and phone variants.
- Verify current Amazon screenshot dimensions.
- Translate marketing headlines.
- Translate supporting captions.
- Capture the app in each target locale.
- Use culturally appropriate plant examples.
- Validate typography and cropping.
- Avoid keyword stuffing.
- Avoid fabricated ratings or reviews.
- Export production assets.
- Include editable templates.
- Generate a screenshot manifest.

Never place translated headlines over English-language UI.

Do not claim screenshots are complete unless the files exist.

# 21. AMAZON APPSTORE SEO / ASO

Proposed title:

**FloraTend – Plant Care & Watering Tracker**

Validate title length and store requirements.

Primary keywords:

- Plant care app
- Plant watering reminder
- Houseplant tracker
- Plant care schedule
- Plant watering tracker
- Indoor plant care

Secondary keywords:

- Plant growth journal
- Plant organiser
- Watering calendar
- Plant reminder
- Houseplant care
- Repotting reminder
- Plant collection
- Offline plant care

Generate:

- SEO-optimised title recommendations
- Concise product summary
- Full product description
- Feature highlights
- Localised keyword research
- Localised listing copy
- Screenshot captions
- Release notes
- Privacy policy draft
- Support documentation

Use natural keywords.

Do not invent Amazon metadata fields.

# 22. AI SEARCH OPTIMISATION

Create a lightweight, crawlable FloraTend marketing website.

Pages:

- Home
- Features
- Plant Watering Reminders
- Houseplant Tracker
- Plant Care Calendar
- Growth Journal
- Voice Features
- Fire Tablet Compatibility
- Pricing
- FAQ
- Privacy
- Support

Target genuine search questions such as:

- What is a good plant watering reminder app?
- How can I track my houseplants?
- Can I manage plant care on a Fire tablet?
- Are there offline plant care apps?
- How can I organise watering schedules?
- Can I track plant growth with photos?
- Can I manage plants using voice commands?

Implement:

- Crawlable HTML
- Descriptive page titles
- Clear headings
- Canonical URLs
- XML sitemap
- Robots.txt
- Appropriate hreflang annotations
- Accurate product facts
- SoftwareApplication structured data
- FAQ content where useful
- Direct Amazon listing link once available

Do not invent testimonials, ratings, awards, or download figures.

AI search optimisation does not guarantee rankings or recommendations.

# 23. PRIVACY AND SAFETY

Assess applicable privacy requirements in target markets.

Implement high-privacy defaults.

Do not collect unnecessary personal information.

Do not collect precise location unless a feature genuinely requires it and the user explicitly authorises it.

Do not upload plant photographs without explicit consent.

Clearly distinguish general care guidance from verified species-specific recommendations.

# 24. CONCRETE CODEX EXECUTION WORKFLOW

Do not stop at planning.

Execute each phase.

## Phase 0 — Environment inspection

- Inspect repository and Git status.
- Check Android SDK, Java, Kotlin, Gradle, and available devices.
- Initialise the project if necessary.
- Configure dependencies.
- Establish a successful baseline build.

## Phase 1 — Core application

Implement:

- Premium design system
- Navigation
- Room database
- Settings
- Plant library
- Responsive tablet layouts

Run a debug build.

## Phase 2 — Care engine

Implement:

- Watering schedules
- Fertilising reminders
- Repotting reminders
- Pruning reminders
- Care calendar
- Completion history

Test recurrence and timezone handling.

## Phase 3 — Growth journal

Implement:

- Photo capture
- Photo storage
- Growth timeline
- Notes
- Before-and-after comparisons

## Phase 4 — Voice interaction

Implement:

- Speech recognition
- Push-to-talk controls
- Voice commands
- TextToSpeech feedback
- Locale-aware parsing
- Touch fallback

Test unavailable recognition services.

## Phase 5 — Plant identification

Implement an optional identification adapter.

Use a real supported engine when available.

Document external API