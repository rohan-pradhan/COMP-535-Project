package socs.network.node;

public class Link {

  RouterDescription router1;
  RouterDescription router2;
  int linkWeight;

  public Link(RouterDescription r1, RouterDescription r2, int aLinkWeight) {
    router1 = r1;
    router2 = r2;
    linkWeight = aLinkWeight;
  }

  public Link(RouterDescription r1, RouterDescription r2) {
    router1 = r1;
    router2 = r2;
    linkWeight = 0;
  }
}
