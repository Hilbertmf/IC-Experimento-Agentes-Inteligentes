package ai.worlds.vacuum;

import ai.worlds.*;


/**
 * Uma classe abstrata que define o comportamento básico de um agente aspirador
 * genérico no ambiente VacuumWorld. Esta classe estende {@link ai.worlds.Agent}
 * e fornece implementações padrão para ações como aspirar, mover, virar e desligar.
 * Agentes específicos (reativos, baseados em modelo) devem estender esta classe
 * e implementar o método {@link #determineAction()}.
 *
 * @author Jill Zimmerman -- jill.zimmerman@goucher.edu
 * @version 1.0
 * @since Original
 */
public abstract class VacuumAgent extends Agent
{  
	/**
	 * Armazena a última ação executada pelo agente. Pode ser usado para
	 * lógica de atualização de modelo ou depuração.
	 */
	public String lastAction = null; // Nota: No seu código, 'lastAction' não é explicitamente atualizado após 'action' ser definido. A variável 'action' da classe pai 'Agent' é a que é usada para execução.
	
	/**
	 * Desliga o aspirador, encerrando sua atividade no ambiente.
	 * O agente para de executar ações.
	 */
    public void shutOff( )
    {
    	body.alive = false; // Define o estado do corpo do agente como não vivo
    }
    
	/**
	 * Executa a ação de aspirar a sujeira na célula atual do agente.
	 *
	 * @param vw O ambiente {@link VacuumWorld} onde a ação de aspirar será executada.
	 */
    void suck(VacuumWorld vw) // visibilidade default (package-private)
    {
        // System.out.println("DEBUG: VacuumAgent.suck() chamado. Chamando vw.grab(body)."); // Comentário de depuração
    	vw.grab(body); // O ambiente instrui o corpo do agente a "pegar" o que está na célula (sujeira)
    }
    
	/**
	 * Executa a próxima ação determinada pelo agente no ambiente.
	 * Este método é chamado pelo sistema do ambiente para que o agente
	 * atue de acordo com sua decisão.
	 *
	 * @param e O {@link ai.worlds.Environment} atual, que neste caso é um {@link VacuumWorld}.
	 */
    @Override // Sobrescreve o método takeAction da classe pai Agent
    public void takeAction(Environment e)
    {
	    VacuumWorld vw = (VacuumWorld) e; // Converte o ambiente genérico para VacuumWorld
	    // Executa a ação apropriada com base na String armazenada em 'action'
	    if (action.equals("suck")) suck(vw);
	    else if (action.equals("forward")) vw.forward(body);
	    else if (action.equals("turn right")) vw.turn(body,"right");
	    else if (action.equals("turn left")) vw.turn(body,"left");
	    else if (action.equals("shut-off")) shutOff();
    }
}