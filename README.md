Projeto Final de Programação Orientada a Objetos – MiniCloud
1. Ideia Geral do Projeto
O projeto MiniCloud tem como objetivo desenvolver um simulador de provedor de serviços em nuvem, inspirado em plataformas reais como Amazon Web Services (AWS), Google Cloud e Microsoft Azure, aplicando os princípios fundamentais da Programação Orientada a Objetos (POO) em um sistema modular, escalável e persistente.
O sistema permite que o usuário crie uma conta, selecione um plano de serviço e gerencie seus próprios recursos de nuvem simulados, como instâncias de computação, bancos de dados gerenciados e buckets de armazenamento.
 Cada recurso possui configurações e custos específicos, e o programa calcula o valor mensal de uso, simulando uma cobrança real de provedores de nuvem.

2. Objetivos do Projeto
Aplicar os conceitos centrais da Programação Orientada a Objetos: abstração, encapsulamento, herança e polimorfismo.


Construir um sistema composto por múltiplas classes inter-relacionadas e organizadas em camadas.


Implementar tratamento de exceções personalizadas e persistência de dados com PostgreSQL.


Desenvolver uma interface gráfica (GUI) que permita interação intuitiva com as principais funcionalidades do sistema.


Demonstrar domínio sobre modelagem de sistemas orientados a objetos e integração com banco de dados relacional.



3. Funcionamento da MiniCloud
O sistema MiniCloud simula o funcionamento básico de uma plataforma de computação em nuvem. As principais etapas são:
Cadastro de Usuários e Planos:
 O usuário cria uma conta e escolhe um plano (por exemplo: Free, Standard ou Pro), que define o limite de crédito mensal e o número máximo de recursos que podem ser criados.


Criação e Gerenciamento de Recursos:
 O usuário pode criar diferentes tipos de recursos de nuvem:


Instância de Computação: simula uma máquina virtual (CPU, memória, custo/hora).


Banco de Dados Gerenciado: simula um serviço de banco de dados em nuvem, com armazenamento e replicação.


Bucket de Armazenamento: simula um serviço de armazenamento de arquivos, com custo baseado em espaço e requisições.


Cálculo de Custos e Persistência:
 O sistema calcula automaticamente o custo mensal total de acordo com o tipo e o tempo de uso de cada recurso.
 Todas as informações de usuários, planos e recursos são armazenadas e recuperadas de um banco de dados PostgreSQL.



4. Relação com os Requisitos da Disciplina
4.1 Fundamentos de POO (70%)
Encapsulamento:
 Todos os atributos das classes serão privados e acessados por meio de métodos get e set, garantindo segurança e consistência dos dados.


Classes:
 O sistema contará com diversas classes, como UsuarioCloud, PlanoCloud, RecursoCloud (classe abstrata), InstanciaComputacao, BancoDadosGerenciado, BucketStorage, GerenciadorCloud e as classes de acesso a dados (DAO).


Classe abstrata e método abstrato:
 A classe RecursoCloud representará um modelo genérico de recurso de nuvem e conterá o método abstrato calcularCustoMensal(), que será implementado de forma específica por cada tipo de recurso.


Herança e sobrescrita:
 As classes InstanciaComputacao, BancoDadosGerenciado e BucketStorage herdarão de RecursoCloud e sobrescreverão o método abstrato, aplicando regras próprias de cálculo de custo.


Polimorfismo:
 O sistema utilizará uma coleção (ArrayList<RecursoCloud>) para armazenar todos os recursos criados, permitindo chamadas polimórficas de métodos, como calcularCustoMensal(), sem conhecer o tipo exato de recurso.


Associação entre classes:
 Cada UsuarioCloud estará associado a um PlanoCloud e poderá possuir uma lista de recursos (ArrayList<RecursoCloud>), representando a relação entre usuário, plano e recursos de nuvem.


Coleções de objetos:
 As listas de usuários, planos e recursos serão implementadas utilizando coleções Java (ArrayList), garantindo flexibilidade no gerenciamento dinâmico dos dados.



