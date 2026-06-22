# Contributing to OpinAI

Thank you for your interest in contributing to OpinAI! We welcome community contributions to help improve the project.

## Code of Conduct

By participating in this project, you agree to abide by standard professional etiquette and respect all contributors.

## How Can I Contribute?

### Reporting Bugs
*   Check the Issues tab to see if the bug has already been reported.
*   If not, open a new issue with a clear description, steps to reproduce, and screenshots if applicable.

### Suggesting Enhancements
*   Open an issue explaining the proposed feature and why it would be beneficial to the project.

### Submitting Pull Requests
1.  Fork the repository and create your branch from `main` or `master`.
2.  Install dependencies and verify changes locally.
3.  Write tests for any new logic or modifications.
4.  Ensure the entire test suite passes successfully.
    *   For the backend: Run `./mvnw clean verify`
    *   For the frontend: Run `npm run build`
5.  Submit a pull request describing the changes and referencing any related issues.

## Style Guidelines

*   **Java**: Follow standard Java coding conventions and Spring Boot best practices. Ensure proper logging with MDC correlation IDs for async operations.
*   **React & TypeScript**: Keep state management clean and localized. Ensure outstanding API calls are cancelled using `AbortController` signals where appropriate.
