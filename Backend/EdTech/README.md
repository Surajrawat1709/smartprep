# EdTech Backend Application

A Spring Boot application for generating educational questions from images using OCR (Tesseract) and LLM integration.

## Features

- **JWT Authentication**: Secure authentication with JWT tokens
- **Question Generation**: Generate questions from images using OCR and LLM
- **User Management**: User registration and authentication
- **CORS Support**: Configured for Angular frontend at http://localhost:4200
- **Question Banking**: Store and manage generated questions
- **Payment Integration**: Ready for payment processing features

## Technology Stack

- Java 21
- Spring Boot 3.5.6
- Spring Security with JWT
- MySQL Database
- Tesseract OCR
- JPA/Hibernate
- Maven

## Prerequisites

1. Java 21 or higher
2. MySQL Server
3. Tesseract OCR installed on your system
   - Download from: https://github.com/tesseract-ocr/tesseract
   - Default path configured: `C:/Program Files/Tesseract-OCR/tessdata`

## Setup Instructions

### 1. Database Setup

Create a MySQL database:
```sql
CREATE DATABASE photo_gallery;
```

### 2. Application Configuration

Update `src/main/resources/application.properties`:

```properties
# Database Configuration
spring.datasource.url=jdbc:mysql://localhost:3306/photo_gallery
spring.datasource.username=root
spring.datasource.password=YOUR_PASSWORD

# JWT Configuration
jwt.secret=YOUR_JWT_SECRET_KEY
jwt.expiration=86400000

# Tesseract Configuration
tesseract.data.path=C:/Program Files/Tesseract-OCR/tessdata
tesseract.language=eng

# LLM API Configuration
llm.api.url=https://api.openai.com/v1/chat/completions
llm.api.key=YOUR_OPENAI_API_KEY
```

### 3. Build and Run

```bash
# Build the project
./mvnw clean compile

# Run the application
./mvnw spring-boot:run
```

The application will start on http://localhost:9876

## API Endpoints

### Authentication

#### Register User
```http
POST /api/auth/signup
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "password123",
  "firstName": "John",
  "lastName": "Doe"
}
```

#### Login
```http
POST /api/auth/login
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "password123"
}
```

Response:
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "type": "Bearer",
  "email": "user@example.com",
  "firstName": "John",
  "lastName": "Doe"
}
```

### Question Generation

#### Generate Questions from Image
```http
POST /api/generate/questions
Authorization: Bearer <JWT_TOKEN>
Content-Type: multipart/form-data

Form Data:
- image: (image file)
- subject: "Mathematics"
- difficulty: "medium"
- questionCount: 5
```

Response:
```json
{
  "questions": [
    {
      "id": "uuid-string",
      "type": "MCQ",
      "question": "What is 2 + 2?",
      "options": ["3", "4", "5", "6"],
      "answer": "4",
      "explanation": "Basic addition: 2 + 2 equals 4"
    }
  ],
  "status": "success",
  "message": "Questions generated successfully"
}
```

### Health Check
```http
GET /api/generate/health
```

## Database Schema

The application automatically creates the following tables:

- `users`: User account information
- `question_banks`: Collections of questions
- `questions`: Individual questions
- `quizzes`: Quiz instances
- `payments`: Payment records

## Development Notes

1. **OCR Integration**: Uses Tesseract OCR to extract text from uploaded images
2. **LLM Integration**: Configured for OpenAI API (can be adapted for other LLM providers)
3. **Security**: JWT-based authentication with CORS support for frontend
4. **Error Handling**: Comprehensive error handling with fallback responses
5. **Logging**: Structured logging for monitoring and debugging

## Troubleshooting

### Common Issues

1. **Tesseract not found**
   - Ensure Tesseract is installed and the path in `application.properties` is correct
   - Verify the tessdata directory exists and contains language files

2. **Database connection issues**
   - Check MySQL is running
   - Verify database name and credentials
   - Ensure database exists

3. **JWT issues**
   - Ensure JWT secret is configured
   - Check token expiration settings

## Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## License

This project is licensed under the MIT License.