/*
 * Original software: Copyright 2013-2020 Signal Messenger, LLC
 * Modified software: Copyright 2019-2022 Anton Alipov, sole trader
 * SPDX-License-Identifier: AGPL-3.0-only
 */
package su.sres.shadowserver.auth;

import su.sres.shadowserver.storage.Account;
import su.sres.shadowserver.storage.Device;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.security.MessageDigest;
import java.util.Optional;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class OptionalAccess {

  public static final String UNIDENTIFIED = "Unidentified-Access-Key";

  public static void verify(Optional<Account>   requestAccount,
                            Optional<Anonymous> accessKey,
                            Optional<Account>   targetAccount,
                            String              deviceSelector)
  {
    try {
      verify(requestAccount, accessKey, targetAccount);

      if (!deviceSelector.equals("*")) {
        long deviceId = Long.parseLong(deviceSelector);

        Optional<Device> targetDevice = targetAccount.get().getDevice(deviceId);

        if (targetDevice.isPresent() && targetDevice.get().isEnabled()) {
          return;
        }

        if (requestAccount.isPresent()) {
          throw new WebApplicationException(Response.Status.NOT_FOUND);
        } else {
          throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        }
      }
    } catch (NumberFormatException e) {
      throw new WebApplicationException(Response.status(422).build());
    }
  }

  public static void verify(Optional<Account>   requestAccount,
                            Optional<Anonymous> accessKey,
                            Optional<Account>   targetAccount)
  {
	  if (requestAccount.isPresent() && targetAccount.isPresent() && targetAccount.get().isEnabled()
			 
			  // safeguard for the source account to be enabled as well 
			  && requestAccount.get().isEnabled()) {
      return;
    }

    //noinspection ConstantConditions
	  if (requestAccount.isPresent() && (targetAccount.isEmpty() || (targetAccount.isPresent() && !targetAccount.get().isEnabled()))) {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }

	  if (accessKey.isPresent() && targetAccount.isPresent() && targetAccount.get().isEnabled() && targetAccount.get().isUnrestrictedUnidentifiedAccess()) {
      return;
    }

    if (accessKey.isPresent()                                      &&
        targetAccount.isPresent()                                  &&
        targetAccount.get().getUnidentifiedAccessKey().isPresent() &&
        targetAccount.get().isEnabled()                            &&
        MessageDigest.isEqual(accessKey.get().getAccessKey(), targetAccount.get().getUnidentifiedAccessKey().get()))
    {
      return;
    }

    throw new WebApplicationException(Response.Status.UNAUTHORIZED);
  }

}