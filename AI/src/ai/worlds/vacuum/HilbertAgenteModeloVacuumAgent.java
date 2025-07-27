package ai.worlds.vacuum;

import java.util.*;
import ai.worlds.Location;

/**
 * Representa um agente aspirador reativo baseado em modelo para o ambiente VacuumWorld.
 * Este agente constrói e mantém um mapa interno do ambiente (modelo) para tomar
 * decisões mais informadas e eficientes, planejando caminhos para locais sujos
 * ou desconhecidos.
 *
 * <p>O agente utiliza algoritmos de busca (BFS) para encontrar os melhores caminhos
 * e gerenciar filas de locais sujos e a serem explorados.</p>
 *
 * @author Hilbert França
 * @version 1.0
 * @since 2023-10-27
 */
public class HilbertAgenteModeloVacuumAgent extends VacuumAgent {

    /**
     * Mapa interno que representa o conhecimento do agente sobre o ambiente.
     * As chaves são objetos {@link ai.worlds.Location} e os valores são Strings
     * que indicam o status da célula:
     * <ul>
     * <li>"C": Limpa</li>
     * <li>"D": Suja</li>
     * <li>"W": Parede/Obstáculo</li>
     * <li>"U": Desconhecida</li>
     * </ul>
     */
    private Map<Location, String> knownMap;

    /**
     * A localização atual estimada do agente no grid do VacuumWorld,
     * de acordo com seu modelo interno.
     */
    private Location currentLocation;

    /**
     * A orientação atual do agente, que pode ser "N" (Norte), "E" (Leste),
     * "S" (Sul) ou "W" (Oeste).
     */
    private String currentOrientation;

    /**
     * Fila (Queue) de locais que o agente sabe que estão sujos e precisam ser aspirados.
     * A ordem de inserção é geralmente a de descoberta.
     */
    private Queue<Location> dirtyLocationsQueue;

    /**
     * Gerador de números aleatórios, usado para decisões em situações ambíguas
     * (e.g., virar aleatoriamente se um caminho não for encontrado).
     */
    private Random random;

    /**
     * Contador do número total de movimentos (incluindo avanços e giros)
     * realizados pelo agente. Usado para a métrica de desempenho.
     */
    private int moveCount;

    /**
     * Construtor para o HilbertAgenteModeloVacuumAgent.
     * Inicializa o mapa interno, a localização e orientação iniciais do agente,
     * as filas de locais sujos e o contador de movimentos.
     * Assume a localização inicial (0,0) como limpa.
     */
    public HilbertAgenteModeloVacuumAgent() {
        knownMap = new HashMap<>();
        currentLocation = new Location(0, 0); // Posição inicial arbitrária, ajustada conforme o ambiente
        currentOrientation = "N";
        knownMap.put(currentLocation, "C"); // Marca a posição inicial como limpa no modelo
        dirtyLocationsQueue = new LinkedList<>();
        random = new Random();
        moveCount = 0;
    }

