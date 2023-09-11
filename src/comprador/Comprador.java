package comprador;

import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.core.AID;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.DFService;
import jade.domain.FIPAException;
import behaviours.RequestPerformer;

public class Comprador extends Agent {
    private String titulo;
    private AID[] vendedores;
    private Comprador esteAgente = this;

    protected void setup() {
        System.out.println("Hola, soy el agente comprador: " + getAID().getName());

        Object[] args = getArguments();

        if(args != null && args.length > 0) {
            titulo = (String)args[0];
            addBehaviour(new TickerBehaviour(this, 10000) {
                protected void onTick() {
                    System.out.println("Intentado comprar el libro: " + titulo);

                    DFAgentDescription template = new DFAgentDescription();
                    ServiceDescription sd = new ServiceDescription();
                    sd.setType("venta-de-libros");
                    template.addServices(sd);

                    try {
                        DFAgentDescription[] resultado = DFService.search(myAgent, template);
                        System.out.println("Se encontraron los siguientes agentes vendedores: ");
                        vendedores = new AID[resultado.length];
                        for(int i = 0; i < resultado.length; i++) {
                            vendedores[i] = resultado[i].getName();
                            System.out.println(vendedores[i].getName());
                        }

                    }catch(FIPAException fe) {
                        fe.printStackTrace();
                    }

                    myAgent.addBehaviour(new RequestPerformer(esteAgente));
                }
            });
        } else {
            System.out.println("No se ha especificado un titulo que comprar");
            doDelete();
        }
    }

    protected void takeDown() {
        System.out.println("Finalizando agente comprador " + getAID().getName());
    }

    public AID[] getVendedores() {
        return vendedores;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getTitulo() {
        return titulo;
    }
}
