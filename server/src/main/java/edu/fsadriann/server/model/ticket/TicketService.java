package edu.fsadriann.server.model.ticket;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class TicketService extends UnicastRemoteObject implements TicketInterface {
  private int index = 0;
  private Ticket[] tickets = new Ticket[100];

  public TicketService() throws RemoteException {
    super();
  }

  @Override
  public Ticket register(Ticket ticket) throws RemoteException {
    Ticket newTicket = new Ticket(String.valueOf(index + 1), new Customer("1", ticket.getCustomerName()));
    tickets[index] = newTicket;
    index++;
    return newTicket;
  }

  @Override
  public boolean validate(Ticket ticket) throws RemoteException {
    for (int i = 0; i < index; i++) {
      if (tickets[i].equals(ticket)) {
        return true;
      }
    }
    return false;
  }
}
