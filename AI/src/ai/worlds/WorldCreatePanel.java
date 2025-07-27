package ai.worlds;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.plaf.metal.MetalLookAndFeel;
import ai.logic.ExtensionFileFilter;
import ai.worlds.vacuum.*;
import ai.worlds.wumpus.*;
import ai.worlds.Agent;

import java.util.HashMap;
import java.util.Map;

/**
 * WorldCreatePanel é um painel de interface gráfica do usuário (GUI) que permite
 * ao usuário criar, simular e executar testes com diferentes agentes em
 * ambientes simulados como VacuumWorld e WumpusWorld.
 *
 * <p>Este painel gerencia a seleção de tipos de ambiente, tipos de agente,
 * configurações de tamanho do mundo e a execução de múltiplos testes (trials)
 * com resultados exibidos.</p>
 *
 * @author Hilbert França
 * @version 1.0
 * @since Original (Adaptado e expandido para novos agentes)
 */
public class WorldCreatePanel extends JPanel implements ActionListener, ItemListener {
	/**
	 * Cor padrão utilizada para elementos com tema Metal Look and Feel.
	 */
	private static final Color metalColor = MetalLookAndFeel.getTextHighlightColor();

    /**
     * ComboBox para seleção do tipo de ambiente (e.g., Vacuum World, Wumpus World).
     */
    private JComboBox envs;
    /**
     * ComboBox para seleção do tipo de agente a ser simulado individualmente.
     */
    private JComboBox agents;
    /**
     * Campo de texto para definir o tamanho X (largura) do grid do ambiente.
     */
    private JTextField xsize;
    /**
     * Campo de texto para definir o tamanho Y (altura) do grid do ambiente.
     */
    private JTextField ysize;
    /**
     * Lista de agentes selecionados para a execução de múltiplos testes (trials).
     */
    private JList trialAgents;
    /**
     * Campo de texto para definir o número de rodadas (trials) a serem executadas.
     */
    JTextField numTrials; // Visibilidade default para acesso interno do TrialSet
    /**
     * CheckBox para habilitar tamanhos de grid aleatórios durante os trials.
     */
    private JCheckBox randomSizes;

    /**
     * Painel superior da GUI.
     */
    private JPanel northPanel = new JPanel();
    /**
     * Painel central da GUI, onde o ambiente de simulação é exibido.
     */
    private JPanel centerPanel = new JPanel();
    /**
     * Painel inferior da GUI, contendo as opções de trials e resultados.
     */
    private JPanel southPanel = new JPanel();
    /**
     * Subpainel na área norte para as opções de criação e simulação.
     */
    private JPanel northCenterPanel = new JPanel();

    /**
     * ScrollPane para o JTextArea de resultados.
     */
    private JScrollPane scroll;
    /**
     * Área de texto onde os resultados dos trials são exibidos.
     */
    private JTextArea results;
    /**
     * Rótulo para o título da seção de resultados.
     */
    private JLabel title;
    /**
     * Painel interno para conter os resultados.
     */
    private JPanel p = new JPanel();
    /**
     * Variável de string não utilizada diretamente, pode ser removida.
     */
    private String s = new String(); // Não utilizado diretamente.
    /**
     * Caminho completo do arquivo do mundo Wumpus carregado.
     */
    private String filepath;
    /**
     * Nome do arquivo do mundo Wumpus carregado.
     */
    private String filename;

    /**
     * Botão para carregar um mundo Wumpus salvo de um arquivo.
     */
    public JButton load = new JButton();
    /**
     * Botão para reconstruir um mundo Wumpus carregado.
     */
    public JButton rebuild = new JButton();
    /**
     * Botão para alternar para o modo de criação de mundo Wumpus.
     */
    public JButton buildWorld = new JButton();
    /**
     * Botão para alternar para o modo de simulação de agente em um ambiente.
     */
    public JButton simWorld = new JButton();

    /**
     * Botão para iniciar a execução de múltiplos trials.
     */
    private JButton trials;
    /**
     * Flag que indica se um mundo Wumpus foi carregado.
     */
    private boolean isLoaded = false;

    /**
     * Array de Strings para os nomes dos tipos de mundo disponíveis.
     */
    String[] worldStrings = {"Vacuum World", "Wumpus World"};
    /**
     * Array de Strings para os nomes dos agentes de Vacuum World disponíveis.
     */
    String[] vacuumStrings = {"Random Vacuum Agent", "Reactive Vacuum Agent", "HilbertAgenteReativoVacuumAgent", "HilbertAgenteModeloVacuumAgent"};
    /**
     * Array de Strings para os nomes dos agentes de Wumpus World disponíveis.
     */
    String[] wumpusStrings = {"Random Wumpus Agent", "Aimless Wumpus Agent", "Logic Testing Agent"};

