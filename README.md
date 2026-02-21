# BookKeeping
Record the debts that we are lending to other people.

## What was implemented

1. **Spring Boot App**: A robust REST API running on Java 21, built using Gradle.
2. **Database Models**: 
   - `Borrower`: Saves name, email, and phone.
   - `Loan`: Tracks amount, date lent, exact 1-month (`dueDate`), and loan status (`ACTIVE` vs `REPAID`).
3. **Core API Endpoints**:
   - `POST /api/borrowers` and `GET /api/borrowers`
   - `POST /api/loans` and `GET /api/loans`
   - `PUT /api/loans/{id}/repay`
4. **Notification Job**: The `NotificationService` is scheduled to run every day at 8:00 AM. It queries the database for any active loans where the due date is today or earlier and dispatches an email exclusively to your personal Gmail account (as requested).
5. **System Architecture Upgrades**: 
   - **Interface-Driven Design**: The Service layer employs interface contracts (`BorrowerService`, `LoanService`) for loose coupling and scalability.
   - **Data Transfer Objects (DTOs)**: API payloads exclusively use `BorrowerDto` and `LoanDto` to securely decouple database entities from external clients, avoiding infinite recursion issues.
   - **Constructor Injection**: Replaced Lombok's `@RequiredArgsConstructor` with explicit constructor injection to favor explicit dependency definitions.
   - **Global Exception Handling**: Any API requests for non-existent entities (like invalid Loan or Borrower IDs) cleanly fail with a structured `404 Not Found` JSON message.
   - **SLF4J Logging**: The Controller and Service layers both boast extensive logging to trace incoming REST requests, data fetches, and actions taken (e.g. Loan repays).

## Frontend UI (GitHub Pages Ready)
The application includes a fully static frontend designed to be hosted cheaply or for free on GitHub Pages. It can be found in the `frontend/` directory.

- **Technology**: Built entirely using Vanilla HTML5, CSS3, and JavaScript (no npm, no React/Vue required).
- **Accessibility**: Includes proper ARIA tags and semantic HTML properties to comply with WCAG standards.
- **Aesthetics**: Clean, responsive layout adhering to modern CSS design token structures.
- **Integration**: The `app.js` handles API communication via asynchronous `fetch` logic, and the backend has been upgraded with a `WebConfig` class to allow Cross-Origin Resource Sharing (CORS).

## Running Locally

### 1. Setup Environment
First, make sure you have a local MySQL database running named `bookkeeping_db`. You can configure your Gmail application password to test the email notifications:

```bash
export GMAIL_USERNAME=your-email@gmail.com
export GMAIL_PASSWORD=your-app-password
```
*(Note: Default dummy values are provided in `application.properties`, so the app will boot even without exporting these, but actual email sending will fail unless they are provided).*

### 2. Start the Application
Run the standard Gradle boot run command:

```bash
./gradlew bootRun
```

### 3. Try the API endpoints

**Add a Borrower**:
```bash
curl -X POST http://localhost:8080/api/borrowers \
-H "Content-Type: application/json" \
-d '{"name": "Alice", "email": "alice@example.com", "phone": "1234567890"}'
```

**Add a Loan**: *(Note: the `dueDate` is automatically set to exactly 1 month from `dateLent`)*
```bash
curl -X POST http://localhost:8080/api/loans \
-H "Content-Type: application/json" \
-d '{
    "borrowerId": 1,
    "amount": 200.50,
    "dateLent": "2026-02-21"
}'
```

**Check Active Loans**:
```bash
curl http://localhost:8080/api/loans
```

## Validation Results

- **Unit Tests**: Full Unit test coverage for `BorrowerService` and `LoanService` using Mockito. Validates entity creation, fetching logic, and error scenarios.
- **Integration Tests**: Comprehensive endpoint validations using `MockMvc` mapped to an H2 testing database running in memory. This correctly replicates exactly how the controllers process HTTP requests and database interactions simultaneously.
- **Context Load Test**: Passed successfully (`BUILD SUCCESSFUL`). The application initializes the H2 in-memory DB during tests and validates all JPA mappings without needing MySQL.
- **Lombok Validations**: Adhered to best practices by utilizing `@Builder.Default` to prevent warning traces during application initialization.
