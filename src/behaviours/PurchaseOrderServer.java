package behaviours;

import gui.BookSellerGui;
import jade.core.behaviours.CyclicBehaviour;
import vendedor.Vendedor;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.ACLMessage;

import javax.swing.*;

public class PurchaseOrderServer extends CyclicBehaviour {
    Vendedor agenteVendedor;
    BookSellerGui bSG;

    public PurchaseOrderServer(Vendedor a, BookSellerGui bSG) {
        agenteVendedor = a;
        this.bSG = bSG;
    }

    public void action() {
        MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL);
        ACLMessage mensaje = agenteVendedor.receive(mt);

        if(mensaje != null) {
            String titulo = mensaje.getContent();
            ACLMessage respuesta = mensaje.createReply();

            Integer precio = (Integer)agenteVendedor.getCatalogo().remove(titulo);
            if(precio != null) {
                respuesta.setPerformative(ACLMessage.INFORM);
                JOptionPane.showMessageDialog(bSG, titulo + " vendido al agente "
                        + mensaje.getSender().getName(), agenteVendedor.getLocalName(), JOptionPane.INFORMATION_MESSAGE);
                System.out.println(titulo + " vendido al agente " + mensaje.getSender().getName());
            } else {
                respuesta.setPerformative(ACLMessage.FAILURE);
                respuesta.setContent("no-disponible");
            }
            agenteVendedor.send(respuesta);
        } else {
            block();
        }
    }
}
