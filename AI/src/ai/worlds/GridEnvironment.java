package ai.worlds;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.plaf.metal.MetalLookAndFeel;
import java.util.Vector;
import ai.worlds.vacuum.*;
import ai.worlds.wumpus.*;

/**
 * Uma classe abstrata que representa um ambiente de simulação com um layout de grid bidimensional
 * ocupado por vários objetos. Esta classe fornece a funcionalidade básica para gerenciar
 * o grid, a colocação de objetos e a interação com os agentes.
 *
 * <p>Subclasses devem implementar lógicas específicas do ambiente (como percepção,
 * legalidade de ações e medida de desempenho) para definir o comportamento do mundo.</p>
 *
 * @author Jill Zimmerman -- jill.zimmerman@goucher.edu
 * @version 1.0
 * @since Original
 */
public abstract class GridEnvironment extends Environment implements Cloneable
{
	/**
	 * Constante de localização para a direção Esquerda.
	 */
	public static final Location LEFT = new Location(-1, 0);
	/**
	 * Constante de localização para a direção Direita.
	 */
	public static final Location RIGHT = new Location(1, 0);
	/**
	 * Constante de localização para a direção Cima (norte).
	 */
	public static final Location UP = new Location(0, 1);
	/**
	 * Constante de localização para a direção Baixo (sul).
	 */
	public static final Location DOWN = new Location(0, -1);
	
	/**
	 * O tamanho total do grid, incluindo as bordas de parede.
	 * Representa (largura + 2, altura + 2).
	 */
	public Location size;
	
	/**
	 * O grid de células do ambiente. Cada elemento é um {@link java.util.Vector}
	 * que contém os objetos presentes naquela célula.
	 */
	public Object grid[][];
	
	/**
	 * Uma matriz booleana que registra se uma célula específica já foi visitada pelo agente principal.
	 */
	public boolean visited[][];
	
	/**
	 * A localização inicial padrão dos agentes no grid (geralmente (1,1) após as paredes).
	 */
	public Location start;
	
	/**
	 * O componente {@link java.awt.Canvas} responsável por desenhar o grid do ambiente.
	 */
	public GridCanvas canvas;
	
	/**
	 * O painel de controle da GUI que exibe informações do ambiente (score, passos, etc.)
	 * e botões de controle (run, step).
	 */
	GridPanel gridPanel;
	
	/**
	 * Flag interna que indica se a célula atual foi visitada pela primeira vez nesta rodada.
	 * Usado para lógica de percepção de "novidade".
	 */
	protected boolean newlyVisited;
	
	/**
	 * A referência ao {@link javax.swing.JFrame} principal que contém este ambiente.
	 */
	JFrame holder;

	/**
	 * Construtor para o GridEnvironment.
	 * Inicializa o grid com suas dimensões, marca as células como não visitadas,
	 * posiciona as paredes e inicializa os agentes.
	 *
	 * @param a Um array de {@link ai.worlds.Agent} que operarão neste ambiente.
	 * @param xsize O número de colunas visíveis do grid (excluindo bordas).
	 * @param ysize O número de linhas visíveis do grid (excluindo bordas).
	 * @param f O {@link javax.swing.JFrame} principal da aplicação, necessário para a GUI.
	 */
	public GridEnvironment(Agent[] a, int xsize, int ysize, JFrame f)
	{
		holder = f;
		size = new Location(xsize + 2, ysize + 2); // Tamanho total incluindo as paredes (x+2, y+2)
		grid = new Object[xsize + 2][ysize + 2];
		visited = new boolean[xsize + 2][ysize + 2];
		// Inicializa todas as células como não visitadas
		for(int i = 0; i < xsize + 2; i++)
			for(int j = 0; j < ysize + 2; j++)
				visited[i][j] = false;
		
		visited[1][1] = true; // A célula inicial (1,1) é marcada como visitada
		newlyVisited = true; // A célula inicial é considerada recém-visitada
		start = new Location(1, 1); // Define a localização inicial padrão
		agents = a; // Atribui o array de agentes
		placeWalls(); // Coloca as paredes ao redor do grid
		initGrid(); // Inicializa o grid com vetores vazios e posiciona agentes
		canvas = new GridCanvas(); // Cria o componente de desenho do grid
		gridPanel = new GridPanel(); // Cria o painel de controle da GUI
	}