4.2 Recursos Complementares (20%)
Exceções personalizadas:
 Será criada uma classe que estende Exception, como LimiteRecursosException, utilizada para sinalizar quando o usuário ultrapassar o limite de recursos permitido por seu plano ou em outras condições inválidas.


Interface gráfica (GUI):
 O sistema contará com uma interface desenvolvida em Java Swing ou JavaFX, composta por telas que permitirão ao usuário realizar:


Cadastro de contas e planos;


Criação e gerenciamento de recursos;


Consulta de custos mensais e informações gerais da nuvem simulada.


Persistência em banco de dados (PostgreSQL):
 Em vez de arquivos CSV ou binários, o projeto utilizará o banco de dados PostgreSQL como mecanismo oficial de persistência.
 Todos os objetos do sistema — usuários, planos e recursos — serão armazenados e recuperados por meio de uma camada de acesso a dados (DAO), que executará as operações de CRUD (Create, Read, Update, Delete) através de instruções SQL.
 Isso garante integridade, consistência e persistência das informações entre diferentes execuções da aplicação.


Substituição autorizada do requisito de CSV/TXT:
 Conforme orientação da professora, o requisito de leitura de dados em CSV/TXT foi substituído pelo uso integral de banco de dados PostgreSQL, em virtude do domínio da equipe nessa tecnologia.
 Assim, todas as leituras e gravações de dados são realizadas diretamente no banco, de forma estruturada e relacional, refletindo práticas reais de desenvolvimento profissional.



4.3 Recursos Organizacionais (10%)
Repositório GitHub:
 O projeto será hospedado em um repositório do GitHub, contendo todos os commits devidamente identificados, com mensagens claras e registro de contribuição de cada integrante do grupo.


Kanban (Trello):
 A gestão das tarefas será feita em um quadro no Trello, dividido em colunas como “Backlog”, “Em andamento” e “Concluído”, com cada membro responsável por atividades específicas.


Fluxograma:
 Será elaborado um fluxograma representando o fluxo principal do sistema — desde o login e criação de conta, passando pela escolha de plano, até a criação de recursos e cálculo de custo.


Diagrama de classes:
 Um diagrama simples representará as principais relações de herança, associação e composição entre as classes UsuarioCloud, PlanoCloud, RecursoCloud e suas subclasses.



5. Arquitetura de Persistência com PostgreSQL
A MiniCloud adotará uma arquitetura em camadas, garantindo separação de responsabilidades e organização do código:
Camada de Domínio:
 Contém as classes que modelam as entidades centrais do sistema, como UsuarioCloud, PlanoCloud e RecursoCloud. Essa camada define as regras de negócio e o comportamento dos objetos.


Camada de Acesso a Dados (DAO):
 Responsável pela comunicação direta com o banco de dados PostgreSQL. Cada entidade possuirá sua própria classe DAO, contendo os métodos necessários para inserir, consultar, atualizar e excluir registros.


Camada de Interface (GUI):
 Composta pelas telas gráficas que permitem interação com o usuário, integrando-se à camada de domínio e à camada DAO para exibir e manipular os dados persistidos.


Essa arquitetura torna o sistema modular, facilita futuras expansões (como novos tipos de recursos de nuvem) e reflete boas práticas de engenharia de software e design orientado a objetos.

6. Conclusão do Escopo
O projeto MiniCloud representa uma aplicação prática e moderna dos conceitos de Programação Orientada a Objetos, integrando teoria e prática em um contexto inspirado em provedores de computação em nuvem.
 A substituição dos arquivos CSV/TXT pelo banco de dados PostgreSQL enriquece o projeto, aproximando-o de ambientes profissionais e reforçando o domínio de tecnologias amplamente utilizadas em Engenharia de Dados e Desenvolvimento de Software.
Com uma estrutura organizada em camadas, uso de herança, polimorfismo, exceções personalizadas, interface gráfica e persistência real em banco de dados, o sistema cumpre plenamente os requisitos da disciplina, demonstrando domínio técnico e aplicabilidade prática dos conceitos de POO.
