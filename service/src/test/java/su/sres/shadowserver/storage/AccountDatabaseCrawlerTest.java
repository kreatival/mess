/*
 * Original software: Copyright 2013-2020 Signal Messenger, LLC
 * Modified software: Copyright 2019-2022 Anton Alipov, sole trader
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package su.sres.shadowserver.storage;

import su.sres.shadowserver.storage.Account;
import su.sres.shadowserver.storage.AccountDatabaseCrawler;
import su.sres.shadowserver.storage.AccountDatabaseCrawlerCache;
import su.sres.shadowserver.storage.AccountDatabaseCrawlerListener;
import su.sres.shadowserver.storage.AccountDatabaseCrawlerRestartException;

import org.junit.Before;
import org.junit.Test;

import su.sres.shadowserver.storage.AccountsManager;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class AccountDatabaseCrawlerTest {

	  private static final UUID ACCOUNT1 = UUID.randomUUID();
	  private static final UUID ACCOUNT2 = UUID.randomUUID();

  private static final int  CHUNK_SIZE        = 1000;
  private static final long CHUNK_INTERVAL_MS = 30_000L;

  private final Account account1 = mock(Account.class);
  private final Account account2 = mock(Account.class);

  private final AccountsManager                accounts = mock(AccountsManager.class);
  private final AccountDatabaseCrawlerListener listener = mock(AccountDatabaseCrawlerListener.class);
  private final AccountDatabaseCrawlerCache    cache    = mock(AccountDatabaseCrawlerCache.class);

  private final AccountDatabaseCrawler        crawler   = new AccountDatabaseCrawler(accounts, cache, Arrays.asList(listener), CHUNK_SIZE, CHUNK_INTERVAL_MS);

  @Before
  public void setup() {
	  when(account1.getUuid()).thenReturn(ACCOUNT1);
	    when(account2.getUuid()).thenReturn(ACCOUNT2);

    when(accounts.getAllFrom(anyInt())).thenReturn(Arrays.asList(account1, account2));
    when(accounts.getAllFrom(eq(ACCOUNT1), anyInt())).thenReturn(Arrays.asList(account2));
    when(accounts.getAllFrom(eq(ACCOUNT2), anyInt())).thenReturn(Collections.emptyList());

    when(cache.claimActiveWork(any(), anyLong())).thenReturn(true);
    when(cache.isAccelerated()).thenReturn(false);
  }

  @Test
  public void testCrawlStart() throws AccountDatabaseCrawlerRestartException {
	  when(cache.getLastUuid()).thenReturn(Optional.empty());

    boolean accelerated = crawler.doPeriodicWork();
    assertThat(accelerated).isFalse();

    verify(cache, times(1)).claimActiveWork(any(String.class), anyLong());
    verify(cache, times(1)).getLastUuid();
    verify(listener, times(1)).onCrawlStart();
    verify(accounts, times(1)).getAllFrom(eq(CHUNK_SIZE));
    verify(accounts, times(0)).getAllFrom(any(UUID.class), eq(CHUNK_SIZE));
    verify(account1, times(0)).getUuid();
    verify(account2, times(1)).getUuid();
    verify(listener, times(1)).timeAndProcessCrawlChunk(eq(Optional.empty()), eq(Arrays.asList(account1, account2)));
    verify(cache, times(1)).setLastUuid(eq(Optional.of(ACCOUNT2)));
    verify(cache, times(1)).isAccelerated();
    verify(cache, times(1)).releaseActiveWork(any(String.class));

    verifyNoMoreInteractions(account1);
    verifyNoMoreInteractions(account2);
    verifyNoMoreInteractions(accounts);
    verifyNoMoreInteractions(listener);
    verifyNoMoreInteractions(cache);
  }

  @Test
  public void testCrawlChunk() throws AccountDatabaseCrawlerRestartException {
	  when(cache.getLastUuid()).thenReturn(Optional.of(ACCOUNT1));

    boolean accelerated = crawler.doPeriodicWork();
    assertThat(accelerated).isFalse();

    verify(cache, times(1)).claimActiveWork(any(String.class), anyLong());
    verify(cache, times(1)).getLastUuid();
    verify(accounts, times(0)).getAllFrom(eq(CHUNK_SIZE));
    verify(accounts, times(1)).getAllFrom(eq(ACCOUNT1), eq(CHUNK_SIZE));
    verify(account2, times(1)).getUuid();
    verify(listener, times(1)).timeAndProcessCrawlChunk(eq(Optional.of(ACCOUNT1)), eq(Arrays.asList(account2)));
    verify(cache, times(1)).setLastUuid(eq(Optional.of(ACCOUNT2)));
    verify(cache, times(1)).isAccelerated();
    verify(cache, times(1)).releaseActiveWork(any(String.class));

    verifyZeroInteractions(account1);

    verifyNoMoreInteractions(account2);
    verifyNoMoreInteractions(accounts);
    verifyNoMoreInteractions(listener);
    verifyNoMoreInteractions(cache);
  }

  @Test
  public void testCrawlChunkAccelerated() throws AccountDatabaseCrawlerRestartException {
    when(cache.isAccelerated()).thenReturn(true);
    when(cache.getLastUuid()).thenReturn(Optional.of(ACCOUNT1));

    boolean accelerated = crawler.doPeriodicWork();
    assertThat(accelerated).isTrue();

    verify(cache, times(1)).claimActiveWork(any(String.class), anyLong());
    verify(cache, times(1)).getLastUuid();
    verify(accounts, times(0)).getAllFrom(eq(CHUNK_SIZE));
    verify(accounts, times(1)).getAllFrom(eq(ACCOUNT1), eq(CHUNK_SIZE));
    verify(account2, times(1)).getUuid();
    verify(listener, times(1)).timeAndProcessCrawlChunk(eq(Optional.of(ACCOUNT1)), eq(Arrays.asList(account2)));
    verify(cache, times(1)).setLastUuid(eq(Optional.of(ACCOUNT2)));
    verify(cache, times(1)).isAccelerated();
    verify(cache, times(1)).releaseActiveWork(any(String.class));

    verifyZeroInteractions(account1);

    verifyNoMoreInteractions(account2);
    verifyNoMoreInteractions(accounts);
    verifyNoMoreInteractions(listener);
    verifyNoMoreInteractions(cache);
  }
  
  @Test
  public void testCrawlChunkRestart() throws AccountDatabaseCrawlerRestartException {
	  when(cache.getLastUuid()).thenReturn(Optional.of(ACCOUNT1));
	  doThrow(AccountDatabaseCrawlerRestartException.class).when(listener).timeAndProcessCrawlChunk(eq(Optional.of(ACCOUNT1)), eq(Arrays.asList(account2)));

    boolean accelerated = crawler.doPeriodicWork();
    assertThat(accelerated).isFalse();

    verify(cache, times(1)).claimActiveWork(any(String.class), anyLong());
    verify(cache, times(1)).getLastUuid();
    verify(accounts, times(0)).getAllFrom(eq(CHUNK_SIZE));
    verify(accounts, times(1)).getAllFrom(eq(ACCOUNT1), eq(CHUNK_SIZE));
    verify(account2, times(0)).getUserLogin();
    verify(listener, times(1)).timeAndProcessCrawlChunk(eq(Optional.of(ACCOUNT1)), eq(Arrays.asList(account2)));
    verify(cache, times(1)).setLastUuid(eq(Optional.empty()));
    verify(cache, times(1)).setAccelerated(false);
    verify(cache, times(1)).isAccelerated();
    verify(cache, times(1)).releaseActiveWork(any(String.class));

    verifyZeroInteractions(account1);

    verifyNoMoreInteractions(account2);
    verifyNoMoreInteractions(accounts);
    verifyNoMoreInteractions(listener);
    verifyNoMoreInteractions(cache);
  }

  @Test
  public void testCrawlEnd() {
	  when(cache.getLastUuid()).thenReturn(Optional.of(ACCOUNT2));

    boolean accelerated = crawler.doPeriodicWork();
    assertThat(accelerated).isFalse();

    verify(cache, times(1)).claimActiveWork(any(String.class), anyLong());
    verify(cache, times(1)).getLastUuid();
    verify(accounts, times(0)).getAllFrom(eq(CHUNK_SIZE));
    verify(accounts, times(1)).getAllFrom(eq(ACCOUNT2), eq(CHUNK_SIZE));
    verify(account1, times(0)).getUserLogin();
    verify(account2, times(0)).getUserLogin();
    verify(listener, times(1)).onCrawlEnd(eq(Optional.of(ACCOUNT2)));
    verify(cache, times(1)).setLastUuid(eq(Optional.empty()));
    verify(cache, times(1)).setAccelerated(false);
    verify(cache, times(1)).isAccelerated();
    verify(cache, times(1)).releaseActiveWork(any(String.class));

    verifyZeroInteractions(account1);
    verifyZeroInteractions(account2);

    verifyNoMoreInteractions(accounts);
    verifyNoMoreInteractions(listener);
    verifyNoMoreInteractions(cache);
  }
}