	/**
	 * Cria uma cópia rasa deste objeto GridEnvironment.
	 * Apenas os campos primitivos e referências são copiados; os objetos referenciados (como o grid em si)
	 * não são copiados profundamente, o que pode levar a efeitos colaterais se os objetos internos forem modificados.
	 * Para uma cópia profunda do grid, use {@link #copyGrid(GridEnvironment, GridEnvironment, int, int)}.
	 *
	 * @return Uma cópia rasa deste GridEnvironment.
	 */
	public Object clone() {
		Object copy = null;
		try {
			// Chama o clone() da superclasse Object para uma cópia rasa
			return super.clone();
		} catch(Exception exception) {
			// Captura qualquer exceção (e.g., CloneNotSupportedException, embora implemente Cloneable)
			System.err.println("Erro ao clonar GridEnvironment: " + exception.getMessage());
		};
		return null;
	}

	/**
	 * Move o corpo do agente para frente na direção atual.
	 * Verifica se a nova localização contém uma parede. Se sim, define a flag de colisão.
	 * Caso contrário, move o corpo para a nova localização e atualiza a flag de visita.
	 *
	 * @param body O {@link ai.worlds.AgentBody} a ser movido.
	 */
	public void forward(AgentBody body)
	{
		// Calcula a nova localização com base na direção atual do corpo
		Location newloc = body.loc.forward(body.heading);
		// Obtém o vetor de objetos na nova localização
		Vector v = (Vector)grid[newloc.x][newloc.y];
		
		// Verifica se a nova localização contém uma parede
		if (v.size() > 0 && v.firstElement() instanceof Wall) {
		    body.bump = true; // Define a flag de colisão no corpo do agente
		} else {
		    body.bump = false; // Reseta a flag de colisão
		    removeObj(body.loc, body);  // Remove o corpo da localização atual
		    addObj(newloc, body); // Adiciona o corpo na nova localização
		    
		    // Verifica se a nova célula foi visitada pela primeira vez
		    if(visited[newloc.x][newloc.y] == false) newlyVisited = true;
		    visited[newloc.x][newloc.y] = true; // Marca a nova célula como visitada
		}		
	}

	/**
	 * Gira o corpo do agente para a esquerda ou para a direita.
	 * A direção é alterada com base nas direções cardeais predefinidas.
	 *
	 * @param body O {@link ai.worlds.AgentBody} a ser girado.
	 * @param direction A String que indica a direção do giro ("left" ou "right").
	 */
	public void turn(AgentBody body, String direction)
	{
		Location[] headings = {new Location(1,0), new Location(0,1), 
			    new Location(-1,0), new Location(0,-1)}; // Leste, Norte, Oeste, Sul
		int now = pos(body.heading,headings); // Obtém a posição atual da direção
		
		if (direction.equals("right"))
			body.heading = headings[(now + 3) % 4]; // Gira 90 graus para a direita (sentido horário)
		else if (direction.equals("left"))
			body.heading = headings[(now + 1) % 4]; // Gira 90 graus para a esquerda (sentido anti-horário)
	}

	/**
	 * Simula a ação de um agente "pegando" um objeto na sua localização atual.
	 * As modificações no código original focam especificamente na sujeira (`Dirt`)
	 * para o ambiente Vacuum World.
	 *
	 * @param body O {@link ai.worlds.AgentBody} que está executando a ação de pegar.
	 */
	public void grab(AgentBody body)
	{
		body.grabbed = false; // Reseta a flag de "agarrado"
		Location loc = body.loc; // Obtém a localização atual do corpo do agente
		Vector v = (Vector)grid[loc.x][loc.y]; // Obtém os objetos na célula atual
		
		System.out.println("DEBUG: GridEnvironment.grab() chamado para agente em (" + loc.x + "," + loc.y + ").");
	    boolean dirtFound = false; // Flag para depuração para verificar se sujeira foi encontrada
		
		for(int i = 0; i < v.size(); i++) {
			Obj o = (Obj)v.elementAt(i);
			
			// MODIFICAÇÃO: Foca em objetos do tipo Dirt (Sujeira)
			if (o instanceof Dirt) {
	            System.out.println("DEBUG: Sujeira encontrada em (" + loc.x + "," + loc.y + ").");
	            body.container.addElement(o); // Adiciona a sujeira ao container do corpo do agente
	            v.removeElement(o); // Remove a sujeira do grid
	            body.grabbed = true; // Define a flag de "agarrado" como verdadeira
	            dirtFound = true; // Marca que sujeira foi encontrada
	            System.out.println("DEBUG: Sujeira removida do grid e adicionada ao container. Container size: " + body.container.size());
	            break; // Sai do loop após pegar uma sujeira (assumindo que só pode pegar um item por vez)
			}
		}
		
		if (!dirtFound) {
	        System.out.println("DEBUG: Nenhuma sujeira encontrada para agarrar em (" + loc.x + "," + loc.y + ").");
	    }
	}

