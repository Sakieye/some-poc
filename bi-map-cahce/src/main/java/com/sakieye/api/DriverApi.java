package com.sakieye.api;

import com.sakieye.Organization;
import java.util.stream.Stream;

public interface DriverApi {

  Stream<Driver> getDrivers(Organization org);
}
