# Arkanoid Game - Object-Oriented Programming Project

## Author
Group 8 - Class 2526I_INT2204_4
1. Phạm Ngọc Dũng - 24020094
2. Phạm Nam Khánh - 24020175
3. Phạm Hữu Tùng - 24020355
4. Nguyễn Hữu Vinh - 24020364

**Instructor**: Kiều Văn Tuyên
**Semester**: HK1 - 2025 - 2026

---

## Description
This is an advanced version of the classic Arkanoid game, developed in Java 17 and JavaFX 21. This project serves as the final assignment for the Object-Oriented Programming course, demonstrating the practical application of OOP principles, design patterns, and multithreading in a complex, functional product.

Beyond the basic mechanics, this project integrates modern features including:

* User Authentication: Full Email/Password and Google sign-in/signup flows.

* Online Services: Uses Firebase (via REST API) for a persistent online leaderboard and user presence tracking.

*  Multiple Game Modes: Features both a classic ADVENTURE mode and a unique LOCAL_BATTLE (2-Player Versus) mode.

**Key features:**
1. Platform: Built on Java 17+ with JavaFX 21 for the GUI, using Maven for build management.

2. OOP Principles: Strongly implements Encapsulation, Inheritance, Polymorphism, and Abstraction (e.g., GameObject -> MovableObject -> Ball/Paddle; Brick hierarchy).

3. Design Patterns: Applies multiple design patterns: Abstract Factory (Bricks), Strategy (Power-ups), Singleton (Managers), and Observer (via JavaFX Bindings). See details below.

4. Multithreading: Uses an ExecutorService for audio and asynchronous Task/CompletableFuture for I/O (asset loading, network calls) to ensure a smooth, non-blocking UI.

5. Advanced Features: Includes sound effects with anti-spam logic, sprite animations, a diverse power-up system, and a level loader.

6. Persistence: Supports an online leaderboard via Firebase and a local save/load stub.

**Game mechanics:**
- Control a paddle to bounce a ball and destroy bricks.
- Collect power-ups (both good and bad) for special abilities.
- Progress through multiple levels loaded from text files.
- Compete for a high score on the online leaderboard.
- Challenge a friend in a 2-player local versus mode.

---

## UML Diagram

### Class Diagram
![Class Diagram](docs/uml/demo_arkanoid.png)

---

## Design Patterns Implementation

### 1. Abstract Factory Pattern
**Used in:** The Brick creation system.
* Abstract Factory: `BrickFactory.java`.
* Concrete Factories: `NormalBrickFactory`, `StrongBrickFactory`, `ExplosiveBrickFactory`, etc.
* Client: `LevelManager.java` uses a Map<Character, BrickFactory> to instantiate the correct Brick subclass based on a character ('N', 'S', 'X') read from a level file.

### 2. Strategy Pattern
**Used in:** The Power-up system..
* Strategy Interface: `PowerUpEffect.java` defines the apply() and remove() methods.
* Concrete Strategies: `FastBallEffect`, `ExpandPaddleEffect`, `LaserPaddleEffect`, etc.
* Context: `PowerUpEffectManager.java` manages which strategies (effects) are currently active and their remaining durations.

### 3. Singleton Pattern
**Used in:** Global resource and service management.
* Purpose: Ensures a single instance for shared resources.
* Used in:
  * `SoundManager.java`: Manages the audio thread pool and sound caching.
  * `ResourceManager.java`: Manages image and sprite sheet caching.
  * `PowerUpSprite.java`: Manages the power-up sprite sheet animations.

---

## Multithreading Implementation

The game uses multiple threads to ensure smooth performance:

1. **Game Loop & Rendering Thread (Main Thread)**: 
The `AnimationTimer` in `GameSceneRoot.java`runs on the `JavaFX Application Thread (JAT)`.
This single thread handles both logic updates (`gameManager.update()`) and rendering (`gameManager.render()`) sequentially, which is the standard, safe practice in JavaFX to prevent concurrency issues.
2. **Audio Thread Pool**: `SoundManager.java` creates an `ExecutorService` (a thread pool) to play sound effects asynchronously. 
This prevents the main game loop from lagging when an audio clip is loaded or played.
3. **Asynchronous I/O Threads**:
   * *Asset Loading*: `AssetLoadingTask.java` (a `javafx.concurrent.Task`) runs on a new background thread to load all images and sounds, allowing the JAT to remain responsive and display a progress bar.

   * *Network I/O*: All authentication and leaderboard calls (`AuthService.java`, `FirebaseScoreService.java`) use `HttpClient.sendAsync(...)` and `CompletableFuture`. This ensures network latency does not freeze the UI.

