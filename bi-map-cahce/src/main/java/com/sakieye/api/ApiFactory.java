package com.sakieye.api;

public class ApiFactory {

  private final DriverApi driverApi;
  private final CarApi carApi;

  // production
  private ApiFactory() {
    this(new FmsDriverApi(), new FmsCarApi());
  }

  // test
  ApiFactory(DriverApi driverApi, CarApi carApi) {
    this.driverApi = driverApi;
    this.carApi = carApi;
  }

  public static ApiFactory getInstance() {
    return InstanceHolder.INSTANCE;
  }

  public DriverApi getDriverApi() {
    return driverApi;
  }

  public CarApi getCarApi() {
    return carApi;
  }

  private static final class InstanceHolder {

    static final ApiFactory INSTANCE = new ApiFactory();
  }
}
