/*
 * Original software: Copyright 2013-2020 Shadow Messenger, LLC
 * Modified software: Copyright 2019-2022 Anton Alipov, sole trader
 * SPDX-License-Identifier: AGPL-3.0-only
 */
package su.sres.shadowserver.util.ua;

import com.vdurmont.semver4j.Semver;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

@RunWith(JUnitParamsRunner.class)
public class UserAgentUtilTest {

    @Test
    @Parameters(method = "argumentsForTestParseUserAgentString")
    public void testParseUserAgentString(final String userAgentString, final UserAgent expectedUserAgent) throws UnrecognizedUserAgentException {
	assertEquals(expectedUserAgent, UserAgentUtil.parseUserAgentString(userAgentString));
    }

    private static Object argumentsForTestParseUserAgentString() {
	return new Object[] {
		new Object[] { "Shadow-Android/4.68.3 Android/25", new UserAgent(ClientPlatform.ANDROID, new Semver("4.68.3"), "Android/25") },
		new Object[] { "Shadow-Android 4.53.7 (Android 8.1)", new UserAgent(ClientPlatform.ANDROID, new Semver("4.53.7"), "(Android 8.1)") },
	};
    }

    @Test
    @Parameters(method = "argumentsForTestParseBogusUserAgentString")
    public void testParseBogusUserAgentString(final String userAgentString) {
	assertThrows(UnrecognizedUserAgentException.class, () -> UserAgentUtil.parseUserAgentString(userAgentString));
    }

    private static Object argumentsForTestParseBogusUserAgentString() {
	return new Object[] {
		null,
		"This is obviously not a reasonable User-Agent string.",
		"Shadow-Android/4.6-8.3.unreasonableversionstring-17"
	};
    }

    @Test
    @Parameters(method = "argumentsForTestParseStandardUserAgentString")
    public void testParseStandardUserAgentString(final String userAgentString, final UserAgent expectedUserAgent) {
	assertEquals(expectedUserAgent, UserAgentUtil.parseStandardUserAgentString(userAgentString));
    }

    private static Object argumentsForTestParseStandardUserAgentString() {
	return new Object[] {
		new Object[] { "This is obviously not a reasonable User-Agent string.", null },
		new Object[] { "Shadow-Android/4.68.3 Android/25", new UserAgent(ClientPlatform.ANDROID, new Semver("4.68.3"), "Android/25") },
		new Object[] { "Shadow-Android/4.68.3", new UserAgent(ClientPlatform.ANDROID, new Semver("4.68.3")) },
		new Object[] { "Shadow-Desktop/1.2.3 Linux", new UserAgent(ClientPlatform.DESKTOP, new Semver("1.2.3"), "Linux") },
		new Object[] { "Shadow-Desktop/1.2.3 macOS", new UserAgent(ClientPlatform.DESKTOP, new Semver("1.2.3"), "macOS") },
		new Object[] { "Shadow-Desktop/1.2.3 Windows", new UserAgent(ClientPlatform.DESKTOP, new Semver("1.2.3"), "Windows") },
		new Object[] { "Shadow-Desktop/1.2.3", new UserAgent(ClientPlatform.DESKTOP, new Semver("1.2.3")) },
		new Object[] { "Shadow-Desktop/1.32.0-beta.3", new UserAgent(ClientPlatform.DESKTOP, new Semver("1.32.0-beta.3")) },
		new Object[] { "Shadow-iOS/3.9.0 (iPhone; iOS 12.2; Scale/3.00)", new UserAgent(ClientPlatform.IOS, new Semver("3.9.0"), "(iPhone; iOS 12.2; Scale/3.00)") },
		new Object[] { "Shadow-iOS/3.9.0 iOS/14.2",                             new UserAgent(ClientPlatform.IOS, new Semver("3.9.0"), "iOS/14.2") },
		new Object[] { "Shadow-iOS/3.9.0", new UserAgent(ClientPlatform.IOS, new Semver("3.9.0")) }
	};
    }

    @Test
    @Parameters(method = "argumentsForTestParseLegacyUserAgentString")
    public void testParseLegacyUserAgentString(final String userAgentString, final UserAgent expectedUserAgent) {
	assertEquals(expectedUserAgent, UserAgentUtil.parseLegacyUserAgentString(userAgentString));
    }

    private static Object argumentsForTestParseLegacyUserAgentString() {
	return new Object[] {
		new Object[] { "This is obviously not a reasonable User-Agent string.", null },
		new Object[] { "Shadow-Android 4.53.7 (Android 8.1)", new UserAgent(ClientPlatform.ANDROID, new Semver("4.53.7"), "(Android 8.1)") },
		new Object[] { "Shadow Desktop 1.2.3", new UserAgent(ClientPlatform.DESKTOP, new Semver("1.2.3")) },
		new Object[] { "Shadow Desktop 1.32.0-beta.3", new UserAgent(ClientPlatform.DESKTOP, new Semver("1.32.0-beta.3")) },
		new Object[] { "Shadow/3.9.0 (iPhone; iOS 12.2; Scale/3.00)", new UserAgent(ClientPlatform.IOS, new Semver("3.9.0"), "(iPhone; iOS 12.2; Scale/3.00)") }
	};
    }
}
