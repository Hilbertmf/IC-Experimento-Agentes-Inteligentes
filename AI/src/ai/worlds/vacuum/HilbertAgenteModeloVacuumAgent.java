package ai.worlds.vacuum;

import java.util.*;
import ai.worlds.Location;

public class HilbertAgenteModeloVacuumAgent extends VacuumAgent {

    // --- Estado interno e memória do agente ---
    private Map<Location, String> knownMap;            // Mapa interno: "C"=clean, "D"=dirty, "W"=wall, "U"=unknown
    private Location currentLocation;                   // Posição atual do agente no grid
    private String currentOrientation;                  // Orientação atual: "N", "E", "S", "W"
    private Queue<Location> dirtyLocationsQueue;        // Filas de posições sujas conhecidas
    private Random random;
    private int moveCount;

    public HilbertAgenteModeloVacuumAgent() {
        knownMap = new HashMap<>();
        currentLocation = new Location(0, 0);
        currentOrientation = "N";
        knownMap.put(currentLocation, "C");
        dirtyLocationsQueue = new LinkedList<>();
        random = new Random();
        moveCount = 0;
    }

    @Override
    public void determineAction() {
        // --- Atualização do modelo com base no percepto e última ação ---
        updateModelBasedOnPerceptAndLastAction();

        // --- Tomada de decisão baseada no modelo interno ---
        // Limpar se a posição atual está suja
        if (knownMap.getOrDefault(currentLocation, "U").equals("D")) {
            action = "suck";
            return;
        }

        // Priorizar sujeira conhecida
        Location target = null;
        while (!dirtyLocationsQueue.isEmpty()) {
            Location peek = dirtyLocationsQueue.peek();
            if (!knownMap.getOrDefault(peek, "U").equals("D")) {
                dirtyLocationsQueue.poll();
            } else {
                target = peek;
                break;
            }
        }

        // Se não há sujeira, explorar células desconhecidas
        if (target == null) {
            target = findNearestUnknown();
        }

        // Desliga se não houver mais nada para fazer
        if (target == null) {
            action = "shut-off";
            return;
        }

        // Planeja caminho para o alvo
        List<Location> path = bfsPath(currentLocation, target);

        if (path != null && path.size() > 1) {
            Location nextStep = path.get(1);
            action = determineTurnOrForward(nextStep);
        } else {
            // Caso sem caminho, gira para tentar se desbloquear
            if (random.nextBoolean()) {
                action = "turn left";
            } else {
                action = "turn right";
            }
        }

        if (action.equals("forward") || action.equals("turn right") || action.equals("turn left")) {
            moveCount++;
        }
    }

    // Atualiza estado interno com base no percepto e última ação tomada
    private void updateModelBasedOnPerceptAndLastAction() {
        Vector currentPercept = (Vector) percept;
        String previousAction = this.action;

        if (currentPercept.elementAt(1) != null && currentPercept.elementAt(1).equals("dirt")) {
            knownMap.put(currentLocation, "D");
            if (!dirtyLocationsQueue.contains(currentLocation)) {
                dirtyLocationsQueue.add(currentLocation);
            }
        } else {
            if (previousAction != null && previousAction.equals("suck")) {
                knownMap.put(currentLocation, "C");
                dirtyLocationsQueue.remove(currentLocation);
            } else if (!knownMap.getOrDefault(currentLocation, "U").equals("D")) {
                knownMap.put(currentLocation, "C");
            }
        }

        if (previousAction != null) {
            if (previousAction.equals("forward")) {
                if (currentPercept.elementAt(0) != null && currentPercept.elementAt(0).equals("bump")) {
                    Location wallLocation = getAdjacentLocation(currentLocation, currentOrientation);
                    if (wallLocation != null) {
                        knownMap.put(wallLocation, "W");
                    }
                } else {
                    Location newLocation = getAdjacentLocation(currentLocation, currentOrientation);
                    if (newLocation != null) {
                        currentLocation = newLocation;
                        if (!knownMap.containsKey(currentLocation)) {
                            knownMap.put(currentLocation, "U");
                        }
                    }
                }
            } else if (previousAction.equals("turn right")) {
                turn("right");
            } else if (previousAction.equals("turn left")) {
                turn("left");
            }
        }
    }