	/**
	 * Determina se a simulação deve ser encerrada.
	 * A simulação termina se todos os agentes no ambiente não estão mais "vivos".
	 *
	 * @return {@code true} se todos os agentes estão mortos e a simulação deve terminar,
	 * {@code false} caso contrário.
	 */
	public boolean termination() 
	{
	    boolean terminate = true;
	    for (int i = 0; i < agents.length; i++) {
		    if (agents[i].body.alive) {
		        terminate = false; // Se um agente está vivo, não termina
		        break; // Não precisa verificar os outros
		    }
	    }
	    return terminate;
	}
	
	/**
	 * Atualiza o estado do ambiente após as ações dos agentes.
	 * Reseta todas as flags de colisão (`bump`) dos corpos dos agentes,
	 * reseta a flag {@link #newlyVisited} e então executa as ações dos agentes.
	 */
	public void updateEnv()
	{
	    for (int i = 0; i < agents.length; i++) {
		    agents[i].body.bump = false; // Dissipa as colisões (bumps)
	    }
	    newlyVisited = false; // Reseta a flag de recém-visitado
	    executeAgentActions(); // Chama o método para executar as ações dos agentes
	}
	
	/**
	 * Remove um objeto específico de uma localização no grid.
	 *
	 * @param loc A {@link ai.worlds.Location} de onde o objeto será removido.
	 * @param ob O {@link ai.worlds.Obj} a ser removido.
	 */
	public void removeObj(Location loc, Obj ob)
	{
		Vector v = (Vector)grid[loc.x][loc.y];
		v.removeElement(ob);
	}

	/**
	 * Adiciona um objeto específico a uma localização no grid.
	 * A localização do objeto também é atualizada.
	 *
	 * @param loc A {@link ai.worlds.Location} onde o objeto será adicionado.
	 * @param ob O {@link ai.worlds.Obj} a ser adicionado.
	 */
	public void addObj(Location loc, Obj ob)
	{
		Vector v = (Vector)grid[loc.x][loc.y];
		v.addElement(ob);
		ob.loc = loc; // Atualiza a localização do objeto
	}

	/**
	 * Coloca objetos {@link ai.worlds.Wall} nas bordas do grid.
	 * Isso cria um "limite" para o ambiente.
	 */
	void placeWalls()
	{
	    Wall w = new Wall(); // Cria uma instância de parede para reutilização
	    // Coloca paredes nas linhas superior e inferior
	    for (int i = 0; i < size.x; i++){
		    Vector v1 = new Vector();
		    Vector v2 = new Vector();
		    v1.addElement(w);
		    v2.addElement(w);
		    grid[i][0] = v1; // Linha inferior
		    grid[i][size.y - 1] = v2; // Linha superior
	    }
	    // Coloca paredes nas colunas esquerda e direita (excluindo os cantos já preenchidos)
	    for (int i = 1; i < size.y - 1; i++){
		    Vector v1 = new Vector();
		    Vector v2 = new Vector();
		    v1.addElement(w);
		    v2.addElement(w);
		    grid[0][i] = v1; // Coluna esquerda
		    grid[size.x - 1][i] = v2; // Coluna direita
	    }
	}
	
	/**
	 * Inicializa o grid colocando um vetor vazio em cada célula "jogável"
	 * (excluindo as bordas de parede) e posicionando os agentes na célula inicial (1,1).
	 */
	void initGrid()
	{
	    // Preenche as células internas do grid com vetores vazios
	    for (int i = 1; i < size.x - 1; i++) {
		    for (int j = 1; j < size.y - 1; j++){
		        Vector v = new Vector();
		        grid[i][j] = v;
		    }
	    }
	    // Posiciona os corpos dos agentes na célula inicial (1,1)
	    for (int i = 0; i < agents.length; i++){
		    Location loc = agents[i].body.loc; // (Esta linha pode ser redundante se loc sempre for (1,1))
		    Vector v = (Vector)grid[1][1];
		    v.addElement(agents[i].body);
	    }
	}