    /**
     * Layout manager para organização de componentes.
     */
    private GridBagLayout gridbag;
    /**
     * Restrições para o GridBagLayout.
     */
    private GridBagConstraints constraints;

    /**
     * Editor de mundo Wumpus, utilizado para criar e salvar configurações de ambiente.
     */
    private WumpusWorldEditor wwe = new WumpusWorldEditor();

    /**
     * A referência ao JFrame principal que contém este painel.
     */
    private JFrame holder;

    /**
     * Construtor para o WorldCreatePanel.
     * Configura a interface gráfica, inicializa os componentes e adiciona listeners.
     *
     * @param f O {@link javax.swing.JFrame} principal que conterá este painel.
     */
    public WorldCreatePanel(JFrame f)
    {
    	holder = f;
    	setLayout(new BorderLayout());
    	//setup NorthPanel
    	envs = new JComboBox(worldStrings);
    	envs.addItemListener(this);
    	agents = new JComboBox(vacuumStrings);
    	xsize = new JTextField("8",2);
    	ysize = new JTextField("8",2);


    	rebuild = new JButton("Rebuild Loaded World");
    	rebuild.setActionCommand("Rebuild World");
    	rebuild.addActionListener(this);
    	rebuild.setEnabled(false);
    	buildWorld.setText("Create a Wumpus World");
    	buildWorld.addActionListener(this);
    	simWorld.setText("Simulate an Agent in an Environment");
    	simWorld.addActionListener(this);
    	load = new JButton("Load Wumpus World");
    	load.addActionListener(this);
    	gridbag = new GridBagLayout();
	    constraints = new GridBagConstraints();
	    constraints.fill = GridBagConstraints.BOTH;

	    gridbag = new GridBagLayout();
	    constraints = new GridBagConstraints();
	    constraints.fill = GridBagConstraints.BOTH;
		northPanel.setBackground(metalColor);
		northPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createRaisedBevelBorder(), BorderFactory.createLoweredBevelBorder()));
		northPanel.setLayout(new BorderLayout());

		JPanel northNorthPanel = new JPanel(gridbag);

		constraints.gridwidth = 2;
		constraints.insets = new Insets(4, 35, 4, 35);
    	gridbag.setConstraints(buildWorld, constraints);
    	northNorthPanel.add(buildWorld);
    	constraints.gridwidth = GridBagConstraints.REMAINDER;
    	gridbag.setConstraints(simWorld, constraints);
    	northNorthPanel.add(simWorld);
    	constraints.gridwidth = 1;
    	constraints.insets = new Insets(8, 4, 8, 4);
		northNorthPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createRaisedBevelBorder(), BorderFactory.createLoweredBevelBorder()));
		northPanel.add(northNorthPanel, BorderLayout.NORTH);

		northCenterPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createRaisedBevelBorder(), BorderFactory.createLoweredBevelBorder()));
		northCenterPanel.setPreferredSize(new Dimension(750, 75));
		northPanel.add(northCenterPanel, BorderLayout.CENTER);

	    add(northPanel, BorderLayout.NORTH);
		trialAgents = new JList(vacuumStrings);
		JScrollPane scrollPane = new JScrollPane(trialAgents);
		scrollPane.setPreferredSize(new Dimension(175,95));
		scrollPane.setBackground(metalColor);
		numTrials = new JTextField("10",2);
		randomSizes = new JCheckBox("",false);
		randomSizes.setBackground(metalColor);
		trials = new JButton("Run Trials");
		trials.addActionListener(this);

		JPanel trialChoicePanel = new JPanel();
		trialChoicePanel.add(new JLabel(" Select Trial Agents: "));
		trialChoicePanel.add(scrollPane);
		trialChoicePanel.setBackground(metalColor);

		gridbag = new GridBagLayout();
	    constraints = new GridBagConstraints();
	    constraints.insets = new Insets(2,8,2,8);
	    constraints.gridwidth = 1;
		JPanel numTrialsPanel = new JPanel(gridbag);
		JLabel l = new JLabel("Random Sizes");
		gridbag.setConstraints(l, constraints);
		numTrialsPanel.add(l);
		constraints.gridwidth = GridBagConstraints.REMAINDER;
		gridbag.setConstraints(randomSizes, constraints);
		numTrialsPanel.add(randomSizes);
		constraints.gridwidth = 1;
		JLabel label = new JLabel("Number of Trials");
		gridbag.setConstraints(label, constraints);
		numTrialsPanel.add(label);
		constraints.gridwidth = GridBagConstraints.REMAINDER;
		gridbag.setConstraints(numTrials, constraints);
		numTrialsPanel.add(numTrials);
		numTrialsPanel.setBackground(metalColor);

		p.setBackground(metalColor);
		p.setLayout(new BorderLayout());
		results = new JTextArea();
		results.setEditable(false);
		scroll = new JScrollPane(results);
		scroll.setPreferredSize(new Dimension(350, 150));
		title = new JLabel("Scores:");
		title.setFont(new Font("SansSerif",Font.ITALIC + Font.BOLD,14));
		p.add("North",title);
		p.add("Center",scroll);

		gridbag = new GridBagLayout();
	    constraints = new GridBagConstraints();
	    constraints.insets = new Insets(8,8,8,8);
		southPanel.setBackground(metalColor);
		southPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createRaisedBevelBorder(), BorderFactory.createLoweredBevelBorder()));
		southPanel.setLayout(gridbag);
		JLabel trialTitle = new JLabel("Agent Trials",0);
		trialTitle.setFont(new Font("SansSerif", Font.ITALIC+Font.BOLD, 14));
		gridbag.setConstraints(trialTitle, constraints);
		southPanel.add(trialTitle);
		gridbag.setConstraints(trialChoicePanel, constraints);
		southPanel.add(trialChoicePanel);
		gridbag.setConstraints(numTrialsPanel, constraints);
		southPanel.add(numTrialsPanel);
		gridbag.setConstraints(trials, constraints);
		southPanel.add(trials);
		gridbag.setConstraints(p, constraints);
		southPanel.add(p);
		add(southPanel, BorderLayout.SOUTH);
		centerPanel.setPreferredSize(new Dimension(750, 400));
		centerPanel.setBackground(Color.white);
		centerPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createRaisedBevelBorder(), BorderFactory.createLoweredBevelBorder()));
		add(centerPanel, BorderLayout.CENTER);
		load.setEnabled(false);
    }

    /**
     * Invoked when an action occurs (e.g., a button is clicked).
     * Dispara diferentes métodos com base no comando da ação.
     *
     * @param e O evento de ação que ocorreu.
     */
    public void actionPerformed(ActionEvent e) {
    	String action = e.getActionCommand();
		if (action.equals("Build Random")) build();
		else if (action.equals("Run Trials")) trials();
		else if(action.equals("Load Wumpus World")) load();
		else if(action.equals("Rebuild World")) rebuild();
		else if(action.equals("Create a Wumpus World")) createWorld();
		else if(action.equals("save")) wwe.save();
		else if(action.equals("Simulate an Agent in an Environment")) simulate();
    }

    /**
     * Alterna a interface do painel para o modo de simulação de agente em um ambiente.
     * Altera a visibilidade e o conteúdo dos painéis para exibir as opções de seleção
     * de ambiente e agente, e os controles de tamanho.
     */
    public void simulate() {
    	buildWorld.setBackground(Color.gray.brighter());
    	simWorld.setBackground(metalColor);
    	northPanel.remove(northCenterPanel);
    	gridbag = new GridBagLayout();
    	constraints = new GridBagConstraints();

    	JButton build = new JButton("Build Random");
    	build.addActionListener(this);

		JPanel choicePanel = new JPanel(gridbag);
    	JLabel label1 = new JLabel(" Environment: ");
    	JLabel label2 = new JLabel(" Agent: ");
    	gridbag.setConstraints(label1, constraints);
    	choicePanel.add(label1);
    	constraints.gridwidth = GridBagConstraints.REMAINDER;
    	gridbag.setConstraints(envs, constraints);
    	choicePanel.add(envs);
    	constraints.gridwidth = 1;
    	gridbag.setConstraints(label2, constraints);
    	choicePanel.add(label2);
    	gridbag.setConstraints(agents, constraints);
    	choicePanel.add(agents);
    	choicePanel.setBackground(metalColor);
    	gridbag = new GridBagLayout();
	    constraints = new GridBagConstraints();
	    constraints.insets = new Insets(0,2,0,2);
	    constraints.fill = GridBagConstraints.BOTH;
		JPanel sizePanel = new JPanel(gridbag);
		JLabel xlabel = new JLabel(" x size: ");
		JLabel ylabel = new JLabel(" y size: ");
		gridbag.setConstraints(xlabel, constraints);
		sizePanel.add(xlabel);
		gridbag.setConstraints(xsize, constraints);
		sizePanel.add(xsize);
		gridbag.setConstraints(build, constraints);
		sizePanel.add(build);
		constraints.gridwidth = GridBagConstraints.REMAINDER;
		gridbag.setConstraints(rebuild, constraints);
		sizePanel.add(rebuild);
		constraints.gridwidth = 1;
		gridbag.setConstraints(ylabel, constraints);
		sizePanel.add(ylabel);
		gridbag.setConstraints(ysize, constraints);
		sizePanel.add(ysize);
		gridbag.setConstraints(load, constraints);
		sizePanel.add(load);
		sizePanel.setBackground(metalColor);
		JLabel buildTitle = new JLabel("Create an Environment and Agent",0);
		buildTitle.setFont(new Font("SansSerif", Font.ITALIC+Font.BOLD, 14));

		gridbag = new GridBagLayout();
		constraints = new GridBagConstraints();
		constraints.gridwidth = 1;
		constraints.fill = GridBagConstraints.BOTH;
		constraints.insets = new Insets(2,2,2,2);
		northCenterPanel = new JPanel(gridbag);
    	northCenterPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createRaisedBevelBorder(), BorderFactory.createLoweredBevelBorder()));
		northCenterPanel.setPreferredSize(new Dimension(750, 75));
		northCenterPanel.setBackground(metalColor);
		gridbag.setConstraints(buildTitle, constraints);
		northCenterPanel.add(buildTitle);
		gridbag.setConstraints(choicePanel, constraints);
		northCenterPanel.add(choicePanel);
		gridbag.setConstraints(sizePanel, constraints);
		northCenterPanel.add(sizePanel);
		northPanel.add(northCenterPanel, BorderLayout.CENTER);
		northPanel.setVisible(false);
		northPanel.setVisible(true);

		remove(centerPanel);
    	centerPanel = new JPanel();
    	centerPanel.setPreferredSize(new Dimension(750, 400));
    	centerPanel.setBackground(Color.white);
    	centerPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createRaisedBevelBorder(), BorderFactory.createLoweredBevelBorder()));
    	centerPanel.setLayout(new BorderLayout());
    	add(centerPanel);
		centerPanel.setVisible(false);
		centerPanel.setVisible(true);
    }

    /**
     * Invoked when o estado de um item em um JComboBox muda.
     * Usado para atualizar as opções de agente e as configurações de tamanho
     * com base no tipo de ambiente selecionado.
     *
     * @param e O evento de mudança de item.
     */
    public void itemStateChanged(ItemEvent e) {
    	String world = (String)envs.getSelectedItem();
    	if (world.equals("Vacuum World")) {
    		changeAgentChoices(vacuumStrings);
    		xsize.setText("8");
    		ysize.setText("8");
    		load.setEnabled(false);
    		rebuild.setEnabled(false);
    		randomSizes.setSelected(true); // Default para Vacuum World pode ser random
    	}
    	else if (world.equals("Wumpus World")) {
    		changeAgentChoices(wumpusStrings);
    		xsize.setText("4");
    		ysize.setText("4");
    		load.setEnabled(true);
    		if(isLoaded) rebuild.setEnabled(true);
    		randomSizes.setSelected(false); // Default para Wumpus World pode ser fixo
    	}
    }

    /**
     * Constrói e exibe um ambiente de simulação com um único agente.
     * O tipo de ambiente e agente são determinados pelas seleções atuais na GUI.
     */
    private void build() {
    	Agent[] a = new Agent[1];
    	a[0] = createAgent((String)agents.getSelectedItem());
    	GridEnvironment world;
    	if (envs.getSelectedItem() ==  "Vacuum World") {
    		int x = Integer.parseInt(xsize.getText());
        	int y = Integer.parseInt(ysize.getText());
    		world = new VacuumWorld(a, x, y, .4, holder); // Probabilidade de sujeira fixa em 0.4
    		holder.setTitle("Artificial Intelligence - Agents and Environments - Vacuum World");
    	}
    	else { // Wumpus World
    		int x = 4; // Tamanho fixo para Wumpus World quando não carregado
    		int y = 4;
    		world = new WumpusWorld(a, x, y, holder);
    		holder.setTitle("Artificial Intelligence - Agents and Environments - Wumpus World");
    	}
    	// Atualiza o painel central para exibir o novo ambiente
    	remove(centerPanel);
    	centerPanel = new JPanel();
    	centerPanel.setPreferredSize(new Dimension(750, 400));
		centerPanel.setBackground(Color.white);
		centerPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createRaisedBevelBorder(), BorderFactory.createLoweredBevelBorder()));
    	centerPanel.setLayout(new BorderLayout());
    	JPanel center2 = new JPanel(new BorderLayout());
    	center2.add(world.canvas, BorderLayout.CENTER);
		centerPanel.add(center2, BorderLayout.CENTER);
		centerPanel.add("North",world.gridPanel);
		add(centerPanel);
		setVisible(false); // Refresca o painel
		setVisible(true);
    }

    /**
     * Inicia a execução de múltiplos trials (rodadas de simulação) para os agentes selecionados.
     * Os trials são executados em uma thread separada para não bloquear a interface.
     */
    private void trials() {
        // Desabilita botões para evitar interferência durante os trials
    	trials.setEnabled(false);
    	envs.setEnabled(false);

        // Inicia o TrialSet em uma nova thread para não travar a UI
    	TrialSet s = new TrialSet();
    	s.start();
    }

    /**
     * Carrega uma configuração de mundo Wumpus de um arquivo.
     * O usuário é solicitado a selecionar um arquivo ".ww" (Wumpus World).
     * Após o carregamento, o ambiente é exibido no painel central.
     */
    private void load() {
    	Agent[] a = new Agent[1];
    	a[0] = createAgent((String)agents.getSelectedItem());
    	GridEnvironment world;
    	int x = Integer.parseInt(xsize.getText()); // Obtém tamanho X do campo de texto
    	int y = Integer.parseInt(ysize.getText()); // Obtém tamanho Y do campo de texto
    	boolean gotFile = getFileName(); // Abre o seletor de arquivo
    	if(gotFile) { // Se um arquivo foi selecionado com sucesso
    		world = new WumpusWorld(a, x, y, holder, filepath); // Cria o mundo Wumpus a partir do arquivo
    		holder.setTitle("Artificial Intelligence - Agents and Environments - Wumpus World");
    		rebuild.setEnabled(true); // Habilita o botão de reconstruir
    		isLoaded = true; // Define a flag de mundo carregado
    		// Atualiza o painel central para exibir o mundo carregado
    		remove(centerPanel);
    		centerPanel = new JPanel();
    		centerPanel.setPreferredSize(new Dimension(750, 400));
    		centerPanel.setBackground(Color.white);
    		centerPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createRaisedBevelBorder(), BorderFactory.createLoweredBevelBorder()));
    		centerPanel.setLayout(new BorderLayout());
    		JPanel center2 = new JPanel(new BorderLayout());
    		center2.add(world.canvas, BorderLayout.CENTER);
    		centerPanel.add(center2, BorderLayout.CENTER);
    		centerPanel.add("North",world.gridPanel);
    		add(centerPanel);
    		setVisible(false); // Refresca o painel
    		setVisible(true);
    	}
    }

    /**
     * Reconstrói o mundo Wumpus atualmente carregado a partir do mesmo arquivo.
     * Isso é útil para reiniciar a simulação no mesmo ambiente sem precisar recarregar o arquivo.
     */
    private void rebuild() {
    	Agent[] a = new Agent[1];
    	a[0] = createAgent((String)agents.getSelectedItem());
    	int x = Integer.parseInt(xsize.getText());
    	int y = Integer.parseInt(ysize.getText());
    	// Recria o WumpusWorld usando o mesmo filepath
    	GridEnvironment world = new WumpusWorld(a, x, y, holder, filepath);
    	// Atualiza o painel central com o mundo reconstruído
    	remove(centerPanel);
		centerPanel = new JPanel();
		centerPanel.setPreferredSize(new Dimension(750, 400));
		centerPanel.setBackground(Color.white);
		centerPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createRaisedBevelBorder(), BorderFactory.createLoweredBevelBorder()));
		centerPanel.setLayout(new BorderLayout());
		JPanel center2 = new JPanel(new BorderLayout());
		center2.add(world.canvas, BorderLayout.CENTER);
		centerPanel.add(center2, BorderLayout.CENTER);
		centerPanel.add("North",world.gridPanel);
		add(centerPanel);
		setVisible(false); // Refresca o painel
		setVisible(true);
    }

    /**
     * Alterna a interface do painel para o modo de criação/edição de um mundo Wumpus.
     * Exibe o {@link WumpusWorldEditor} no painel central.
     */
    private void createWorld() {
    	simWorld.setBackground(Color.gray.brighter());
    	buildWorld.setBackground(metalColor);
    	JButton saveButton = new JButton("Click here to save your world.");
    	saveButton.setActionCommand("save");
    	saveButton.addActionListener(this);
    	northPanel.remove(northCenterPanel);
    	gridbag = new GridBagLayout();
    	northCenterPanel = new JPanel(gridbag);
    	northCenterPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createRaisedBevelBorder(), BorderFactory.createLoweredBevelBorder()));
		northCenterPanel.setPreferredSize(new Dimension(750, 75));
		northCenterPanel.add(saveButton); // Adiciona o botão de salvar
		northPanel.add(northCenterPanel, BorderLayout.CENTER); // Atualiza o painel norte
    	remove(centerPanel);
    	centerPanel = new JPanel();
    	centerPanel.setPreferredSize(new Dimension(750, 400));
    	centerPanel.setBackground(Color.white);
    	centerPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createRaisedBevelBorder(), BorderFactory.createLoweredBevelBorder()));
    	centerPanel.setLayout(new BorderLayout());
    	centerPanel.add(wwe, BorderLayout.CENTER); // Adiciona o editor de mundo
    	centerPanel.add(new JLabel("Directions: Right-click in a cell you'd like to add something to.  Check what you'd like to add.  When you are satisfied with your world, save it."), BorderLayout.NORTH); // Instruções
    	add(centerPanel);
    	centerPanel.setVisible(false);
    	centerPanel.setVisible(true);
    }

    /**
     * Abre uma caixa de diálogo JFileChooser para permitir que o usuário selecione
     * um arquivo de configuração de mundo Wumpus (.ww).
     *
     * @return {@code true} se um arquivo válido foi selecionado, {@code false} caso contrário.
     */
    private boolean getFileName() {
    	try {
			JFileChooser chooser = new JFileChooser();
			ExtensionFileFilter filter = new ExtensionFileFilter();
			filter.addExtension("ww"); // Filtra por arquivos .ww
		    filter.setDescription("Wumpus World");
			chooser.setFileFilter(filter);
			int returnVal = chooser.showOpenDialog(this);
			if (returnVal == JFileChooser.APPROVE_OPTION) { // Se o usuário aprovou a seleção
				filepath = chooser.getSelectedFile().getPath(); // Salva o caminho completo
				filename = chooser.getSelectedFile().getName(); // Salva o nome do arquivo
				if (filename.endsWith(".ww")) { // Confirma a extensão
					return true;
				}
			}
		} catch(Exception ex) {
            // Em caso de erro (e.g., permissão negada, arquivo inválido), pode-se logar ou notificar o usuário
            System.err.println("Erro ao obter nome do arquivo: " + ex.getMessage());
        }
		return false; // Retorna falso se nenhum arquivo válido foi selecionado ou ocorreu um erro
    }

    /**
     * Atualiza as opções de agentes disponíveis nos JComboBoxes e JList de seleção
     * com base no tipo de mundo selecionado.
     *
     * @param agentName Um array de Strings contendo os nomes dos agentes válidos para o mundo atual.
     */
    private void changeAgentChoices(String[] agentName)
    {
	    agents.removeAllItems(); // Remove todos os itens existentes
	    trialAgents.removeAll(); // Limpa a JList de trial agents
	    trialAgents.setListData(agentName); // Define os novos dados para a JList
	    for (int i=0; i<agentName.length; i++) {
	        agents.addItem(agentName[i]); // Adiciona os novos itens ao JComboBox
	    }
    }

    /**
     * Cria e retorna uma nova instância de um agente com base no nome fornecido.
     * Mapeia os nomes das Strings para as classes de agente correspondentes.
     *
     * @param agentName A String que representa o nome do agente a ser criado.
     * @return Uma nova instância do {@link ai.worlds.Agent} correspondente, ou um
     * {@link ai.worlds.wumpus.AimlessWumpusAgent} como fallback.
     */
    private Agent createAgent(String agentName) {
    	if (agentName.equals("Random Vacuum Agent")) return new RandomVacuumAgent();
    	else if (agentName.equals("Reactive Vacuum Agent")) return new ReactiveVacuumAgent();
    	else if (agentName.equals("Random Wumpus Agent")) return new RandomWumpusAgent();
    	else if (agentName.equals("Logic Testing Agent")) return new LogicTestingAgent();
    	else if (agentName.equals("HilbertAgenteReativoVacuumAgent")) {
    	    return new HilbertAgenteReativoVacuumAgent();
    	}
    	else if (agentName.equals("HilbertAgenteModeloVacuumAgent")) {
    	    return new HilbertAgenteModeloVacuumAgent();
    	}
    	else  return new AimlessWumpusAgent(); // Fallback para WumpusWorld
    }

    /**
     * Uma classe interna que estende {@link java.lang.Thread} para executar
     * múltiplos testes (trials) com agentes em ambientes simulados em segundo plano.
     * Coleta e exibe os resultados médios dos testes.
     */
    private class TrialSet extends Thread {
        /**
         * Mapa para acumular os scores totais do "Score Proposto 1 (Limpeza - Movimentos)"
         * para cada tipo de agente durante os trials.
         */
        private Map<String, Integer> totalScoresProposed1 = new HashMap<>();
        /**
         * Mapa para acumular a contagem total de movimentos para cada tipo de agente
         * durante os trials.
         */
        private Map<String, Integer> totalMoveCounts = new HashMap<>();


    	/**
    	 * O método `run()` é o ponto de entrada para a execução da thread {@code TrialSet}.
    	 * Ele coordena a execução de múltiplas rodadas de simulação para os agentes selecionados,
    	 * coleta os resultados e os exibe na interface e no console.
    	 */
    	public void run() {
            // Limpa o JTextArea no início e exibe uma mensagem de inicialização
            results.setText("");
            results.append("Iniciando Testes...\n\n");
            System.out.println("DEBUG (TrialSet.run): Iniciando simulações de trials...");

    		Object[] agts = trialAgents.getSelectedValues(); // Obtém os agentes selecionados da JList
            if (agts == null || agts.length == 0) {
                results.append("Nenhum agente selecionado para os testes.\n");
                trials.setEnabled(true);
                envs.setEnabled(true);
                System.out.println("DEBUG (TrialSet.run): Nenhum agente selecionado, retornando.");
                return;
            }

            // Inicializa os mapas de totais de score e movimentos para cada agente
            for (Object agtObj : agts) {
                String agentName = (String) agtObj;
                totalScoresProposed1.put(agentName, 0);
                totalMoveCounts.put(agentName, 0);
                System.out.println("DEBUG (TrialSet.run): Inicializando mapas de scores para o agente: " + agentName);
            }

            int num_trials = Integer.parseInt(numTrials.getText()); // Obtém o número total de trials
            System.out.println("DEBUG (TrialSet.run): Número TOTAL de trials lido do campo: " + num_trials);

    		for (int j=0; j<num_trials; j++) { // Loop para cada trial
                System.out.println("DEBUG (TrialSet.run): >>> Iniciando Rodada " + (j + 1) + " de " + num_trials + " <<<");

    			int x = Integer.parseInt(xsize.getText()); // Tamanho X inicial
    			int y = Integer.parseInt(ysize.getText()); // Tamanho Y inicial

    			if (randomSizes.isSelected()){ // Se "Random Sizes" estiver selecionado, gera tamanhos aleatórios
    				x = (int) (Math.random()*18 + 2); // Tamanho X entre 2 e 19
    				y = (int) (Math.random()*18 + 2); // Tamanho Y entre 2 e 19
                    System.out.println("DEBUG (TrialSet.run): Tamanhos aleatórios para rodada " + (j+1) + ": X=" + x + ", Y=" + y);
    			} else {
                    System.out.println("DEBUG (TrialSet.run): Tamanhos fixos para rodada " + (j+1) + ": X=" + x + ", Y=" + y);
                }

                GridEnvironment baseWorld;
                // Cria um agente temporário apenas para instanciar o mundo base (necessário para o construtor do ambiente)
                Agent tempAgentForBaseWorld = createAgent((String)agts[0]);

                if (envs.getSelectedItem().equals("Vacuum World")) {
                    // Cria um VacuumWorld com probabilidade de sujeira de 0.25 para os trials
                    baseWorld = new VacuumWorld(new Agent[]{tempAgentForBaseWorld}, x, y, .25, holder);
                    System.out.println("DEBUG (TrialSet.run): Criado VacuumWorld base para rodada " + (j+1));
                } else {
                    // Cria um WumpusWorld (sem carregar de arquivo para trials simples)
                    baseWorld = new WumpusWorld(new Agent[]{tempAgentForBaseWorld}, x, y, holder);
                    System.out.println("DEBUG (TrialSet.run): Criado WumpusWorld base para rodada " + (j+1));
                }

                // Itera sobre cada agente selecionado para executar a simulação na mesma configuração de mundo
    			for (int i=0; i<agts.length; i++) {
    				String agentName = (String)agts[i];
    				Agent currentAgent = createAgent(agentName); // Cria uma nova instância do agente para cada rodada/tipo
                    Agent[] singleAgentArray = {currentAgent}; // Array com o único agente para o mundo

                    GridEnvironment worldForAgent;
                    if (baseWorld instanceof VacuumWorld) {
                        // Para VacuumWorld, cria uma nova instância para cada agente para garantir um estado inicial "limpo"
                        worldForAgent = new VacuumWorld(singleAgentArray, x, y, .25, holder);
                        // Copia a configuração inicial do grid (disposição da sujeira) do mundo base
                        // O +2 é para considerar as bordas adicionadas internamente pelo GridEnvironment
                        GridEnvironment.copyGrid(baseWorld, worldForAgent, x + 2, y + 2);
                        System.out.println("DEBUG (TrialSet.run): Criado VacuumWorld para agente " + agentName + " na rodada " + (j+1));
                    } else { // WumpusWorld
                        worldForAgent = new WumpusWorld(singleAgentArray, x, y, holder);
                        // Para WumpusWorld, copia a estrutura interna (poços, wumpus, ouro) do mundo base
                        if (baseWorld instanceof WumpusWorld && worldForAgent instanceof WumpusWorld) {
                             ((WumpusWorld)worldForAgent).w = ((WumpusWorld)baseWorld).w; // Copia o objeto WumpusWorldData
                        }
                        System.out.println("DEBUG (TrialSet.run): Criado WumpusWorld para agente " + agentName + " na rodada " + (j+1));
                    }

    				worldForAgent.step = 0; // Reseta o contador de passos para a simulação atual
    				worldForAgent.agents = singleAgentArray; // Garante que apenas o agente atual está ativo
    				worldForAgent.display = false; // Desativa a exibição da GUI durante os trials para melhor desempenho

                    System.out.println("DEBUG (TrialSet.run): Rodando worldForAgent.run() para " + agentName + " na rodada " + (j+1) + "...");
    				worldForAgent.run(); // Executa a simulação do agente no ambiente

    				if (worldForAgent instanceof VacuumWorld) {
    	                // Para VacuumWorld, obtemos o score e a contagem de movimentos do agente
    	                int scoreProp1 = worldForAgent.performanceMeasure(currentAgent);
    	                int agentMoveCount = currentAgent.getMoveCount();

    	                System.out.println("DEBUG (TrialSet.run): SCORES Agente: " + agentName + " | Score Proposto 1 = " + scoreProp1 + " | Movimentos = " + agentMoveCount);

    	                totalScoresProposed1.put(agentName, totalScoresProposed1.get(agentName) + scoreProp1);
    	                totalMoveCounts.put(agentName, totalMoveCounts.get(agentName) + agentMoveCount);
                        System.out.println("DEBUG (TrialSet.run): Scores acumulados para VacuumWorld (Total Parcial): " + agentName + ", score: " + totalScoresProposed1.get(agentName) + ", Mov: " + totalMoveCounts.get(agentName));

                    } else { // Se for WumpusWorld, um score diferente pode ser coletado se aplicável
                       // Atualmente, não há coleta de score específica para WumpusWorld neste bloco,
                       // mas o framework permite a implementação de performanceMeasure() na classe WumpusWorld.
                    }
    			}
                System.out.println("DEBUG (TrialSet.run): <<< FIM da Rodada " + (j + 1) + " >>>");
    		}

            // --- IMPRIME OS RESULTADOS MÉDIOS NO CONSOLE APÓS TODAS AS TRIALS ---
            System.out.println("\n--- DEBUG (TrialSet.run): INICIANDO IMPRESSÃO DOS RESULTADOS FINAIS NO CONSOLE ---");
            System.out.println("\n--- Resultados Médios Finais dos Testes (" + num_trials + " Rodadas) ---");
            for (Object agtObj : agts) {
                String agentName = (String) agtObj;
                System.out.println("DEBUG (TrialSet.run): Processando agente para impressão final: " + agentName);
                
                // Verifica se o agente tem dados acumulados de scores propostos e movimentos (principalmente para VacuumWorld)
                if (totalScoresProposed1.containsKey(agentName)) {
                    
                    System.out.println(String.format("Agente: %s", agentName));

                    // Imprime scores propostos e movimentos apenas se for Vacuum World, onde eles são aplicáveis
                    if (envs.getSelectedItem().equals("Vacuum World")) {
                         double avgProp1 = (double) totalScoresProposed1.get(agentName) / num_trials;
                         double avgMoveCounts = (double) totalMoveCounts.get(agentName) / num_trials;
                         System.out.println(String.format("  Média Score Proposto 1 (Limpeza - Movimentos): %.2f", avgProp1));
                         System.out.println(String.format("  Média Movimentos: %.2f", avgMoveCounts));
                    }
                    System.out.println("------------------------------------");
                } else {
                    System.out.println(String.format("Agente: %s (Nenhum dado de score disponível para o ambiente selecionado)", agentName));
                    System.out.println("------------------------------------");
                }
            }
            // --- FIM DA IMPRESSÃO NO CONSOLE ---


    		// Atualiza o JTextArea na interface (isso já estava funcionando)
            System.out.println("DEBUG (TrialSet.run): Atualizando JTextArea na interface.");
    		p.remove(scroll); // Remove e readiciona o scroll para forçar a atualização visual
            results.setText("--- Resultados Médios dos Testes (" + num_trials + " Rodadas) ---\n\n");
            for (Object agtObj : agts) {
                String agentName = (String) agtObj;
                if (totalScoresProposed1.containsKey(agentName)) { // Verifica novamente a presença de dados para o agente
                    results.append("Agente: " + agentName + "\n");
                    
                    if (envs.getSelectedItem().equals("Vacuum World")) {
                        double avgProp1 = (double) totalScoresProposed1.get(agentName) / num_trials;
                        double avgMoveCounts = (double) totalMoveCounts.get(agentName) / num_trials;
                        results.append(String.format("  Média Score Proposto 1 (Limpeza - Movimentos): %.2f\n", avgProp1));
                        results.append(String.format("  Média Movimentos: %.2f\n", avgMoveCounts));
                    }
                    results.append("\n");
                } else {
                    results.append("Agente: " + agentName + " (Nenhum dado de score disponível para o ambiente selecionado)\n\n");
                }
            }

    		trials.setEnabled(true); // Reabilita os botões da GUI
    		envs.setEnabled(true);
    		p.add(scroll); // Readiciona o scroll pane
    		p.setVisible(false); // Força um repaint do painel de resultados
    		p.setVisible(true);
            System.out.println("DEBUG (TrialSet.run): TrialSet.run() finalizado.");
    	}
    }
}