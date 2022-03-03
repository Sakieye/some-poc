package com.sakieye.api;

import com.sakieye.Organization;
import java.util.stream.Stream;

public class FmsCarApi implements CarApi {

  protected FmsCarApi() {}

  @Override
  public Stream<Car> getCars(Organization org) {
    System.out.println("real car api");
    return null;
  }
}
