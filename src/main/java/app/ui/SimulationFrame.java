package app.ui;

import app.model.world.World;
import lombok.Getter;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import java.awt.BorderLayout;

/**
 * Окно графической визуализации симуляции.
 */
@Getter
public class SimulationFrame extends JFrame {
    private final transient WorldPanel worldPanel;
    private final transient Timer repaintTimer;

    /**
     * Создаёт окно отображения мира.
     *
     * @param world игровой мир
     */
    public SimulationFrame(World world) {
        super("Война цивилизаций");
        this.worldPanel = new WorldPanel(world);
        this.repaintTimer = new Timer(150, event -> worldPanel.repaint());

        setLayout(new BorderLayout());
        add(worldPanel, BorderLayout.CENTER);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        pack();
        setLocationRelativeTo(null);
    }

    /**
     * Показывает окно и запускает автообновление карты.
     */
    public void open() {
        SwingUtilities.invokeLater(() -> {
            setVisible(true);
            repaintTimer.start();
        });
    }

    /**
     * Останавливает визуализацию.
     */
    public void stopRendering() {
        repaintTimer.stop();
    }
}