---

## Installation

1. Clone the project from the repository.
2. Open the project in the IDE.
3. Run the project.

## Usage

### Controls Adventure
| Key / Input           | Action (Adventure Mode) | Action (Versus Mode) | 
|-----------------------|--------|----------------------|
| `←` / `A` / `D` / `→` | Move paddle left/right | N/A                  |
| `Mouse`               | Move paddle to cursor position | N/A                  |
| `W` / `S`             |          N/A                      | Player 1 (Left) Move Up/Down |
| `↑` / `↓`             |               N/A                 | Player 2 (Right) Move Up/Down |
| `ENTER`               |            N/A                    | Player 2 Launch Ball / Restart |
| `SPACE`               | Launch ball / Shoot laser |  Player 1 Launch Ball|
| `ESC`                 | Pause / Resume Game | Pause / Resume Game  |


### How to Play
1. **Start the game**: Click "New Game" from the main menu.
2. **Control the paddle**: Use arrow keys or A/D to move left and right.
3. **Launch the ball**: Press SPACE to launch the ball from the paddle.
4. **Destroy bricks**: Bounce the ball to hit and destroy bricks.
5. **Collect power-ups**: Catch falling power-ups for special abilities.
6. **Avoid losing the ball**: Keep the ball from falling below the paddle.
7. **Complete the level**: Destroy all destructible bricks to advance.

### Power-ups
| Icon | Name | Effect |
|------|------|--------|
| 🟦 | Expand Paddle | Increases paddle width for 10 seconds |
| 🟥 | Shrink Paddle | Decreases paddle width for 10 seconds |
| ⚡ | Fast Ball | Increases ball speed by 30% |
| 🐌 | Slow Ball | Decreases ball speed by 30% |
| 🎯 | Multi Ball | Spawns 2 additional balls |
| 🔫 | Laser Gun | Shoot lasers to destroy bricks for 15 seconds |
| 🧲 | Magnet | Ball sticks to paddle, launch with SPACE |
| 🛡️ | Shield | Protects from losing one life |
| 🔥 | Fire Ball | Ball passes through bricks for 12 seconds |

### Scoring System
- Normal Brick: 100 points
- Strong Brick: 300 points
- Explosive Brick: 500 points + nearby bricks
- Power-up Collection: 50 points
- Combo Multiplier: x2, x3, x4... for consecutive hits

---

## Demo

### Screenshots

**Main Menu**  
![Main Menu](docs/screenshots/menu.png)

**Gameplay**  
![Gameplay](docs/screenshots/gameplay.png)

**Power-ups in Action**  
![Power-ups](docs/screenshots/powerups.png)

**Leaderboard**  
![Leaderboard](docs/screenshots/leaderboard.png)

### Video Demo
[![Video Demo](docs/screenshots/video-thumbnail.png)](docs/demo/gameplay.mp4)

*Full gameplay video is available in `docs/demo/gameplay.mp4`*

---

## Future Improvements

### Planned Features
1. **Additional game modes**
   - Time attack mode
   - Survival mode with endless levels
   - Co-op multiplayer mode

2. **Enhanced gameplay**
   - Boss battles at end of worlds
   - More power-up varieties (freeze time, shield wall, etc.)
   - Achievements system

3. **Technical improvements**
   - Migrate to LibGDX or JavaFX for better graphics
   - Add particle effects and advanced animations
   - Implement AI opponent mode
   - Add online leaderboard with database backend

---

## Technologies Used

| Technology | Version | Purpose |
|------------|---------|---------|
| Java | 17+ | Core language |
| JavaFX | 19.0.2 | GUI framework |
| Maven | 3.9+ | Build tool |
| Jackson | 2.15.0 | JSON processing |

---

## License

This project is developed for educational purposes only.

**Academic Integrity:** This code is provided as a reference. Please follow your institution's academic integrity policies.

---

## Notes

- The game was developed as part of the Object-Oriented Programming with Java course curriculum.
- All code is written by group members with guidance from the instructor.
- Some assets (images, sounds) may be used for educational purposes under fair use.
- The project demonstrates practical application of OOP concepts and design patterns.

---

*Last updated: [Ngày/Tháng/Năm]*
