# ☁️ MiniCloud – Simulador de Provedor de Nuvem

Projeto desenvolvido como trabalho final da disciplina **Programação Orientada a Objetos (POO)**, com o objetivo de aplicar os conceitos de **encapsulamento, herança, polimorfismo, abstração** e **persistência de dados** por meio do **PostgreSQL**.

---

## 📘 Sobre o projeto

A **MiniCloud** simula um provedor de serviços em nuvem semelhante à AWS, permitindo que o usuário:

- Crie uma conta e selecione um plano de serviço (Free, Standard, Pro);  
- Crie e gerencie recursos de nuvem:
  - **Instâncias de Computação**
  - **Bancos de Dados Gerenciados**
  - **Buckets de Armazenamento**
- Calcule o custo mensal de uso dos recursos criados.  

O sistema possui interface gráfica (Swing/JavaFX), persistência de dados via **JDBC + PostgreSQL** e uma arquitetura modular em camadas (Domínio, DAO e GUI).

---

## ⚙️ Requisitos

Para rodar o projeto localmente, é necessário ter instalado:

| Componente | Versão recomendada |
|-------------|--------------------|
| Java (JDK)  | 17 ou superior |
| PostgreSQL  | 14 ou superior |
| IDE         | IntelliJ, Eclipse ou NetBeans |

---

```plaintext

## 🧩 Estrutura do Projeto
MiniCloud/
├── src/
│ ├── main/
│ │ ├── java/
│ │ │ ├── br/com/minicloud/
│ │ │ │ ├── dominio/ # Classes de domínio (UsuarioCloud, PlanoCloud, etc.)
│ │ │ │ ├── dao/ # Classes DAO com JDBC e PostgreSQL
│ │ │ │ ├── excecoes/ # Exceções personalizadas
│ │ │ │ └── ui/ # Interface gráfica (Swing/JavaFX)
│ │ └── resources/
│ │ └── config.properties # Configuração do banco (não subir pro Git)
│ └── test/
├── database/
│ ├── schema.sql # Script de criação das tabelas
│ └── sample_data.sql # Dados iniciais (planos de serviço)
├── .gitignore
└── README.md
```

---

## 🧠 Passo a Passo – Como Rodar Localmente

### 1️⃣ Clonar o repositório

bash

git clone https://github.com/seu-usuario/minicloud-poo.git

cd minicloud-poo

###2️⃣ Configuração do Banco de Dados PostgreSQL (MiniCloud)


```
Baixei e configure na IDE do mesmo modo que é configurado o JDK do java o driver de JDBC do postgres: https://jdbc.postgresql.org/download/

Apos o Download va em File > Project Structure > Modules > Dependecies > Clicar no + e Adicionar o Driver e da Apply


Execute os comandos abaixo em um terminal SQL do PostgreSQL (psql) usando um usuário com permissão de superusuário, como postgres.

Esses comandos criam o banco, o usuário, e concedem todas as permissões necessárias para o sistema MiniCloud funcionar corretamente.
Inclui permissões para SCHEMA, TABLES e SEQUENCES — que são essenciais no PostgreSQL e não estão cobertas apenas por GRANT ALL PRIVILEGES ON DATABASE.

-- 1) Criar o banco de dados
CREATE DATABASE minicloud;

-- 2) Criar usuário para a aplicação
CREATE USER minicloud_user WITH PASSWORD 'minicloud_senha';

-- 3) Dar permissão para o usuário conectar no banco
GRANT ALL PRIVILEGES ON DATABASE minicloud TO minicloud_user;

-- 4) Conectar ao banco
\c minicloud;

-- 5) Garantir acesso ao schema public
GRANT USAGE ON SCHEMA public TO minicloud_user;

-- 6) Permissão total para manipular tabelas já existentes (SELECT/INSERT/UPDATE/DELETE)
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO minicloud_user;

-- 7) Permissão nas sequências (necessário por causa do SERIAL / IDENTITY)
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO minicloud_user;

-- 8) Garantir que futuras tabelas criadas também tenham permissões automaticamente
ALTER DEFAULT PRIVILEGES IN SCHEMA public
GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO minicloud_user;

ALTER DEFAULT PRIVILEGES IN SCHEMA public
GRANT ALL ON SEQUENCES TO minicloud_user;



\i database/schema.sql

Para popular a tabela de planos, rode:

\i database/sample_data.sql

```

3️⃣ Criar o arquivo de configuração

Crie o arquivo src/main/resources/config.properties com suas credenciais locais:
```
db.url=jdbc:postgresql://localhost:5432/minicloud

db.user=minicloud_user

db.password=minicloud_senha


Importante: não suba este arquivo para o GitHub.
Adicione ele ao .gitignore:

src/main/resources/config.properties
```
4️⃣ Executar o projeto

Abra o projeto na sua IDE preferida (ou terminal) e execute a classe principal:

java -cp target/minicloud.jar br.com.minicloud.Main


Ou simplesmente clique em Run dentro da IDE.


🧱 Scripts SQL
database/schema.sql

Contém todas as tabelas necessárias (planos, usuarios, recursos, instancias_computacao, bancos_dados_gerenciados, buckets_storage).

database/sample_data.sql

Exemplo de dados iniciais:

INSERT INTO planos (nome, limite_credito, limite_recursos) VALUES
('FREE',     50.00,  3),
('STANDARD', 200.00, 10),
('PRO',     1000.00, 50);

💾 Conexão com o banco

A conexão é gerenciada pela classe ConexaoBD, que lê o arquivo config.properties e inicializa o driver JDBC:

Connection conexao = DriverManager.getConnection(url, user, password);


Cada classe DAO (por exemplo, UsuarioDAO, PlanoDAO, RecursoDAO) utiliza essa conexão para executar comandos SQL (INSERT, SELECT, UPDATE, DELETE).

🧱 Boas práticas do repositório

Commits claros: use mensagens como feat: criar classe UsuarioDAO ou fix: ajustar cálculo de custo.

Branch por integrante: crie branches como mateus-dev e joao-dev para facilitar merge.

Tarefas no Trello:

“Backlog” → tarefas a fazer

“Em andamento” → em execução

“Concluído” → finalizado e commitado

🧩 Tecnologias utilizadas

Java 17

PostgreSQL

JDBC

Swing / JavaFX

Git / Trello / GitHub











