package com.vku.job.repositories.projection;

import java.sql.Date;

public interface UserProjection {

    Long getId();

    String getFullName();

    String getUsername();

    Date getCreatedAt();

    Integer getIsActive();
}