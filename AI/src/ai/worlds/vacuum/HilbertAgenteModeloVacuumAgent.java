package ai.worlds.vacuum;

import java.util.*;
import ai.worlds.Location;

public class HilbertAgenteModeloVacuumAgent extends VacuumAgent {

    private Map<Location, String> knownMap; // "C":Clean, "D":Dirty, "W":Wall, "U":Unknown
    private Location currentLocation;
    private String currentOrientation; // "N", "E", "S", "W"
    private Queue<Location> dirtyLocationsQueue; // Locais sujos conhecidos
    private Queue<Location> explorationQueue; // Locais desconhecidos para explorar
    private Random random;
    private int moveCount; // Contador de movimentos para score

    public HilbertAgenteModeloVacuumAgent() {
//    	Modelo do Ambiente
    	knownMap = new HashMap<>();
//    	Estado interno
        currentLocation = new Location(0, 0);
        currentOrientation = "N";
        knownMap.put(currentLocation, "C");

//        Começa a explorar do local inicial
        dirtyLocationsQueue = new LinkedList<>();
        explorationQueue = new LinkedList<>();
        explorationQueue.add(currentLocation);

        random = new Random();
        moveCount = 0;
    }

//    processo de decisão
    @Override
    public void determineAction() {
//    	Atualização do Modelo
    	updateModelBasedOnPerceptAndLastAction();

        Vector currentPercept = (Vector) percept;

        // Prioridade 1: Aspirar se o local atual tem sujeira
        if (knownMap.getOrDefault(currentLocation, "U").equals("D")) {
            action = "suck";
            return;
        }

        Location targetLocation = null;

        // Prioridade 2: Ir para um local sujo conhecido
        while (!dirtyLocationsQueue.isEmpty()) {
            Location potentialDirty = dirtyLocationsQueue.peek();
            // Se o local na fila já foi limpo, remove e tenta o próximo
            if (knownMap.getOrDefault(potentialDirty, "U").equals("C")) {
                dirtyLocationsQueue.poll();
            } else {
                targetLocation = potentialDirty;
                break;
            }
        }

        // Prioridade 3: Se não há sujeira, explorar um local desconhecido
        if (targetLocation == null) {
            while (!explorationQueue.isEmpty()) {
                Location potentialUnknown = explorationQueue.peek();
                // Se o local já é conhecido e não é "U", remove e tenta o próximo
                if (knownMap.containsKey(potentialUnknown) && !knownMap.get(potentialUnknown).equals("U")) {
                    explorationQueue.poll();
                } else {
                    targetLocation = potentialUnknown;
                    break;
                }
            }
        }

        // Prioridade 4: Mover em direção ao alvo ou explorar aleatoriamente se não houver alvo válido
        if (targetLocation != null && !targetLocation.equals(currentLocation)) {
            action = getNextActionToTarget(targetLocation);
            // Conta apenas movimentos que alteram a posição ou orientação
            if (action.equals("forward") || action.equals("turn right") || action.equals("turn left")) {
                moveCount++;
            }
        } else { // Se não há alvo ou já no alvo (e não sujo), gira aleatoriamente para tentar descobrir algo
            action = random.nextBoolean() ? "turn right" : "turn left";
            moveCount++; // Conta o giro aleatório
        }

        // Condição para desligar (shut-off)
        // Se a percepção indica "home", e todo o mapa conhecido está limpo e explorado
        if (currentPercept.elementAt(2) != null && currentPercept.elementAt(2).equals("home")) {
            boolean allKnownClean = true;
            for (String status : knownMap.values()) {
                if (status.equals("D") || status.equals("U")) { // Ainda há sujeira ou locais desconhecidos
                    allKnownClean = false;
                    break;
                }
            }
            if (allKnownClean && dirtyLocationsQueue.isEmpty() && explorationQueue.isEmpty()) {
                action = "shut-off";
            }
        }
    }

    private void updateModelBasedOnPerceptAndLastAction() {
        Vector currentPercept = (Vector) percept;
        String previousAction = this.action;

        // Atualiza o status do local atual
        if (currentPercept.elementAt(1) != null && currentPercept.elementAt(1).equals("dirt")) {
            knownMap.put(currentLocation, "D");
            if (!dirtyLocationsQueue.contains(currentLocation)) { // Evita duplicatas
                dirtyLocationsQueue.add(currentLocation);
            }
        } else {
            if (previousAction != null && previousAction.equals("suck")) {
                knownMap.put(currentLocation, "C");
                dirtyLocationsQueue.remove(currentLocation); // Remove da fila de sujos após aspirar
            } else if (!knownMap.getOrDefault(currentLocation, "U").equals("D")) {
                 knownMap.put(currentLocation, "C"); // Se não tem sujeira e não era D, marca como C
            }
        }

        // Atualiza a posição e orientação com base na ação anterior e percepção
        if (previousAction != null) {
            if (previousAction.equals("forward")) {
                if (currentPercept.elementAt(0) != null && currentPercept.elementAt(0).equals("bump")) {
                    Location wallLocation = getAdjacentLocation(currentLocation, currentOrientation);
                    if (wallLocation != null) {
                        knownMap.put(wallLocation, "W"); // Marca parede
                    }
                } else { // Avanço bem-sucedido
                    currentLocation = getAdjacentLocation(currentLocation, currentOrientation);
                    // Adiciona novo local ao mapa e à fila de exploração se desconhecido
                    if (currentLocation != null && !knownMap.containsKey(currentLocation)) {
                        knownMap.put(currentLocation, "U");
                        explorationQueue.add(currentLocation);
                    }
                }
            } else if (previousAction.equals("turn right")) {
                turn("right");
            } else if (previousAction.equals("turn left")) {
                turn("left");
            }
        }
    }

    // Calcula localização adjacente com base na orientação
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

    // Atualiza a orientação do agente
    private void turn(String direction) {
        String[] orientations = {"N", "E", "S", "W"};
        int currentIndex = Arrays.asList(orientations).indexOf(currentOrientation);

        if (direction.equals("right")) {
            currentOrientation = orientations[(currentIndex + 1) % 4];
        } else if (direction.equals("left")) {
            currentOrientation = orientations[(currentIndex - 1 + 4) % 4];
        }
    }

    // Determina a ação para chegar ao alvo
    private String getNextActionToTarget(Location target) {
        int dx = target.x - currentLocation.x;
        int dy = target.y - currentLocation.y;

        // Prioriza movimentos ao longo do eixo X se houver diferença
        if (dx != 0) {
            if (dx > 0) { // Alvo à direita
                return currentOrientation.equals("E") ? "forward" : (currentOrientation.equals("N") ? "turn right" : "turn left");
            } else { // Alvo à esquerda
                return currentOrientation.equals("W") ? "forward" : (currentOrientation.equals("N") ? "turn left" : "turn right");
            }
        } else if (dy != 0) { // Prioriza movimentos ao longo do eixo Y se houver diferença
            if (dy < 0) { // Alvo acima
                return currentOrientation.equals("N") ? "forward" : (currentOrientation.equals("E") ? "turn left" : "turn right");
            } else { // Alvo abaixo
                return currentOrientation.equals("S") ? "forward" : (currentOrientation.equals("E") ? "turn right" : "turn left");
            }
        }
        return "suck"; // Deve sugar se já estiver no alvo sujo
    }

    public int getMoveCount() {
        return moveCount;
    }
}