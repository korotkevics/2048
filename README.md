# Play 2048

A 2048 game built using clean Hexagonal Architecture principles in Java/Spring Boot.

To build and run the entire application stack execute `docker compose up --build` from the root directory; the game will be available at `http://localhost:85`.

# Details

## Back-end

- **Architecture:** Strict Hexagonal Architecture. The `domain` package is dependency-free and isolated from framework-specific code.
- **Concurrency:** Auto-play runs in isolated virtual threads, managed via UUID-based session tokens to prevent orphaned execution.
- **Messaging:** Asynchronous state updates and AI suggestions are pushed via WebSocket (STOMP) through a `DomainEventStream`.
- **Quality:** High-level facades are verified using JGiven BDD scenarios, achieving >85% domain line coverage.
- **AI Engines:**
    - **Algo-based:** Depth-6 Expectimax with parallel root evaluation and Gradient weight heuristics.
    - **LLM-based:** Prompt-engineered `qwen2.5:1.5b` via Ollama, augmented with backend-calculated strategy scores.

## Front-end

- **State Management:** React with Redux Toolkit and TypeScript for type-safe state transitions.
- **Communication:** Bi-directional communication using REST for configuration and WebSocket for real-time game state synchronization.
- **UX:** Responsive CSS Grid layout with support for keyboard shortcuts (Arrows, Undo, AI Suggest, Auto-Play).

## Implementation Remarks

- **Persistence:** PostgreSQL is utilized for high scores and game history, ensuring state survives refreshes or container restarts.
- **Customization:** Dynamic `GameSettings` (tile values, spawn probabilities) are fully supported by both the game engine and AI strategies.
- **Development:** Built using Gemini CLI in an interactive, automated loop, focusing on surgical refactors and rigorous verification.