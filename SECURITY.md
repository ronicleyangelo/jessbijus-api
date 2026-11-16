# Acesso administrativo

O catálogo e o envio de pedidos são públicos. O painel exige um administrador ativo no banco. Não há cadastro público de administradores.

## Primeiro administrador

Configure `NEON_JDBC_URL`, `NEON_DB_USER` e `NEON_DB_PASSWORD` no ambiente do processo. Um arquivo `.env` não é carregado automaticamente pelo Spring.

No IntelliJ, abra **Run → Edit Configurations → AdminBootstrap → Environment variables** e copie as três variáveis do Neon da configuração `JessbijusApiApplication`. Cada configuração tem seu próprio ambiente; a execução do bootstrap não herda as variáveis da API. O bootstrap valida essas propriedades e inicializa a conexão antes de solicitar login e senha.

Execute a classe `br.com.jessbijus.api.security.AdminBootstrap` com essas três variáveis. Informe o login e uma senha de 8 a 128 caracteres com pelo menos um símbolo, depois confirme a senha. No console da IDE, a entrada pode ficar visível; prefira um terminal com console nativo e não salve o conteúdo do console.

Pelo PowerShell, dentro de `backend`, com Java e Maven Wrapper configurados:

```powershell
.\mvnw.cmd package
java -jar .\target\backend-0.0.1-SNAPSHOT.jar --bootstrap-admin
```

O comando aplica a migração, cria somente o primeiro administrador e encerra sem abrir um servidor HTTP. Um bloqueio transacional no PostgreSQL impede duas configurações iniciais simultâneas. Se já houver qualquer usuário, o comando recusa a criação. A senha e o hash não são impressos nem recebidos por argumento de linha de comando.

## Esqueci a senha

No ambiente que contém as mesmas variáveis `NEON_JDBC_URL`, `NEON_DB_USER` e `NEON_DB_PASSWORD` da API, execute:

```powershell
java -jar .\target\backend-0.0.1-SNAPSHOT.jar --reset-admin-password
```

Informe o login do administrador, a nova senha e a confirmação quando solicitado. O comando não abre um servidor HTTP. A redefinição invalida as sessões anteriores. Execute-o apenas em um terminal com acesso autorizado ao banco da loja. No console da IDE, a senha pode ficar visível.

Depois, execute `JessbijusApiApplication` normalmente ou `java -jar .\target\backend-0.0.1-SNAPSHOT.jar`. Não ative o perfil `bootstrap-admin` no servidor normal. Abra `/admin/login` no frontend.

As antigas variáveis `JESSBIJUS_ADMIN_USERNAME` e `JESSBIJUS_ADMIN_PASSWORD_HASH` não são mais utilizadas. A API inicia mesmo antes de existir um administrador, com o painel inacessível até a configuração inicial.

## Desenvolvimento e produção

- Para HTTP local, defina `SESSION_COOKIE_SECURE=false` na execução da API. O padrão é `true` e exige HTTPS.
- A loja e a API usam o mesmo domínio, incluindo o proxy Angular em desenvolvimento. Não é necessário habilitar CORS.
- O cookie de sessão é HttpOnly, SameSite=Lax e expira após 30 minutos sem atividade. O servidor troca o identificador de sessão no login. Não há senha, token de autenticação ou cabeçalho Basic armazenado no navegador.
- O token CSRF é obtido em `GET /api/csrf`; o Angular envia o cabeçalho `X-XSRF-TOKEN`. Login, logout e alteração de senha exigem CSRF. Apenas o envio público de pedidos permanece isento.
- Mantenha HTTPS em produção. Caso termine TLS em um proxy, configure o reconhecimento de HTTPS somente para proxies confiáveis no ambiente de hospedagem.
- As sessões ficam na memória de uma instância e terminam quando ela reinicia. Para várias réplicas, adote Spring Session com armazenamento compartilhado antes de escalar.
- O login bloqueia por 15 minutos na terceira falha por usuário ou na 12ª falha por IP. Um login bem-sucedido limpa as falhas do usuário. A troca de senha tem um limite separado de três respostas de validação inválida por IP. O HTTP 429 inclui Retry-After. Os contadores ficam em memória por instância.

## Senhas e revogação

As senhas usam Argon2id, salt aleatório de 16 bytes, saída de 32 bytes, memória de 19 MiB, duas iterações e paralelismo 1. O `DelegatingPasswordEncoder` grava o identificador `{argon2id}` para permitir evolução futura. Meça latência e memória no servidor antes de elevar os custos; a atualização dos hashes existentes deverá ser adicionada quando os parâmetros forem alterados.

Em **Meu perfil → Alterar senha**, informe a senha atual e a nova senha. A nova deve ser diferente da atual. Após a alteração, todas as sessões anteriores perdem acesso: a sessão atual é encerrada e as outras são recusadas na próxima requisição. Desativar o usuário no banco também revoga seu acesso na próxima requisição.

A recuperação por e-mail ainda depende de um provedor de envio e será implementada separadamente. Não existe senha padrão ou endpoint de recuperação provisório.

## Migração e validação

Flyway controla exclusivamente o schema `jessbijus_auth`, com `admin_users` e histórico de migrações. O usuário de migração precisa ter permissão para criar esse schema. Não habilite baseline automático em schemas existentes. As tabelas legadas da loja continuam com o comportamento Hibernate `ddl-auto=update`; a migração completa do catálogo é um trabalho separado.

Antes da primeira publicação, valide em uma cópia do PostgreSQL: migração, execução do comando inicial, login, troca de senha, logout e cookies HTTPS. Os testes automatizados locais usam H2 em modo PostgreSQL; o bloqueio exclusivo do comando inicial é específico de PostgreSQL e tem testes unitários, não um teste de integração com Neon.

```powershell
.\mvnw.cmd test
```

No frontend:

```powershell
npm test -- --watch=false
npm run build
```
