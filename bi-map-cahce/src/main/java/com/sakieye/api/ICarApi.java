package com.sakieye.api;

import com.sakieye.Organization;
import java.util.stream.Stream;

public interface ICarApi {

  Stream<Car> getCars(Organization org);
}
