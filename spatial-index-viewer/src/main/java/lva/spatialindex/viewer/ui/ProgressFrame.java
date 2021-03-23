package lva.spatialindex.viewer.ui;

import lombok.NonNull;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;

/**
 * @author vlitvinenko
 */
public class ProgressFrame extends JFrame {
    private final JProgressBar progressBar = new JProgressBar();
    private final JLabel messageLabel = new JLabel();

    public ProgressFrame() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(7, 7, 7, 7));

        panel.add(messageLabel, BorderLayout.NORTH);
        panel.add(progressBar, BorderLayout.CENTER);

        progressBar.setStringPainted(true);

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

        setBounds(0, 0, 500, 70);
        setResizable(false);
        setTitle("Shape Viewer");

        setContentPane(panel);

        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation(dim.width / 2 - getSize().width / 2, dim.height / 2 - getSize().height / 2);
    }

    public void setProgress(int value) {
        progressBar.setValue(value);
    }

    public void setMessage(@NonNull String message) {
        messageLabel.setText(message);
    }
}
