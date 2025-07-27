package ai.worlds;

import java.awt.*;
import javax.swing.plaf.metal.MetalLookAndFeel;

/**
 * Representa o corpo físico de um agente no ambiente de simulação.
 * O AgentBody é responsável por sua representação gráfica e por
 * registrar estados físicos como colisão (`bump`) e se está vivo (`alive`).
 *
 * @author Jill Zimmerman -- jill.zimmerman@goucher.edu
 * @version 1.0
 * @since Original
 */
public class AgentBody extends Obj
{
	/**
	 * Flag que indica se o corpo do agente está atualmente segurando (grabbed) um objeto,
	 * como sujeira ou ouro.
	 */
	public boolean grabbed = false;
	
	/**
	 * Construtor padrão para AgentBody.
	 * Inicializa o corpo do agente como "vivo".
	 */
	public AgentBody()
	{
		alive = true;
	}
	
	/**
	 * Desenha a representação gráfica do corpo do agente no ambiente.
	 * O corpo é desenhado como um quadrado 3D. Se houver colisão (`bump`),
	 * uma linha vermelha é desenhada para indicar o impacto.
	 * A orientação do agente é indicada por uma linha preta.
	 *
	 * @param g O contexto gráfico onde o corpo será desenhado.
	 * @param p O ponto (coordenadas x,y) no qual a célula do agente começa.
	 * @param cellSize O tamanho em pixels de uma única célula do grid.
	 */
	@Override // Indica que este método sobrescreve o método draw da classe pai Obj
	public void draw(Graphics g, Point p, int cellSize)
	{
	    Color metalColor = MetalLookAndFeel.getTextHighlightColor();
	    g.setColor(metalColor);
	    Point ruc; // Ponto superior direito para o retângulo do corpo
	    // Ajusta a posição de desenho se houve colisão para simular um "empurrão"
	    if (!bump)
		    ruc = new Point (p.x+cellSize/4,p.y+cellSize/4);
	    else
		    ruc = new Point (p.x+cellSize/4+heading.x*cellSize/4, // heading.x e heading.y representam a direção do movimento
				     p.y+cellSize/4-heading.y*cellSize/4); // heading.y é invertido devido ao sistema de coordenadas Y
	    g.fill3DRect(ruc.x,ruc.y,cellSize/2,cellSize/2,true); // Desenha o corpo como um retângulo 3D

	    // Desenha uma linha vermelha para indicar colisão (bump)
	    if (bump)
	    {
		    g.setColor(Color.red);
		    // Calcula as coordenadas da linha de colisão
		    int x1 = p.x + cellSize/2 + heading.x*cellSize/2 - Math.abs(heading.y)*cellSize/4;
		    int y1 = p.y + cellSize/2 - heading.y*cellSize/2 - Math.abs(heading.x)*cellSize/4;
		    int x2 = p.x + cellSize/2 + heading.x*cellSize/2 + Math.abs(heading.y)*cellSize/4;
		    int y2 = p.y + cellSize/2 - heading.y*cellSize/2 + Math.abs(heading.x)*cellSize/4;	
		    g.drawLine(x1,y1,x2,y2); // Desenha a linha
	    }
	    // Desenha a orientação do agente (um "nariz" ou seta) se o agente estiver vivo
	    if (alive) {
		    g.setColor(Color.black);
		    Point cp; // Centro do corpo
		    if(!bump)
			    cp = new Point(p.x+cellSize/2,p.y+cellSize/2);
		    else // Ajusta o centro se houver colisão
			    cp = new Point(p.x+cellSize/2+heading.x*cellSize/4,
					   p.y+cellSize/2-heading.y*cellSize/4);
		    // Calcula os pontos para desenhar o "nariz" com base na orientação
		    int x1 = cp.x + (-heading.x+1-Math.abs(heading.x))*cellSize/8;
		    int y1 = cp.y + (heading.y+1-Math.abs(heading.y))*cellSize/8;
		    int x2 = cp.x + (heading.x)*cellSize/8;
		    int y2 = cp.y + (-heading.y)*cellSize/8;
		    int x3 = cp.x + (-heading.x-1+Math.abs(heading.x))*cellSize/8; 
		    int y3 = cp.y + (heading.y-1+Math.abs(heading.y))*cellSize/8;
		    g.drawLine(x1,y1,x2,y2); // Desenha as duas linhas que formam o "nariz"
		    g.drawLine(x2,y2,x3,y3);
	    }
	}
}