# Weather Forecast Application

A Spring Boot application that provides weather forecasts based on postal codes with international support, caching, and robust error handling.

## Features

- 🌍 International postal code support (US, Canada, UK, and more)
- 🌡️ Current temperature, daily highs/lows, and 24-hour forecast
- 💾 Smart caching with 15-minute expiry to reduce API calls
- 🔄 Circuit breaker pattern for resilient API integration
- 📊 Cache indicators showing fresh vs. cached data
- 🌐 RESTful API design following Spring Boot best practices

## Architecture

The application follows a layered architecture:

- **Controllers**: Handle HTTP requests and responses
- **Services**: Business logic and orchestration
- **Clients**: External API integrations
- **Models**: Data representations
- **Repository**: Cache management

## Prerequisites

- Java 17 or higher
- Maven 3.6+

## Getting Started

1. **Clone the repository**

   ```bash
   git clone https://github.com/yourusername/weather-forecast.git
   cd weather-forecast
   ```

2. **Build the application**

   ```bash
   ./mvnw clean install
   ```

3. **Run the application**

   ```bash
   ./mvnw spring-boot:run
   ```

4. **Access the application**
   Open your browser and navigate to `http://localhost:8080`

## API Endpoints

### Get Weather by Postal Code

```
GET /api/weather/zipcode/{zipCode}?countryCode={countryCode}
```

**Parameters:**

- `zipCode`: The postal code (required)
- `countryCode`: ISO 3166-1 alpha-2 country code (optional, defaults to US)

**Example:**

```bash
curl "http://localhost:8080/api/weather/zipcode/10001?countryCode=US"
```

**Response:**

```json
{
  "zipCode": "10001",
  "coordinates": {
    "latitude": 40.7305,
    "longitude": -73.9925
  },
  "currentTemperature": 22.5,
  "highTemperature": 25.0,
  "lowTemperature": 18.0,
  "hourlyForecast": [
    {
      "time": "2025-04-21T00:00:00",
      "temperature": 20.5
    }
  ],
  "timestamp": "2025-04-21T15:30:00",
  "fromCache": false
}
```

## Technical Stack

- **Spring Boot 3.x**: Application framework
- **Spring Web**: REST API development
- **Spring Cache**: Caching abstraction
- **Caffeine**: In-memory caching implementation
- **Resilience4j**: Circuit breaker implementation
- **Jackson**: JSON processing
- **JUnit 5 & Mockito**: Testing
- **Maven**: Build tool

## External APIs Used

- **Nominatim API**: Geocoding (postal code to coordinates)
- **Open-Meteo API**: Weather data

## Features in Detail

### Caching

The application uses Caffeine cache to store weather data for 15 minutes, reducing API calls for frequently requested locations. Cache status is clearly indicated in the UI and API responses.

### Circuit Breaker

Resilience4j circuit breakers protect against cascading failures when external APIs are unavailable. The circuit breaker monitors failure rates and temporarily stops requests to failing services.

### International Support

The application supports postal codes from multiple countries with format validation:

- United States (e.g., 10001)
- Canada (e.g., M5V 3L9)
- United Kingdom (e.g., SW1A 1AA)
- Australia (e.g., 2000)
- And many more...

### Error Handling

Comprehensive error handling provides user-friendly messages for:

- Invalid postal codes
- Mismatched country/postal code combinations
- API failures
- Network issues

## Configuration

The application can be configured via `application.properties`:

```properties
# Server configuration
server.port=8080

# Cache configuration
spring.cache.cache-names=weatherCache
spring.cache.caffeine.spec=maximumSize=100,expireAfterWrite=15m

# Circuit breaker configuration
resilience4j.circuitbreaker.instances.nominatimApi.failureRateThreshold=50
resilience4j.circuitbreaker.instances.nominatimApi.waitDurationInOpenState=60s
resilience4j.circuitbreaker.instances.openMeteoApi.failureRateThreshold=50
resilience4j.circuitbreaker.instances.openMeteoApi.waitDurationInOpenState=60s
```

## Testing

The project includes comprehensive unit tests for all components:

```bash
# Run all tests
./mvnw test

# Run tests with coverage report
./mvnw test jacoco:report
```

## API Usage Best Practices

- The application implements rate limiting by adding a 1-second delay between Nominatim API requests
- Caching reduces unnecessary API calls for the same location
- Circuit breakers prevent excessive requests to failing services

## Project Structure

```
src/
├── main/
│   ├── java/
│   │   └── com/example/weatherforecast/
│   │       ├── client/        # External API clients
│   │       ├── config/        # Application configuration
│   │       ├── controller/    # REST controllers
│   │       ├── exception/     # Custom exceptions
│   │       ├── model/         # Domain models
│   │       ├── repository/    # Cache repository
│   │       └── service/       # Business logic
│   └── resources/
│       ├── static/           # Frontend files
│       └── application.properties
└── test/                     # Unit tests
```

## Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Acknowledgments

- [Nominatim](https://nominatim.org/) for geocoding services
- [Open-Meteo](https://open-meteo.com/) for weather data
- Spring Boot community for excellent documentation and support
