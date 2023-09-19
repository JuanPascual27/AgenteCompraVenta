package vendedor;

import jade.core.Agent;
import java.util.Hashtable;
import jade.core.behaviours.OneShotBehaviour;
import gui.BookSellerGui;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.domain.DFService;
import behaviours.OfferRequestServer;
import behaviours.PurchaseOrderServer;

public class Vendedor extends Agent {
    private Hashtable catalago;
    private BookSellerGui miGUI;

    protected void setup() {
        System.out.println("Hola soy el agente vendedor: " + getAID().getName());
        catalago = new Hashtable();
        miGUI = new BookSellerGui(this);
        miGUI.showGui();

        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());

        ServiceDescription sd = new ServiceDescription();
        sd.setType("venta-de-libros");
        sd.setName("comercio-de-libro");
        dfd.addServices(sd);

        try {
            DFService.register(this, dfd);
        }catch(FIPAException fe) {
            fe.printStackTrace();
        }

        addBehaviour(new OfferRequestServer(this));

        addBehaviour(new PurchaseOrderServer(this, miGUI));
    }

    protected void takeDown() {
        try {
            DFService.deregister(this);
        }catch(FIPAException fe) {
            fe.printStackTrace();
        }
        System.out.println("Finalizando el agente vendedor " + getAID().getName());
        miGUI.dispose();
    }

    public void actualizarCatalogo(final String titulo, final int precio) {
        addBehaviour(new OneShotBehaviour() {
            public void action() {
                catalago.put(titulo, precio);
                System.out.println(titulo + " ha sido insertado en el catalogo con el precio: " + precio);
            }
        });
    }

    public Hashtable getCatalogo() {
        return catalago;
    }
}
