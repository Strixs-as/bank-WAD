# Troubleshooting: почта (SMTP) и `java.lang.instrument ASSERTION FAILED`

## 1) Почему письма не доходят

В логах было:

- `SocketTimeoutException: Connect timed out`
- `Couldn't connect to host, port: smtp.gmail.com, 587`

Это **не ошибка кода** — это означает, что **исходящие соединения на SMTP-порты заблокированы сетью/фаерволом/провайдером**.

Проверка из Windows PowerShell:

```powershell
Test-NetConnection smtp.gmail.com -Port 587
Test-NetConnection smtp.gmail.com -Port 465
```

Если `TcpTestSucceeded : False` — приложение физически не может достучаться до Gmail SMTP.

### Что можно сделать

- Разрешить исходящие соединения на 587 (STARTTLS) или 465 (SMTPS) в фаерволе/антивирусе/роутере.
- Если вы в корпоративной сети — попросить открыть порты или используйте другой почтовый шлюз.
- Использовать локальный SMTP для разработки (например, MailHog / Papercut SMTP).
- Использовать API-провайдеров (SendGrid/Mailgun) вместо SMTP.

## 2) Dev-режим (без SMTP)

В проекте почта управляется флагом:

- `app.mail.enabled` — включает/выключает SMTP попытки.

Когда SMTP недоступен, рекомендуемый dev-режим:

- `app.mail.enabled=false`
- `app.mail.dev-log-fallback=true`

В этом режиме письма **не отправляются**, но их содержимое попадает в лог (чтобы можно было тестировать коды разблокировки/логина).

## 3) Как включить реальную отправку (когда сеть позволяет)

Задайте переменные окружения (секреты не храним в репозитории):

```powershell
$env:APP_MAIL_ENABLED="true"
$env:SPRING_MAIL_PASSWORD="<gmail app password>"
```

И убедитесь, что `Test-NetConnection` к 587/465 = `True`.

## 4) `java.lang.instrument ASSERTION FAILED ... JPLISAgent.c:876`

Это сообщение обычно появляется из-за конфликта Java-агентов в режиме Debug/Profiler (IntelliJ Capture Agent, async-profiler, code coverage, hot swap, и т.п.).

### Что делать

1. Запустите приложение **без профайлера** (обычный Run, без async-profiler/JFR capture).
2. В Run/Debug Configuration проверьте VM options и уберите лишние:
   - `-javaagent:...captureAgent...`
   - `-agentpath:...libasyncProfiler...`
   - другие сторонние `-javaagent` (coverage/hotswap)

После отключения агентов приложение продолжит работать, а assertion пропадёт.