    // Auxiliares para movimentação e orientação
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

    private void turn(String direction) {
        String[] orientations = {"N", "E", "S", "W"};
        int currentIndex = -1;
        for (int i = 0; i < orientations.length; i++) {
            if (orientations[i].equals(currentOrientation)) {
                currentIndex = i;
                break;
            }
        }
        if (currentIndex != -1) {
            if (direction.equals("right")) {
                currentOrientation = orientations[(currentIndex + 1) % 4];
            } else if (direction.equals("left")) {
                currentOrientation = orientations[(currentIndex - 1 + 4) % 4];
            }
        }
    }

    private String turnRight(String ori) {
        String[] orientations = {"N", "E", "S", "W"};
        int i = Arrays.asList(orientations).indexOf(ori);
        return orientations[(i + 1) % 4];
    }

    private String turnLeft(String ori) {
        String[] orientations = {"N", "E", "S", "W"};
        int i = Arrays.asList(orientations).indexOf(ori);
        return orientations[(i - 1 + 4) % 4];
    }

    // Decide se virar ou andar para ir para a célula vizinha
    private String determineTurnOrForward(Location next) {
        int dx = next.x - currentLocation.x;
        int dy = next.y - currentLocation.y;

        String neededDir = null;
        if (dx == 1) neededDir = "E";
        else if (dx == -1) neededDir = "W";
        else if (dy == 1) neededDir = "S";
        else if (dy == -1) neededDir = "N";

        if (currentOrientation.equals(neededDir)) {
            return "forward";
        } else if (turnLeft(currentOrientation).equals(neededDir)) {
            return "turn left";
        } else if (turnRight(currentOrientation).equals(neededDir)) {
            return "turn right";
        } else {
            return "turn right"; // fallback
        }
    }

    // Busca caminho usando BFS
    private List<Location> bfsPath(Location start, Location goal) {
        Queue<Location> queue = new LinkedList<>();
        Map<Location, Location> cameFrom = new HashMap<>();
        queue.add(start);
        cameFrom.put(start, null);

        while (!queue.isEmpty()) {
            Location current = queue.poll();
            if (current.equals(goal)) break;

            for (String dir : List.of("N", "E", "S", "W")) {
                Location neighbor = getAdjacentLocation(current, dir);
                if (neighbor == null) continue;
                String cellStatus = knownMap.getOrDefault(neighbor, "U");
                if (!cellStatus.equals("W") && !cameFrom.containsKey(neighbor)) {
                    queue.add(neighbor);
                    cameFrom.put(neighbor, current);
                }
            }
        }

        if (!cameFrom.containsKey(goal)) return null;

        List<Location> path = new LinkedList<>();
        for (Location at = goal; at != null; at = cameFrom.get(at)) {
            path.add(0, at);
        }
        return path;
    }

    // Busca a célula desconhecida mais próxima
    private Location findNearestUnknown() {
        Queue<Location> queue = new LinkedList<>();
        Set<Location> visited = new HashSet<>();
        queue.add(currentLocation);
        visited.add(currentLocation);

        while (!queue.isEmpty()) {
            Location current = queue.poll();
            for (String dir : List.of("N", "E", "S", "W")) {
                Location neighbor = getAdjacentLocation(current, dir);
                if (neighbor == null) continue;
                if (visited.contains(neighbor)) continue;

                String status = knownMap.getOrDefault(neighbor, "U");
                if (status.equals("U")) {
                    return neighbor;
                }
                if (!status.equals("W")) {
                    queue.add(neighbor);
                    visited.add(neighbor);
                }
            }
        }
        return null;
    }

    public int getMoveCount() {
        return moveCount;
    }
}
