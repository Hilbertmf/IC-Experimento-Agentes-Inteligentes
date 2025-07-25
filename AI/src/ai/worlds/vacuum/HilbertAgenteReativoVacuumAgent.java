package ai.worlds.vacuum;

import java.util.Vector;
import java.util.Random;

public class HilbertAgenteReativoVacuumAgent extends VacuumAgent {

    private Random random = new Random();

    // Adicionamos contadores para as novas metodologias de score
    private int cleanedSquaresCount;
    private int moveCount;

    public HilbertAgenteReativoVacuumAgent() {
        cleanedSquaresCount = 0;
        moveCount = 0;
    }

    @Override
    public void determineAction() {
        Vector p = (Vector) percept;

        // Prioridade 1: Aspirar se houver sujeira no quadrado atual
        if (p.elementAt(1) != null && p.elementAt(1).equals("dirt")) {
            action = "suck";
        }
        // Prioridade 2: Reagir se o agente bateu em uma parede/obstáculo
        else if (p.elementAt(0) != null && p.elementAt(0).equals("bump")) {
            // Se bateu, vire aleatoriamente para a direita ou esquerda
            if (random.nextBoolean()) {
                action = "turn right";
            } else {
                action = "turn left";
            }
            moveCount++;
        }
        // Prioridade 3: Comportamento padrão de exploração (se não há sujeira nem batida)
        // Ele vai sempre tentar ir para frente ou girar para explorar.
        else {
            // Mais chance de ir para frente para cobrir mais terreno,
            // mas com chances de girar para não ficar preso em corredores.
            int choice = random.nextInt(4); // 0, 1, 2 ou 3
            switch (choice) {
                case 0:
                case 1:
                case 2: action = "forward"; break; // Maior chance de ir para frente
                case 3: action = "turn right"; break; // Chance de virar (pode ser left também)
            }
            moveCount++;
        }

    }

    public int getMoveCount() {
        return moveCount;
    }
}