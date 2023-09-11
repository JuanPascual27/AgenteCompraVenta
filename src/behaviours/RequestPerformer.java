package behaviours;

import jade.core.behaviours.Behaviour;
import jade.core.AID;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.ACLMessage;
import comprador.Comprador;

public class RequestPerformer extends Behaviour {
    private AID mejorVendedor;
    private int mejorPrecio;
    private int respuestas = 0;
    private MessageTemplate mt;
    private int estado = 0;
    private Comprador agenteComprador;
    private String titulo;

    public RequestPerformer(Comprador a) {
        agenteComprador = a;
        titulo = a.getTitulo();
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
}