	/**
	 * Preenche as células internas do grid com instâncias de uma classe de objeto
	 * com uma dada probabilidade. O agente principal é temporariamente removido
	 * da célula (1,1) antes de preencher e depois recolocado para evitar ser substituído.
	 *
	 * @param prob A probabilidade (entre 0.0 e 1.0) de cada célula ser preenchida
	 * com uma nova instância do objeto.
	 * @param c A {@link java.lang.Class} do objeto a ser adicionado ao grid.
	 */
	public void fillGrid(double prob, Class c)
	{
	    // Remove temporariamente o primeiro agente da célula inicial (1,1)
	    Object a = ((Vector) grid[1][1]).elementAt(0);
	    ((Vector) grid[1][1]).removeElementAt(0);
	    
	    // Itera sobre as células internas do grid
	    for (int i = 1; i < size.x - 1; i++) {
		    for (int j = 1; j < size.y - 1; j++){
		        if (Math.random() < prob) { // Com a probabilidade dada
			        try {
			            // Adiciona uma nova instância do objeto na célula
			            addObj(new Location(i,j), (Obj)c.newInstance());
			        }
			        catch(Exception e) {
                        System.err.println("Erro ao instanciar classe em fillGrid: " + e.getMessage());
                    }
		        }
		    }
	    }
	    ((Vector) grid[1][1]).addElement(a); // Recoloca o agente na célula inicial
	}
	
	/**
	 * Adiciona uma nova instância de um objeto de uma classe específica a uma localização do grid.
	 *
	 * @param loc A {@link ai.worlds.Location} onde o objeto será adicionado.
	 * @param c A {@link java.lang.Class} do objeto a ser adicionado.
	 */
	public void fillLoc(Location loc, Class c) {
		try {
			addObj(loc, (Obj) c.newInstance());
		} catch(Exception e) {
            System.err.println("Erro ao instanciar classe em fillLoc: " + e.getMessage());
        }
	}

	/**
	 * Determina se um {@link java.util.Vector} de objetos contém pelo menos
	 * uma instância de uma dada classe.
	 *
	 * @param v O {@link java.util.Vector} de objetos a ser verificado.
	 * @param c A {@link java.lang.Class} a ser procurada.
	 * @return {@code true} se o Vector contém uma instância da classe, {@code false} caso contrário.
	 */
	public boolean contains(Vector v, Class c)
	{
	    boolean holds = false;
	    for (int i = 0; i < v.size(); i++) {
		    if (c.isInstance(v.elementAt(i))) { // Verifica se o elemento é uma instância da classe
		        holds = true;
		        break; // Se encontrou, não precisa continuar
		    }
	    }
	    return holds;
	}

	/**
	 * Retorna a primeira instância de uma dada classe encontrada em um {@link java.util.Vector}.
	 *
	 * @param v O {@link java.util.Vector} de objetos a ser procurado.
	 * @param c A {@link java.lang.Class} do objeto a ser retornado.
	 * @return A primeira instância de {@link ai.worlds.Obj} da classe especificada,
	 * ou {@code null} se nenhuma for encontrada.
	 */
	public Obj getItem(Vector v, Class c)
	{
	    for (int i = 0; i < v.size(); i++) {
		    if (c.isInstance(v.elementAt(i))) {
		        return (Obj) v.elementAt(i);
		    }
	    }
	    return null;
	}
	
	/**
	 * Determina se um objeto de uma dada classe está presente em alguma das
	 * células vizinhas (norte, sul, leste, oeste) de uma localização específica.
	 *
	 * @param loc A {@link ai.worlds.Location} central para verificar os vizinhos.
	 * @param c A {@link java.lang.Class} do objeto a ser procurado.
	 * @return {@code true} se um objeto da classe for encontrado em um vizinho, {@code false} caso contrário.
	 */
	public boolean neighbor(Location loc, Class c)
	{
	    return contains((Vector)grid[loc.x][loc.y+1],c) || // Norte
		   contains((Vector)grid[loc.x+1][loc.y],c) || // Leste
		   contains((Vector)grid[loc.x][loc.y-1],c) || // Sul
		   contains((Vector)grid[loc.x-1][loc.y],c);  // Oeste
	}
	
	/**
	 * Retorna a posição (índice) de uma dada direção (`heading`) em um array de direções.
	 * Útil para calcular giros.
	 *
	 * @param heading A {@link ai.worlds.Location} que representa a direção a ser encontrada.
	 * @param headings Um array de {@link ai.worlds.Location}s representando as direções possíveis.
	 * @return O índice da direção no array, ou -1 se não for encontrada.
	 */
	int pos(Location heading, Location[] headings)
	{
		for (int i = 0; i < headings.length; i++) {
			if (heading.x == headings[i].x &&
			    heading.y == headings[i].y)
				return i;
		}
		return -1;
	}

