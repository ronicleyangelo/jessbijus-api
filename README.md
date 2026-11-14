# Jessbijus API

API Spring Boot 4.1.2 e Java 21 para a loja Jessbijus. Persistência PostgreSQL no Neon.

Configure as variáveis de `.env.example` no ambiente do processo. O arquivo é apenas um modelo; não coloque credenciais reais no Git. Para gerar a senha administrativa, siga [SECURITY.md](SECURITY.md).

No Windows, com Java 21 em `JAVA_HOME`, execute `./mvnw.cmd spring-boot:run`. A API fica em `http://localhost:8080/jessbijus/api` por padrão. O frontend Angular usa um proxy local para essa porta.

As rotas de catálogo e envio de pedidos são públicas. `/api/admin/**` exige `ROLE_ADMIN`.
