package gui;

import javax.swing.*;

import comprador.Comprador;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class BookBuyerGui extends JFrame {
    private Comprador miAgente;
    private JTextField titulo;
    private JTable resultados;

    public BookBuyerGui(Comprador a) {
        super(a.getLocalName());

        miAgente = a;

        JPanel p = new JPanel();
        p.setLayout(new GridLayout(4, 1));
        p.add(new JLabel("Titulo del libro a comprar:"));
        titulo = new JTextField(30);
        p.add(titulo);
        p.add(new JLabel("Resultados:"));
        getContentPane().add(p, BorderLayout.CENTER);

        JButton addButton = new JButton("Buscar y comprar");
        addButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                try {
                    String title = titulo.getText().trim();

                    miAgente.actualizarCatalogo(title, Integer.parseInt(titulo));
                    titulo.setText("");
                    priceField.setText("");
                }catch(Exception e) {
                    JOptionPane.showMessageDialog(BookSellerGui.this, "Invalid values","Error",JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        p = new JPanel();
        p.add(addButton);
        getContentPane().add(p, BorderLayout.SOUTH);

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                myAgent.doDelete();
            }
        });

        setResizable(false);
    }

    public void showGui() {
        pack();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int centerX = (int)screenSize.getWidth() / 2;
        int centerY = (int)screenSize.getHeight() / 2;

        setLocation(centerX - getWidth() / 2, centerY - getHeight() / 2);
        super.setVisible(true);
    }
}