	/**
	 * Tira um "instantâneo" do ambiente, redesenhando o canvas e atualizando
	 * as informações no painel de controle (percepções, ações, score, passos).
	 */
	public void snapshot()
	{
	   canvas.moveflag = true; // Indica que houve um movimento para otimizar o redesenho
	   if(canvas.getGraphics() != null) {
		   canvas.update(canvas.getGraphics()); // Atualiza o canvas
	   }
	   
	   // Atualiza os rótulos de percepção e ação do primeiro agente no painel de controle
	   if (agents[0].action != null) {
		gridPanel.action.setText(agents[0].action);
		Vector percept = (Vector) (agents[0].percept);
		String p = new String();
		for (int i = 0; i < percept.size(); i++) {
		    p = p + " " + percept.elementAt(i); // Concatena as percepções
		}
		gridPanel.percepts.setText(p);
	    }
	   
	   // Atualiza score e passos
	   gridPanel.score.setText(Integer.toString(agents[0].score));
	   gridPanel.steps.setText(Integer.toString(step));
	   gridPanel.repaint(0); // Força um repaint do painel de controle
	}
	
	/**
	 * Copia o conteúdo de um grid de ambiente de origem para um grid de ambiente de destino.
	 * Isso cria novas instâncias dos objetos dentro de cada célula,
	 * garantindo que as modificações no grid de destino não afetem o grid de origem.
	 *
	 * @param from O {@link GridEnvironment} de onde o grid será copiado.
	 * @param to O {@link GridEnvironment} para onde o grid será copiado.
	 * @param xsize O número de colunas do grid (incluindo as bordas).
	 * @param ysize O número de linhas do grid (incluindo as bordas).
	 */
	public static final void copyGrid(GridEnvironment from, GridEnvironment to, int xsize, int ysize)
	{
		Object[][] newGrid = new Object[xsize][ysize]; // Cria um novo grid para o destino
		for (int i = 0; i < xsize; i++) {
			for (int j = 0; j < ysize; j++) {
				Vector vnew = new Vector(); // Novo vetor para a célula de destino
				Vector vold = (Vector)from.grid[i][j]; // Vetor de objetos da célula de origem
				for (int k = 0; k < vold.size(); k++) {
					Class c = vold.elementAt(k).getClass(); // Obtém a classe do objeto
					Obj o = null;
					try {
                        o = (Obj)c.newInstance(); // Cria uma nova instância do objeto
                    }
					catch(Exception ex) {
                        System.err.println("Erro ao instanciar objeto durante copyGrid: " + ex.getMessage());
                    };
					vnew.addElement(o); // Adiciona a nova instância ao vetor da célula de destino
				}
				newGrid[i][j] = vnew; // Atribui o novo vetor de objetos à célula no grid de destino
			}
		}
		to.grid = newGrid; // Define o grid do ambiente de destino para a nova cópia
	}
	
	/**
	 * {@link java.awt.Canvas} interno para desenhar o grid do ambiente.
	 * Gerencia o desenho das células, objetos, e informações adicionais
	 * como o mapa interno do agente Wumpus ou células visitadas pelo agente Vacuum.
	 */
    public class GridCanvas extends Canvas
    {
		/**
		 * O tamanho em pixels de uma única célula do grid.
		 */
		int cellSize = 35;
		/**
		 * Coordenada X de início para o desenho do grid principal.
		 */
		int startx = 25;
		/**
		 * Coordenada Y de início para o desenho do grid principal.
		 */
		int starty = 25;
		/**
		 * Coordenada X de fim para o desenho do grid principal.
		 */
		int endx;
		/**
		 * Coordenada Y de fim para o desenho do grid principal.
		 */
		int endy;
		/**
		 * Coordenada X de início para o desenho do segundo grid (mapa do agente).
		 */
		int startx2 = 0;
		/**
		 * Coordenada X de fim para o desenho do segundo grid (mapa do agente).
		 */
		int endx2 = 0;
		/**
		 * Coordenada X da última localização conhecida do agente principal.
		 * Usado para otimizar o redesenho.
		 */
		int lastx = 1;
		/**
		 * Coordenada Y da última localização conhecida do agente principal.
		 * Usado para otimizar o redesenho.
		 */
		int lasty = 1;
		/**
		 * Coordenada X da localização atual do agente principal.
		 */
		int currentx = 1;
		/**
		 * Coordenada Y da localização atual do agente principal.
		 */
		int currenty = 1;
		/**
		 * Flag que indica se houve um movimento do agente, sugerindo um redesenho otimizado.
		 */
		boolean moveflag = false;
    
