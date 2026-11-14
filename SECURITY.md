# Acesso administrativo

O catálogo, a saúde da API e o envio de pedidos são públicos. Todas as rotas `/api/admin/**` exigem `ROLE_ADMIN`. Clientes não precisam de conta nesta etapa.

Configure estas variáveis no ambiente do backend antes de iniciá-lo:

- `JESSBIJUS_ADMIN_USERNAME`: usuário do administrador.
- `JESSBIJUS_ADMIN_PASSWORD_HASH`: senha codificada com BCrypt, incluindo o prefixo `{bcrypt}`. Exemplo de formato: `{bcrypt}$2a$12$...`.

Para gerar o hash, execute `AdminPasswordHashGenerator` pelo IntelliJ, digite uma senha com pelo menos 12 caracteres no console e copie o valor `JESSBIJUS_ADMIN_PASSWORD_HASH={bcrypt}...` exibido. Na configuração de execução de `JessbijusApiApplication`, abra **Edit Configurations → Environment variables** e adicione `JESSBIJUS_ADMIN_USERNAME` e `JESSBIJUS_ADMIN_PASSWORD_HASH`. Reinicie a API. Não coloque a senha ou o hash no repositório. Em produção, use HTTPS e guarde o hash em um gerenciador de segredos.

O painel Angular abre `/admin/login`. As credenciais ficam apenas na memória da aba e são enviadas somente nas requisições administrativas. Ao atualizar a página ou clicar em **Sair**, é necessário entrar novamente. O token CSRF é obtido em `GET /api/csrf`; o Angular o envia no cabeçalho `X-XSRF-TOKEN` nas alterações administrativas.
