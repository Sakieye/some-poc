package com.sakieye.api;

public class Car {

  private final int carId;
  private final String plateNumber;

  public Car(int carId, String plateNumber) {
    this.carId = carId;
    this.plateNumber = plateNumber;
  }

  public int getCarId() {
    return carId;
  }

  public String getPlateNumber() {
    return plateNumber;
  }
}
