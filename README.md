# 🏦 Bank System — Банковская Система

Полнофункциональная банковская система с REST API и веб-интерфейсом, разработанная на **Spring Boot 3** + **SQL Server**.

---

## ✨ Возможности

| Модуль | Функциональность |
|---|---|
| 👤 Пользователи | Регистрация, вход, JWT-авторизация, роли (USER, ADMIN, MANAGER) |
| 🏦 Счета | Открытие счетов (Checking, Savings, Investment), мультивалютность (USD, EUR, RUB) |
| 💸 Переводы | Переводы между счетами, история транзакций |
| 💳 Карты | Выпуск дебетовых/кредитных карт, управление статусом |
| 📋 Кредиты | Оформление кредитов, расчёт процентов, график платежей |
| 💰 Вклады | Открытие депозитов, автоматический расчёт процентов |

---

## 🛠 Технологии

- **Backend**: Spring Boot 3.2, Spring Data JPA, Hibernate 6
- **Database**: Microsoft SQL Server 2022
- **Security**: JWT (JJWT 0.12), BCrypt
- **Frontend**: HTML5, CSS3, JavaScript (Vanilla)
- **Build**: Maven 3.6+
- **Java**: 17+

---

## 🚀 Быстрый старт

### Минимальные требования
- Java JDK 17+
- Maven 3.6+
- Microsoft SQL Server (локально)

### Запуск за 3 шага

```bash
# 1. Клонировать репозиторий
git clone https://github.com/Strixs-as/bank-WAD.git
cd bank-WAD

# 2. Собрать проект
./mvnw clean package -DskipTests

# 3. Запустить (SQL Server должен быть запущен)
java -Dspring.profiles.active=sqlserver -jar target/bank_system-1.0-SNAPSHOT.jar
```

Откройте браузер: **http://localhost:8080**

---

## ⚙️ Конфигурация SQL Server

Настройки в `src/main/resources/application-sqlserver.properties`:

```properties
spring.datasource.url=jdbc:sqlserver://localhost:1433;databaseName=BankSystem;encrypt=false;trustServerCertificate=true
spring.datasource.username=sa
spring.datasource.password=sa
```

> ⚠️ Убедитесь, что SQL Server запущен, TCP/IP включён, логин `sa` активен.
> База данных `BankSystem` создаётся автоматически при первом запуске.

---

## 🔌 REST API

Base URL: `http://localhost:8080`

Все защищённые эндпоинты требуют заголовок:
```
Authorization: Bearer <JWT_TOKEN>
```

---

### 🔐 Аутентификация — `/api/auth`

| Метод | URL | Описание | Auth |
|---|---|---|---|
| `POST` | `/api/auth/register` | Регистрация нового пользователя | ❌ |
| `POST` | `/api/auth/login` | Вход, получение JWT токена | ❌ |

#### POST `/api/auth/register`
```json
// Request
{
  "firstName": "Иван",
  "lastName": "Иванов",
  "email": "ivan@example.com",
  "password": "secret123"
}

// Response 201
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "message": "Успешно"
}
```

#### POST `/api/auth/login`
```json
// Request
{
  "email": "ivan@example.com",
  "password": "secret123"
}

// Response 200
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "message": "Успешно"
}
```

---

### 🏦 Счета — `/api/accounts`

| Метод | URL | Описание | Auth |
|---|---|---|---|
| `POST` | `/api/accounts` | Создать новый счёт | ✅ |
| `GET` | `/api/accounts` | Получить все счета пользователя | ✅ |
| `GET` | `/api/accounts/{accountNumber}` | Получить счёт по номеру | ✅ |

#### POST `/api/accounts`
```json
// Request
{
  "accountType": "CHECKING",   // CHECKING | SAVINGS | INVESTMENT
  "currency": "RUB",           // RUB | USD | EUR
  "initialDeposit": 1000.00
}

// Response 201 — объект Account
```

---

### 💸 Транзакции — `/api/transactions`

| Метод | URL | Описание | Auth |
|---|---|---|---|
| `POST` | `/api/transactions/transfer` | Перевод между счетами | ✅ |
| `POST` | `/api/transactions/deposit` | Пополнение счёта | ✅ |
| `POST` | `/api/transactions/withdraw` | Снятие со счёта | ✅ |
| `GET` | `/api/transactions/account/{accountId}` | История транзакций счёта | ✅ |

