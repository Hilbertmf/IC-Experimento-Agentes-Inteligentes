package ai.worlds.vacuum;


import java.util.Vector;
import ai.worlds.*;
import javax.swing.*;
/**
 * A Vacuum environment. 
 * @author Jill Zimmerman -- jill.zimmerman@goucher.edu
 *
 */
public class VacuumWorld extends GridEnvironment
{
    public VacuumWorld(Agent[] a, int xsize, int ysize, double probDirt, JFrame f) {
    	super(a,xsize,ysize, f);
//    	fillGrid(probDirt,(new Dirt()).getClass());
    	fillGrid(0.9,(new Dirt()).getClass());
    }
    
    /**
     * Determine if an action is legal.
     * @param a is an action string.
     */   
    public boolean legalAction(String a) {
    	if (a == null)return false;
    	return (a.equals("suck"))||(a.equals("forward"))||(a.equals("turn right"))||(a.equals("turn left"))||(a.equals("shut-off"));
    }
    
    /**
     * Get the next percept.
     * @param a is the agent.
     */  
//    public Object getPercept(Agent a) {
//    	Location loc = a.body.loc;
//    	Vector v = new Vector(4);
//    	Vector gr = (Vector) grid[loc.x][loc.y];
//    	if (a.body.bump) v.addElement("bump");
//    		else v.addElement("");
//    	if (contains(gr,(new Dirt()).getClass())) v.addElement("dirt");
//    		else v.addElement("");
//    	if (loc.x == 1 && loc.y == 1) v.addElement("home");
//    		else v.addElement("");
//    	return v;
//    }
    
    public Object getPercept(Agent a) {
        Location loc = a.body.loc;
        Vector v = new Vector(4);
        Vector gr = (Vector) grid[loc.x][loc.y];

        boolean isDirty = contains(gr, (new Dirt()).getClass());
        System.out.println("DEBUG: Agente em (" + loc.x + "," + loc.y + "). Percepção de sujeira: " + isDirty);

        if (a.body.bump) v.addElement("bump");
        else v.addElement("");
        if (isDirty) v.addElement("dirt"); // Usa a variável isDirty
        else v.addElement("");
        if (loc.x == 1 && loc.y == 1) v.addElement("home");
        else v.addElement("");
        return v;
    }
   
    /**
     * Determine performance of the agent.
     * @param a is the agent.
     */
    public int performanceMeasure(Agent a) {
        AgentBody body = a.body;
        
        // --- Cálculo do Score Padrão (Jill Zimmerman) ---
        int standardScore = 100 * body.container.size() - step;
        if (!body.alive && !(body.loc.x == 1 && body.loc.y == 1)) {
            standardScore = standardScore - 1000;
        }
        a.score = standardScore; // Armazena no campo 'score' do agente

        System.out.println("DEBUG: Performance Measure Padrão para " + a.getClass().getSimpleName() + ": Sujeiras limpas (container size) = " + body.container.size() + ", Passos = " + step + ", Score calculado = " + standardScore);

        // --- Cálculo e Armazenamento do Score Proposto 1 (Limpeza) ---
        // Aqui, você usaria o valor ATUAL do container, que é o correto no final do trial.
        a.scoreProposed1 = body.container.size(); // Armazena no campo 'scoreProposed1'
        System.out.println("DEBUG2: Performance Measure 1 (Limpeza) para " + a.getClass().getSimpleName() + ": Sujeiras limpas (container size) = " + a.scoreProposed1 + ", Score calculado = " + a.scoreProposed1);

        // --- Cálculo e Armazenamento do Score Proposto 2 (Limpeza - Movimentos) ---
        a.scoreProposed2 = body.container.size() - a.getMoveCount(); // Armazena no campo 'scoreProposed2'
        System.out.println("DEBUG3: Performance Measure 2 (Limpeza - Movimentos) para " + a.getClass().getSimpleName() + ": Sujeiras limpas = " + body.container.size() + ", Movimentos = " + a.getMoveCount() + ", Score calculado = " + a.scoreProposed2);
        
        return a.score; // Retorna o score padrão, pois é o que o Environment espera em takeStep()
    }
    
//    public int performanceMeasure(Agent a) {
//        AgentBody body = a.body;
//        // O score é 100 * o número de sujeiras no container do agente menos o número de passos
//        int score = 100 * body.container.size() - step;
//        if (! body.alive && !(body.loc.x==1 && body.loc.y==1)) score = score - 1000;
//        a.score=score;
//        System.out.println("DEBUG: Performance Measure para " + a.getClass().getSimpleName() + ": Sujeiras limpas (container size) = " + body.container.size() + ", Passos = " + step + ", Score calculado = " + score);
//        return score;
//    }
//    public int performanceMeasure(Agent a) {
//    	AgentBody body = a.body;
//    	int score = 100 * body.container.size() - step;
//    	if (! body.alive && !(body.loc.x==1 && body.loc.y==1)) score = score - 1000;
//    	a.score=score;
//    	return score;
//    }
    
    // number of cleansed positions
    public int performanceMeasure1(Agent a) {
    	return a.scoreProposed1;
//    	AgentBody body = a.body;
//    	System.out.println("DEBUG2: Performance Measure 2 para " + a.getClass().getSimpleName() + ": Sujeiras limpas (container size) = " + body.container.size() + ", Score calculado = " + body.container.size());
//    	return a.body.container.size();
    }
    
    public int performanceMeasure2(Agent a) {
    	return a.scoreProposed2;
//    	AgentBody body = a.body;
//    	System.out.println("DEBUG3: Performance Measure 3 para " + a.getClass().getSimpleName() + ": Sujeiras limpas (container size) = " + body.container.size() + ", Score calculado = " + (body.container.size() - a.getMoveCount()));
//    	return a.body.container.size() - a.getMoveCount();
    }
    
}

