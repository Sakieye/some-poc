package com.sakieye.api;

import com.sakieye.Organization;
import java.util.stream.Stream;

public class FmsDriverApi implements DriverApi {

  protected FmsDriverApi() {}

  @Override
  public Stream<Driver> getDrivers(Organization org) {
    System.out.println("real driver api");
    return null;
  }
}
