package edu.fsadriann.server.model.ticket;

import java.io.Serializable;

public class Customer implements Serializable {

  private static final long serialVersionUID = 2L;

  private String id;
  private String names;

  public Customer(String id, String names) {
    this.id = id;
    this.names = names;
  }

  public String getId() {
    return id;
  }

  public String getNames() {
    return names;
  }

  public void setNames(String names) {
    this.names = names;
  }
}