    /**
     * Determina a próxima ação a ser executada pelo agente.
     * Esta é a função central do agente, que coordena a atualização do modelo
     * e a tomada de decisão baseada nesse modelo.
     * As ações são priorizadas: aspirar, ir para sujeira conhecida, explorar desconhecido, ou desligar.
     *
     * @Override indica que este método sobrescreve o método determineAction() da classe pai {@link ai.worlds.Agent}.
     */
    @Override
    public void determineAction() {
        // --- Atualização do modelo com base no percepto e última ação ---
        updateModelBasedOnPerceptAndLastAction();

        // --- Tomada de decisão baseada no modelo interno ---
        // Prioridade 1: Limpar se a posição atual está suja de acordo com o modelo
        if (knownMap.getOrDefault(currentLocation, "U").equals("D")) {
            action = "suck";
            return;
        }

        // Prioridade 2: Buscar sujeira conhecida para aspirar
        Location target = null;
        while (!dirtyLocationsQueue.isEmpty()) {
            Location peek = dirtyLocationsQueue.peek();
            // Verifica se o local ainda está sujo no modelo; se não, remove da fila
            if (!knownMap.getOrDefault(peek, "U").equals("D")) {
                dirtyLocationsQueue.poll();
            } else {
                target = peek; // Encontrou um local sujo válido como alvo
                break;
            }
        }

        // Prioridade 3: Se não há sujeira conhecida, explorar células desconhecidas
        if (target == null) {
            target = findNearestUnknown(); // Encontra a célula 'U' (desconhecida) mais próxima
        }

        // Prioridade 4: Desligar se não houver mais nada para fazer (sem alvos de sujeira ou exploração)
        if (target == null) {
            action = "shut-off";
            return;
        }

        // Prioridade 5: Planejar caminho para o alvo encontrado
        // Calcula o caminho mais curto até o alvo usando Busca em Largura (BFS)
        List<Location> path = bfsPath(currentLocation, target);

        // Se um caminho válido foi encontrado e há um próximo passo
        if (path != null && path.size() > 1) {
            Location nextStep = path.get(1); // O próximo passo no caminho
            action = determineTurnOrForward(nextStep); // Decide virar ou avançar
        } else {
            // Se nenhum caminho foi encontrado para o alvo (ex: bloqueado),
            // o agente vira aleatoriamente para tentar se desobstruir ou encontrar um novo caminho.
            if (random.nextBoolean()) {
                action = "turn left";
            } else {
                action = "turn right";
            }
        }

        // Incrementa o contador de movimentos se a ação for de movimento ou giro
        if (action.equals("forward") || action.equals("turn right") || action.equals("turn left")) {
            moveCount++;
        }
    }

    /**
     * Atualiza o modelo interno do agente ({@link #knownMap}, {@link #currentLocation},
     * {@link #dirtyLocationsQueue}) com base na percepção atual do ambiente
     * e na última ação executada.
     *
     * <p>Esta é a fase de percepção e aprendizado do agente, onde ele integra
     * novas informações ao seu conhecimento do mundo.</p>
     */
    private void updateModelBasedOnPerceptAndLastAction() {
        Vector currentPercept = (Vector) percept;
        String previousAction = this.action;

        // Verifica a percepção de sujeira na célula atual
        if (currentPercept.elementAt(1) != null && currentPercept.elementAt(1).equals("dirt")) {
            knownMap.put(currentLocation, "D"); // Marca a célula atual como suja
            if (!dirtyLocationsQueue.contains(currentLocation)) {
                dirtyLocationsQueue.add(currentLocation); // Adiciona à fila de sujeira se ainda não estiver lá
            }
        } else {
            // Se não há sujeira percebida e a ação anterior foi "suck", a célula está limpa
            if (previousAction != null && previousAction.equals("suck")) {
                knownMap.put(currentLocation, "C"); // Marca a célula como limpa
                dirtyLocationsQueue.remove(currentLocation); // Remove da fila de sujeira
            }
            // Se não aspirou e não é sujeira, e não era 'D', assume que está limpo após ter passado
            else if (!knownMap.getOrDefault(currentLocation, "U").equals("D")) {
                knownMap.put(currentLocation, "C");
            }
        }

        // Atualiza a posição e o modelo com base na ação anterior
        if (previousAction != null) {
            if (previousAction.equals("forward")) {
                // Se houve "bump", a célula para onde o agente tentou ir é uma parede
                if (currentPercept.elementAt(0) != null && currentPercept.elementAt(0).equals("bump")) {
                    Location wallLocation = getAdjacentLocation(currentLocation, currentOrientation);
                    if (wallLocation != null) {
                        knownMap.put(wallLocation, "W"); // Marca a parede no mapa
                    }
                } else {
                    // Se não houve "bump", o avanço foi bem-sucedido, atualiza a localização
                    Location newLocation = getAdjacentLocation(currentLocation, currentOrientation);
                    if (newLocation != null) {
                        currentLocation = newLocation; // Atualiza a localização interna do agente
                        // Se a nova localização ainda não é conhecida, marca como "U" (desconhecida)
                        if (!knownMap.containsKey(currentLocation)) {
                            knownMap.put(currentLocation, "U");
                            // (Opcional: adicionar à fila de exploração aqui ou em outro método de descoberta)
                        }
                    }
                }
            } else if (previousAction.equals("turn right")) {
                turn("right"); // Atualiza a orientação interna do agente
            } else if (previousAction.equals("turn left")) {
                turn("left"); // Atualiza a orientação interna do agente
            }
        }
    }

