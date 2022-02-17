package com.sakieye.api;

import com.sakieye.Organization;
import java.util.stream.Stream;

public interface IDriverApi {

  Stream<Driver> getDrivers(Organization org);
}
