package edu.fsadriann.server.model.observer;

public abstract class Observer {

  protected Subject subject;

  protected Observer(Subject subject) {
    this.subject = subject;
    this.subject.attach(this);
  }

  public abstract void update();

}