    /**
     * Calcula a localização adjacente a uma dada célula, com base em uma orientação específica.
     *
     * @param loc A localização de referência.
     * @param orientation A orientação ("N", "E", "S", "W") para determinar a célula adjacente.
     * @return A {@link ai.worlds.Location} da célula adjacente, ou {@code null} se a orientação for inválida.
     */
    private Location getAdjacentLocation(Location loc, String orientation) {
        int x = loc.x;
        int y = loc.y;
        switch (orientation) {
            case "N": return new Location(x, y - 1);
            case "E": return new Location(x + 1, y);
            case "S": return new Location(x, y + 1);
            case "W": return new Location(x - 1, y);
            default: return null;
        }
    }

    /**
     * Gira o agente em uma direção especificada (direita ou esquerda),
     * atualizando sua {@link #currentOrientation}.
     *
     * @param direction A direção para girar ("right" para direita, "left" para esquerda).
     */
    private void turn(String direction) {
        String[] orientations = {"N", "E", "S", "W"};
        int currentIndex = -1;
        // Encontra o índice da orientação atual no array
        for (int i = 0; i < orientations.length; i++) {
            if (orientations[i].equals(currentOrientation)) {
                currentIndex = i;
                break;
            }
        }
        // Atualiza a orientação com base na direção do giro
        if (currentIndex != -1) {
            if (direction.equals("right")) {
                currentOrientation = orientations[(currentIndex + 1) % 4];
            } else if (direction.equals("left")) {
                currentOrientation = orientations[(currentIndex - 1 + 4) % 4];
            }
        }
    }

    /**
     * Calcula a orientação resultante de um giro de 90 graus para a direita a partir de uma dada orientação.
     *
     * @param ori A orientação de partida ("N", "E", "S", "W").
     * @return A nova orientação após um giro para a direita.
     */
    private String turnRight(String ori) {
        String[] orientations = {"N", "E", "S", "W"};
        int i = Arrays.asList(orientations).indexOf(ori);
        return orientations[(i + 1) % 4];
    }

    /**
     * Calcula a orientação resultante de um giro de 90 graus para a esquerda a partir de uma dada orientação.
     *
     * @param ori A orientação de partida ("N", "E", "S", "W").
     * @return A nova orientação após um giro para a esquerda.
     */
    private String turnLeft(String ori) {
        String[] orientations = {"N", "E", "S", "W"};
        int i = Arrays.asList(orientations).indexOf(ori);
        return orientations[(i - 1 + 4) % 4];
    }

    /**
     * Determina a ação necessária (virar ou avançar) para mover o agente da
     * {@link #currentLocation} para uma célula vizinha específica ({@code next}).
     *
     * @param next A {@link ai.worlds.Location} da próxima célula no caminho.
     * @return A String da ação ("forward", "turn right", "turn left") necessária para alcançar {@code next}.
     */
    private String determineTurnOrForward(Location next) {
        int dx = next.x - currentLocation.x;
        int dy = next.y - currentLocation.y;

        String neededDir = null; // Direção para onde o agente precisa estar virado
        if (dx == 1) neededDir = "E";
        else if (dx == -1) neededDir = "W";
        else if (dy == 1) neededDir = "S";
        else if (dy == -1) neededDir = "N";

        // Compara a orientação atual com a direção necessária e decide a ação
        if (currentOrientation.equals(neededDir)) {
            return "forward";
        } else if (turnLeft(currentOrientation).equals(neededDir)) {
            return "turn left";
        } else if (turnRight(currentOrientation).equals(neededDir)) {
            return "turn right";
        } else {
            return "turn right"; // Fallback para virar à direita se algo der errado (não deve ocorrer se 'neededDir' for sempre uma das 4 direções)
        }
    }

