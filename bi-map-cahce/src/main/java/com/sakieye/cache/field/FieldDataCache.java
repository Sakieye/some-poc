package com.sakieye.cache.field;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.sakieye.Organization;
import com.sakieye.api.ApiFactory;
import com.sakieye.api.Car;
import com.sakieye.api.Driver;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class FieldDataCache {

  private final OrgFieldCache<String, Integer> carCache;
  private final OrgFieldCache<String, Integer> driverCache;

  // production
  private FieldDataCache() {
    this(ApiFactory.getInstance());
  }

  // test
  FieldDataCache(ApiFactory apiFactory) {
    this.carCache =
        new OrgFieldCache<String, Integer>() {
          @Override
          BiMap<String, Integer> getFromDbOrApi(Organization org) {
            return HashBiMap.create(
                apiFactory
                    .getCarApi()
                    .getCars(org)
                    .collect(Collectors.toMap(Car::getPlateNumber, Car::getCarId)));
          }
        };
    this.driverCache =
        new OrgFieldCache<String, Integer>() {
          @Override
          BiMap<String, Integer> getFromDbOrApi(Organization org) {
            return HashBiMap.create(
                apiFactory
                    .getDriverApi()
                    .getDrivers(org)
                    .collect(Collectors.toMap(Driver::getDriverName, Driver::getDriverId)));
          }
        };
  }

  public static FieldDataCache getInstance() {
    return InstanceHolder.INSTANCE;
  }

  public int toId(FieldSource fieldSource, String name, Organization org)
      throws IdNotMatchException, ExecutionException {
    switch (fieldSource) {
      case CAR:
        return carCache.getIdFromName(name, fieldSource, org);
      case DRIVER:
        return driverCache.getIdFromName(name, fieldSource, org);
      default:
        throw new UnsupportedOperationException("not support field source: " + fieldSource);
    }
  }

  public String toName(FieldSource fieldSource, int id, Organization org)
      throws ExecutionException {
    switch (fieldSource) {
      case CAR:
        return carCache.getNameFromId(id, org);
      case DRIVER:
        return driverCache.getNameFromId(id, org);
      default:
        throw new UnsupportedOperationException("not support field source: " + fieldSource);
    }
  }

  public void invalidate(Organization org) {
    this.driverCache.cache.invalidate(org);
    this.carCache.cache.invalidate(org);
  }

  private abstract static class OrgFieldCache<NAME, ID> {

    private final LoadingCache<Organization, BiMap<NAME, ID>> cache;

    private OrgFieldCache() {
      this.cache =
          CacheBuilder.newBuilder()
              .expireAfterWrite(5, TimeUnit.MINUTES)
              .build(
                  new CacheLoader<Organization, BiMap<NAME, ID>>() {
                    @Override
                    public BiMap<NAME, ID> load(Organization key) {
                      return getFromDbOrApi(key);
                    }
                  });
    }

    abstract BiMap<NAME, ID> getFromDbOrApi(Organization org);

    private ID getIdFromName(NAME nameKey, FieldSource fieldSource, Organization org)
        throws IdNotMatchException, ExecutionException {
      final BiMap<NAME, ID> fieldCacheMap;
      synchronized (fieldCacheMap = cache.get(org)) {
        // When the key is updated, the old cache won't contain the key, it will fetch db/api at
        // least once, if still not find the key will throw IdNotMatchException
        if (!fieldCacheMap.containsKey(nameKey)) {
          BiMap<NAME, ID> apiOrDbMap = getFromDbOrApi(org);
          checkId(apiOrDbMap.get(nameKey), fieldSource, nameKey.toString());
          fieldCacheMap.clear();
          fieldCacheMap.putAll(apiOrDbMap);
        }
      }
      return fieldCacheMap.get(nameKey);
    }

    private NAME getNameFromId(ID idKey, Organization org) throws ExecutionException {
      final BiMap<NAME, ID> fieldCacheMap;
      synchronized (fieldCacheMap = cache.get(org)) {
        if (!fieldCacheMap.inverse().containsKey(idKey)) {
          BiMap<NAME, ID> apiOrDbMap = getFromDbOrApi(org);
          fieldCacheMap.clear();
          fieldCacheMap.putAll(apiOrDbMap);
        }
      }
      return fieldCacheMap.inverse().get(idKey);
    }
  }

  private static void checkId(Object id, FieldSource fieldSource, String name)
      throws IdNotMatchException {
    if (id == null) {
      throw new IdNotMatchException(
          "can't find the corresponding id for this " + fieldSource + ": " + name);
    }
    if (id.getClass() == Integer.class && Integer.parseInt(id.toString()) <= 0) {
      throw new IdNotMatchException(
          "can't find the corresponding id for this " + fieldSource + ": " + name);
    } else if (id.getClass() == String.class && ((String) id).isEmpty()) {
      throw new IdNotMatchException(
          "can't find the corresponding id for this " + fieldSource + ": " + name);
    }
  }

  private static final class InstanceHolder {

    static final FieldDataCache INSTANCE = new FieldDataCache();
  }
}
