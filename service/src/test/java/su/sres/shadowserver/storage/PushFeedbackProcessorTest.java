/*
 * Original software: Copyright 2013-2020 Signal Messenger, LLC
 * Modified software: Copyright 2019-2022 Anton Alipov, sole trader
 * SPDX-License-Identifier: AGPL-3.0-only
 */
package su.sres.shadowserver.storage;

import org.junit.Before;
import org.junit.Test;

import su.sres.shadowserver.storage.Account;
import su.sres.shadowserver.storage.AccountDatabaseCrawlerRestartException;
import su.sres.shadowserver.storage.AccountsManager;
import su.sres.shadowserver.storage.Device;
import su.sres.shadowserver.storage.PushFeedbackProcessor;
import su.sres.shadowserver.util.Util;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.*;

public class PushFeedbackProcessorTest {

    private AccountsManager accountsManager = mock(AccountsManager.class);

    private Account uninstalledAccount = mock(Account.class);
    private Account mixedAccount = mock(Account.class);
    private Account freshAccount = mock(Account.class);
    private Account cleanAccount = mock(Account.class);
    private Account stillActiveAccount = mock(Account.class);
    private Account undiscoverableAccount = mock(Account.class);

    private Device uninstalledDevice = mock(Device.class);
    private Device uninstalledDeviceTwo = mock(Device.class);
    private Device installedDevice = mock(Device.class);
    private Device installedDeviceTwo = mock(Device.class);
    private Device recentUninstalledDevice = mock(Device.class);
    private Device stillActiveDevice = mock(Device.class);
    private Device undiscoverableDevice = mock(Device.class);

    @Before
    public void setup() {
	when(uninstalledDevice.getUninstalledFeedbackTimestamp()).thenReturn(Util.todayInMillis() - TimeUnit.DAYS.toMillis(2));
	when(uninstalledDevice.getLastSeen()).thenReturn(Util.todayInMillis() - TimeUnit.DAYS.toMillis(2));
	when(uninstalledDeviceTwo.getUninstalledFeedbackTimestamp()).thenReturn(Util.todayInMillis() - TimeUnit.DAYS.toMillis(3));
	when(uninstalledDeviceTwo.getLastSeen()).thenReturn(Util.todayInMillis() - TimeUnit.DAYS.toMillis(3));

	when(installedDevice.getUninstalledFeedbackTimestamp()).thenReturn(0L);
	when(installedDeviceTwo.getUninstalledFeedbackTimestamp()).thenReturn(0L);

	when(recentUninstalledDevice.getUninstalledFeedbackTimestamp()).thenReturn(Util.todayInMillis() - TimeUnit.DAYS.toMillis(1));
	when(recentUninstalledDevice.getLastSeen()).thenReturn(Util.todayInMillis());

	when(stillActiveDevice.getUninstalledFeedbackTimestamp()).thenReturn(Util.todayInMillis() - TimeUnit.DAYS.toMillis(2));
	when(stillActiveDevice.getLastSeen()).thenReturn(Util.todayInMillis());

	when(undiscoverableDevice.getUninstalledFeedbackTimestamp()).thenReturn(Util.todayInMillis() - TimeUnit.DAYS.toMillis(2));
	when(undiscoverableDevice.getLastSeen()).thenReturn(Util.todayInMillis() - TimeUnit.DAYS.toMillis(2));

	when(uninstalledAccount.getDevices()).thenReturn(Set.of(uninstalledDevice));
	when(mixedAccount.getDevices()).thenReturn(Set.of(installedDevice, uninstalledDeviceTwo));
	when(freshAccount.getDevices()).thenReturn(Set.of(recentUninstalledDevice));
	when(cleanAccount.getDevices()).thenReturn(Set.of(installedDeviceTwo));
	when(stillActiveAccount.getDevices()).thenReturn(Set.of(stillActiveDevice));
	when(undiscoverableAccount.getDevices()).thenReturn(Set.of(undiscoverableDevice));

	when(uninstalledAccount.isEnabled()).thenReturn(true);
	when(uninstalledAccount.isDiscoverableByUserLogin()).thenReturn(true);
	when(uninstalledAccount.getUuid()).thenReturn(UUID.randomUUID());
	when(uninstalledAccount.getUserLogin()).thenReturn("+18005551234");

	when(undiscoverableAccount.isEnabled()).thenReturn(true);
	when(undiscoverableAccount.isDiscoverableByUserLogin()).thenReturn(false);
	when(undiscoverableAccount.getUuid()).thenReturn(UUID.randomUUID());
	when(undiscoverableAccount.getUserLogin()).thenReturn("+18005559876");
    }

    @Test
    public void testEmpty() throws AccountDatabaseCrawlerRestartException {
	PushFeedbackProcessor processor = new PushFeedbackProcessor(accountsManager);
	processor.timeAndProcessCrawlChunk(Optional.of(UUID.randomUUID()), Collections.emptyList());

	verifyZeroInteractions(accountsManager);
    }

    @Test
    public void testUpdate() throws AccountDatabaseCrawlerRestartException {
	PushFeedbackProcessor processor = new PushFeedbackProcessor(accountsManager);
	processor.timeAndProcessCrawlChunk(Optional.of(UUID.randomUUID()), List.of(uninstalledAccount, mixedAccount, stillActiveAccount, freshAccount, cleanAccount, undiscoverableAccount));

	verify(uninstalledDevice).setApnId(isNull());
	verify(uninstalledDevice).setGcmId(isNull());
	verify(uninstalledDevice).setFetchesMessages(eq(false));

	verify(accountsManager).update(eq(uninstalledAccount));

	verify(uninstalledDeviceTwo).setApnId(isNull());
	verify(uninstalledDeviceTwo).setGcmId(isNull());
	verify(uninstalledDeviceTwo).setFetchesMessages(eq(false));

	verify(installedDevice, never()).setApnId(any());
	verify(installedDevice, never()).setGcmId(any());
	verify(installedDevice, never()).setFetchesMessages(anyBoolean());

	verify(accountsManager).update(eq(mixedAccount));

	verify(recentUninstalledDevice, never()).setApnId(any());
	verify(recentUninstalledDevice, never()).setGcmId(any());
	verify(recentUninstalledDevice, never()).setFetchesMessages(anyBoolean());

	verify(accountsManager, never()).update(eq(freshAccount));

	verify(installedDeviceTwo, never()).setApnId(any());
	verify(installedDeviceTwo, never()).setGcmId(any());
	verify(installedDeviceTwo, never()).setFetchesMessages(anyBoolean());

	verify(accountsManager, never()).update(eq(cleanAccount));

	verify(stillActiveDevice).setUninstalledFeedbackTimestamp(eq(0L));
	verify(stillActiveDevice, never()).setApnId(any());
	verify(stillActiveDevice, never()).setGcmId(any());
	verify(stillActiveDevice, never()).setFetchesMessages(anyBoolean());

	verify(accountsManager).update(eq(stillActiveAccount));
    }

}