# IC-Experimento-Agentes-Inteligentes

---

## 📄 Descrição

Este projeto é um simulador de ambiente baseado em grid, desenvolvido em Java, que demonstra o comportamento de agentes inteligentes em diferentes mundos. Atualmente, foca na simulação de ambientes como o **Mundo do Aspirador de Pó** (Vacuum World) e o **Mundo do Wumpus** (Wumpus World), permitindo a visualização da interação dos agentes com o ambiente e seus objetos.

Ele foi construído para ilustrar conceitos de Inteligência Artificial, como percepção, ação e tomada de decisão de agentes baseados em conhecimento.

---

## ✨ Recursos Principais

* **Ambiente de Grid 2D:** Representação visual clara do ambiente e dos agentes.
* **Simulação de Agentes:** Suporte para diferentes tipos de agentes (e.g., Agente Aspirador, Agente Wumpus).
* **Interatividade:** Controles para "Passo a Passo" (Step) e "Execução Contínua" (Run) da simulação.
* **Visualização de Percepções e Ações:** Exibição em tempo real das percepções do agente e das ações executadas.
* **Mapeamento Interno do Agente:** Para o agente Wumpus, exibe o mapa de crenças (possíveis Wumpus e Poços). Para o agente Aspirador, exibe as células já visitadas.
* **Score e Passos:** Acompanhamento do desempenho do agente e do número de passos da simulação.

---

## 🚀 Como Usar e Executar

Para rodar este projeto, siga os passos abaixo:

### Pré-requisitos

Certifique-se de ter o **Java Development Kit (JDK) 8 ou superior** instalado em sua máquina.

### Instalação e Execução

1.  **Clone o Repositório:**

2.  **Compile o Código:**
    Navegue até a raiz do projeto (onde você encontra a pasta `ai/`). No terminal, execute:
    ```bash
    javac ai/worlds/**/*.java
    ```
    *Isso compilará todas as classes necessárias, incluindo as dos subpacotes `vacuum` e `wumpus`.*

3.  **Execute a Simulação:**
    Após a compilação, você pode iniciar a simulação. Geralmente, há uma classe `Main` ou similar que inicializa o ambiente. Um exemplo de execução pode ser:
    ```bash
    java ai.worlds.vacuum.VacuumWorld
    ```
    *Consulte a documentação Javadoc ou o código-fonte para identificar a classe principal a ser executada para cada mundo.*

4.  **Interação com a GUI:**
    Uma janela de simulação será aberta, permitindo que você controle a execução (passo a passo, contínua) e observe o comportamento do agente.

---

## 🛠️ Tecnologias Utilizadas

* **Java**
* **Swing/AWT** (para a interface gráfica)

---

## 📚 Documentação

A documentação completa do projeto, gerada via Javadoc, está disponível na pasta `docs/`. Para visualizá-la offline, descompacte a pasta `docs.zip` (se fornecida separadamente) e abra o arquivo `index.html` em seu navegador.

---

## 📝 Relatório e Análise

Para uma análise detalhada do projeto, incluindo os resultados da simulação, tabelas de desempenho e gráficos, consulte o arquivo `relatorio_final.pdf` e `redacao_do_projeto.pdf` (fornecidos separadamente).

---
