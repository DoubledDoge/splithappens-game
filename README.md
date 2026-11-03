# Split Happens

[![CI/CD](https://github.com/DoubledDoge/splithappens-game/actions/workflows/ci-cd.yml/badge.svg)](https://github.com/DoubledDoge/splithappens-game/actions/workflows/ci-cd.yml)
[![CodeQL](https://github.com/DoubledDoge/splithappens-game/actions/workflows/codeql.yml/badge.svg)](https://github.com/DoubledDoge/splithappens-game/actions/workflows/codeql.yml)
[![Min SDK](https://img.shields.io/badge/minSdk-24-brightgreen)](#requirements)
[![License](https://img.shields.io/badge/license-PolyForm%20Strict%201.0.0-blue)](LICENSE)

## Features

- **Two game modes**
	- **Wildcard** (default): the full experience with every special payout.
	- **Traditional**: regular blackjack payouts only.
- **Full player actions**: hit, stand, double down, split (up to 4 hands), surrender, and insurance
  when the dealer shows an ace.
- **Chip bankroll**: start with 2,500 chips.
- **Persistent history**: sessions, games, hands, and cards are stored in a local Room database, so
  your results and net chips survive app restarts.
- **Player profile**: display name, chip balance, profile picture, and per-mode net chip stats.
- **Settings**: dark theme, card totals, dealer rule banner, sound effects, haptic feedback, fast
  deal, and auto-stand on 21.

## Rules

- The dealer draws until reaching 17 or more and stands on all 17s.
- Hands are dealt from a shoe that reshuffles between rounds, never mid-round, once fewer than 25%
  of cards remain.
- A hand made after a split cannot count as a natural blackjack.
- Insurance is capped at half the original bet.
- Surrender is only available on the first two cards of an unsplit hand.
- If the dealer has blackjack, only a player natural (Blackjack, Queen + Ace, Jack + Ace) pushes.
  Every other hand loses.

### Payouts

The multiplier is the total chips returned per chip wagered, rounded down in the house's favour. A
multiplier of 2 means the stake back plus an equal win.

| Result            | Multiplier | Hand                                | Modes    |
|-------------------|------------|-------------------------------------|----------|
| Loss              | 0          |                                     | Both     |
| Surrender         | 0.5        | Half the wager back                 | Both     |
| Push              | 1          | Stake returned                      | Both     |
| Win               | 2          | Any normal win                      | Both     |
| Blackjack         | 3          | Ace + a ten-value card              | Both     |
| Queen Jack        | 10         | Queen + Ace                         | Wildcard |
| Natural Blackjack | 50         | Jack + Ace                          | Wildcard |
| Triple Seven      | 100        | 21 with three 7s                    | Wildcard |
| Six Card          | 120        | Six cards without busting           | Wildcard |
| Seven Card        | 250        | Seven or more cards without busting | Wildcard |

Insurance pays 3x the insurance bet if the dealer has blackjack.

## Tech Stack

| Area             | Choice                                                           |
|------------------|------------------------------------------------------------------|
| Language         | Kotlin 2.4                                                       |
| UI               | Jetpack Compose with Material 3, Navigation Compose              |
| Persistence      | Room 3 (`androidx.room3`) with KSP and the Android SQLite driver |
| Images           | Coil                                                             |
| Build            | Gradle 9.7 (Kotlin DSL) with a version catalog, AGP 9.2          |
| Min / target SDK | 29 / 37                                                          |

## Project Structure

```
splithappens-game/
├── docs/                           Design notes (Jetpack Room, AdMob integration)
├── .github/                        CI/CD, release, and CodeQL workflows
└── splithappens/
    ├── schemas/                    Room-generated schema JSON (committed)
    └── src/
        ├── main/java/io/github/doubleddoge/splithappens/
        │   ├── game/               Shoe, Round, HandValue, HandEvaluator, GameSessionManager
        │   ├── data/               Database, entities, DAOs, enums, GameHistory view
        │   ├── navigation/         Screen routes and AppNavigation
        │   ├── components/         Shared composables
        │   ├── ui/theme/           Colors, typography, theme
        │   └── *Screen.kt / *ViewModel.kt   Splash, Home, Menu, Profile, Settings
        ├── test/                   JVM unit tests (hand values, entities, results)
        └── androidTest/            Instrumented tests (DAOs, constraints, cascades, session manager)
```

## Data Model

Room stores everything locally. Deleting a user cascades down through their sessions, games, hands,
and hand cards.

```
Users ─┬─ ProfilePictures (1:1)
       └─ Sessions ── Games ── Hands ── HandCards ── Cards
```

A read-only `GameHistory` view derives one row per game (dealer total, player hands, net chips) from
the raw tables, so there is no second copy of the data to drift out of sync. Split hands are
modelled with a `parentHandId` rather than a separate status. See [
`docs/Jetpack Room.md`](docs/Jetpack%20Room.md) for the full schema, conventions, and payout
details.

## Getting Started

### Prerequisites

- Android Studio (latest stable)
- A JDK supported by the Gradle wrapper (CI uses Temurin 25)
- An Android device or emulator running API 29 or higher

### Build and run

```bash
git clone https://github.com/DoubledDoge/splithappens.git
cd splithappens

# Build a debug APK
./gradlew :splithappens:assembleDebug

# Install on a connected device or emulator
./gradlew :splithappens:installDebug
```

Or open the project in Android Studio and run the `splithappens` configuration.

### Tests

```bash
# JVM unit tests
./gradlew test

# Instrumented tests (needs a device or emulator)
./gradlew :splithappens:connectedAndroidTest
```

### Release builds

Release signing is read from environment variables. Without them, the release build is left
unsigned.

| Variable                    | Purpose                   |
|-----------------------------|---------------------------|
| `SIGNING_KEYSTORE_PATH`     | Path to the keystore file |
| `SIGNING_KEYSTORE_PASSWORD` | Keystore password         |
| `SIGNING_KEY_ALIAS`         | Key alias                 |
| `SIGNING_KEY_PASSWORD`      | Key password              |

Version info can be passed with `-PversionName=1.2.3 -PversionCode=10203`. Without them, the app
builds as `0.0.0-dev` with version code 1.

```bash
./gradlew :splithappens:assembleRelease
```

## Contributing

Use [Conventional Commits](https://www.conventionalcommits.org) for commit messages, since the
release changelog is generated from them, and fill out the pull request template.

## License

Licensed under the [PolyForm Strict License 1.0.0](LICENSE).