    /**
     * Realiza uma Busca em Largura (BFS - Breadth-First Search) no {@link #knownMap}
     * para encontrar o caminho mais curto de uma localização de início ({@code start})
     * até um objetivo ({@code goal}).
     *
     * <p>O BFS considera células desconhecidas ('U') e limpas ('C') como transitáveis,
     * mas evita paredes ('W').</p>
     *
     * @param start A {@link ai.worlds.Location} de onde a busca começa.
     * @param goal A {@link ai.worlds.Location} que é o objetivo da busca.
     * @return Uma {@link java.util.List} de {@link ai.worlds.Location} representando o caminho
     * do início ao objetivo (incluindo ambos), ou {@code null} se nenhum caminho for encontrado.
     */
    private List<Location> bfsPath(Location start, Location goal) {
        Queue<Location> queue = new LinkedList<>();
        Map<Location, Location> cameFrom = new HashMap<>(); // Para reconstruir o caminho
        queue.add(start);
        cameFrom.put(start, null); // O ponto de partida não tem "pai"

        while (!queue.isEmpty()) {
            Location current = queue.poll();
            if (current.equals(goal)) break; // Chegou ao objetivo

            // Explora os vizinhos do local atual
            for (String dir : List.of("N", "E", "S", "W")) {
                Location neighbor = getAdjacentLocation(current, dir);
                if (neighbor == null) continue; // Ignora vizinhos nulos (fora dos limites, se aplicável)
                String cellStatus = knownMap.getOrDefault(neighbor, "U"); // Obtém status do vizinho
                // Se não é parede e ainda não foi visitado, adiciona à fila e registra o caminho
                if (!cellStatus.equals("W") && !cameFrom.containsKey(neighbor)) {
                    queue.add(neighbor);
                    cameFrom.put(neighbor, current);
                }
            }
        }

        if (!cameFrom.containsKey(goal)) return null; // Se o objetivo não foi alcançado

        // Reconstrói o caminho do objetivo ao início
        List<Location> path = new LinkedList<>();
        for (Location at = goal; at != null; at = cameFrom.get(at)) {
            path.add(0, at); // Adiciona no início da lista para manter a ordem correta
        }
        return path;
    }

    /**
     * Realiza uma Busca em Largura (BFS) a partir da {@link #currentLocation} do agente
     * para encontrar a célula "U" (desconhecida) mais próxima no {@link #knownMap}.
     *
     * <p>Esta função é usada para guiar o agente na exploração de áreas não mapeadas.</p>
     *
     * @return A {@link ai.worlds.Location} da célula desconhecida mais próxima,
     * ou {@code null} se todas as células acessíveis forem conhecidas.
     */
    private Location findNearestUnknown() {
        Queue<Location> queue = new LinkedList<>();
        Set<Location> visited = new HashSet<>(); // Para evitar ciclos e revisitas desnecessárias
        queue.add(currentLocation);
        visited.add(currentLocation);

        while (!queue.isEmpty()) {
            Location current = queue.poll();
            // Explora os vizinhos
            for (String dir : List.of("N", "E", "S", "W")) {
                Location neighbor = getAdjacentLocation(current, dir);
                if (neighbor == null) continue;
                if (visited.contains(neighbor)) continue; // Já visitado nesta busca

                String status = knownMap.getOrDefault(neighbor, "U"); // Obtém status do vizinho
                if (status.equals("U")) {
                    return neighbor; // Encontrou a primeira célula desconhecida
                }
                // Se não é parede, adiciona à fila para explorar seus vizinhos
                if (!status.equals("W")) {
                    queue.add(neighbor);
                    visited.add(neighbor);
                }
            }
        }
        return null; // Não encontrou nenhuma célula desconhecida acessível
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