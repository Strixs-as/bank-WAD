# 🏦 Bank System — Расширенная Банковская Система

Полнофункциональная банковская система с REST API, веб-интерфейсом на Thymeleaf и полноправной админ-панелью. Разработана на **Spring Boot 3** + **Microsoft SQL Server**.

---

## ✨ Возможности

| Модуль | Функциональность |
|---|---|
| 👤 **Авторизация и Роли** | Регистрация, вход, JWT + Сессии, разделение доступа (USER, ADMIN) |
| 🏦 **Управление Счетами** | Открытие текущих, сберегательных и инвестиционных счетов в разных валютах (USD, EUR, RUB) |
| 💸 **Денежные Операции** | Внутренние переводы, пополнение, снятие, история всех транзакций |
| 💳 **Пластиковые Карты** | Выпуск карт привязанных к счету, блокировка, разблокировка и деактивация |
| 📋 **Кредитование** | Подача заявок на кредиты, рассмотрение и утверждение заявок через админ-панель |
| 💰 **Вклады (Депозиты)** | Открытие срочных вкладов с автоматическим расчётом процентной ставки |
| 🛡 **Админ-панель** | Мониторинг всех пользователей онлайн, управление счетами пользователей, одобрение/отклонение запросов |

---

## 🛠 Технологии

**Backend Core:**
- Java 17+, Spring Boot 3.2
- Spring Web MVC & REST
- Spring Security (JWT + HTTP Sessions)
- Spring Data JPA + Hibernate 6

**База данных:**
- Microsoft SQL Server 2022
- HikariCP Connection Pool

**Frontend:**
- Thymeleaf (шаблонизатор HTML)
- HTML5, CSS3, Vanilla JS
- Клиентская валидация и взаимодействие с REST API

---

## 🚀 Быстрый старт

### Минимальные требования
- **Java JDK 17+**
- **Maven 3.6+**
- **Microsoft SQL Server** (локально установлен)

### Запуск проекта

1. **Клонируйте репозиторий:**
   ```bash
   git clone https://github.com/Strixs-as/bank-WAD.git
   cd bank_system
   ```

2. **Соберите проект:**
   ```bash
   ./mvnw clean package -DskipTests
   ```

3. **Запустите приложение:**
   ```bash
   java -Dspring.profiles.active=sqlserver -jar target/bank_system-1.0-SNAPSHOT.jar
   ```

> 🌐 Откройте приложение в браузере: **http://localhost:8080**

---

## ✉️ Почта (SMTP) и локальный запуск без ошибок

В некоторых сетях SMTP-порты до `smtp.gmail.com` (587/465) **блокируются** (в логах будет `SocketTimeoutException: Connect timed out`).
Это **не баг приложения** — это ограничение сети/фаервола.

Чтобы сайт и API всегда работали корректно локально:

- По умолчанию отправка почты **выключена**: `app.mail.enabled=false`
- Вместо реальной отправки используется **dev-fallback**: письмо логируется в консоль/лог (без падений)

Включить реальную почту можно только если ваша сеть пропускает SMTP и вы задали переменные окружения:

- `SPRING_MAIL_APP_PASSWORD` (или `SPRING_MAIL_PASSWORD`) — Gmail App Password
- `APP_MAIL_ENABLED=true`

Настройки лежат в `src/main/resources/application-sqlserver.properties`.

---

## 🧯 "java.lang.instrument ASSERTION FAILED ... JPLISAgent.c:876"

Это сообщение генерируется **JDK agent-инструментации** (javaagent) при запуске из IDE.
Обычно связано не с кодом проекта, а с конфликтом агентов (debugger/capture/profiler/hotswap).

Что делать (IntelliJ IDEA):

1. Запустите приложение **без профайлера и capture agent**.
   - Отключите Async Profiler / Java Flight Recorder capture в Run Configuration
2. Если включены плагины типа JRebel / DevTools hot swap — временно отключите.
3. Если ошибка появляется только при Debug, попробуйте обычный Run.

Важно: на работоспособность API/сайта это обычно не влияет, но сильно засоряет вывод.

---

## ⚙️ Конфигурация SQL Server

Убедитесь, что параметры в `src/main/resources/application-sqlserver.properties` совпадают с вашими учетными данными:

```properties
spring.datasource.url=jdbc:sqlserver://localhost:1433;databaseName=BankSystem;encrypt=false;trustServerCertificate=true
spring.datasource.username=sa
spring.datasource.password=sa
```

> ⚠️ **Важно:** База данных `BankSystem` создаётся и инициализируется автоматически при первом запуске (спасибо компоненту `DatabaseInitializer`). Убедитесь, что служба SQL Server запущена и TCP/IP подключение включено.

---

## 🔌 Как работает REST API

Вы можете взаимодействовать с системой через сторонние программы (например, Postman).
Защищённые эндпоинты требуют передачи токена: `Authorization: Bearer <JWT_TOKEN>`.

### 🔐 Аутентификация `/api/auth`
- `POST /register` — Регистрация нового клиента
- `POST /login` — Вход и получение JWT токена

### 🏦 Счета `/api/accounts`
- `POST /` — Создать счёт (`CHECKING`, `SAVINGS`, `INVESTMENT`)
- `GET /` — Список всех доступных счетов

### 💸 Транзакции `/api/transactions`
- `POST /deposit` — Пополнение баланса
- `POST /withdraw` — Снятие средств
- `POST /transfer` — Перевод между активными счетами
- `GET /account/{id}` — Выписка (история совершенных операций)

### 📋 Заявки `/api/loans` и `/api/deposits`
- **Кредиты**: `POST /` (оформление заявок), `PUT /{id}/approve` (одобрение админом).
- **Депозиты**: `POST /` (открытие вкладов), `PUT /{id}/close` (досрочное закрытие).

> 📦 Готовая коллекция Postman для тестирования: [`docs/BankSystem.postman_collection.json`](docs/BankSystem.postman_collection.json)

---

## 📁 Структура веб-приложения (MVC)

В дополнение к REST API проект реализует классическую монолитную архитектуру MVC:

```text
bank_system/
├── docs/                 # Документация, шпаргалки и Postman-коллекции
├── src/main/java/com/techstore/bank_system/
│   ├── controller/       # Web (MVC) контроллеры, отдающие HTML
│   ├── resource/         # REST API контроллеры, отдающие JSON
│   ├── service/          # Бизнес-логика приложения
│   ├── repository/       # Доступ к БД через Spring Data
│   ├── entity/           # JPA Сущности таблиц БД
│   ├── dto/              # Классы передачи данных
│   ├── config/           # Настройки безопасности веб-интерфейса и API
│   └── util/             # Автоматическая инициализация и утилиты
└── src/main/resources/
    ├── templates/        # HTML страницы Thymeleaf (home, login, register, admin, profile)
    ├── static/           # Статика (CSS, скрипты)
    └── application*.properties # Настройки профилей среды
```

---

## 📄 Лицензия

MIT License — свободное использование в образовательных целях.

---

## 👤 Разработчик

**Макеш Найман** — Студент ВТиПО-33  
GitHub: [Strixs-as](https://github.com/Strixs-as)  
Email: [makeshnaiman@gmail.com](mailto:makeshnaiman@gmail.com)

*Учебный проект включает в себя лабораторные работы по дисциплинам Web Application Development (WAD) и Java EE.*
