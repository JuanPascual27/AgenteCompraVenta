package behaviours;

import gui.BookBuyerGui;
import jade.core.behaviours.Behaviour;
import jade.core.AID;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.ACLMessage;
import comprador.Comprador;

import java.awt.*;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

public class RequestPerformer extends Behaviour {
    private AID mejorVendedor;
    private int mejorPrecio;
    private int respuestas = 0;
    private MessageTemplate mt;
    private int estado = 0;
    private Comprador agenteComprador;
    private String titulo;
    private JTable vendedores;
    private BookBuyerGui bBG;

    public RequestPerformer(Comprador a, JTable res, BookBuyerGui bBG) {
        agenteComprador = a;
        titulo = a.getTitulo();
        vendedores = res;
        this.bBG = bBG;
    }

    public void action() {
        switch(estado) {
            case 0:
                ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
                for(int i = 0; i < agenteComprador.getVendedores().length; i++) {
                    cfp.addReceiver(agenteComprador.getVendedores()[i]);
                }

                cfp.setContent(titulo);
                cfp.setConversationId("transaccion-de-libro");
                cfp.setReplyWith("cfp" + System.currentTimeMillis());
                myAgent.send(cfp);

                mt = MessageTemplate.and(MessageTemplate.MatchConversationId("transaccion-de-libro"),
                        MessageTemplate.MatchInReplyTo(cfp.getReplyWith()));
                estado = 1;
                break;

            case 1:
                ACLMessage reply = agenteComprador.receive(mt);
                if(reply != null) {
                    if(reply.getPerformative() == ACLMessage.PROPOSE) {
                        int price = Integer.parseInt(reply.getContent());
                        comprobarAgente(reply.getSender().getName(), price);
                        if(mejorVendedor == null || price < mejorPrecio) {
                            mejorPrecio = price;
                            mejorVendedor = reply.getSender();
                        }
                    }
                    respuestas++;
                    if(respuestas >= agenteComprador.getVendedores().length) {
                        estado = 2;
                    }
                } else {
                    block();
                }
                break;

            case 2:
                ACLMessage order = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
                order.addReceiver(mejorVendedor);
                order.setContent(titulo);
                order.setConversationId("transaccion-de-libro");
                order.setReplyWith("order" + System.currentTimeMillis());
                agenteComprador.send(order);

                mt = MessageTemplate.and(MessageTemplate.MatchConversationId("transaccion-de-libro"),
                        MessageTemplate.MatchInReplyTo(order.getReplyWith()));

                estado = 3;

                break;

            case 3:
                reply = myAgent.receive(mt);
                if (reply != null) {
                    if (reply.getPerformative() == ACLMessage.INFORM) {
                        marcarComprado(reply.getSender().getName());
                        JOptionPane.showMessageDialog(bBG, titulo +" comprado satisfactoriamente al agente "
                                + reply.getSender().getName() + "\n Precio: " + mejorPrecio, agenteComprador.getLocalName(), JOptionPane.INFORMATION_MESSAGE);
                        System.out.println(titulo +" comprado satisfactoriamente al agente " + reply.getSender().getName());
                        System.out.println("Precio = "+ mejorPrecio);
                        myAgent.doDelete();
                    }
                    else {
                        System.out.println("Intento fallido: el libro solicitado ya se ha vendido.");
                    }

                    estado = 4;
                }
                else {
                    block();
                }
                break;
        }
    }

    public boolean done() {
        if (estado == 2 && mejorVendedor == null) {
            System.out.println("Intento fallido: "+ titulo +" no disponible para venta");
        }
        return ((estado == 2 && mejorVendedor == null) || estado == 4);
    }

    public void comprobarAgente(String sender, int precio) {
        DefaultTableModel modelo = (DefaultTableModel) vendedores.getModel();
        Object[] nuevaFila = {sender, precio};
        boolean filaExistente = false;
        for (int i = 0; i < modelo.getRowCount(); i++) {
            if (modelo.getValueAt(i, 0).equals(sender)) {
                modelo.setValueAt(precio, i, 1);
                filaExistente = true;
                break;
            }
        }

        if (!filaExistente) {
            Object[] row = new Object[2];
            row[0] = sender;
            row[1] = precio;
            ((DefaultTableModel) vendedores.getModel()).addRow(row);
        }
    }

    public void marcarComprado(String comprado) {
        DefaultTableModel modelo = (DefaultTableModel) vendedores.getModel();
        int i = 0;
        while(i < modelo.getRowCount()) {
            if (modelo.getValueAt(i, 0).equals(comprado))
                break;
            i++;
        }
        final int x = i;
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (row == x)
                    c.setBackground(Color.GREEN);
                else
                    c.setBackground(table.getBackground());
                return c;
            }
        };
        vendedores.setDefaultRenderer(Object.class, renderer);
        bBG.repaint();
    }
}
