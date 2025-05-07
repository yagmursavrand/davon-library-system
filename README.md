# Davon Library System

A web-based system for managing library inventory, users, and checkouts.

## Git Workflow

We follow the Git Flow branching model:

- `main`: Production-ready code
- `develop`: Integration branch for features
- `feature/*`: Feature branches (e.g., `feature/frontend-login`, `feature/backend-api`)

### Example Workflow

1. `git checkout develop`
2. `git checkout -b feature/my-feature`
3. Work on the feature and commit changes
4. `git push origin feature/my-feature`
5. Create a Pull Request to merge into `develop`
6. After testing, `develop` is merged into `main` for production

### Commit Message Conventions

- `feat:` for new features
- `fix:` for bug fixes
- `docs:` for documentation
- `refactor:` for code refactoring
- `test:` for adding tests
- `chore:` for config or setup changes