		/**
		 * Construtor para GridCanvas.
		 * Configura a cor de fundo e ajusta o tamanho das células com base no tamanho do grid.
		 */
		public GridCanvas()
		{
		    Color metalColor = MetalLookAndFeel.getDesktopColor();
		    setBackground(metalColor);
		    // Ajusta o tamanho da célula se o grid for muito grande para caber na tela
		    if (size.x > 12 || size.y > 10) cellSize = 280 / (size.x - 2);
		    startx2 = endx + 60; // Calcula a posição inicial do segundo grid
		    endx2 = startx2 + (size.x - 2) * cellSize;
		}
		
		/**
		 * Método de pintura principal para desenhar o grid do ambiente e o mapa do agente.
		 * Desenha as células, objetos, linhas do grid, e informações específicas do agente.
		 *
		 * @param g O contexto gráfico no qual desenhar.
		 */
		public void paint(Graphics g)
		{
		    int numRows = size.y - 2; // Número de linhas visíveis (excluindo paredes)
		    int numCols = size.x - 2; // Número de colunas visíveis (excluindo paredes)
		    Location loc = agents[0].body.loc; // Localização do primeiro agente
		    currentx = loc.x; 
		    currenty = loc.y;
		
		    // Se não houve movimento, desenha retângulos brancos para limpar a tela
		    if (! moveflag){  
				endx = startx + numCols * cellSize;
				endy = starty + numRows * cellSize;
				startx2 = endx + 60;
			    endx2 = startx2 + numCols * cellSize;
				g.setColor(Color.white);
				g.fillRect(startx,starty,numCols * cellSize,numRows * cellSize); // Grid principal
				g.fillRect(startx2, starty, numCols * cellSize, numRows * cellSize); // Segundo grid
		    }
		
		    // Desenha as linhas das colunas e linhas
		    g.setColor(Color.black);
		    for (int i = 0; i <= numCols; i++) {
		    	g.drawLine(startx + i * cellSize, starty,
		    			startx + i * cellSize, endy); // Linhas verticais do grid principal
		    	g.drawLine(startx2 + i * cellSize, starty,
		    			startx2 + i * cellSize, endy); // Linhas verticais do segundo grid
		    }
		    // Desenha os números das colunas
		    for (int i = 1; i <= numCols; i++) {
		    	g.drawString(Integer.toString(i),
		    			startx + i * cellSize - cellSize / 2, endy + 15);
		    	g.drawString(Integer.toString(i),
		    			startx2 + i * cellSize - cellSize / 2, endy + 15);
		    }
		    for (int i = 0; i <= numRows; i++) {
		    	g.drawLine(startx, starty + i * cellSize, endx,
		    			starty + i * cellSize); // Linhas horizontais do grid principal
		    	g.drawLine(startx2, starty + i * cellSize, endx2,
		    			starty + i * cellSize); // Linhas horizontais do segundo grid
		    }
		    // Desenha os números das linhas
		    for (int i = 0; i < numRows; i++) {
		    	g.drawString(Integer.toString(numRows - i), startx - 15,
		    			starty + (i + 1) * cellSize - cellSize / 2);
		    	g.drawString(Integer.toString(numRows - i), startx2 - 15,
		    			starty + (i + 1) * cellSize - cellSize / 2);
		    }
		    
		    // Desenha os objetos do grid (ambiente real)
		    for (int i = 1; i <= numCols; i++) {
				for (int j = 1; j <= numRows; j++){
				    Vector v = (Vector) grid[i][j];
				    for (int k = 0; k < v.size(); k++) {
					    ((Obj)v.elementAt(k)).draw(g, screenpos(i,j), cellSize);
					}
				}
			}
		    g.setColor(Color.black);
		    
		    // Lógica para desenhar o mapa interno do agente (Wumpus ou Vacuum)
		    WumpusAgent wAgent = null;
		    VacuumAgent vAgent = null;
		    if (agents[0] instanceof WumpusAgent) wAgent = (WumpusAgent) agents[0];
		    else if (agents[0] instanceof VacuumAgent) vAgent = (VacuumAgent) agents[0];
		    
		    if (wAgent != null) { // Se for um agente Wumpus, desenha seu mapa de lógica
		    	WumpusLogic logic = wAgent.logic;
		    	Object[][] mygrid = logic.grid; // Grid interno de percepções do agente
		    	if (logic != null && mygrid != null) {
		    		for (int x = 1; x <= size.x - 2; x++) {
		    			for (int y = 1; y <= size.y - 2; y++) {
		    				Vector square = (Vector)mygrid[x][y];
		    				String w = "", p = "";
		    				if (square.size() > 1) {w = (String)square.elementAt(1);} // Possivelmente Wumpus
		    				if (square.size() > 2) {p = (String)square.elementAt(2);} // Possivelmente Poço
		    				g.setColor(Color.white);
		    				g.fillRect(screenpos2(x, y).x + 2, screenpos2(x,y).y + 2, cellSize - 4, cellSize - 4); // Limpa a célula do mapa
		    				g.setColor(Color.black);
		    				g.drawString(w, screenpos2(x,y).x + 2, screenpos2(x,y).y + 15); // Desenha 'W'
		    				g.drawString(p, screenpos2(x,y).x + 2, screenpos2(x,y).y + 30); // Desenha 'P'
		    			}
		    		}
		    	}
		    }
		    else if (vAgent != null) { // Se for um agente Vacuum, desenha células visitadas
		    	for (int x = 1; x <= size.x - 2; x++) {
	    			for (int y = 1; y <= size.y - 2; y++) {
	    				if (visited[x][y]) { // Se a célula foi visitada
	    					g.drawString("V", screenpos2(x,y).x + cellSize/2 - g.getFontMetrics().stringWidth("V")/2, screenpos2(x,y).y + cellSize/2 + g.getFontMetrics().getAscent()/2); // Desenha 'V' centralizado
	    				}
	    			}
		    	}
		    }
		    
		    lastx = currentx; // Atualiza a última posição conhecida do agente
		    lasty = currenty;
		    moveflag = false; // Reseta a flag de movimento
		}
    
