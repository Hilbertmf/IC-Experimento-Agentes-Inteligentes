package ai.worlds.vacuum;
import java.awt.*;
import ai.worlds.*;

/**
 * Representa um objeto de sujeira (Dirt) no ambiente VacuumWorld.
 * Este objeto é renderizado graficamente como um quadrado cinza.
 *
 * @author Jill Zimmerman -- jill.zimmerman@goucher.edu
 * @version 1.0
 * @since Original
 */
public class Dirt extends Obj
{
    /**
     * Desenha a sujeira na representação gráfica do ambiente.
     * A sujeira é desenhada como um quadrado cinza preenchido.
     *
     * @param g O contexto gráfico onde a sujeira será desenhada.
     * @param p O ponto (coordenadas x,y) no qual a célula da sujeira começa.
     * @param cellSize O tamanho em pixels de uma única célula do grid.
     */
    public void draw(Graphics g, Point p, int cellSize)
    {
	    g.setColor(Color.gray); // Define a cor para cinza
	    g.fillRect(p.x+1,p.y+1,cellSize-1,cellSize-1); // Desenha um retângulo preenchido para representar a sujeira
    }
}