#### POST `/api/transactions/transfer`
```json
// Request
{
  "fromAccountNumber": "ACC-00000001",
  "toAccountNumber": "ACC-00000002",
  "amount": 500.00,
  "description": "Перевод"
}
```

#### POST `/api/transactions/deposit`
```json
// Request
{
  "accountNumber": "ACC-00000001",
  "amount": 1000.00,
  "description": "Пополнение"
}
```

---

### 💳 Карты — `/api/cards`

| Метод | URL | Описание | Auth |
|---|---|---|---|
| `POST` | `/api/cards/create/{accountId}` | Выпустить карту для счёта | ✅ |
| `GET` | `/api/cards` | Все карты пользователя | ✅ |
| `GET` | `/api/cards/active` | Активные карты пользователя | ✅ |
| `PUT` | `/api/cards/{cardId}/block` | Заблокировать карту | ✅ |
| `PUT` | `/api/cards/{cardId}/unblock` | Разблокировать карту | ✅ |
| `PUT` | `/api/cards/{cardId}/deactivate` | Деактивировать карту | ✅ |

---

### 📋 Кредиты — `/api/loans`

| Метод | URL | Описание | Auth |
|---|---|---|---|
| `POST` | `/api/loans` | Подать заявку на кредит | ✅ |
| `GET` | `/api/loans` | Кредиты пользователя | ✅ |
| `PUT` | `/api/loans/{loanId}/approve` | Одобрить кредит | ✅ |
| `PUT` | `/api/loans/{loanId}/reject` | Отклонить кредит | ✅ |
| `PUT` | `/api/loans/{loanId}/disburse` | Выдать кредит | ✅ |

#### POST `/api/loans`
```json
// Request
{
  "amount": 50000.00,
  "durationMonths": 12,
  "currency": "RUB",
  "accountId": 1
}
```

---

### 💰 Вклады — `/api/deposits`

| Метод | URL | Описание | Auth |
|---|---|---|---|
| `POST` | `/api/deposits` | Открыть вклад | ✅ |
| `GET` | `/api/deposits` | Вклады пользователя | ✅ |
| `PUT` | `/api/deposits/{depositId}/close` | Закрыть вклад | ✅ |

#### POST `/api/deposits`
```json
// Request
{
  "amount": 10000.00,
  "durationMonths": 6,
  "currency": "RUB",
  "accountId": 1
}
```

---

### 📝 Типичный сценарий работы с API

```
1. POST /api/auth/register  → получить токен
2. POST /api/accounts       → создать счёт (в ответе — accountNumber и id)
3. POST /api/transactions/deposit → пополнить счёт
4. POST /api/cards/create/{accountId} → выпустить карту
5. POST /api/loans          → подать заявку на кредит
6. POST /api/deposits       → открыть вклад
```

> 📦 Готовая коллекция Postman: [`docs/BankSystem.postman_collection.json`](docs/BankSystem.postman_collection.json)

---

## 📁 Структура проекта

```
bank_system/
├── src/main/java/com/techstore/bank_system/
│   ├── BankSystemApplication.java      — точка входа
│   ├── DatabaseInitializer.java        — создаёт БД до старта JPA
│   ├── resource/                       — REST контроллеры (API)
│   │   ├── AuthResource.java           — /api/auth
│   │   ├── AccountResource.java        — /api/accounts
│   │   ├── TransactionResource.java    — /api/transactions
│   │   ├── CardResource.java           — /api/cards
│   │   ├── LoanResource.java           — /api/loans
│   │   └── DepositResource.java        — /api/deposits
│   ├── entity/                         — JPA сущности
│   ├── repository/                     — Spring Data репозитории
│   ├── service/                        — бизнес-логика
│   ├── dto/                            — DTO объекты
│   └── util/                           — JWT, DataInitializer
└── src/main/resources/
    ├── application.properties
    ├── application-sqlserver.properties
    └── static/                         — HTML/CSS/JS фронтенд
```

---

## 📄 Лицензия

MIT License — свободное использование.

---

## 👤 Автор

**Найман**  
GitHub: [Strixs-as](https://github.com/Strixs-as)

