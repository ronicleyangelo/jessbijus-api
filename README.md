# Jessbijus API

API Spring Boot com PostgreSQL. Configure `NEON_JDBC_URL`, `NEON_DB_USER` e `NEON_DB_PASSWORD` no ambiente do processo.

Para desenvolvimento HTTP local, configure também `SESSION_COOKIE_SECURE=false`. Em produção o padrão exige HTTPS.

Antes de acessar o painel pela primeira vez, execute `AdminBootstrap` no IntelliJ ou gere o JAR com `.\mvnw.cmd package` e execute:

```powershell
java -jar .\target\backend-0.0.1-SNAPSHOT.jar --bootstrap-admin
```

Depois inicie `JessbijusApiApplication` normalmente. Veja [SECURITY.md](SECURITY.md) para configuração, migrações, sessões e troca de senha.