		/**
		 * Redesenha apenas as áreas que contêm o agente (localização antiga e nova).
		 * Isso otimiza o redesenho para evitar repintar o grid inteiro a cada passo.
		 *
		 * @param g O contexto gráfico no qual desenhar.
		 */
		public void update(Graphics g)
		{
		    // Limpa as células antigas e novas do agente
		    g.setColor(Color.white);  
		    g.fillRect(screenpos(lastx,lasty).x, screenpos(lastx,lasty).y,
		    		cellSize,cellSize); // Célula antiga
		    g.fillRect(screenpos(currentx,currenty).x, 
			       screenpos(currentx,currenty).y,cellSize,cellSize); // Célula nova
		    paint(g); // Chama o método paint para redesenhar
		}
    
		/**
		 * Converte coordenadas do grid (x,y) para coordenadas de tela para o grid principal.
		 *
		 * @param x A coordenada X do grid (coluna).
		 * @param y A coordenada Y do grid (linha).
		 * @return Um {@link java.awt.Point} representando as coordenadas de tela.
		 */
		Point screenpos(int x, int y)
		{
		    return new Point(startx + cellSize * (x - 1), endy - cellSize * y);
		}
		
		/**
		 * Converte coordenadas do grid (x,y) para coordenadas de tela para o segundo grid (mapa do agente).
		 *
		 * @param x A coordenada X do grid (coluna).
		 * @param y A coordenada Y do grid (linha).
		 * @return Um {@link java.awt.Point} representando as coordenadas de tela para o segundo grid.
		 */
		Point screenpos2(int x, int y) {
			return new Point(startx2 + cellSize * (x - 1), endy - cellSize * y);
		}
		
		/**
		 * Atualiza e redesenha a parte do grid correspondente às localizações antiga e nova do agente.
		 * Similar ao {@code update(Graphics g)}, mas aceita localizações explícitas.
		 *
		 * @param oldloc A localização antiga do agente.
		 * @param newloc A nova localização do agente.
		 * @param g O contexto gráfico.
		 */
		public void updateHere(Location oldloc, Location newloc, Graphics g)
		{
		    g.setColor(Color.white);  
		    Point p1 = screenpos(oldloc.x,oldloc.y);
		    Point p2 = screenpos(newloc.x, newloc.y);
		    g.fillRect(p1.x, p1.y,cellSize,cellSize);
		    g.fillRect(p2.x, p2.y,cellSize,cellSize);
		    paint(g);
		}
    }

