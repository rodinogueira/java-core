# Java Core - Persistence Engine

Este repositório contém uma implementação robusta de persistência de dados utilizando **Java Standard Edition (Java SE)** e **SQLite**. 

## Destaques Técnicos
- **JDBC Puro:** Implementação sem dependência de ORMs, garantindo máxima performance e controle sobre as consultas SQL.
- **Design Patterns:** Utilização do padrão **Repository** para desacoplamento entre a lógica de negócio e a camada de dados.
- **Segurança de Dados:** Uso de `PreparedStatement` para prevenção de SQL Injection.
- **Arquitetura Limpa:** Separação clara entre a entidade de domínio (`Point`) e a lógica de controle.

## Como Executar
1. Certifique-se de ter o driver JDBC do SQLite no classpath.
2. Compile: `javac -cp ".:lib/sqlite-jdbc.jar" *.java`
3. Execute: `java -cp ".:lib/sqlite-jdbc.jar" Main`