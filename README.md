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
| 💱 Обмен валют | Актуальные курсы, конвертация |

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

Подробная инструкция: [`docs/QUICK_START.md`](docs/QUICK_START.md)

### Минимальные требования
- Java JDK 17+
- Maven 3.6+
- Microsoft SQL Server (локально или Docker)

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

## 📁 Структура проекта

```
bank_system/
├── src/
│   ├── main/
│   │   ├── java/com/techstore/bank_system/
│   │   │   ├── BankSystemApplication.java   — точка входа
│   │   │   ├── DatabaseInitializer.java     — создаёт БД до старта JPA
│   │   │   ├── controller/                  — REST контроллеры
│   │   │   ├── entity/                      — JPA сущности
│   │   │   ├── repository/                  — Spring Data репозитории
│   │   │   ├── service/                     — бизнес-логика
│   │   │   ├── dto/                         — DTO объекты
│   │   │   └── util/                        — утилиты (JWT, DataInitializer)
│   │   └── resources/
│   │       ├── application.properties       — активный профиль
│   │       ├── application-sqlserver.properties
│   │       ├── application-h2.properties
│   │       └── static/                      — HTML/CSS/JS фронтенд
├── docs/                                    — документация
├── pom.xml
└── README.md
```

---

## 📄 Лицензия

MIT License — свободное использование.

