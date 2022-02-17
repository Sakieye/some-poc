package com.sakieye.cache.field;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sakieye.Organization;
import com.sakieye.api.ApiFactory;
import com.sakieye.api.Driver;
import com.sakieye.api.IDriverApi;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FieldDataCacheTest {
  private final IDriverApi driverApi = Mockito.mock(IDriverApi.class);
  private final ApiFactory apiFactory = Mockito.mock(ApiFactory.class);
  private final FieldDataCache fieldDataCache = new FieldDataCache(apiFactory);

  private final ExecutorService pool = Executors.newFixedThreadPool(8);
  private final int org1SleepMs = 1;
  private final int org2SleepMs = 10;
  private final Organization org1 = new Organization(org1SleepMs);
  private final Organization org2 = new Organization(org2SleepMs);
  private final int driverId1 = 1;
  private final int driverId2 = 2;
  private final String driverName1 = "driverName1";
  private final String driverName2 = "driverName2";

  private final int queryCount = 1000;
  private final AtomicLong org1timer = new AtomicLong();
  private final AtomicLong org2timer = new AtomicLong();

  @BeforeEach
  void setUp() {
    fieldDataCache.invalidate(org1);
    fieldDataCache.invalidate(org2);
    org1timer.set(0L);
    org2timer.set(0L);
    mockApiFactory(org1timer, org2timer);
  }

  @RepeatedTest(10)
  void toId() {
    List<CompletableFuture<Integer>> futures = new ArrayList<>();
    for (int i = 0; i < queryCount; i++) {
      CompletableFuture<Integer> future1 =
          CompletableFuture.supplyAsync(
              () -> {
                try {
                  return fieldDataCache.toId(FieldSource.DRIVER, driverName1, org1);
                } catch (Exception e) {
                  e.printStackTrace();
                }
                return null;
              },
              pool);
      CompletableFuture<Integer> future2 =
          CompletableFuture.supplyAsync(
              () -> {
                try {
                  return fieldDataCache.toId(FieldSource.DRIVER, driverName2, org2);
                } catch (Exception e) {
                  e.printStackTrace();
                }
                return null;
              },
              pool);
      futures.add(future1);
      futures.add(future2);
    }

    List<Integer> driverIds =
        futures.stream().map(CompletableFuture::join).collect(Collectors.toList());

    assertEquals(queryCount * 2, driverIds.size());
    verify(driverApi, Mockito.times(2)).getDrivers(any());
    assertTrue(org1timer.get() < org2SleepMs);
    assertTrue(org2timer.get() >= org2SleepMs);
  }

  @RepeatedTest(10)
  void toName() {
    List<CompletableFuture<String>> futures = new ArrayList<>();
    for (int i = 0; i < queryCount; i++) {
      CompletableFuture<String> future1 =
          CompletableFuture.supplyAsync(
              () -> {
                try {
                  return fieldDataCache.toName(FieldSource.DRIVER, driverId1, org1);
                } catch (Exception e) {
                  e.printStackTrace();
                }
                return null;
              },
              pool);
      CompletableFuture<String> future2 =
          CompletableFuture.supplyAsync(
              () -> {
                try {
                  return fieldDataCache.toName(FieldSource.DRIVER, driverId2, org2);
                } catch (Exception e) {
                  e.printStackTrace();
                }
                return null;
              },
              pool);
      futures.add(future1);
      futures.add(future2);
    }

    List<String> driverIds =
        futures.stream().map(CompletableFuture::join).collect(Collectors.toList());

    assertEquals(queryCount * 2, driverIds.size());
    verify(driverApi, Mockito.times(2)).getDrivers(any());
    assertTrue(org1timer.get() < org2SleepMs);
    assertTrue(org2timer.get() >= org2SleepMs);
  }

  void mockApiFactory(AtomicLong org1timer, AtomicLong org2timer) {
    when(driverApi.getDrivers(any()))
        .thenAnswer(
            invocationOnMock -> {
              long start = System.currentTimeMillis();

              Organization org = invocationOnMock.getArgument(0);
              Thread.sleep(org.getOrgId());
              Driver driver;
              // must in return data, or it will throw IdNotMatchException
              if (org.getOrgId() == 1) {
                driver = new Driver(driverId1, driverName1);
                org1timer.addAndGet(System.currentTimeMillis() - start);
              } else {
                driver = new Driver(driverId2, driverName2);
                org2timer.addAndGet(System.currentTimeMillis() - start);
              }

              return Stream.of(driver);
            });
    when(apiFactory.getDriverApi()).thenReturn(driverApi);
  }
}
