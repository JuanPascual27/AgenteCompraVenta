package behaviours;

import jade.core.behaviours.CyclicBehaviour;
import vendedor.Vendedor;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.ACLMessage;

public class OfferRequestServer extends CyclicBehaviour {
    Vendedor agenteVendedor;

    public OfferRequestServer(Vendedor a) {
        agenteVendedor = a;
    }

    public void action() {
        MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.CFP);
        ACLMessage mensaje = agenteVendedor.receive(mt);

        if(mensaje != null) {
            String titulo = mensaje.getContent();
            ACLMessage respuesta = mensaje.createReply();

            Integer precio = (Integer)agenteVendedor.getCatalogo().get(titulo);

            if(precio != null) {
                respuesta.setPerformative(ACLMessage.PROPOSE);
                respuesta.setContent(String.valueOf(precio.intValue()));
            } else {
                respuesta.setPerformative(ACLMessage.REFUSE);
                respuesta.setContent("no-disponible");
            }

            agenteVendedor.send(respuesta);
        } else {
            block();
        }
    }
}
