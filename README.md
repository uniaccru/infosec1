# Лабораторная работа #1

REST API на Java, Spring Boot, Gradle и PostgreSQL.
Проект демонстрирует аутентификацию пользователей, JWT-защиту API,
хэширование паролей и автоматические security-проверки в GitHub Actions.

## Стек

- Java 17
- Spring Boot 3.5.8
- Spring Security 6
- Spring Data JPA / Hibernate
- PostgreSQL
- Gradle
- JWT (`jjwt`)
- BCrypt
- SpotBugs (SAST)
- OWASP Dependency-Check (SCA)

## Структура проекта

```text
src/main/java/com/example/api
├── config       # конфигурация Spring Security
├── controller   # REST-контроллеры
├── dto          # объекты запросов и ответов API
├── entity       # JPA-сущности
├── exception    # обработка ошибок API
├── repository   # доступ к данным через Spring Data JPA
├── security     # JWT-сервис и JWT-фильтр
└── service      # бизнес-логика
```

## API

### Проверка состояния приложения

```http
GET /health
```

Доступен без JWT.

Пример ответа:

```json
{
  "status": "UP"
}
```

### Аутентификация

```http
POST /auth/login
Content-Type: application/json
```

Тело запроса:

```json
{
  "login": "demo",
  "password": "demo-password"
}
```

Пример через curl:

```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"login":"demo","password":"demo-password"}'
```

Успешный ответ содержит JWT:

```json
{
  "message": "Login successful for user 'demo'.",
  "token": "<JWT>"
}
```

### Получение данных

```http
GET /api/data
Authorization: Bearer $TOKEN
```

Пример:

```bash
TOKEN="<JWT из ответа /auth/login>"

curl http://localhost:8080/api/data \
  -H "Authorization: Bearer $TOKEN"
```

Без токена endpoint возвращает `401 Unauthorized`:

```bash
curl -i http://localhost:8080/api/data
```

Готовый набор запросов для Insomnia находится в
[`insomnia/functional-api.json`](./insomnia/functional-api.json).

## Реализованные меры защиты

### Защита от SQL-инъекций

Доступ к пользователям реализован через Spring Data JPA и репозиторий
[`UserRepository.java`](./src/main/java/com/example/api/repository/UserRepository.java).
Поиск пользователя выполняется методом:

```java
findByLogin(String login)
```

SQL формируется Hibernate с параметрами. Конкатенация пользовательского ввода
в SQL-запросы не используется.

### Защита паролей

Пароли не сохраняются в открытом виде. В
[`SecurityConfig.java`](./src/main/java/com/example/api/config/SecurityConfig.java)
используется `BCryptPasswordEncoder`.

При входе пароль проверяется через:

```java
passwordEncoder.matches(rawPassword, passwordHash)
```

В базе хранится только поле `password_hash` сущности
[`User.java`](./src/main/java/com/example/api/entity/User.java).

### JWT-аутентификация

После успешного входа [`AuthService.java`](./src/main/java/com/example/api/service/AuthService.java)
создаёт JWT с subject, равным логину пользователя.

[`JwtAuthenticationFilter.java`](./src/main/java/com/example/api/security/JwtAuthenticationFilter.java):

1. читает заголовок `Authorization`;
2. проверяет формат `Bearer <token>`;
3. проверяет подпись и срок действия JWT;
4. загружает пользователя из PostgreSQL;
5. устанавливает аутентификацию в `SecurityContext`.

Защита endpoint-ов настроена в
[`SecurityConfig.java`](./src/main/java/com/example/api/config/SecurityConfig.java):

- `/auth/login` и `/health` доступны без токена;
- остальные endpoint-ы требуют аутентификацию;
- сессии отключены, используется stateless JWT-аутентификация;
- CSRF отключён для stateless REST API.

### Защита от XSS

Пользовательские строки, возвращаемые API, экранируются через Spring
`HtmlUtils.htmlEscape(...)` перед формированием ответа. Это предотвращает
возврат HTML/JavaScript-разметки как активного содержимого.

### Конфигурация секретов

Пароль PostgreSQL, JWT secret и demo-параметры передаются через переменные
окружения. Секреты не должны храниться в исходном коде или коммититься в
репозиторий.

## CI/CD и security-сканеры

GitHub Actions workflow находится в
[`.github/workflows/ci.yml`](./.github/workflows/ci.yml).

При push в `main` и при Pull Request запускаются:

1. `gradle test` — сборка и тесты;
2. `gradle spotbugsMain` — SAST-анализ Java-кода;
3. `gradle dependencyCheckAnalyze` — SCA-проверка Gradle-зависимостей по базе CVE.

## Скриншоты отчётов

### SAST — SpotBugs

SpotBugs проанализировал Java-классы проекта и не обнаружил предупреждений:

![SpotBugs report](./images/Снимок%20экрана%202026-11-02%20в%2002.59.33.png)

### SCA — OWASP Dependency-Check

Dependency-Check сформировал отчёт по зависимостям проекта. На момент создания скриншота в отчёте указано `Vulnerabilities Found: 0`:

![Dependency-Check report](./images/Снимок%20экрана%202026-11-02%20в%2002.59.40.png)
