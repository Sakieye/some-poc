package com.sakieye.api;

public class ApiFactory {

  private final IDriverApi driverApi;
  private final ICarApi carApi;

  // production
  private ApiFactory() {
    this(new DriverApi(), new CarApi());
  }

  // test
  ApiFactory(IDriverApi driverApi, ICarApi carApi) {
    this.driverApi = driverApi;
    this.carApi = carApi;
  }

  public static ApiFactory getInstance() {
    return InstanceHolder.INSTANCE;
  }

  public IDriverApi getDriverApi() {
    return driverApi;
  }

  public ICarApi getCarApi() {
    return carApi;
  }

  private static final class InstanceHolder {

    static final ApiFactory INSTANCE = new ApiFactory();
  }
}
