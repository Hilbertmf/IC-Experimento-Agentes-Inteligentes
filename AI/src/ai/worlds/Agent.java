package ai.worlds;

/**
 * Uma classe abstrata que serve como base para todos os agentes inteligentes
 * no framework de simulação de mundos de IA.
 * Todos os agentes devem implementar métodos para determinar suas ações
 * e para performá-las dentro de um ambiente.
 *
 * @author Jill Zimmerman -- jill.zimmerman@goucher.edu
 * @version 1.0
 * @since Original
 */
public abstract class Agent
{
	/**
	 * O corpo físico do agente, que interage diretamente com o ambiente.
	 * Representa a presença e a capacidade de movimento do agente no mundo.
	 */
	public AgentBody body;
	
	/**
	 * O score atual do agente, usado para medir seu desempenho no ambiente.
	 * Pode representar pontos ganhos, sujeira limpa, objetivos alcançados, etc.
	 */
	public int score;
	
	/**
	 * Contador de movimentos realizados pelo agente. Usado para métricas
	 * de eficiência, como o score "Limpeza - Movimentos" no VacuumWorld.
	 */
	protected int moveCount; // Tornamos protected para permitir acesso por classes filhas sem expor publicamente.
	
	/**
	 * A percepção atual do ambiente recebida pelo agente.
	 * O tipo exato deste objeto varia dependendo do ambiente.
	 */
	public Object percept;
	
	/**
	 * A ação que o agente determinou que deve executar em seu próximo passo.
	 * Esta String é interpretada pelo ambiente.
	 */
	public String action;
	

	/**
	 * Construtor padrão para a classe Agent.
	 * Inicializa o corpo do agente, o score e o contador de movimentos.
	 */
	public Agent()
	{
		body = new AgentBody(); // Cada agente tem seu próprio corpo
		score = 0;
		moveCount = 0;
	}

	/**
	 * Método abstrato que deve ser implementado por qualquer subclasse de Agent.
	 * É responsável por definir a lógica de tomada de decisão do agente,
	 * armazenando a ação escolhida na variável `action`.
	 */
	public abstract void determineAction();
	
	/**
	 * Método abstrato que deve ser implementado por qualquer subclasse de Agent.
	 * É responsável por executar a ação atualmente definida na variável `action`
	 * dentro do contexto do ambiente.
	 *
	 * @param e O {@link ai.worlds.Environment} no qual o agente está inserido e executará a ação.
	 */
	public abstract void takeAction(Environment e);
	
    /**
     * Método abstrato que deve ser implementado por qualquer subclasse de Agent.
     * Retorna o número total de movimentos (avanços e giros) realizados pelo agente.
     *
     * @return O total de movimentos.
     */
    public abstract int getMoveCount();
}