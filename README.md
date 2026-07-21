<div align="center">

# 🎬 CineTicket - Movie Review & Reservation System

[![Java](https://img.shields.io/badge/Java-17-4a4e69?style=for-the-badge&logo=openjdk&logoColor=white)](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.2.3-4a4e69?style=for-the-badge&logo=spring&logoColor=white)](https://spring.io/projects/spring-boot)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-4a4e69?style=for-the-badge&logo=mysql&logoColor=white)](https://www.mysql.com/)
[![JWT](https://img.shields.io/badge/JWT-0.12.6-4a4e69?style=for-the-badge&logo=jsonwebtokens&logoColor=white)](https://github.com/jwtk/jjwt)
[![Stripe](https://img.shields.io/badge/Stripe-22.0.0-4a4e69?style=for-the-badge&logo=stripe&logoColor=white)](https://stripe.com/)
[![License](https://img.shields.io/badge/License-MIT-4a4e69?style=for-the-badge&logo=opensourceinitiative&logoColor=white)](LICENSE)

**A modern, secure, and scalable RESTful API for movie reviews and theater seat reservations**

🌐 **[Live Demo: https://cineticket.onrender.com](https://cineticket.onrender.com)**

[Overview](#-overview) •
[Features](#-key-features) •
[Demo](#️-frontend-demo) •
[Architecture](#-architecture) •
[Installation](#-installation) •
[API Reference](#-api-reference) •
[Security](#-security) •
[Documentation](#-documentation) •
[Contributing](#-contributing) •
[Contact](#-contact)

</div>

## 📋 Overview

**CineTicket** is an enterprise-grade Spring Boot application that provides a comprehensive solution for movie enthusiasts. It seamlessly integrates movie reviews with theater seat reservations, offering a complete platform for users to discover movies, share opinions, and book seats for upcoming shows.

Built with modern Java and Spring technologies, this API implements industry best practices including JWT authentication, role-based access control, Stripe payment integration, PDF receipt generation, email notifications, rate limiting, and comprehensive API documentation.

The system is deployed and fully functional at [https://cineticket.onrender.com](https://cineticket.onrender.com).

## 🖥️ Frontend Demo

| ![Admin Dashboard](docs/gifs/admin.gif) | ![User Experience](docs/gifs/user.gif) |
|:---:|:---:|
| **👨‍💼 Admin Dashboard** | **🧑‍💻 User Experience** |
| *Manage movies, theaters, showtimes, and view reservation data* | *Browse movies, read reviews, select seats, and complete the reservation process* |

## ✨ Key Features

| 🎬 **Movie Platform** | 🎟️ **Reservation System** | 🔒 **Enterprise Security** |
|:---|:---|:---|
| • Browse & search movies<br>• Rate & review movies<br>• Manage user reviews<br>• Movie recommendations | • Theater management<br>• Showtime scheduling<br>• Seat selection & booking<br>• Stripe payment integration<br>• PDF receipt generation<br>• Email notifications | • JWT authentication<br>• Role-based access control<br>• Rate limiting protection<br>• Secure transactions |
### 🌟 Advanced Features

| 💳 **Payment Processing** | 📧 **Email Notifications** | 🎫 **Reservation Management** |
|:---|:---|:---|
| • Stripe integration<br>• Checkout sessions<br>• Webhook handling<br>• Receipt generation | • Confirmation emails<br>• PDF attachments<br>• Branded templates | • Interactive seat selection<br>• Status tracking<br>• Concurrent booking protection |

## 🏗️ Architecture

### Technology Stack

- **Backend**: Java 17, Spring Boot 3.5.16, Spring Data JPA
- **Database**: MySQL 8.0
- **Security**: Spring Security, JJWT 0.12.6
- **API Documentation**: Swagger OpenAPI 3.0
- **Payment Processing**: Stripe API 22.0.0
- **PDF Generation**: iText 5.5.13.3
- **Email Service**: Spring Mail
- **Utilities**: Lombok, Resilience4j, MessageSource
- **Testing**: JUnit 5, Mockito, Testcontainers
- **Storage**: AWS S3 (AWS SDK v2)
- **Ops**: Spring Boot Actuator (`/actuator/health`), Dependabot

## 🔍 Installation

### Prerequisites

- Java 17 or higher
- Maven 3.6+
- MySQL 8.0+

### Quick Start Guide

1. **Clone the repository**

```bash
git clone https://github.com/chaudhary-lakshay/CineTicket.git
cd CineTicket
```

2. **Configure the database**

```sql
CREATE DATABASE moviereviewdbupdated;
```

3. **Configure application properties**

Update `src/main/resources/application.properties` with your database credentials, JWT configuration, and Stripe API keys.

> **⚠️ Security Note**: Generate a secure JWT secret using `openssl rand -base64 64` and never commit it to version control.

4. **Set up environment variables**

Create a `.env` file based on the provided `.env.example` with your configuration.

```bash
# Copy the example file
cp .env.example .env

# Edit the .env file with your actual values
# For security, generate a new JWT secret:
openssl rand -base64 64
```

5. **Build and run the application**

```bash
mvn clean install
mvn spring-boot:run
```

6. **Access the application**

- API: [http://localhost:8080](http://localhost:8080)
- Swagger UI: [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)

### Local Development Users

Seed data (including local dev users) is controlled by the `SQL_INIT_MODE` environment
variable — `always` by default for local development, set it to `never` in production.
No default credentials ship in production; `JWT_SECRET` is mandatory (the app fails fast
without it), and `CORS_ORIGINS` configures allowed origins.

## 🔥 API Reference

<p align="center">
  <a href="https://me3333-6732.postman.co/workspace/4f28700f-ee89-485e-a928-767cd44234f9">
    <img src="https://img.shields.io/badge/Postman-View%20Complete%20Collection-4a4e69?style=for-the-badge&logo=postman&logoColor=white" alt="View Complete Collection">
  </a>
</p>

### API Categories

<p align="center">
  <a href="#-authentication"><img src="https://img.shields.io/badge/🔑_Authentication-2_Endpoints-4a4e69?style=flat-square" alt="Authentication: 2 Endpoints"></a>
  <a href="#-movies"><img src="https://img.shields.io/badge/🎬_Movies-6_Endpoints-4a4e69?style=flat-square" alt="Movies: 6 Endpoints"></a>
  <a href="#-reviews"><img src="https://img.shields.io/badge/⭐_Reviews-4_Endpoints-4a4e69?style=flat-square" alt="Reviews: 4 Endpoints"></a>
</p>
<p align="center">
  <a href="#-theaters"><img src="https://img.shields.io/badge/🏛️_Theaters-6_Endpoints-4a4e69?style=flat-square" alt="Theaters: 6 Endpoints"></a>
  <a href="#-showtimes"><img src="https://img.shields.io/badge/📅_Showtimes-7_Endpoints-4a4e69?style=flat-square" alt="Showtimes: 7 Endpoints"></a>
  <a href="#-seats"><img src="https://img.shields.io/badge/💺_Seats-2_Endpoints-4a4e69?style=flat-square" alt="Seats: 2 Endpoints"></a>
</p>
<p align="center">
  <a href="#-reservations"><img src="https://img.shields.io/badge/🎟️_Reservations-6_Endpoints-4a4e69?style=flat-square" alt="Reservations: 6 Endpoints"></a>
</p>

### 🔑 Authentication

<table>
  <thead>
    <tr>
      <th>Method</th>
      <th>Endpoint</th>
      <th>Description</th>
      <th>Access</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td><code>POST</code></td>
      <td><code>/api/v1/auth/register</code></td>
      <td>Register new user</td>
      <td><img src="https://img.shields.io/badge/Public-6c757d?style=flat-square" alt="Public"></td>
    </tr>
    <tr>
      <td><code>POST</code></td>
      <td><code>/api/v1/auth/login</code></td>
      <td>Get JWT token</td>
      <td><img src="https://img.shields.io/badge/Public-6c757d?style=flat-square" alt="Public"></td>
    </tr>
  </tbody>
</table>

### 🎬 Movies

<table>
  <thead>
    <tr>
      <th>Method</th>
      <th>Endpoint</th>
      <th>Description</th>
      <th>Access</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td><code>GET</code></td>
      <td><code>/api/v1/movies</code></td>
      <td>Get all movies</td>
      <td><img src="https://img.shields.io/badge/Public-6c757d?style=flat-square" alt="Public"></td>
    </tr>
    <tr>
      <td><code>GET</code></td>
      <td><code>/api/v1/movies?search={query}</code></td>
      <td>Search movies</td>
      <td><img src="https://img.shields.io/badge/Public-6c757d?style=flat-square" alt="Public"></td>
    </tr>
    <tr>
      <td><code>GET</code></td>
      <td><code>/api/v1/movies/{id}</code></td>
      <td>Get movie by ID</td>
      <td><img src="https://img.shields.io/badge/Public-6c757d?style=flat-square" alt="Public"></td>
    </tr>
    <tr>
      <td><code>POST</code></td>
      <td><code>/api/v1/movies</code></td>
      <td>Add movie</td>
      <td><img src="https://img.shields.io/badge/Admin-343a40?style=flat-square" alt="Admin"></td>
    </tr>
    <tr>
      <td><code>PUT</code></td>
      <td><code>/api/v1/movies/{id}</code></td>
      <td>Update movie</td>
      <td><img src="https://img.shields.io/badge/Admin-343a40?style=flat-square" alt="Admin"></td>
    </tr>
    <tr>
      <td><code>DELETE</code></td>
      <td><code>/api/v1/movies/{id}</code></td>
      <td>Delete movie</td>
      <td><img src="https://img.shields.io/badge/Admin-343a40?style=flat-square" alt="Admin"></td>
    </tr>
  </tbody>
</table>

### ⭐ Reviews

<table>
  <thead>
    <tr>
      <th>Method</th>
      <th>Endpoint</th>
      <th>Description</th>
      <th>Access</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td><code>POST</code></td>
      <td><code>/api/v1/reviews/movies/{movieId}</code></td>
      <td>Add review</td>
      <td><img src="https://img.shields.io/badge/User-5a6268?style=flat-square" alt="User"> <img src="https://img.shields.io/badge/Admin-343a40?style=flat-square" alt="Admin"></td>
    </tr>
    <tr>
      <td><code>GET</code></td>
      <td><code>/api/v1/reviews/my-reviews</code></td>
      <td>Get user reviews</td>
      <td><img src="https://img.shields.io/badge/User-5a6268?style=flat-square" alt="User"> <img src="https://img.shields.io/badge/Admin-343a40?style=flat-square" alt="Admin"></td>
    </tr>
    <tr>
      <td><code>PUT</code></td>
      <td><code>/api/v1/reviews/{reviewId}</code></td>
      <td>Update review</td>
      <td><img src="https://img.shields.io/badge/Owner-495057?style=flat-square" alt="Owner"> <img src="https://img.shields.io/badge/Admin-343a40?style=flat-square" alt="Admin"></td>
    </tr>
    <tr>
      <td><code>DELETE</code></td>
      <td><code>/api/v1/reviews/{reviewId}</code></td>
      <td>Delete review</td>
      <td><img src="https://img.shields.io/badge/Owner-495057?style=flat-square" alt="Owner"> <img src="https://img.shields.io/badge/Admin-343a40?style=flat-square" alt="Admin"></td>
    </tr>
  </tbody>
</table>

### 🏛️ Theaters

<table>
  <thead>
    <tr>
      <th>Method</th>
      <th>Endpoint</th>
      <th>Description</th>
      <th>Access</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td><code>GET</code></td>
      <td><code>/api/v1/theaters</code></td>
      <td>Get all theaters</td>
      <td><img src="https://img.shields.io/badge/Public-6c757d?style=flat-square" alt="Public"></td>
    </tr>
    <tr>
      <td><code>GET</code></td>
      <td><code>/api/v1/theaters/{id}</code></td>
      <td>Get theater by ID</td>
      <td><img src="https://img.shields.io/badge/Public-6c757d?style=flat-square" alt="Public"></td>
    </tr>
    <tr>
      <td><code>GET</code></td>
      <td><code>/api/v1/theaters/search?location={location}</code></td>
      <td>Search theaters</td>
      <td><img src="https://img.shields.io/badge/Public-6c757d?style=flat-square" alt="Public"></td>
    </tr>
    <tr>
      <td><code>POST</code></td>
      <td><code>/api/v1/theaters</code></td>
      <td>Add theater</td>
      <td><img src="https://img.shields.io/badge/Admin-343a40?style=flat-square" alt="Admin"></td>
    </tr>
    <tr>
      <td><code>PUT</code></td>
      <td><code>/api/v1/theaters/{id}</code></td>
      <td>Update theater</td>
      <td><img src="https://img.shields.io/badge/Admin-343a40?style=flat-square" alt="Admin"></td>
    </tr>
    <tr>
      <td><code>DELETE</code></td>
      <td><code>/api/v1/theaters/{id}</code></td>
      <td>Delete theater</td>
      <td><img src="https://img.shields.io/badge/Admin-343a40?style=flat-square" alt="Admin"></td>
    </tr>
  </tbody>
</table>

### 📅 Showtimes

<table>
  <thead>
    <tr>
      <th>Method</th>
      <th>Endpoint</th>
      <th>Description</th>
      <th>Access</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td><code>GET</code></td>
      <td><code>/api/v1/showtimes?date={date}</code></td>
      <td>Get showtimes by date</td>
      <td><img src="https://img.shields.io/badge/Public-6c757d?style=flat-square" alt="Public"></td>
    </tr>
    <tr>
      <td><code>GET</code></td>
      <td><code>/api/v1/showtimes/movies/{movieId}</code></td>
      <td>Get showtimes by movie</td>
      <td><img src="https://img.shields.io/badge/Public-6c757d?style=flat-square" alt="Public"></td>
    </tr>
    <tr>
      <td><code>GET</code></td>
      <td><code>/api/v1/showtimes/theaters/{theaterId}</code></td>
      <td>Get showtimes by theater</td>
      <td><img src="https://img.shields.io/badge/Public-6c757d?style=flat-square" alt="Public"></td>
    </tr>
    <tr>
      <td><code>GET</code></td>
      <td><code>/api/v1/showtimes/available</code></td>
      <td>Get available showtimes</td>
      <td><img src="https://img.shields.io/badge/Public-6c757d?style=flat-square" alt="Public"></td>
    </tr>
    <tr>
      <td><code>POST</code></td>
      <td><code>/api/v1/showtimes</code></td>
      <td>Add showtime</td>
      <td><img src="https://img.shields.io/badge/Admin-343a40?style=flat-square" alt="Admin"></td>
    </tr>
    <tr>
      <td><code>PUT</code></td>
      <td><code>/api/v1/showtimes/{id}</code></td>
      <td>Update showtime</td>
      <td><img src="https://img.shields.io/badge/Admin-343a40?style=flat-square" alt="Admin"></td>
    </tr>
    <tr>
      <td><code>DELETE</code></td>
      <td><code>/api/v1/showtimes/{id}</code></td>
      <td>Delete showtime</td>
      <td><img src="https://img.shields.io/badge/Admin-343a40?style=flat-square" alt="Admin"></td>
    </tr>
  </tbody>
</table>

### 💺 Seats

<table>
  <thead>
    <tr>
      <th>Method</th>
      <th>Endpoint</th>
      <th>Description</th>
      <th>Access</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td><code>GET</code></td>
      <td><code>/api/v1/seats/showtimes/{showtimeId}</code></td>
      <td>Get all seats</td>
      <td><img src="https://img.shields.io/badge/Public-6c757d?style=flat-square" alt="Public"></td>
    </tr>
    <tr>
      <td><code>GET</code></td>
      <td><code>/api/v1/seats/showtimes/{showtimeId}/available</code></td>
      <td>Get available seats</td>
      <td><img src="https://img.shields.io/badge/Authenticated-4a4e69?style=flat-square" alt="Authenticated"></td>
    </tr>
  </tbody>
</table>

### 🎟️ Reservations

<table>
  <thead>
    <tr>
      <th>Method</th>
      <th>Endpoint</th>
      <th>Description</th>
      <th>Access</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td><code>POST</code></td>
      <td><code>/api/v1/reservations</code></td>
      <td>Create reservation</td>
      <td><img src="https://img.shields.io/badge/Authenticated-4a4e69?style=flat-square" alt="Authenticated"></td>
    </tr>
    <tr>
      <td><code>GET</code></td>
      <td><code>/api/v1/reservations/my-reservations</code></td>
      <td>Get user reservations</td>
      <td><img src="https://img.shields.io/badge/Authenticated-4a4e69?style=flat-square" alt="Authenticated"></td>
    </tr>
    <tr>
      <td><code>GET</code></td>
      <td><code>/api/v1/reservations/{id}</code></td>
      <td>Get reservation by ID</td>
      <td><img src="https://img.shields.io/badge/Owner-495057?style=flat-square" alt="Owner"> <img src="https://img.shields.io/badge/Admin-343a40?style=flat-square" alt="Admin"></td>
    </tr>
    <tr>
      <td><code>DELETE</code></td>
      <td><code>/api/v1/reservations/{id}</code></td>
      <td>Cancel reservation</td>
      <td><img src="https://img.shields.io/badge/Owner-495057?style=flat-square" alt="Owner"> <img src="https://img.shields.io/badge/Admin-343a40?style=flat-square" alt="Admin"></td>
    </tr>
    <tr>
      <td><code>GET</code></td>
      <td><code>/api/v1/reservations</code></td>
      <td>Get all reservations</td>
      <td><img src="https://img.shields.io/badge/Admin-343a40?style=flat-square" alt="Admin"></td>
    </tr>
    <tr>
      <td><code>GET</code></td>
      <td><code>/api/v1/reservations/reports/revenue</code></td>
      <td>Get revenue report</td>
      <td><img src="https://img.shields.io/badge/Admin-343a40?style=flat-square" alt="Admin"></td>
    </tr>
  </tbody>
</table>

## 🔒 Security

| **Security Features** | **Authentication Flow** |
|:---|:---|
| • **JWT Authentication**: Secure token-based authentication<br>• **Password Encryption**: BCrypt encoding (strength 12)<br>• **Role-Based Access Control**: User/admin permissions<br>• **Rate Limiting**: 100 requests per minute<br>• **Concurrent Access Control**: Pessimistic locking<br>• **Transactional Operations**: Data integrity<br>• **Secure Payments**: Stripe integration<br>• **Environment Variables**: Secure credential management | 1. User registers or logs in with credentials<br>2. Server validates credentials and returns a JWT token<br>3. Client includes JWT in Authorization header<br>4. Server validates token and grants access based on roles<br><br>**Example Header:**<br>`Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...` |

## 📖 Documentation

### API Documentation

| **Swagger UI** | **Postman Collection** |
|:---|:---|
| Interactive API documentation with request/response examples<br><br>• [Local Development](http://localhost:8080/swagger-ui/index.html)<br>• [Production](https://cineticket.onrender.com/swagger-ui/index.html)<br>• [OpenAPI Specification](https://cineticket.onrender.com/api-docs) | Complete API testing suite with environments<br><br>[![Run in Postman](https://img.shields.io/badge/Postman-Run%20in%20Postman-4a4e69?style=for-the-badge&logo=postman&logoColor=white)](https://me3333-6732.postman.co/workspace/4f28700f-ee89-485e-a928-767cd44234f9)<br><br>*Also available in `docs/postman` directory* |

### Response Format

All API responses follow a consistent format:

```json
{
  "success": true,
  "message": "operation.success.message",
  "data": {"Response data here"}
}
```

### Postman Collection Features

- **Complete API Coverage**: All endpoints from authentication to reservations
- **Environment Variables**: Pre-configured for development and testing
- **Authentication Handling**: Automatic JWT token management
- **Test Scripts**: Response validation and environment variable extraction
- **Request Examples**: Sample payloads for all operations

## 💻 Contributing

Contributions are welcome! Here's how you can contribute:

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/amazing-feature`
3. Commit your changes: `git commit -m 'Add some amazing feature'`
4. Push to the branch: `git push origin feature/amazing-feature`
5. Open a Pull Request


## 🔗 Related Links

- 🎬 **Original Problem Statement**: [roadmap.sh](https://roadmap.sh/projects/movie-reservation-system/solutions?u=658efb1fae22c12523f4de17)
- 🌐 **Live Demo**: [https://cineticket.onrender.com](https://cineticket.onrender.com)
- 📚 **API Documentation**: [https://cineticket.onrender.com/swagger-ui/index.html](https://cineticket.onrender.com/swagger-ui/index.html)


## 👨‍💻 Contact

**Lakshay Chaudhary**
📧 Email: [lakshaychaudhary2003@gmail.com](mailto:lakshaychaudhary2003@gmail.com)
💼 GitHub: [chaudhary-lakshay](https://github.com/chaudhary-lakshay)

---

<div align="center">
  <p>⭐ Star this repository if you find it helpful!</p>
  <p>
    <sub>Built with ❤️ by Lakshay Chaudhary</sub>
  </p>
</div>
