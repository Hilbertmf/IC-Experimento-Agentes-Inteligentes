package ai.worlds.vacuum;


import java.util.Vector;
import ai.worlds.*;
import javax.swing.*;

/**
 * Representa o ambiente simulado do mundo do aspirador (VacuumWorld).
 * Este ambiente é um grid onde agentes podem se mover, aspirar sujeira
 * e interagir com paredes.
 *
 * <p>A classe estende {@link ai.worlds.GridEnvironment} para gerenciar o grid
 * e a interação com os agentes. Inclui métodos para popular o grid com sujeira,
 * verificar ações legais, fornecer percepções aos agentes e calcular o desempenho.</p>
 *
 * @author Jill Zimmerman -- jill.zimmerman@goucher.edu
 * @version 1.0
 * @since Original
 */
public class VacuumWorld extends GridEnvironment
{
    /**
     * Construtor para o ambiente VacuumWorld.
     * Inicializa o grid com as dimensões especificadas e popula-o com sujeira
     * com uma dada probabilidade.
     *
     * @param a Um array de {@link ai.worlds.Agent} que operarão neste ambiente.
     * @param xsize O número de colunas (largura) do grid.
     * @param ysize O número de linhas (altura) do grid.
     * @param probDirt A probabilidade (entre 0.0 e 1.0) de uma célula ser inicializada com sujeira.
     * @param f O {@link javax.swing.JFrame} principal da aplicação, usado para renderização.
     */
    public VacuumWorld(Agent[] a, int xsize, int ysize, double probDirt, JFrame f) {
    	super(a,xsize,ysize, f);
    	// Preenche o grid com sujeira baseada na probabilidade
    	fillGrid(probDirt,(new Dirt()).getClass());
    }
    
    /**
     * Determina se uma ação específica é legal (permitida) no ambiente VacuumWorld.
     *
     * @param a Uma String representando a ação a ser verificada (ex: "suck", "forward").
     * @return {@code true} se a ação é legal, {@code false} caso contrário.
     */   
    @Override // Sobrescreve o método legalAction da classe pai Environment
    public boolean legalAction(String a) {
    	if (a == null) return false; // Ação nula não é legal
    	return (a.equals("suck")) || (a.equals("forward")) || (a.equals("turn right")) || (a.equals("turn left")) || (a.equals("shut-off"));
    }
    
    /**
     * Gera a percepção atual para um dado agente.
     * A percepção inclui informações sobre colisão (`"bump"`), sujeira (`"dirt"`)
     * na célula atual e se a célula atual é o ponto inicial (`"home"`).
     *
     * @param a O {@link ai.worlds.Agent} para o qual a percepção será gerada.
     * @return Um {@link java.util.Vector} contendo Strings que descrevem a percepção:
     * <ul>
     * <li>Elemento 0: "bump" se houve colisão, ou "" (string vazia)</li>
     * <li>Elemento 1: "dirt" se a célula atual tem sujeira, ou ""</li>
     * <li>Elemento 2: "home" se a célula atual é a localização inicial (1,1), ou ""</li>
     * </ul>
     * <p>Nota: O ponto inicial (1,1) é um padrão para o ambiente base.</p>
     */  
    @Override // Sobrescreve o método getPercept da classe pai Environment
    public Object getPercept(Agent a) {
        Location loc = a.body.loc; // Localização atual do corpo do agente
        Vector v = new Vector(4); // Vetor para armazenar as percepções

        // Verifica se há sujeira na localização atual
        boolean isDirty = contains((Vector) grid[loc.x][loc.y], (new Dirt()).getClass());
        // System.out.println("DEBUG: Agente em (" + loc.x + "," + loc.y + "). Percepção de sujeira: " + isDirty); // Comentário de depuração

        // Adiciona "bump" se o agente colidiu, caso contrário, string vazia
        if (a.body.bump) v.addElement("bump");
        else v.addElement("");
        
        // Adiciona "dirt" se a célula atual tem sujeira, caso contrário, string vazia
        if (isDirty) v.addElement("dirt");
        else v.addElement("");
        
        // Adiciona "home" se a célula atual é a localização inicial (1,1), caso contrário, string vazia
        if (loc.x == 1 && loc.y == 1) v.addElement("home");
        else v.addElement("");
        
        return v;
    }
   
    /**
     * Calcula a medida de desempenho de um agente no ambiente VacuumWorld.
     * A medida de desempenho é definida como o número de células limpas
     * subtraído do número total de movimentos realizados pelo agente.
     *
     * @param a O {@link ai.worlds.Agent} para o qual a medida de desempenho será calculada.
     * @return Um inteiro representando o score de desempenho do agente.
     * Um score mais alto (menos negativo) indica melhor desempenho.
     */
    @Override // Sobrescreve o método performanceMeasure da classe pai Environment
    public int performanceMeasure(Agent a) {
        AgentBody body = a.body;
        // O score é calculado como o número de itens "coletados" (sujeiras limpas)
        // menos o número de movimentos registrados pelo agente.
        a.score = body.container.size() - a.getMoveCount();
        // System.out.println("DEBUG: Performance Measure (Limpeza - Movimentos) para " + a.getClass().getSimpleName() + ": Sujeiras limpas = " + body.container.size() + ", Movimentos = " + a.getMoveCount() + ", Score calculado = " + a.score); // Comentário de depuração
        
        return a.score;
    }    
}