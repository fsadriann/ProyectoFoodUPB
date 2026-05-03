package edu.fsadriann.server.model.history;

import edu.fsadriann.app.linkedlist.singly.singly.LinkedList;
import edu.fsadriann.app.stack.list.Stack;
import edu.fsadriann.server.model.observer.Subject;

public class History extends Subject {

  private Stack<Action> actions;

  public History() {
    this.actions = new Stack<>(new LinkedList<>());
  }

  public void addAction(String description) {
    this.actions.push(new Action(description));
    this.notifyObservers();
  }

  public String getLastAction() {
    if (actions.isEmpty()) {
      return "No actions yet.";
    }
    Action lastAction = actions.peek();
    return lastAction.getTimestamp() + ": " + lastAction.getDescription();
  }
}
