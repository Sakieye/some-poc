package com.sakieye;

import java.util.Objects;

public class Organization {

    private final int orgId;

    public Organization(int orgId) {
        this.orgId = orgId;
    }

    public int getOrgId() {
        return orgId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Organization that = (Organization) o;
        return orgId == that.orgId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(orgId);
    }
}
