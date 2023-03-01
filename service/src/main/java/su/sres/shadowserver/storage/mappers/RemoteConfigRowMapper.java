/*
 * Original software: Copyright 2013-2020 Signal Messenger, LLC
 * Modified software: Copyright 2019-2022 Anton Alipov, sole trader
 * SPDX-License-Identifier: AGPL-3.0-only
 */
package su.sres.shadowserver.storage.mappers;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import su.sres.shadowserver.storage.RemoteConfig;
import su.sres.shadowserver.storage.RemoteConfigs;

import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.Arrays;
import java.util.HashSet;
import java.util.UUID;


public class RemoteConfigRowMapper implements RowMapper<RemoteConfig> {

  @Override
  public RemoteConfig map(ResultSet rs, StatementContext ctx) throws SQLException {
	  return new RemoteConfig(rs.getString(RemoteConfigs.NAME),
              rs.getInt(RemoteConfigs.PERCENTAGE),
              new HashSet<>(Arrays.asList((UUID[])rs.getArray(RemoteConfigs.UUIDS).getArray())),
              rs.getString(RemoteConfigs.DEFAULT_VALUE),
              rs.getString(RemoteConfigs.VALUE),
              rs.getString(RemoteConfigs.HASH_KEY));
  }
}
