# 🚀 Быстрый Старт — Bank System

## 1. Требования

| Компонент | Версия |
|---|---|
| Java JDK | 17+ |
| Maven | 3.6+ |
| SQL Server | 2019/2022 (SSMS) |

---

## 2. Настройка SQL Server

### Включить аутентификацию SQL Server (sa)

1. Откройте **SSMS** → подключитесь к `localhost`
2. Правой кнопкой на сервер → **Properties** → **Security**
3. Выберите **SQL Server and Windows Authentication mode**
4. **Security** → **Logins** → `sa` → **Properties**:
   - Установите пароль: `sa`
   - **Status** → Login: **Enabled**
5. Перезапустите службу SQL Server

### Включить TCP/IP

1. Откройте **SQL Server Configuration Manager**
2. **SQL Server Network Configuration** → **Protocols for MSSQLSERVER**
3. Включите **TCP/IP**
4. Перезапустите службу SQL Server

> База данных `BankSystem` создаётся **автоматически** при первом запуске приложения.

---

## 3. Сборка и запуск

```bash
# Клонировать
git clone https://github.com/Strixs-as/bank-WAD.git
cd bank-WAD

# Собрать
./mvnw clean package -DskipTests

# Запустить с SQL Server
java -Dspring.profiles.active=sqlserver -jar target/bank_system-1.0-SNAPSHOT.jar
```

Или запустить через **IntelliJ IDEA**:
- Run → Edit Configurations → Active Profiles: `sqlserver`
- Запустить `BankSystemApplication`

---

## 4. Открыть в браузере

```
http://localhost:8080
```

### Тестовые данные

После первого запуска автоматически создаются роли:
- `USER` — обычный пользователь
- `ADMIN` — администратор
- `MANAGER` — менеджер

Зарегистрируйтесь через **http://localhost:8080/register.html**

---

## 5. Профили запуска

| Профиль | База данных | Команда |
|---|---|---|
| `sqlserver` | SQL Server localhost:1433 | `-Dspring.profiles.active=sqlserver` |
| `h2` | H2 in-memory (без настройки) | `-Dspring.profiles.active=h2` |

---

## 6. Устранение проблем

### Порт 8080 уже занят
```powershell
# Найти процесс
netstat -ano | findstr :8080
# Завершить процесс (замените PID)
taskkill /PID <PID> /F
```

### Login failed for user 'sa'
- Проверьте, что SQL Server запущен
- Убедитесь, что режим аутентификации — **Mixed Mode**
- Логин `sa` должен быть включён (Enabled)
- Проверьте пароль в `application-sqlserver.properties`

