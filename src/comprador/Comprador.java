package comprador;

import gui.BookBuyerGui;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.core.AID;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.DFService;
import jade.domain.FIPAException;
import behaviours.RequestPerformer;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class Comprador extends Agent {
    private String titulo;
    private AID[] vendedores;
    private Comprador esteAgente = this;
    private BookBuyerGui miGui;

    protected void setup() {
        System.out.println("Hola, soy el agente comprador: " + getAID().getName());
        miGui = new BookBuyerGui(this);
        miGui.showGui();
    }

    protected void takeDown() {
        System.out.println("Finalizando agente comprador " + getAID().getName());
    }

    public void intentarComprar(String titulo) {
        this.titulo = titulo;
        addBehaviour(new TickerBehaviour(this, 10000) {
            protected void onTick() {
                System.out.println("Intentado comprar el libro: " + titulo);

                DFAgentDescription template = new DFAgentDescription();
                ServiceDescription sd = new ServiceDescription();
                sd.setType("venta-de-libros");
                template.addServices(sd);
                JTable res = new JTable();

                try {
                    DFAgentDescription[] resultado = DFService.search(myAgent, template);
                    vendedores = new AID[resultado.length];
                    if (resultado.length == 0)
                        miGui.getSP().setViewportView(new TextArea("No se han encontrado vendedores para el libro: " + titulo));
                    else {
                        res.setModel(new javax.swing.table.DefaultTableModel(
                                new Object [][] {},
                                new String [] {"Vendedor", "Precio"}
                        ) {
                            boolean[] canEdit = new boolean [] {false, false};
                            public boolean isCellEditable(int rowIndex, int columnIndex) {
                                return canEdit [columnIndex];
                            }
                        });
                        res.getTableHeader().setReorderingAllowed(false);
                        miGui.getSP().setViewportView(res);
                        System.out.println("Se encontraron los siguientes agentes vendedores: ");
                        for (int i = 0; i < resultado.length; i++) {
                            vendedores[i] = resultado[i].getName();
                            Object[] row = new Object[2];
                            row[0] = vendedores[i].getName();
                            row[1] = "No disponible";
                            ((DefaultTableModel) res.getModel()).addRow(row);
                            System.out.println(vendedores[i].getName());
                        }
                    }
                }catch(FIPAException fe) {
                    fe.printStackTrace();
                }

                myAgent.addBehaviour(new RequestPerformer(esteAgente, res, miGui));
            }
        });
    }

    public AID[] getVendedores() {
        return vendedores;
    }

    public String getTitulo() {
        return titulo;
    }
}
