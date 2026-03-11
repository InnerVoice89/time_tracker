# Time Tracker

REST приложение для учета рабочего времени пользователей.

## Технологии

- Java 11
- Servlets
- JDBC
- Docker и Docker Compose (для контейнерной версии)
- PostgreSQL (если запуск без Docker)
- Tomcat
- Maven

## Функционал

- регистрация пользователя
- авторизация
- запуск задачи
- пауза/возобновление задачи
- завершение задачи
- история задач
- статистика за период
- админ управление пользователями

## Запуск проекта

### 1. Клонируйте репозиторий:

git clone https://github.com/InnerVoice89/time_tracker.git

### 2. Сборка проекта

mvn clean package

### 3. Запуск через Docker

docker-compose up --build

### 4. Приложение будет доступно

http://localhost:8080

## Предварительная настройка

После запуска приложения в базе автоматически создается тестовый администратор:

| Параметр | Значение |
|----------|----------|
| Логин    | admin    |
| Пароль   | admin    |
| Роль     | ADMIN    |

Используйте эти данные для тестирования API.

## Структура проекта

src/main/java/com/tracker

- dao — работа с БД
- services — бизнес логика
- servlets — REST endpoints
- dto — объекты передачи данных
- utils — вспомогательные классы
- security — авторизация и фильтры
- config — конфигурация приложения

## База данных

PostgreSQL запускается через docker-compose.

Настройки:

DB_NAME=tracker  
DB_USERNAME=admin  
DB_PASSWORD=admin

Можно поменять настройки БД через переменные окружения в docker-compose(*Опционально)

## API

### Авторизация

POST /auth

Параметры (application/x-www-form-urlencoded):

| Параметр | Значение |
|----------|----------|
| username | admin    |
| password | admin    |

### Задачи

Создание нового пользователя:
POST /api/admin/create-user  
JSON Body:  
{
"id":1,
"username":"user",
"password":"pass",
"timeZone":"Europe/Moscow",
"roles":[
"USER"
]
}

Создание новой задачи:  
POST /api/task/start?taskName=Task

Завершение задачи:  
POST /api/task/stop/{id}

Пауза/возобновление существующей задачи:  
POST /api/task/pause/{id}

Получение информации по конкретной задаче:  
GET /api/admin/task/{id}

Получение информации по всем задачам пользователя :
GET /api/admin/tasks/history/{id}

Задачи пользователя за период:
POST /api/admin/period  
JSON Body:  
{
"userId":1,
"periodStart":"2025-02-01 00:00:00",
"periodEnd":"2025-05-04 00:00:00"
}

## Документация (JavaDoc)

Для удобства изучения API и классов проекта, JavaDoc можно сгенерировать командой:

mvn javadoc:javadoc -Dencoding=UTF-8 -Doclint=none -Dprivate

После успешной генерации HTML-файлы будут доступны в:

target/site/apidocs/index.html

## Roadmap / Планы развития

В дальнейшем планируется:

- добавление unit и integration тестов
- внедрение Liquibase миграций
- добавление Swagger/OpenAPI документации
- разработка **frontend UI** для работы с системой через браузер;

## Примечания

-Все даты и времена возвращаются с учетом временной зоны пользователя.

-Эндпоинты /api/admin/* доступны только для пользователей с ролью ADMIN.