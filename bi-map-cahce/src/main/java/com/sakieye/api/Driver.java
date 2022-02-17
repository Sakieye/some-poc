package com.sakieye.api;

import java.util.Objects;

public class Driver {

  private final int driverId;
  private final String driverName;

  public Driver(int driverId, String driverName) {
    this.driverId = driverId;
    this.driverName = driverName;
  }

  public int getDriverId() {
    return driverId;
  }

  public String getDriverName() {
    return driverName;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Driver driver = (Driver) o;
    return driverId == driver.driverId;
  }

  @Override
  public int hashCode() {
    return Objects.hash(driverId);
  }
}
