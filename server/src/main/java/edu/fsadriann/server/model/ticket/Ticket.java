package edu.fsadriann.server.model.ticket;

import java.io.Serializable;

public class Ticket implements Serializable {

  private static final long serialVersionUID = 1L;

  private String id;
  private Customer customer;

  public Ticket(String id, Customer customer) {
    this.id = id;
    this.customer = customer;
  }

  public String getId() {
    return id;
  }

  public String getCustomerName() {
    return customer.getNames();
  }
}
