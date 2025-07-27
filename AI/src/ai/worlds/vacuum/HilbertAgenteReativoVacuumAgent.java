package ai.worlds.vacuum;

import java.util.Vector;
import java.util.Random;

/**
 * Representa um agente aspirador reativo simples no ambiente VacuumWorld.
 * Este agente opera com base em percepções diretas e um conjunto de regras
 * condição-ação fixas, sem manter um modelo interno do ambiente ou memória
 * de estados passados.
 *
 * @author Hilbert França
 * @version 1.0
 * @since 2023-10-27
 */
public class HilbertAgenteReativoVacuumAgent extends VacuumAgent {

    /**
     * Gerador de números aleatórios, usado para decisões de giro quando
     * o agente encontra um obstáculo ou está explorando.
     */
    private Random random = new Random();

    /**
     * Contador de quadrados limpos. Embora não usado diretamente na lógica de ação,
     * pode ser mantido para compatibilidade com a coleta de métricas externas.
     */
    private int cleanedSquaresCount; // Esta variável não é usada no determineAction() fornecido, mas é mantida por estar no seu código original.

    /**
     * Contador do número total de movimentos (incluindo avanços e giros)
     * realizados pelo agente. Utilizado para o cálculo da métrica de desempenho.
     */
    private int moveCount;

    /**
     * Construtor para o HilbertAgenteReativoVacuumAgent.
     * Inicializa os contadores de limpeza (se aplicável) e movimentos.
     */
    public HilbertAgenteReativoVacuumAgent() {
        this.cleanedSquaresCount = 0; // Inicializado, mas não utilizado na lógica de ação
        this.moveCount = 0;
    }

    /**
     * Determina a próxima ação do agente com base em sua percepção atual.
     * O agente segue uma hierarquia de prioridades para decidir sua ação:
     * aspirar sujeira, reagir a colisões ou explorar o ambiente.
     *
     * @Override indica que este método sobrescreve o método determineAction() da classe pai {@link ai.worlds.Agent}.
     */
    @Override
    public void determineAction() {
        Vector p = (Vector) percept; // A percepção atual do ambiente

        // Prioridade 1: Aspirar se houver sujeira no quadrado atual (percepção de "dirt")
        if (p.elementAt(1) != null && p.elementAt(1).equals("dirt")) {
            action = "suck";
        }
        // Prioridade 2: Reagir se o agente bateu em uma parede/obstáculo (percepção de "bump")
        else if (p.elementAt(0) != null && p.elementAt(0).equals("bump")) {
            // Se bateu, vire aleatoriamente para a direita ou esquerda para desobstruir
            if (random.nextBoolean()) {
                action = "turn right";
            } else {
                action = "turn left";
            }
            moveCount++; // Incrementa o contador de movimentos pelo giro
        }
        // Prioridade 3: Comportamento padrão de exploração (se não há sujeira nem batida)
        // O agente tentará ir para frente na maioria das vezes, mas também girará para explorar diferentes direções.
        else {
            // Uma escolha aleatória com peso para priorizar o avanço:
            // 0, 1, 2 para "forward" (75% de chance)
            // 3 para "turn right" (25% de chance)
            int choice = random.nextInt(4);
            switch (choice) {
                case 0:
                case 1:
                case 2:
                    action = "forward";
                    break; // Maior chance de ir para frente para cobrir mais terreno
                case 3:
                    action = "turn right";
                    break; // Chance de virar para não ficar preso e explorar novas áreas
            }
            moveCount++; // Incrementa o contador de movimentos pelo avanço ou giro
        }
    }

    /**
     * Retorna a contagem total de movimentos (incluindo avanços e giros)
     * realizados pelo agente.
     *
     * @return O número total de movimentos.
     */
    public int getMoveCount() {
        return moveCount;
    }
}