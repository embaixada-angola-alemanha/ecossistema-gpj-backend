# GPJ -- Sistema de Gestao de Projecto (Backend)

API REST para gestao de projectos do Ecossistema Digital. Spring Boot 3.4 com Java 21, base de dados PostgreSQL, migracao Flyway, autenticacao Keycloak e mensageria RabbitMQ.

Faz parte do **Ecossistema Digital -- Embaixada de Angola na Alemanha**.

> Repositorio principal: [ecossistema-project](https://github.com/embaixada-angola-alemanha/ecossistema-project)

---

## Descricao

O **GPJ (Gestao de Projectos)** e o sistema central de acompanhamento de todos os projectos de desenvolvimento do Ecossistema Digital. Fornece uma API completa para gestao de sprints, tarefas, registos de tempo, blockers e milestones, com maquinas de estado para controlo de transicoes, tracking de dependencias entre tarefas, e metricas de progresso (burndown, velocity, capacity planning).

## Stack Tecnologica

| Camada        | Tecnologia                                      |
| ------------- | ----------------------------------------------- |
| Framework     | Spring Boot 3.4.3                               |
| Linguagem     | Java 21                                         |
| Base de Dados | PostgreSQL                                      |
| Migracao      | Flyway                                          |
| ORM           | Spring Data JPA / Hibernate                     |
| Mapeamento    | MapStruct 1.5.5                                 |
| Seguranca     | Spring Security OAuth2 Resource Server (Keycloak) |
| Mensageria    | RabbitMQ (Spring AMQP)                          |
| Documentacao  | SpringDoc OpenAPI 2.3 (Swagger UI)              |
| Monitorizacao | Spring Boot Actuator                            |
| Testes        | JUnit 5, Spring Boot Test, H2 (in-memory)       |
| Container     | Docker (Eclipse Temurin 21 JRE Alpine)          |
| CI/CD         | GitHub Actions                                  |

## Estrutura do Projecto

```
src/main/java/ao/gov/embaixada/gpj/
  GpjApplication.java          # Classe principal Spring Boot
  config/
    OpenApiConfig.java          # Configuracao Swagger/OpenAPI
  controller/
    SprintController.java       # CRUD + transicoes de estado de sprints
    TaskController.java         # CRUD + transicoes de estado de tarefas
    TimeLogController.java      # Registo de horas trabalhadas
    BlockerController.java      # Gestao de blockers
    MilestoneController.java    # Gestao de milestones
    DashboardController.java    # Endpoints de metricas e relatorios
  dto/
    SprintCreateRequest.java    # DTOs de entrada/saida para Sprints
    SprintResponse.java
    SprintUpdateRequest.java
    TaskCreateRequest.java      # DTOs de entrada/saida para Tasks
    TaskResponse.java
    TaskUpdateRequest.java
    TaskDependencyRequest.java
    TimeLogCreateRequest.java   # DTOs de Time Logs
    TimeLogResponse.java
    BlockerCreateRequest.java   # DTOs de Blockers
    BlockerResponse.java
    MilestoneCreateRequest.java # DTOs de Milestones
    MilestoneResponse.java
    DashboardResponse.java      # DTOs de Dashboard/Metricas
    BurndownResponse.java
    BurndownPoint.java
    VelocityResponse.java
    CapacityResponse.java
    ProjectReportResponse.java
  entity/
    Sprint.java                 # Entidade JPA - Sprint
    Task.java                   # Entidade JPA - Task (com dependencias M2M)
    TimeLog.java                # Entidade JPA - Registo de tempo
    Blocker.java                # Entidade JPA - Blocker
    Milestone.java              # Entidade JPA - Milestone
  enums/
    SprintStatus.java           # PLANNING, ACTIVE, COMPLETED, CANCELLED
    TaskStatus.java             # BACKLOG, TODO, IN_PROGRESS, IN_REVIEW, DONE, BLOCKED, CANCELLED
    TaskPriority.java           # Niveis de prioridade
    BlockerStatus.java          # Estados de blockers
    MilestoneStatus.java        # Estados de milestones
  exception/
    GlobalExceptionHandler.java         # Handler centralizado de excepcoes
    ResourceNotFoundException.java      # 404
    InvalidStateTransitionException.java # Transicao de estado invalida
    CircularDependencyException.java    # Dependencia circular detectada
    CapacityExceededException.java      # Capacidade do sprint excedida
  mapper/
    SprintMapper.java           # MapStruct: Sprint <-> DTO
    TaskMapper.java             # MapStruct: Task <-> DTO
    TimeLogMapper.java          # MapStruct: TimeLog <-> DTO
    BlockerMapper.java          # MapStruct: Blocker <-> DTO
    MilestoneMapper.java        # MapStruct: Milestone <-> DTO
  repository/
    SprintRepository.java       # Spring Data JPA repositories
    TaskRepository.java
    TimeLogRepository.java
    BlockerRepository.java
    MilestoneRepository.java
  service/
    SprintService.java          # Logica de negocio de Sprints
    TaskService.java            # Logica de negocio de Tasks
    TimeLogService.java         # Logica de negocio de Time Logs
    BlockerService.java         # Logica de negocio de Blockers
    MilestoneService.java       # Logica de negocio de Milestones
    DashboardService.java       # Calculo de metricas e relatorios
  statemachine/
    SprintStateMachine.java     # Maquina de estados: PLANNING -> ACTIVE -> COMPLETED
    TaskStateMachine.java       # Maquina de estados: BACKLOG -> TODO -> IN_PROGRESS -> IN_REVIEW -> DONE
  integration/
    GpjEventPublisher.java      # Publicacao de eventos via RabbitMQ
    GpjMonitorConsumer.java     # Consumidor de eventos de monitorizacao

src/main/resources/
  application.yml               # Configuracao principal
  application-staging.yml       # Configuracao de staging
  application-production.yml    # Configuracao de producao
  application-test.yml          # Configuracao de testes
  db/migration/
    V1__create_gpj_tables.sql   # Schema inicial (sprints, tasks, time_logs, task_dependencies)
    V2__add_blockers_milestones.sql # Tabelas de blockers e milestones

src/test/java/ao/gov/embaixada/gpj/
  GpjApplicationTest.java
  controller/                   # Testes de integracao dos controllers
    SprintControllerTest.java
    TaskControllerTest.java
    BlockerControllerTest.java
    MilestoneControllerTest.java
    DashboardControllerTest.java
  service/                      # Testes unitarios dos servicos
    SprintServiceTest.java
    TaskServiceTest.java
    TimeLogServiceTest.java
    BlockerServiceTest.java
    MilestoneServiceTest.java
    DashboardServiceTest.java
  statemachine/                 # Testes das maquinas de estado
    SprintStateMachineTest.java
    TaskStateMachineTest.java
```

## Funcionalidades

### Gestao de Sprints
- CRUD completo de sprints
- Maquina de estados: `PLANNING` -> `ACTIVE` -> `COMPLETED` (ou `CANCELLED`)
- Capacity planning (horas disponiveis por sprint)
- Datas de inicio e fim

### Gestao de Tarefas
- CRUD completo de tarefas
- Maquina de estados: `BACKLOG` -> `TODO` -> `IN_PROGRESS` -> `IN_REVIEW` -> `DONE`
- Estados adicionais: `BLOCKED`, `CANCELLED`
- Tracking de dependencias entre tarefas (many-to-many)
- Deteccao de dependencias circulares
- Atribuicao de responsavel (assignee)
- Horas estimadas e consumidas
- Percentagem de progresso

### Registo de Tempo
- Registo de horas trabalhadas por tarefa
- Calculo automatico de horas consumidas
- Historico por utilizador e data

### Blockers e Milestones
- Gestao de impedimentos (blockers) com estados
- Milestones do projecto com tracking de progresso

### Dashboard e Metricas
- Dados de burndown chart (ideal vs. real)
- Calculo de velocity por sprint
- Capacity planning e utilizacao
- Relatorios de projecto agregados

### Integracao
- Publicacao de eventos via RabbitMQ (GpjEventPublisher)
- Consumo de eventos de monitorizacao (GpjMonitorConsumer)
- Modulos commons partilhados: `commons-dto`, `commons-security`, `commons-audit`, `commons-integration`

## Pre-requisitos

- **Java** 21 (JDK)
- **Maven** >= 3.9
- **PostgreSQL** >= 15
- **Keycloak** (realm `ecossistema`)
- **RabbitMQ** >= 3.12

## Como Executar

### Base de Dados

```bash
# Criar base de dados
createdb gpj_db

# As migracoes Flyway sao executadas automaticamente ao iniciar a aplicacao
```

### Desenvolvimento

```bash
# Compilar
mvn clean compile

# Executar
mvn spring-boot:run

# A API estara disponivel em http://localhost:8084
```

### Testes

```bash
# Executar todos os testes (utiliza H2 in-memory)
mvn test

# Executar testes com relatorio
mvn test -Dmaven.test.failure.ignore=false
```

### Build de Producao

```bash
# Criar JAR
mvn clean package -DskipTests

# O artefacto e gerado em target/ecossistema-gpj-backend-0.1.0-SNAPSHOT.jar
```

### Docker

```bash
# Build da imagem (requer JAR compilado)
mvn clean package -DskipTests
docker build -t ecossistema-gpj-backend .

# Executar container
docker run -p 8084:8084 \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://host:5432/gpj_db \
  -e SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_ISSUER_URI=http://keycloak:8080/realms/ecossistema \
  ecossistema-gpj-backend
```

## Configuracao de Ambiente

### Perfis Spring

| Perfil        | Ficheiro                      | Descricao            |
| ------------- | ----------------------------- | -------------------- |
| (default)     | `application.yml`             | Desenvolvimento local |
| `staging`     | `application-staging.yml`     | Ambiente de staging  |
| `production`  | `application-production.yml`  | Ambiente de producao |
| `test`        | `application-test.yml`        | Testes (H2)          |

### Variaveis Principais

| Variavel                                                      | Descricao                        | Default                                          |
| ------------------------------------------------------------- | -------------------------------- | ------------------------------------------------ |
| `server.port`                                                 | Porta do servidor                | `8084`                                           |
| `spring.datasource.url`                                       | URL da base de dados             | `jdbc:postgresql://localhost:5432/gpj_db`        |
| `spring.security.oauth2.resourceserver.jwt.issuer-uri`        | URL do Keycloak                  | `http://localhost:8080/realms/ecossistema`       |
| `spring.rabbitmq.host`                                        | Host do RabbitMQ                 | `localhost`                                      |

## Documentacao da API

Com a aplicacao em execucao, a documentacao OpenAPI esta disponivel em:

- **Swagger UI**: [http://localhost:8084/swagger-ui.html](http://localhost:8084/swagger-ui.html)
- **OpenAPI JSON**: [http://localhost:8084/v3/api-docs](http://localhost:8084/v3/api-docs)

### Endpoints Principais

| Metodo | Endpoint                          | Descricao                    |
| ------ | --------------------------------- | ---------------------------- |
| GET    | `/api/sprints`                    | Listar sprints               |
| POST   | `/api/sprints`                    | Criar sprint                 |
| PATCH  | `/api/sprints/{id}/status`        | Transicao de estado          |
| GET    | `/api/tasks`                      | Listar tarefas               |
| POST   | `/api/tasks`                      | Criar tarefa                 |
| PATCH  | `/api/tasks/{id}/status`          | Transicao de estado          |
| POST   | `/api/tasks/{id}/dependencies`    | Adicionar dependencia        |
| GET    | `/api/time-logs`                  | Listar registos de tempo     |
| POST   | `/api/time-logs`                  | Registar tempo               |
| GET    | `/api/blockers`                   | Listar blockers              |
| POST   | `/api/blockers`                   | Criar blocker                |
| GET    | `/api/milestones`                 | Listar milestones            |
| POST   | `/api/milestones`                 | Criar milestone              |
| GET    | `/api/dashboard`                  | Dashboard geral              |
| GET    | `/api/dashboard/burndown/{id}`    | Dados de burndown            |
| GET    | `/api/dashboard/velocity`         | Dados de velocity            |

## Licenca

Projecto interno da Embaixada de Angola na Alemanha. Todos os direitos reservados.

---

> **Ecossistema Digital** | Embaixada de Angola na Alemanha
> Dominio: `embaixada-angola.site`