	/**
	 * Um painel interno que fornece controles para a simulação do ambiente,
	 * como botões "Run" e "Step", e exibe informações como score, percepções,
	 * ações e passos.
	 */
    class GridPanel extends JPanel implements ActionListener
    {
		/**
		 * Rótulo que exibe o score atual do agente principal.
		 */
		JLabel score = new JLabel(" 0");
		/**
		 * Rótulo que exibe as percepções atuais do agente principal.
		 */
		JLabel percepts = new JLabel ("        ");
		/**
		 * Rótulo que exibe a ação atual sendo executada pelo agente principal.
		 */
		JLabel action = new JLabel ("        ");
		/**
		 * Rótulo que exibe o número de passos (turnos) da simulação.
		 */
		JLabel steps = new JLabel(" 0");
		/**
		 * Campo de texto para o usuário definir o número máximo de passos para a simulação.
		 */
		JTextField maxStepsField = new JTextField("1000",4);
		/**
		 * Botão para iniciar a execução contínua da simulação.
		 */
		JButton run = new JButton("Run");
		/**
		 * Botão para executar a simulação um passo por vez.
		 */
		JButton step = new JButton("Step");
    
		/**
		 * Construtor para GridPanel.
		 * Configura a disposição dos componentes, cores, e adiciona os listeners de ação.
		 */
		GridPanel()
		{
		    setBackground(Color.white);
		    percepts.setForeground(Color.darkGray);
		    action.setForeground(Color.darkGray);
		    steps.setForeground(Color.darkGray);
		    score.setForeground(Color.darkGray);
		    
		    // Painel para percepções e ações
		    JPanel p1 = new JPanel();
		    p1.setBackground(Color.white);
		    p1.setLayout(new GridLayout(1,4));
		    p1.add(new JLabel("   Agent Percepts: "));
		    p1.add(percepts);
		    p1.add(new JLabel("   Agent Action: "));
		    p1.add(action);
		
		    // Painel para passos, score e maxSteps
		    JPanel p2 = new JPanel();
		    GridBagLayout gridbag = new GridBagLayout();
		    GridBagConstraints constraints = new GridBagConstraints();
		    constraints.insets = new Insets(2, 8, 2, 8); // Margens internas
		    constraints.gridwidth = 1; // Uma célula por padrão
		    p2.setBackground(Color.white);
		    p2.setLayout(gridbag);
		    
		    JLabel label = new JLabel("Steps: ");
		    gridbag.setConstraints(label, constraints);
		    p2.add(label);
		    gridbag.setConstraints(steps, constraints);
		    p2.add(steps);
		    JLabel label2 = new JLabel("Score: ");
		    gridbag.setConstraints(label2, constraints);
		    p2.add(label2);
		    constraints.gridwidth = GridBagConstraints.REMAINDER; // Restante da linha
		    gridbag.setConstraints(score, constraints);
		    p2.add(score);
		    constraints.gridwidth = 1; // Volta para uma célula
		    JLabel label3 = new JLabel("Max Steps: ");
		    gridbag.setConstraints(label3, constraints);
		    p2.add(label3);
		    gridbag.setConstraints(maxStepsField, constraints);
		    p2.add(maxStepsField);
		
		    // Painel para botões Run e Step
		    JPanel p3 = new JPanel();
		    p3.setLayout(new GridLayout(1,3));
		    run.addActionListener(this);
		    p3.add(run); 
		    step.addActionListener(this);
		    p3.add(step);
		
		    // Painel para organizar p1 e p2
		    JPanel p4 = new JPanel();
		    p4.setBackground(Color.white);
		    p4.setLayout(new BorderLayout());
		    p4.add("West",p1);
		    p4.add("East",p2);
		
		    // Define a borda e o layout principal do GridPanel
		    setBorder(BorderFactory.createCompoundBorder(
		    BorderFactory.createRaisedBevelBorder(),
		    BorderFactory.createLoweredBevelBorder()));
		    setLayout(new BorderLayout());
		    add("West",p3);
		    add("Center",p4);
		} 
		
		/**
		 * Invoked when an action occurs. Implementação do {@link java.awt.event.ActionListener}.
		 * Gerencia os eventos dos botões "Run" e "Step".
		 *
		 * @param e O evento de ação.
		 */
		public void actionPerformed(ActionEvent e)
		{
		    String actionCommand = e.getActionCommand();
		    if (actionCommand.equals("Run")){
				run.setEnabled(false); // Desabilita o botão Run
				step.setEnabled(false); // Desabilita o botão Step
				maxSteps = Integer.parseInt(maxStepsField.getText()); // Define o número máximo de passos
				run(); // Inicia a simulação contínua (chamada no Environment pai)
		    }
		    else if (actionCommand.equals("Step")) {
				maxSteps = Integer.parseInt(maxStepsField.getText()); // Define o número máximo de passos
				takeStep(); // Executa um único passo (chamada no Environment pai)
		    }
		}
    }
}